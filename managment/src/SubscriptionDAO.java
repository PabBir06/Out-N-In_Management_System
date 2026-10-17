import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDAO {

    public void insert(Subscription s, int customerId) throws SQLException {
        String sql = "INSERT INTO subscription (customer_id, start_date, end_date) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.setDate(2, Date.valueOf(s.getStartDate()));
            ps.setDate(3, Date.valueOf(s.getEndDate()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) s.setId(k.getInt(1));
            }
        }
    }

    public boolean hasActiveSubscription(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM subscription WHERE customer_id = ? AND CURDATE() BETWEEN start_date AND end_date";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public List<Subscription> findByCustomer(int customerId) throws SQLException {
        List<Subscription> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM subscription WHERE customer_id = ?")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Subscription(rs.getInt("id"),
                            rs.getDate("start_date").toString(),
                            rs.getDate("end_date").toString()));
                }
            }
        }
        return list;
    }

    public void update(Subscription s) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE subscription SET start_date=?, end_date=? WHERE id=?")) {
            ps.setDate(1, Date.valueOf(s.getStartDate()));
            ps.setDate(2, Date.valueOf(s.getEndDate()));
            ps.setInt(3, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM subscription WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}