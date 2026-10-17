import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishDAO {

    public void insert(Dish d) throws SQLException {
        String sql = "INSERT INTO dish (name, category, price) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getCategory());
            ps.setDouble(3, d.getPrice());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) d.setId(k.getInt(1));
            }
        }
        for (Ingredient i : d.getIngredients()) {
            linkIngredient(d.getId(), i.getId());
        }
    }

    public void linkIngredient(int dishId, int ingredientId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT IGNORE INTO dish_ingredient (dish_id, ingredient_id) VALUES (?,?)")) {
            ps.setInt(1, dishId);
            ps.setInt(2, ingredientId);
            ps.executeUpdate();
        }
    }

    public void unlinkIngredient(int dishId, int ingredientId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM dish_ingredient WHERE dish_id=? AND ingredient_id=?")) {
            ps.setInt(1, dishId);
            ps.setInt(2, ingredientId);
            ps.executeUpdate();
        }
    }

    public Dish findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM dish WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Dish> findAll() throws SQLException {
        List<Dish> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM dish")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void update(Dish d) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE dish SET name=?, category=?, price=? WHERE id=?")) {
            ps.setString(1, d.getName());
            ps.setString(2, d.getCategory());
            ps.setDouble(3, d.getPrice());
            ps.setInt(4, d.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM dish WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Dish map(ResultSet rs) throws SQLException {
        Dish d = new Dish(rs.getInt("id"), rs.getString("name"), rs.getString("category"), rs.getDouble("price"));
        String sql = "SELECT i.* FROM ingredient i JOIN dish_ingredient di ON di.ingredient_id = i.id WHERE di.dish_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, d.getId());
            try (ResultSet r2 = ps.executeQuery()) {
                while (r2.next()) d.addIngredient(IngredientDAO.map(r2));
            }
        }
        return d;
    }
}