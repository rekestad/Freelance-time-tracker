package a9.iprogmob.a9;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import a9.iprogmob.a9.models.TimeLog;
import a9.iprogmob.a9.utils.DatabaseHelper;
import a9.iprogmob.a9.utils.Utils;
import a9.iprogmob.a9.models.Workplace;

/**
 * Skapa/uppdatera TimeLog
 */
public class CreateEditTimeLogActivity extends AppCompatActivity {

    private static final int FLAG_ACTION_CREATE = 0;
    private static final int FLAG_ACTION_EDIT = 1;
    private static final int FLAG_DATE_START = 0;
    private static final int FLAG_DATE_END = 1;
    private int flagAction;
    private static int flagDate;
    private TextView startTime;
    private TextView endTime;
    private EditText comment;
    private DatabaseHelper db;
    private Workplace wp;
    private TimeLog tl;
    private static DatePickerDialog.OnDateSetListener fromDateListener, toDateListener;
    private static TimePickerDialog.OnTimeSetListener fromTimeListener, toTimeListener;

    /**
     * Sätt diverse variabler och clicklisterners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_time_log);
        db = new DatabaseHelper(this);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        comment = findViewById(R.id.comment);
        Button pickStart = findViewById(R.id.pickStart);
        Button pickEnd = findViewById(R.id.pickEnd);
        Button submitBtn = findViewById(R.id.createBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEditTimeLog();
            }
        });

        pickStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDateAndTime(FLAG_DATE_START);
            }
        });
        pickEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectDateAndTime(FLAG_DATE_END);
            }
        });

        Bundle bundle = getIntent().getExtras();
        String type = Utils.getTypeFromBundle(bundle);
        initiatePickerListeners();

        if (type.equals("create")) {
            flagAction = FLAG_ACTION_CREATE;
            setActionBar("Create entry");
            int id = (int) bundle.get("workplaceId");
            wp = db.getWorkplace(id);

        } else if (type.equals("edit")) {
            flagAction = FLAG_ACTION_EDIT;
            submitBtn.setText("Save");
            setActionBar("Edit entry");

            int id = (int) bundle.get("timeLogId");
            tl = db.getTimeLog(id);
            startTime.setText(tl.getStartTime());
            endTime.setText(tl.getEndTime());
            comment.setText(tl.getComment());
        }
    }

    /**
     * Instansiera lyssnare som hanterar returen av DatePickerFragment
     * och TimePickerFragment
     */
    private void initiatePickerListeners() {
        fromDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = Utils.formatDatePickerOutput(year, month, dayOfMonth);
                setStartTimeText(date, false);
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        };

        toDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = Utils.formatDatePickerOutput(year, month, dayOfMonth);
                setEndTimeText(date, false);
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        };

        fromTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = Utils.formatTimePickerOutput(hourOfDay, minute);
                setStartTimeText(time, true);
            }
        };

        toTimeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = Utils.formatTimePickerOutput(hourOfDay, minute);
                setEndTimeText(time, true);
            }
        };
    }

    public void setStartTimeText(String text, boolean append) {
        if (append) {
            startTime.append(text);
        } else {
            startTime.setText(text);
        }
    }

    public void setEndTimeText(String text, boolean append) {
        if (append) {
            endTime.append(text);
        } else {
            endTime.setText(text);
        }
    }

    /**
     * Används i onCreate() för att sätta korrekt titel i ActionBar:en .
     */
    private void setActionBar(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * Skapa/uppdatera TimeLog
     * Returnerar tillbaka till WorkplaceActivity alternativt MainActivity.
     */
    private void createEditTimeLog() {
        String start = startTime.getText().toString();
        String end = endTime.getText().toString();
        String commentTxt = this.comment.getText().toString();

        // validering
        if (start.length() != 16 && end.length() != 16) {
            Utils.toast("Error: Please fill in start and end datetime", this);
        } else if (Utils.calculateMinutes(start, end) <= 0) {
            Utils.toast("Error: Start/End time diff must be at least 1 min", this);
        } else {
            // skapa
            if (flagAction == FLAG_ACTION_CREATE) {
                TimeLog newTl = new TimeLog();
                newTl.setWorkplaceId(wp.getId());
                newTl.setActive(0);
                newTl.setStartTime(start);
                newTl.setEndTime(end);
                newTl.setTotalMinutes(Utils.calculateMinutes(start, end));
                newTl.setComment(commentTxt);
                db.insertTimeLog(newTl);

            }
            // uppdatera
            else if (flagAction == FLAG_ACTION_EDIT) {
                tl.setStartTime(start);
                tl.setEndTime(end);
                tl.setTotalMinutes(Utils.calculateMinutes(start, end));
                tl.setComment(commentTxt);
                db.updateTimeLog(tl);
            }

            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    /**
     * Visar en klickbar tillbakapil uppe i vänstra hörnet
     */
    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    /**
     * Används för att öppna en datePicker. flagDate sätts till den medskickade
     * variabeln för att sedan bestämma valet av OnDateSetListener och OnTimeSetListener
     */
    public void selectDateAndTime(int flag) {
        flagDate = flag;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    /**
     * Öppnar en datePicker. Vilken OnDateSetListener som ska användas bestäms av vad
     * flagDate är satt till (sätts vid anropet till selectDateAndTime).
     */
    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            if (flagDate == FLAG_DATE_START)
                return new DatePickerDialog(getActivity(), fromDateListener, year, month, day);
            else
                return new DatePickerDialog(getActivity(), toDateListener, year, month, day);
        }
    }

    /**
     * Öppnar en timePicker. Vilken OnTimeSetListener som ska användas bestäms av vad
     * flagDate är satt till (sätts vid anropet till selectDateAndTime).
     */
    public static class TimePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            if (flagDate == FLAG_DATE_START)
                return new TimePickerDialog(getActivity(), fromTimeListener, hour, minute, true);
            else
                return new TimePickerDialog(getActivity(), toTimeListener, hour, minute, true);
        }
    }
}
