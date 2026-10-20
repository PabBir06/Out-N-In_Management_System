import java.sql.SQLException;
import java.util.List;

/** The UI talks to this interface, never directly to JDBC. */
public interface RestaurantStore {
    List<Ingredient> ingredients() throws SQLException;
    List<Dish> dishes() throws SQLException;
    List<Menu> menus() throws SQLException;
    List<Employee> employees() throws SQLException;
    List<Customer> customers() throws SQLException;
    void saveIngredient(Ingredient item) throws SQLException;
    void saveDish(Dish item) throws SQLException;
    void saveMenu(Menu item) throws SQLException;
    void saveEmployee(Employee item) throws SQLException;
    void saveCustomer(Customer item) throws SQLException;
    /** Which must be a whitelisted entity type; deletes one or many as one operation. */
    void delete(String entity, List<Integer> ids) throws SQLException;
    void subscribe(Customer customer, Subscription subscription) throws SQLException;
    void prepareDish(int dishId) throws SQLException;
}
