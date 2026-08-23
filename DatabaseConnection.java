import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class DatabaseConnection {

    private static final String SERVER_NAME =
            System.getenv().getOrDefault("DB_SERVER", "127.0.0.1");

    private static final String PORT =
            System.getenv().getOrDefault("DB_PORT", "1433");

    private static final String DATABASE_NAME =
            System.getenv().getOrDefault(
                    "DB_NAME",
                    "StudentManagementSystem"
            );

    private static final String USERNAME =
            System.getenv().getOrDefault("DB_USER", "sa");

    private static final String PASSWORD =
            System.getenv("DB_PASSWORD");

    private static final String URL =
            "jdbc:sqlserver://" + SERVER_NAME + ":" + PORT +
            ";databaseName=" + DATABASE_NAME +
            ";encrypt=true;" +
            "trustServerCertificate=true;" +
            "loginTimeout=30;";

    public static Connection getConnection() {
        if (PASSWORD == null || PASSWORD.isBlank()) {
            JOptionPane.showMessageDialog(
                    null,
                    "DB_PASSWORD environment variable is not configured.",
                    "Configuration Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }

        try {
            return DriverManager.getConnection(
                    URL,
                    USERNAME,
                    PASSWORD
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Database connection failed: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return null;
        }
    }

    public static void testConnection() {
        try (Connection connection = getConnection()) {
            if (connection != null) {
                JOptionPane.showMessageDialog(
                        null,
                        "Database connection successful."
                );
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Unable to close the database connection.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
