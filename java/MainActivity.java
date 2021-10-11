package a9.iprogmob.a9;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import a9.iprogmob.a9.adapters.WorkplaceAdapter;
import a9.iprogmob.a9.utils.DatabaseHelper;
import a9.iprogmob.a9.utils.Utils;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 200;
    private DatabaseHelper db;
    private RecyclerView recyclerView;

    /**
     * Sätter diverse variabler och anropar populateList()
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        populateList();
    }

    /**
     * Populerar recyclerView:en med alla workplace-objekt som finns i databasen
     */
    private void populateList() {
        WorkplaceAdapter adapter = new WorkplaceAdapter(this, db);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Körs när användaren klickar på Create workplace i menyn uppe
     * i högra hörnet. Skapar en intent till CreateEditWorkplace och skickar
     * med "type" => "create".
     */
    private void createWorkplace() {
        Intent intent = new Intent(MainActivity.this, CreateEditWorkplaceActivity.class);
        intent.putExtra("type", "create");
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * Hanterar resultatet av intenten skapad i metoden createWorkplace()
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            populateList();
            Utils.toast("New workplace created", this);
        }
    }

    /**
     * Visar en meny uppe i högra hörnet. Layouten återfinns i main_menu.xml
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * hanterar användarens val i menyn uppe i högra hörnet
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createWorkplace:
                createWorkplace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * För att uppdatera recyclerView:en varje gång användaren återkommer
     */
    @Override
    protected void onResume() {
        super.onResume();
        populateList();
    }
}
