import java.sql.*;
import java.util.List;
import java.util.stream.Collectors;

public class SqlRestaurantStore implements RestaurantStore {
    private final IngredientDAO ingredients=new IngredientDAO();
    private final DishDAO dishes=new DishDAO();
    private final MenuDAO menus=new MenuDAO();
    private final EmployeeDAO employees=new EmployeeDAO();
    private final CustomerDAO customers=new CustomerDAO();

    @Override public List<Ingredient> ingredients() throws SQLException {return ingredients.findAll();}
    @Override public List<Dish> dishes() throws SQLException {return dishes.findAll();}
    @Override public List<Menu> menus() throws SQLException {return menus.findAll();}
    @Override public List<Employee> employees() throws SQLException {return employees.findAll();}
    @Override public List<Customer> customers() throws SQLException {return customers.findAll();}
    @Override public void saveIngredient(Ingredient i) throws SQLException {
        if(i.getId()==0) ingredients.insert(i);else ingredients.update(i);
    }
    @Override public void saveDish(Dish d) throws SQLException {
        if(d.getId()==0) dishes.insert(d);else dishes.update(d);
    }
    @Override public void saveMenu(Menu m) throws SQLException {
        if(m.getId()==0) menus.insert(m);else menus.update(m);
    }
    @Override public void saveEmployee(Employee e) throws SQLException {
        if(e.getId()==0) employees.insert(e);else employees.update(e);
    }
    @Override public void saveCustomer(Customer c) throws SQLException {
        if(c.getId()==0) {
            if(customers.findByEmail(c.getEmail()) != null) throw new IllegalArgumentException("Customer email already exists.");
            customers.insert(c);
        } else customers.update(c);
    }
    @Override public void delete(String entity,List<Integer> ids) throws SQLException {
        if(ids.isEmpty()) return;
        String table=switch(entity) {
            case "ingredient","dish","menu","employee","customer" -> entity;
            default -> throw new IllegalArgumentException("Unknown table: " + entity);
        };
        String placeholders=ids.stream().map(x->"?").collect(Collectors.joining(","));
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try(PreparedStatement ps=c.prepareStatement("DELETE FROM " + table + " WHERE id IN ("+placeholders+")")) {
                for(int i=0;i<ids.size();i++) ps.setInt(i+1,ids.get(i));
                ps.executeUpdate(); c.commit();
            } catch(SQLException|RuntimeException e) { c.rollback();throw e; }
        }
    }
    @Override public void subscribe(Customer customer, Subscription s) throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // Serializes renewals for the same customer, including concurrent sessions.
                try(PreparedStatement lock=c.prepareStatement("SELECT id FROM customer WHERE id=? FOR UPDATE")) {
                    lock.setInt(1,customer.getId());
                    try(ResultSet rs=lock.executeQuery()) {if(!rs.next()) throw new SQLException("Customer not found.");}
                }
                try(PreparedStatement overlap=c.prepareStatement(
                    "SELECT COUNT(*) FROM subscription WHERE customer_id=? AND start_date <= ? AND end_date >= ?")) {
                    overlap.setInt(1,customer.getId());overlap.setDate(2,Date.valueOf(s.getEndDate()));overlap.setDate(3,Date.valueOf(s.getStartDate()));
                    try(ResultSet rs=overlap.executeQuery()) {if(rs.next() && rs.getInt(1)>0) throw new IllegalArgumentException("Subscription dates overlap an existing subscription.");}
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO subscription(customer_id,start_date,end_date) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1,customer.getId());ps.setDate(2,Date.valueOf(s.getStartDate()));ps.setDate(3,Date.valueOf(s.getEndDate()));ps.executeUpdate();
                    try(ResultSet rs=ps.getGeneratedKeys()) {if(rs.next()) s.setId(rs.getInt(1));}
                }
                c.commit(); customer.subscribe(s);
            } catch(SQLException|RuntimeException e) {c.rollback();throw e;}
        }
    }
    @Override public void prepareDish(int id) throws SQLException {dishes.prepare(id);}
}
