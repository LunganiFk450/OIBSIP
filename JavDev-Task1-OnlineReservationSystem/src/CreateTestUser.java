
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class CreateTestUser {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("================================");
        System.out.println(" CREATE TEST USER ACCOUNT");
        System.out.println("================================");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }

        String checkSql = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection()) {

            try (PreparedStatement checkStatement =
                    connection.prepareStatement(checkSql)) {

                checkStatement.setString(1, username);

                try (ResultSet result = checkStatement.executeQuery()) {
                    if (result.next()) {
                        System.out.println("That username already exists.");
                        return;
                    }
                }
            }

            try (PreparedStatement insertStatement =
                    connection.prepareStatement(insertSql)) {

                insertStatement.setString(1, username);
                insertStatement.setString(2, password);

                insertStatement.executeUpdate();

                System.out.println("Test account created successfully!");
                System.out.println("You can now use it on the login page.");
            }

        } catch (SQLException e) {
            System.out.println("Could not create account: " + e.getMessage());
        }
    }
}
