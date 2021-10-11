package a9.iprogmob.a9.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import a9.iprogmob.a9.R;
import a9.iprogmob.a9.WorkplaceActivity;
import a9.iprogmob.a9.models.TimeLog;
import a9.iprogmob.a9.models.Workplace;
import a9.iprogmob.a9.utils.DatabaseHelper;

/**
 * Adapterklass för att knyta Workplace-data mellan databasen och recyclerView:en
 */
public class WorkplaceAdapter extends RecyclerView.Adapter<WorkplaceAdapter.WorkplaceViewHolder> {
    private final DatabaseHelper db;
    private Context ctx;
    private List<Workplace> workplaceList;

    /**
     * DatabaseHelper-objektet behövs för onBindViewHolder
     */
    public WorkplaceAdapter(Context ctx, DatabaseHelper db) {
        this.db = db;
        this.ctx = ctx;
        this.workplaceList = db.getAllWorkplace();
    }

    @NonNull
    @Override
    public WorkplaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ctx);
        View view = inflater.inflate(R.layout.workplace_card_layout, null);
        return new WorkplaceViewHolder(view);
    }

    /**
     * Sätter variabler för utskrift, samt button-listensers.
     * Om det finns ett aktivt TimeLog-objekt i databasen så ska texten
     * "Clocked-in since..." visas för den Workplacen i recyclerView:en.
     */
    @Override
    public void onBindViewHolder(@NonNull WorkplaceViewHolder holder, int position) {
        final Workplace wp = workplaceList.get(position);
        holder.name.setText(wp.getName());

        TimeLog tl = db.getActiveTimeLog(wp.getId());

        if(tl != null) {
            holder.status.setText("Clocked-in since " + tl.getStartTime());
            holder.status.setVisibility(View.VISIBLE);
        }

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (v.getContext(), WorkplaceActivity.class);
                intent.putExtra("workplaceId", wp.getId());
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workplaceList.size();
    }

    class WorkplaceViewHolder extends RecyclerView.ViewHolder {
        Button name;
        TextView status;

        WorkplaceViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.companyName);
            status = itemView.findViewById(R.id.status);
        }
    }
}
