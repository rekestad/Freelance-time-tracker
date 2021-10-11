package a9.iprogmob.a9.models;

/**
 * Workplace
 */
public class Workplace {

    private int id;
    private String name;
    private double chargePerHour;
    private String currency;

    /**
     * Constructors
     */
    public Workplace() {
    }

    public Workplace(int id, String name, double chargePerHour, String currency) {
        this(name, chargePerHour, currency);
        this.id = id;
    }

    public Workplace(String name, double chargePerHour, String currency) {
        this.name = name;
        this.chargePerHour = chargePerHour;
        this.currency = currency;
    }

    /**
     * Getters/Setters
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getChargePerHour() {
        return chargePerHour;
    }

    public void setChargePerHour(double chargePerHour) {
        this.chargePerHour = chargePerHour;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Debug printout
     */
    @Override
    public String toString() {
        String output = "\nWORKPLACE_ID: " + id + "\n";
        output += "NAME: " + name + "\n";
        output += "CHARGE_PER_HOUR: " + chargePerHour + "\n";
        output += "CURRENCY: " + currency + "\n";

        return output;
    }
}
