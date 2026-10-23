import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {

    public void insert(Ingredient i) throws SQLException {
        String sql = "INSERT INTO ingredient (name, unit, quantity_in_stock, unit_price) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getName());
            ps.setString(2, i.getUnit());
            ps.setDouble(3, i.getQuantityInStock());
            ps.setDouble(4, i.getUnitPrice());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) i.setId(k.getInt(1));
            }
        }
    }

    public Ingredient findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM ingredient WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Ingredient> findAll() throws SQLException {
        List<Ingredient> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM ingredient")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void update(Ingredient i) throws SQLException {
        String sql = "UPDATE ingredient SET name=?, unit=?, quantity_in_stock=?, unit_price=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, i.getName());
            ps.setString(2, i.getUnit());
            ps.setDouble(3, i.getQuantityInStock());
            ps.setDouble(4, i.getUnitPrice());
            ps.setInt(5, i.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM ingredient WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    static Ingredient map(ResultSet rs) throws SQLException {
        return new Ingredient(rs.getInt("id"), rs.getString("name"), rs.getString("unit"),
                rs.getDouble("quantity_in_stock"), rs.getDouble("unit_price"));
    }
}