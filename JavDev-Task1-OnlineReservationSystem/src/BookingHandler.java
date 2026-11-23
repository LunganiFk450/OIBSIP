
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BookingHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        // Only POST requests are allowed.
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"success\":false,\"message\":\"Method not allowed.\"}");
            return;
        }

        // Check whether the passenger is logged in.
        String username = SessionManager.getUsername(
                exchange.getRequestHeaders().getFirst("Cookie")
        );

        if (username == null) {
            sendJson(exchange, 401,
                    "{\"success\":false,\"message\":\"Please log in first.\"}");
            return;
        }

        try {

            // Read booking form data.
            String requestBody;

            try (var input = exchange.getRequestBody()) {
                requestBody = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            Map<String, String> form = parseForm(requestBody);

            String passengerName =
                    form.getOrDefault("passengerName", "").trim();

            String trainNumber =
                    form.getOrDefault("trainNumber", "").trim();

            String travelClass =
                    form.getOrDefault("travelClass", "").trim();

            String travelDateText =
                    form.getOrDefault("travelDate", "").trim();

            // Validate required fields.
            if (passengerName.isBlank()
                    || trainNumber.isBlank()
                    || travelClass.isBlank()
                    || travelDateText.isBlank()) {

                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Please complete all booking fields.\"}");
                return;
            }

            if (passengerName.length() > 100) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Passenger name is too long.\"}");
                return;
            }

            // Validate travel class.
            if (!travelClass.equals("Economy")
                    && !travelClass.equals("Business")
                    && !travelClass.equals("First Class")) {

                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Invalid travel class.\"}");
                return;
            }

            // Validate travel date.
            LocalDate travelDate;

            try {
                travelDate = LocalDate.parse(travelDateText);
            } catch (DateTimeParseException e) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Invalid travel date.\"}");
                return;
            }

            if (travelDate.isBefore(LocalDate.now())) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Travel date cannot be in the past.\"}");
                return;
            }

            // Connect to SQLite database.
            try (Connection connection = DBConnection.getConnection()) {

                connection.setAutoCommit(false);

                try {

                    String source;
                    String destination;

                    // Check train availability.
                    String trainSql =
                            "SELECT source, destination, available_seats " +
                            "FROM trains WHERE train_number = ?";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(trainSql)) {

                        statement.setString(1, trainNumber);

                        try (ResultSet result = statement.executeQuery()) {

                            if (!result.next()) {
                                connection.rollback();

                                sendJson(exchange, 404,
                                        "{\"success\":false,\"message\":\"Train not found.\"}");
                                return;
                            }

                            source = result.getString("source");
                            destination = result.getString("destination");

                            if (result.getInt("available_seats") <= 0) {
                                connection.rollback();

                                sendJson(exchange, 409,
                                        "{\"success\":false,\"message\":\"No seats available.\"}");
                                return;
                            }
                        }
                    }

                    // Deduct one available seat.
                    String updateSql =
                            "UPDATE trains SET available_seats = available_seats - 1 " +
                            "WHERE train_number = ? AND available_seats > 0";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(updateSql)) {

                        statement.setString(1, trainNumber);

                        if (statement.executeUpdate() != 1) {
                            connection.rollback();

                            sendJson(exchange, 409,
                                    "{\"success\":false,\"message\":\"Seat is no longer available.\"}");
                            return;
                        }
                    }

                    // Generate unique PNR.
                    String pnr = "ORS" +
                            UUID.randomUUID()
                                    .toString()
                                    .replace("-", "")
                                    .substring(0, 12)
                                    .toUpperCase();

                    // Insert reservation, including account username.
                    String insertSql =
                            "INSERT INTO reservations " +
                            "(pnr, passenger_name, train_number, travel_class, " +
                            "travel_date, source, destination, booking_date, status, username) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(insertSql)) {

                        statement.setString(1, pnr);
                        statement.setString(2, passengerName);
                        statement.setString(3, trainNumber);
                        statement.setString(4, travelClass);
                        statement.setString(5, travelDate.toString());
                        statement.setString(6, source);
                        statement.setString(7, destination);
                        statement.setString(8, LocalDateTime.now().toString());
                        statement.setString(9, "CONFIRMED");
                        statement.setString(10, username);

                        statement.executeUpdate();
                    }

                    // Commit transaction.
                    connection.commit();

                    String response =
                            "{\"success\":true," +
                            "\"message\":\"Reservation confirmed successfully.\"," +
                            "\"pnr\":\"" + pnr + "\"}";

                    sendJson(exchange, 200, response);

                } catch (SQLException | RuntimeException e) {

                    connection.rollback();
                    throw e;
                }
            }

        } catch (SQLException | RuntimeException e) {

            System.out.println("Booking error: " + e.getMessage());

            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"Unable to complete booking. Please try again.\"}");
        }
    }

    // Parse form data.
    private Map<String, String> parseForm(String body) {

        Map<String, String> values = new HashMap<>();

        for (String pair : body.split("&")) {

            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0], StandardCharsets.UTF_8
            );

            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";

            values.put(key, value);
        }

        return values;
    }

    // Send JSON response.
    private void sendJson(
            HttpExchange exchange,
            int status,
            String response
    ) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type", "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
