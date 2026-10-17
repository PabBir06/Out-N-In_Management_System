import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    private final SubscriptionDAO subscriptionDAO = new SubscriptionDAO();

    public boolean insert(Customer cu) throws SQLException {
        Customer existing = findByEmail(cu.getEmail());
        if (existing != null) {
            cu.setId(existing.getId());
            cu.getSubscriptions().clear();
            cu.getSubscriptions().addAll(existing.getSubscriptions());
            return false;
        }

        String sql = "INSERT INTO customer (name, last_name, phone, email, registration_date) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getLastName());
            ps.setString(3, cu.getPhone());
            ps.setString(4, cu.getEmail());
            ps.setDate(5, Date.valueOf(cu.getRegistrationDate()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) cu.setId(k.getInt(1));
            }
        }
        for (Subscription s : cu.getSubscriptions()) {
            subscriptionDAO.insert(s, cu.getId());
        }
        return true;
    }

    public Customer findByEmail(String email) throws SQLException {
        if (email == null) return null;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM customer WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }


    public boolean addSubscription(Customer cu, Subscription s) throws SQLException {
        if (subscriptionDAO.hasActiveSubscription(cu.getId())) {
            return false;
        }
        subscriptionDAO.insert(s, cu.getId());
        cu.subscribe(s);
        return true;
    }

    public Customer findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM customer WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM customer")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void update(Customer cu) throws SQLException {
        String sql = "UPDATE customer SET name=?, last_name=?, phone=?, email=?, registration_date=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cu.getName());
            ps.setString(2, cu.getLastName());
            ps.setString(3, cu.getPhone());
            ps.setString(4, cu.getEmail());
            ps.setDate(5, Date.valueOf(cu.getRegistrationDate()));
            ps.setInt(6, cu.getId());
            ps.executeUpdate();
        }
    }

    /** Deletes the customer; subscriptions are removed by ON DELETE CASCADE. */
    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM customer WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        Customer cu = new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("last_name"),
                rs.getString("phone"), rs.getString("email"), rs.getDate("registration_date").toString());
        for (Subscription s : subscriptionDAO.findByCustomer(cu.getId())) cu.subscribe(s);
        return cu;
    }
}