package a9.iprogmob.a9.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import a9.iprogmob.a9.R;
import a9.iprogmob.a9.utils.Utils;
import a9.iprogmob.a9.WorkplaceActivity;
import a9.iprogmob.a9.models.TimeLog;
import a9.iprogmob.a9.models.Workplace;

/**
 * Adapterklass för att knyta TimeLog-data mellan databasen och recyclerView:en
 */
public class TimeLogAdapter extends RecyclerView.Adapter<TimeLogAdapter.TimeLogViewHolder> {
    private Context ctx;
    private List<TimeLog> timeLogList;
    private Workplace wp;
    private WorkplaceActivity wa;

    /**
     * Workplace-objektet används för att visa belopp och valuta.
     * WorkplaceActivity-objektet behövs för access till deleteEntry() och editEntry()
     * i WorkplaceActivity
     */
    public TimeLogAdapter(Context ctx, List<TimeLog> TimeLogList, Workplace wp, WorkplaceActivity wa) {
        this.ctx = ctx;
        this.timeLogList = TimeLogList;
        this.wp = wp;
        this.wa = wa;
    }


    @NonNull
    @Override
    public TimeLogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.timelog_card_layout, null);
        return new TimeLogViewHolder(view);
    }

    /**
     * Sätter variabler för utskrift, samt button-listensers.
     * Kommentarer-TextView:en visas bara om det finns en kommentar.
     */
    @Override
    public void onBindViewHolder(@NonNull TimeLogViewHolder holder, int position) {
        final TimeLog tl = timeLogList.get(position);
        holder.date.setText(Utils.displayDate(tl.getStartTime()));
        holder.time.setText(tl.getDisplayTime());
        holder.sum.setText(tl.getDisplaySum(wp));

        holder.delete.setOnClickListener(new DeleteBtnListener(tl.getId()));
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editEntry(tl.getId());
            }
        });

        String comment = tl.getComment();
        if(comment != null && comment.length() > 0) {
            holder.comment.setText(tl.getComment());
            holder.comment.setVisibility(View.VISIBLE);
        }

    }

    /**
     * Vidarebefordrar anrop om att få ändra ett TimeLog-entry till WorkPlaceActivity
     */
    private void editEntry(int id) {
        wa.editTimeLog(id);
    }

    @Override
    public int getItemCount() {
        return timeLogList.size();
    }

    class TimeLogViewHolder extends RecyclerView.ViewHolder {
        TextView date;
        TextView time;
        TextView sum;
        TextView comment;
        ImageButton delete;
        ImageButton edit;

        TimeLogViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.date);
            time = itemView.findViewById(R.id.time);
            sum = itemView.findViewById(R.id.sum);
            comment = itemView.findViewById(R.id.comment);
            delete = itemView.findViewById(R.id.deleteBtn);
            edit = itemView.findViewById(R.id.editBtn);
        }
    }

    class DeleteBtnListener implements View.OnClickListener {

        int entryId;

        DeleteBtnListener(int entryId) {
            this.entryId = entryId;
        }

        @Override
        public void onClick(View v) {
            AlertDialog.Builder ad = new AlertDialog.Builder(ctx);
            ad.setTitle("Delete log entry");
            ad.setMessage("Do you want to delete?");
            ad.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            ad.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    wa.deleteTimeLog(entryId);
                }
            });

            AlertDialog dialog = ad.create();
            dialog.show();
        }
    }
}
