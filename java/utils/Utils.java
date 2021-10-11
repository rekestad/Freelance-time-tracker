package a9.iprogmob.a9.utils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Diverse statiska hjälpmetoder
 */
public class Utils {

    private final static DateFormat DATE_PARSE_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.GERMAN);
    private final static DateFormat DATE_DISPLAY_FORMAT = new SimpleDateFormat("E dd MMM yyyy", Locale.ENGLISH);

    /**
     * Visa en Toast för användaren.
     */
    public static void toast(String message, Context ctx) {
        Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Returnerar diff i minuter mellan två dateTime-objekt
     */
    public static int calculateMinutes(String strStart, String strEnd) {
        Date start = parseDate(strStart);
        Date end = parseDate(strEnd);
        long diff = end.getTime() - start.getTime();
        return (int) TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
    }

    /**
     * Formaterar datum till mer läsbart format
     */
    public static String displayDate(String date) {
        return DATE_DISPLAY_FORMAT.format(parseDate(date));
    }

    /**
     * Konverterar en datumsträng till ett Date-objekt
     */
    private static Date parseDate(String strDate) {
        Date date = null;

        try {
            date = DATE_PARSE_FORMAT.parse(strDate);
        } catch (ParseException e) {
            Log.e("Utils", "Error parse date");
        }

        return date;
    }

    /**
     * Konverterar minuter till timmar
     */
    public static double minutesToHours(int minutes) {
        return (double) minutes / 60.00;
    }

    /**
     * Returnerar tiden just nu i formatet YYYY-MM-DD hh:mm
     * Innehåller lite fulhack pga. evinnerliga problem med AM/PM vs 24h
     */
    public static String getCurrentDateTime() {
        String timeAmPm = new SimpleDateFormat("hh:mm aa").format(new java.util.Date().getTime());
        DateFormat timeInFormat = new SimpleDateFormat("hh:mm aa");
        DateFormat timeOutFormat = new SimpleDateFormat("HH:mm");
        String time24 = null;
        try {
            time24 = timeOutFormat.format(timeInFormat.parse(timeAmPm));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        String date = getCurrentDate();

        return date + " " + time24;
    }

    /**
     * Returnerar det nuvarande datumet i det deklarerade SimpleDateFormat-formatet
     */
    private static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(new Date());
    }

    public static String getTypeFromBundle(Bundle bundle) {
        if (bundle != null) {
            String type = (String) bundle.get("type");
            if (type != null)
                return type;
        }

        return "";
    }

    /**
     * Formaterar outputen från en datepicker till formatet YYYY-MM-DD
     */
    public static String formatDatePickerOutput(int year, int month, int day) {
        month++;
        String dayStr = (day < 10) ? "0" + day : "" + day;
        String monthStr = (month < 10) ? "0" + month : "" + month;
        return year + "-" + monthStr + "-" + dayStr + " ";
    }

    /**
     * Formaterar outputen från en timepicker till formatet HH:MM
     */
    public static String formatTimePickerOutput(int hourOfDay, int minute) {
        String hourStr = (hourOfDay < 10) ? "0" + hourOfDay : "" + hourOfDay;
        String minuteStr = (minute < 10) ? "0" + minute : "" + minute;
        return hourStr + ":" + minuteStr;
    }

    /**
     * Omständig metod bara för att visa flyttal med två decimaler och punkt (.)
     * som decimaltecken. Används vid utskrift av timmar, pengasummor m.m.
     */
    public static String displayDouble(double number) {
        DecimalFormat df = new DecimalFormat("#.00");
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        return df.format(number);
    }
}
