import java.time.LocalDate;

public class Subscription {
    private int id;
    private String startDate;
    private String endDate;
    public Subscription(int id, String startDate, String endDate) {
        this.id = id;
        setDates(startDate, endDate);
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { setDates(startDate, endDate); }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { setDates(startDate, endDate); }
    public void setDates(String start, String end) {
        LocalDate s = Checks.date(start, "Start date");
        LocalDate e = Checks.date(end, "End date");
        if (e.isBefore(s)) throw new IllegalArgumentException("Subscription end date is before start date.");
        startDate = s.toString(); endDate = e.toString();
    }
    public boolean isActive() { return isActiveOn(LocalDate.now()); }
    public boolean isActiveOn(LocalDate day) {
        return !day.isBefore(LocalDate.parse(startDate)) && !day.isAfter(LocalDate.parse(endDate));
    }
}
