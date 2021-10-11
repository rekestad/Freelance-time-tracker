package a9.iprogmob.a9;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import a9.iprogmob.a9.utils.DatabaseHelper;
import a9.iprogmob.a9.utils.Utils;
import a9.iprogmob.a9.models.Workplace;

/**
 * Skapa/uppdatera Workplace
 */
public class CreateEditWorkplaceActivity extends AppCompatActivity {

    private static final int FLAG_CREATE = 0;
    private static final int FLAG_EDIT = 1;
    private int flag;
    private EditText companyName;
    private EditText chargePerHour;
    private EditText currency;
    private DatabaseHelper db;
    private Workplace wp;

    /**
     * Sätt diverse variabler och clicklisterners.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_workplace);

        db = new DatabaseHelper(this);
        companyName = findViewById(R.id.companyName);
        chargePerHour = findViewById(R.id.chargePerHour);
        currency = findViewById(R.id.currency);
        Button submitBtn = findViewById(R.id.createBtn);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEditWorkplace();
            }
        });

        Bundle bundle = getIntent().getExtras();
        String type = Utils.getTypeFromBundle(bundle);

        if (type.equals("create")) {
            flag = FLAG_CREATE;
            setActionBar("Create workplace");

        } else if (type.equals("edit")) {
            submitBtn.setText("Save");
            flag = FLAG_EDIT;
            setActionBar("Edit workplace");

            int id = (int) bundle.get("workplaceId");
            wp = db.getWorkplace(id);

            companyName.setText(wp.getName());
            chargePerHour.setText(String.valueOf(wp.getChargePerHour()));
            currency.setText(wp.getCurrency());
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
     * Skapa/uppdatera Workplace
     * Returnerar tillbaka till WorkplaceActivity
     */
    private void createEditWorkplace() {
        String name = this.companyName.getText().toString();
        String charge = this.chargePerHour.getText().toString();
        String currencyTxt = this.currency.getText().toString().toUpperCase();

        // validering
        if (name.length() == 0 && charge.length() == 0 && currencyTxt.length() == 0) {
            Utils.toast("Error: Please fill in all fields", this);
        } else {
            // skapa
            if (flag == FLAG_CREATE) {
                if (db.checkIfWorkplaceNameExist(name)) {
                    Utils.toast("Error: Workplace already exists", this);
                } else {
                    Workplace wp = new Workplace(name, Double.parseDouble(charge), currencyTxt);
                    db.insertWorkplace(wp);
                }
            }
            // uppdatera
            else if (flag == FLAG_EDIT) {
                wp.setName(name);
                wp.setChargePerHour(Double.parseDouble(charge));
                wp.setCurrency(currencyTxt);
                db.updateWorkplace(wp);
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
}
