
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String DB_PATH =Path.of("Database", "reservation.db").toAbsolutePath().toString();
    
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("DATABASE PATH: " + DB_PATH);
            Connection connection =DriverManager.getConnection(URL);

            System.out.println("Database connected successfully!");

            return connection;

        } catch (ClassNotFoundException e) {

            throw new SQLException("SQLite JDBC driver not found. Check your lib folder.",e);
        }
    }
}
