import java.util.ArrayList;
import java.util.List;

public class Customer extends Person {
    private String registrationDate;
    private final List<Subscription> subscriptions = new ArrayList<>();

    public Customer(int id, String name, String lastName, String phone, String email,
                    String registrationDate) {
        super(id, name, lastName, phone, email);
        this.registrationDate = registrationDate;
    }

    public String getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(String registrationDate) { this.registrationDate = registrationDate; }

    public void subscribe(Subscription subscription) { subscriptions.add(subscription); }
    public List<Subscription> getSubscriptions() { return subscriptions; }

    @Override
    public String getDescription() {
        return "Customer " + getFullName() + " (registered " + registrationDate + ")";
    }
}