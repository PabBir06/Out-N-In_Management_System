import java.sql.SQLException;

/** Demo: saves and reads data from MySQL. Run schema.sql first. */
public class MainDB {
    public static void main(String[] args) {
        try {
            CustomerDAO customerDAO = new CustomerDAO();
            EmployeeDAO employeeDAO = new EmployeeDAO();
            employeeDAO.delete(2);
            employeeDAO.delete(3);
            customerDAO.delete(2);
        } catch (SQLException ex) {
            System.err.println("Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}