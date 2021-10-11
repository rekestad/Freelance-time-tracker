package a9.iprogmob.a9;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import a9.iprogmob.a9.adapters.TimeLogAdapter;
import a9.iprogmob.a9.models.TimeLog;
import a9.iprogmob.a9.models.Workplace;
import a9.iprogmob.a9.utils.DatabaseHelper;
import a9.iprogmob.a9.utils.Utils;

/**
 * Visar allt innehåll för en workplace, dvs. en lista med timelogs,
 * summering av timmar och pengar, knappar för in/utstämpling m.m.
 */
public class WorkplaceActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "a9.iprogmob.a9.notification";
    private static final CharSequence CHANNEL_NAME = "Freelancer";
    private static final String CHANNEL_DESCRIPTION = "Display clocked-in/out status";
    private static final int EDIT_ENTRY_REQUEST_CODE = 400;
    private static final int CREATE_ENTRY_REQUEST_CODE = 200;
    private static final int EDIT_WORKPLACE_REQUEST_CODE = 300;
    private static DatePickerDialog.OnDateSetListener dateListener;
    private static TimePickerDialog.OnTimeSetListener timeListener;
    private Workplace wp;
    private RecyclerView recyclerView;
    private DatabaseHelper db;
    private TextView totalTime;
    private TextView totalSum;
    private TextView status;
    private Button clockBtn;
    private Button clockLaterBtn;
    private int notificationId;
    private String pickedTime;

    /**
     * Sätt diverse variabeler och clicklisteners
     * WorkplaceId't från bundle används för att hämta Workplace-objektet i db
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workplace);

        db = new DatabaseHelper(this);
        notificationId = new Random().nextInt();
        totalTime = findViewById(R.id.totalTime);
        totalSum = findViewById(R.id.totalSum);
        status = findViewById(R.id.status);
        clockBtn = findViewById(R.id.clockBtn);
        clockLaterBtn = findViewById(R.id.clockLaterBtn);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        initiatePickerListeners();

        clockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clockInOut(null);
            }
        });

        clockLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            int id = (int) bundle.get("workplaceId");
            wp = db.getWorkplace(id);
            setActionBar(wp.getName());
            refresh();
        }
    }

    /**
     * Skapar lyssnare för hantering av returen från DatePickerFragment
     * och TimePickerFragment
     */
    private void initiatePickerListeners() {
        dateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                String date = Utils.formatDatePickerOutput(year, month, dayOfMonth);
                setPickedTime(date, false);
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getFragmentManager(), "timePicker");
            }
        };

        timeListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String time = Utils.formatTimePickerOutput(hourOfDay, minute);
                setPickedTime(time, true);
                clockInOut(pickedTime);
            }
        };
    }

    /**
     * Används för att sätta pickedTime - se metoden ovan.
     */
    public void setPickedTime(String text, boolean append) {
        if (append) {
            pickedTime += text;
        } else {
            pickedTime = text;
        }
    }

    /**
     * Används i onCreate() för att titeln i ActionBar:en till namnet på Workplace'n.
     */
    private void setActionBar(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    /**
     * körs när användaren väljer att skapa en manuell timeLog.
     * Skapar en intent till CreateEditTimeLog med "type" satt till "create"
     */
    private void createTimeLog() {
        Intent intent = new Intent(WorkplaceActivity.this, CreateEditTimeLogActivity.class);
        intent.putExtra("type", "create");
        intent.putExtra("workplaceId", wp.getId());
        startActivityForResult(intent, CREATE_ENTRY_REQUEST_CODE);
    }

    /**
     * körs när användaren väljer att redigera en existerande timeLog-inlägg.
     * Skapar en intent till CreateEditTimeLog med "type" satt till "edit"
     */
    public void editTimeLog(int id) {
        Intent intent = new Intent(WorkplaceActivity.this, CreateEditTimeLogActivity.class);
        intent.putExtra("type", "edit");
        intent.putExtra("timeLogId", id);
        startActivityForResult(intent, EDIT_ENTRY_REQUEST_CODE);
    }

    /**
     * Körs när användaren väljer att redigera en workplace.
     * Skapar en intent till CreateEditWorkplace med "type" satt till "edit"
     */
    private void editWorkplace() {
        Intent intent = new Intent(WorkplaceActivity.this, CreateEditWorkplaceActivity.class);
        intent.putExtra("type", "edit");
        intent.putExtra("workplaceId", wp.getId());
        startActivityForResult(intent, EDIT_WORKPLACE_REQUEST_CODE);
    }

    /**
     * Hanterar returen av de intents som skapas i de föregående tre metoderna.
     * Om Workplacen uppdateras så måste namnet i ActionBar'en uppdateras.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CREATE_ENTRY_REQUEST_CODE:
                    refresh();
                    Utils.toast("New time log entry created", this);
                    break;
                case EDIT_WORKPLACE_REQUEST_CODE:
                    wp = db.getWorkplace(wp.getId());
                    setActionBar(wp.getName());
                    refresh();
                    Utils.toast("Changes to workplace saved", this);
                    break;
                case EDIT_ENTRY_REQUEST_CODE:
                    refresh();
                    Utils.toast("Changes to time log saved", this);
                    break;
            }
        }
    }

    /**
     * Uppdaterar alla objekt som visas till korrekt status beroende på instämplad eller inte.
     * Anropas på lite här och var.
     */
    private void refresh() {
        TimeLog tl = db.getActiveTimeLog(wp.getId());

        if (tl == null) {
            status.setVisibility(View.GONE);
            clockBtn.setBackgroundResource(R.color.colorBtnGreen);
            clockLaterBtn.setBackgroundResource(R.color.colorBtnBlue);
            clockBtn.setText("Clock In Now");
            clockLaterBtn.setText("Start Clock At...");
        } else {
            status.setText("On the clock since " + tl.getStartTime());
            status.setVisibility(View.VISIBLE);
            clockBtn.setBackgroundResource(R.color.colorBtnRed);
            clockLaterBtn.setBackgroundResource(R.color.colorBtnGray);
            clockBtn.setText("Clock Out Now");
            clockLaterBtn.setText("Stop Clock At...");
        }

        totalTime.setText(getTotalHoursString());
        totalSum.setText(getTotalSumString());

        TimeLogAdapter adapter = new TimeLogAdapter(this, db.getAllTimeLog(wp.getId()), wp, this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Summerar antalet arbetade timmar för den aktuella workplace'en och formaterar för display.
     * Används även vid export till emailklient
     */
    private String getTotalHoursString() {
        double totalHours = Utils.minutesToHours(db.sumTime(wp.getId()));
        return Utils.displayDouble(totalHours) + " h";
    }

    /**
     * Räknar ut intjänat belopp baserat på antalet arbetade timmar för den aktuella workplace'en och
     * formaterar för display. Används även vid export till emailklient
     */
    private String getTotalSumString() {
        double totalHours = Utils.minutesToHours(db.sumTime(wp.getId()));
        double sum = totalHours * wp.getChargePerHour();
        return Utils.displayDouble(sum) + " " + wp.getCurrency();
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
     * Visar en meny uppe i högra hörnet. Layouten återfinns i workplace_menu.xml
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.workplace_menu, menu);
        return true;
    }

    /**
     * hanterar användarens val i menyn uppe i högra hörnet
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manualLogEntry:
                createTimeLog();
                return true;
            case R.id.exportEntries:
                exportToEmail();
                return true;
            case R.id.editWorkplace:
                editWorkplace();
                return true;
            case R.id.deleteWorkplace:
                deleteWorkplace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Anropas när användaren trycker på delete i menyn uppe i högra hörnet
     */
    private void deleteWorkplace() {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("Warning!");
        ad.setMessage("Are you really sure you want to delete " + wp.getName() + "? This action cannot be undone.");
        ad.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        ad.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                db.deleteWorkplace(wp.getId());
                finish();
            }
        });

        AlertDialog dialog = ad.create();
        dialog.show();
    }

    /**
     * Tar bort en TimeLog i databasen baserat på id. Kallas från TimeLogAdapter.java
     */
    public void deleteTimeLog(int entryId) {
        db.deleteTimeLog(entryId);
        Utils.toast("Entry deleted", this);
        refresh();
    }

    /**
     * Stämplar in/ut en användare.
     *
     * Parametern "time" sätts när användaren trycker på knappen "Start clock at..." respektive "Stop clock at.."
     * Om "time" inte är satt används klockslaget just nu.
     */
    private void clockInOut(String time) {
        String nowTime = (time != null) ? time : Utils.getCurrentDateTime();
        TimeLog tl = db.getActiveTimeLog(wp.getId());

        if (tl == null) {
            TimeLog newTl = new TimeLog(wp.getId(), 1, nowTime, null, 0, null);
            db.insertTimeLog(newTl);
            displayNotification(nowTime);
        } else {
            String startTime = tl.getStartTime();

            if (Utils.calculateMinutes(startTime, nowTime) >= 0) {
                tl.setActive(0);
                tl.setEndTime(nowTime);
                tl.setTotalMinutes(Utils.calculateMinutes(startTime, nowTime));
                db.updateTimeLog(tl);
                removeNotification();
            } else {
                Utils.toast("Error: " + nowTime + " is before " + startTime, this);
            }
        }

        refresh();
    }

    /**
     * Visa notifikation vid instämpling
     */
    private void displayNotification(String startTime) {
        createNotificationChannel();

        NotificationCompat.Builder notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("Freelancer")
                .setContentText("Clocked-in since " + startTime)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        notificationManager.notify(notificationId, notification.build());

    }

    /**
     * Tar bort notifikation när användaren klockar ut
     */
    private void removeNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(notificationId);
    }

    /**
     * Skapar kanal för notiser
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESCRIPTION);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Exportera alla TimeLogs för en Workplace till en emailklient
     */
    private void exportToEmail() {
        List<TimeLog> timeLogList = db.getAllTimeLog(wp.getId());

        if(timeLogList.size() == 0) {
            Utils.toast("No entries exist", this);
        } else {

            String subject = "Freelancer export: " + wp.getName() + ", " + Utils.getCurrentDateTime();
            StringBuilder message = new StringBuilder();
            String totalHours = "Total hours: " + getTotalHoursString() + "\n";
            String totalSum = "Total sum: " + getTotalSumString() + "\n\n";
            message.append(totalHours);
            message.append(totalSum);
            message.append("Log entries:\n\n");

            for (TimeLog tl : timeLogList) {
                message.append(tl.exportString(wp));
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("plain/text");
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, message.toString());

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    /**
     * Öppnar en datePicker. Se även metoden initializePickers()
     */
    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), dateListener, year, month, day);
        }
    }

    /**
     * Öppnar en timePicker. Se även metoden initializePickers()
     */
    public static class TimePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(getActivity(), timeListener, hour, minute, true);
        }
    }
}
