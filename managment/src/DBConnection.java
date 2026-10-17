import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // changer les valeurs ici pour connecter a votre databse
    private static final String URL = getenv("DB_URL",
            "jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
    private static final String USER = getenv("DB_USER", "root");
    private static final String PASSWORD = getenv("DB_PASSWORD", "root");

    private static String getenv(String key, String def) {
        String v = System.getenv(key);
        return v != null ? v : def;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}