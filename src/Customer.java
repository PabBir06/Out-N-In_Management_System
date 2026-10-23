import java.util.ArrayList;
import java.util.List;

public class Customer extends Person {
    private String registrationDate;
    private final List<Subscription> subscriptions = new ArrayList<>();
    public Customer(int id, String name, String lastName, String phone, String email, String registrationDate) {
        super(id, name, lastName, phone, email); setRegistrationDate(registrationDate);
    }
    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String date) { registrationDate = Checks.date(date, "Registration date").toString(); }
    public void subscribe(Subscription s) { subscriptions.add(s); }
    public List<Subscription> getSubscriptions() { return subscriptions; }
    public boolean hasActiveSubscription() { return subscriptions.stream().anyMatch(Subscription::isActive); }
    @Override public String getDescription() { return "Customer " + getFullName() + " (registered " + registrationDate + ")"; }
}
