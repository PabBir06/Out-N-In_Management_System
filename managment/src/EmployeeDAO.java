import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {

    public void insert(Employee e) throws SQLException {
        String sql = "INSERT INTO employee (name, last_name, phone, email, role, salary, hire_date) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getLastName());
            ps.setString(3, e.getPhone());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getRole());
            ps.setDouble(6, e.getSalary());
            ps.setDate(7, Date.valueOf(e.getHireDate()));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) e.setId(k.getInt(1));
            }
        }
    }

    public Employee findById(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM employee WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Employee> findAll() throws SQLException {
        List<Employee> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM employee")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void update(Employee e) throws SQLException {
        String sql = "UPDATE employee SET name=?, last_name=?, phone=?, email=?, role=?, salary=?, hire_date=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getLastName());
            ps.setString(3, e.getPhone());
            ps.setString(4, e.getEmail());
            ps.setString(5, e.getRole());
            ps.setDouble(6, e.getSalary());
            ps.setDate(7, Date.valueOf(e.getHireDate()));
            ps.setInt(8, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM employee WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Employee map(ResultSet rs) throws SQLException {
        return new Employee(rs.getInt("id"), rs.getString("name"), rs.getString("last_name"),
                rs.getString("phone"), rs.getString("email"), rs.getString("role"),
                rs.getDouble("salary"), rs.getDate("hire_date").toString());
    }
}