import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Environment variables override local db.properties; credentials are not committed. */
public final class DBConnection {
    private DBConnection() {}
    private static final Properties CONFIG = new Properties();
    static {
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) CONFIG.load(input);
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
    private static String get(String env, String property, String fallback) {
        String value = System.getenv(env);
        return value != null ? value : CONFIG.getProperty(property, fallback);
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            get("DB_URL", "db.url", "jdbc:mysql://localhost:3306/restaurant_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"),
            get("DB_USER", "db.user", "root"),
            get("DB_PASSWORD", "db.password", ""));
    }
}
