import java.time.LocalDate;

public class Subscription {
    private int id;
    private String startDate; // format: yyyy-MM-dd
    private String endDate;   // format: yyyy-MM-dd

    public Subscription(int id, String startDate, String endDate) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public Boolean isActive() {
        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return !today.isBefore(start) && !today.isAfter(end);
    }
}