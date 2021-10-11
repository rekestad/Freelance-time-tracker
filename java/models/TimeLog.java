package a9.iprogmob.a9.models;

import a9.iprogmob.a9.utils.Utils;

/**
 * TimeLog
 */
public class TimeLog {
    private int id;
    private int workplaceId;
    private int active;
    private String startTime;
    private String endTime;
    private int totalMinutes;
    private String comment;

    /**
     * Constructors
     */
    public TimeLog() {
    }

    public TimeLog(int id, int workplaceId, int active, String startTime, String endTime, int totalMinutes, String comment) {
        this(workplaceId, active, startTime, endTime, totalMinutes, comment);
        this.id = id;
    }

    public TimeLog(int workplaceId, int active, String startTime, String endTime, int totalMinutes, String comment) {
        this.workplaceId = workplaceId;
        this.active = active;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalMinutes = totalMinutes;
        this.comment = comment;
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

    public int getWorkplaceId() {
        return workplaceId;
    }

    public void setWorkplaceId(int workplaceId) {
        this.workplaceId = workplaceId;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public int getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(int totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * Summerar och formaterar intjänat belopp för en work place
     */
    public String getDisplaySum(Workplace wp) {
        double sum = Utils.minutesToHours(totalMinutes) * wp.getChargePerHour();
        return Utils.displayDouble(sum) + " " + wp.getCurrency();
    }

    /**
     * Formaterar startTime och endTime för utskrift
     */
    public String getDisplayTime() {
        String time = startTime.substring(11, startTime.length());
        time += " - ";
        time += endTime.substring(11, endTime.length());
        time += " (" + Utils.displayDouble(Utils.minutesToHours(totalMinutes)) + " h)";

        return time;
    }

    /**
     * Används för att exportera alla TimeLogs för en Workplace till till en mailklient.
     * Se WorkplaceActivity.exportToEmail()
     */
    public String exportString(Workplace wp) {
        String output = "Start time: " + getStartTime() + "\n";
        output += "End time: " + getEndTime() + "\n";
        output += "Total hours: " + Utils.displayDouble(Utils.minutesToHours(totalMinutes)) + "\n";
        output += "Total sum: " + getDisplaySum(wp) + "\n";

        if(comment != null && comment.length() > 0) {
            output += "Comment:\n" + comment + "\n";
        }

        output += "-------------------------\n";

        return output;
    }
}
