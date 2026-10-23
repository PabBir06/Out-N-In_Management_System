import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MenuDAO {
    private final DishDAO dishDAO = new DishDAO();
    public List<Menu> findAll() throws SQLException {
        List<Menu> result = new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); Statement st=c.createStatement();ResultSet rs=st.executeQuery("SELECT * FROM menu ORDER BY id")) {
            while(rs.next()) result.add(map(rs));
        }
        return result;
    }
    private Menu map(ResultSet rs) throws SQLException {
        Menu menu=new Menu(rs.getInt("id"),rs.getString("name"));
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("SELECT dish_id FROM menu_dish WHERE menu_id=? ORDER BY dish_id")) {
            ps.setInt(1,menu.getId());
            try(ResultSet dishes=ps.executeQuery()) {
                while(dishes.next()) {
                    Dish d=dishDAO.findById(dishes.getInt(1));
                    if (d!=null) menu.addDish(d);
                }
            }
        }
        return menu;
    }
    public void insert(Menu m) throws SQLException {save(m,true);}
    public void update(Menu m) throws SQLException {save(m,false);}
    private void save(Menu m,boolean create) throws SQLException {
        if(m.getDishes().isEmpty()) throw new IllegalArgumentException("A menu needs at least one dish.");
        if(!m.isAvailable()) throw new IllegalStateException("A menu cannot include an unavailable dish.");
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                if(create) {
                    try(PreparedStatement ps=c.prepareStatement("INSERT INTO menu(name) VALUES (?)",Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1,m.getName());ps.executeUpdate();
                        try(ResultSet keys=ps.getGeneratedKeys()) {if(keys.next()) m.setId(keys.getInt(1));}
                    }
                } else {
                    try(PreparedStatement ps=c.prepareStatement("UPDATE menu SET name=? WHERE id=?")) {
                        ps.setString(1,m.getName());ps.setInt(2,m.getId());
                        if(ps.executeUpdate()!=1) throw new SQLException("Menu not found.");
                    }
                    try(PreparedStatement ps=c.prepareStatement("DELETE FROM menu_dish WHERE menu_id=?")) {ps.setInt(1,m.getId());ps.executeUpdate();}
                }
                try(PreparedStatement ps=c.prepareStatement("INSERT INTO menu_dish(menu_id,dish_id) VALUES (?,?)")) {
                    for(Dish d:m.getDishes()) {ps.setInt(1,m.getId());ps.setInt(2,d.getId());ps.addBatch();}
                    ps.executeBatch();
                }
                c.commit();
            } catch(SQLException | RuntimeException ex) {c.rollback();throw ex;}
        }
    }
    public void delete(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement ps=c.prepareStatement("DELETE FROM menu WHERE id=?")) {ps.setInt(1,id);ps.executeUpdate();}
    }
}
