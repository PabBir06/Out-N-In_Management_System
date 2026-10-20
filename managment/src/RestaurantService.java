import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;

/** Business rules shared by SQL mode, Swing UI and in-memory demonstration. */
public class RestaurantService {
    public static final double SUBSCRIBER_DISCOUNT = 0.10; // configurable project policy
    public static final double LOW_STOCK_THRESHOLD = 5.0; // measured in ingredient's own unit
    private final RestaurantStore store;
    public RestaurantService(RestaurantStore store){this.store=store;}
    public RestaurantStore store(){return store;}
    public double priceForCustomer(Menu menu,Customer customer){
        if(menu==null || !menu.isAvailable())throw new IllegalStateException("Menu contains an unavailable dish.");
        return Math.round(menu.getPrice()*(customer!=null && customer.hasActiveSubscription()?1-SUBSCRIBER_DISCOUNT:1)*100)/100.0;
    }
    public Subscription renew(Customer customer,int months) throws SQLException {
        if(months < 1 || months > 36) throw new IllegalArgumentException("Renewal must be between 1 and 36 months.");
        LocalDate now=LocalDate.now();
        LocalDate lastEnd=customer.getSubscriptions().stream().map(s->LocalDate.parse(s.getEndDate()))
            .max(Comparator.naturalOrder()).orElse(now.minusDays(1));
        LocalDate start=lastEnd.isBefore(now)?now:lastEnd.plusDays(1);
        Subscription subscription=new Subscription(0,start.toString(),start.plusMonths(months).minusDays(1).toString());
        store.subscribe(customer,subscription);
        return subscription;
    }
}
