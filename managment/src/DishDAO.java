import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DishDAO {
    public void insert(Dish d) throws SQLException { save(d, true); }
    public void update(Dish d) throws SQLException { save(d, false); }
    private void save(Dish d, boolean create) throws SQLException {
        if (d.getIngredients().isEmpty()) throw new IllegalArgumentException("A dish needs at least one recipe ingredient.");
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if (create) {
                    try (PreparedStatement ps = c.prepareStatement("INSERT INTO dish (name,category,price) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1,d.getName()); ps.setString(2,d.getCategory()); ps.setDouble(3,d.getPrice());
                        ps.executeUpdate();
                        try (ResultSet r=ps.getGeneratedKeys()) { if (r.next()) d.setId(r.getInt(1)); }
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement("UPDATE dish SET name=?,category=?,price=? WHERE id=?")) {
                        ps.setString(1,d.getName()); ps.setString(2,d.getCategory()); ps.setDouble(3,d.getPrice()); ps.setInt(4,d.getId());
                        if (ps.executeUpdate() != 1) throw new SQLException("Dish not found.");
                    }
                    try (PreparedStatement ps=c.prepareStatement("DELETE FROM dish_ingredient WHERE dish_id=?")) { ps.setInt(1,d.getId()); ps.executeUpdate(); }
                }
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO dish_ingredient (dish_id,ingredient_id,quantity_required) VALUES (?,?,?)")) {
                    for (Ingredient i : d.getIngredients()) {
                        ps.setInt(1,d.getId()); ps.setInt(2,i.getId()); ps.setDouble(3,d.getRecipeQuantities().get(i.getId())); ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
            } catch (SQLException | RuntimeException ex) { c.rollback(); throw ex; }
        }
    }
    public void linkIngredient(int dishId, int ingredientId) throws SQLException { linkIngredient(dishId, ingredientId, 1.0); }
    public void linkIngredient(int dishId, int ingredientId, double amount) throws SQLException {
        Checks.positive(amount, "Recipe quantity");
        try (Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(
            "INSERT INTO dish_ingredient (dish_id,ingredient_id,quantity_required) VALUES (?,?,?) ON DUPLICATE KEY UPDATE quantity_required=VALUES(quantity_required)")) {
            ps.setInt(1,dishId);ps.setInt(2,ingredientId);ps.setDouble(3,amount);ps.executeUpdate();
        }
    }
    public void unlinkIngredient(int dishId,int ingredientId) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM dish_ingredient WHERE dish_id=? AND ingredient_id=?")) {
            ps.setInt(1,dishId);ps.setInt(2,ingredientId);ps.executeUpdate();
        }
    }
    public Dish findById(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT * FROM dish WHERE id=?")) {
            ps.setInt(1,id);
            try(ResultSet rs=ps.executeQuery()) { return rs.next()?map(rs):null; }
        }
    }
    public List<Dish> findAll() throws SQLException {
        List<Dish> result=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();Statement st=c.createStatement();ResultSet rs=st.executeQuery("SELECT * FROM dish ORDER BY id")) {
            while(rs.next()) result.add(map(rs));
        }
        return result;
    }
    private Dish map(ResultSet rs) throws SQLException {
        Dish d=new Dish(rs.getInt("id"),rs.getString("name"),rs.getString("category"),rs.getDouble("price"));
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement(
            "SELECT i.*,di.quantity_required FROM ingredient i JOIN dish_ingredient di ON di.ingredient_id=i.id WHERE di.dish_id=?")) {
            ps.setInt(1,d.getId());
            try(ResultSet r=ps.executeQuery()) { while(r.next()) d.addIngredient(IngredientDAO.map(r),r.getDouble("quantity_required")); }
        }
        return d;
    }
    public void delete(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM dish WHERE id=?")) {
            ps.setInt(1,id);ps.executeUpdate();
        }
    }
    /** All stock checks and updates are performed under a single transaction and row locks. */
    public void prepare(int dishId) throws SQLException {
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // lock ingredient records in stable ID order to minimize deadlocks
                String sql="SELECT i.id,i.name,i.quantity_in_stock,di.quantity_required FROM ingredient i JOIN dish_ingredient di " +
                    "ON di.ingredient_id=i.id WHERE di.dish_id=? ORDER BY i.id FOR UPDATE";
                List<double[]> uses=new ArrayList<>();
                try(PreparedStatement ps=c.prepareStatement(sql)) {
                    ps.setInt(1,dishId);
                    try(ResultSet rs=ps.executeQuery()) {
                        while(rs.next()) {
                            double stock=rs.getDouble("quantity_in_stock"), required=rs.getDouble("quantity_required");
                            if(stock + 1e-9 < required) throw new IllegalStateException("Not enough stock for " + rs.getString("name"));
                            uses.add(new double[]{rs.getInt("id"),required});
                        }
                    }
                }
                if (uses.isEmpty()) throw new IllegalStateException("Dish has no recipe or does not exist.");
                try(PreparedStatement ps=c.prepareStatement("UPDATE ingredient SET quantity_in_stock=quantity_in_stock-? WHERE id=?")) {
                    for(double[] item:uses) {ps.setDouble(1,item[1]);ps.setInt(2,(int)item[0]);ps.addBatch();}
                    ps.executeBatch();
                }
                c.commit();
            } catch(SQLException | RuntimeException ex) {c.rollback();throw ex;}
        }
    }
}
