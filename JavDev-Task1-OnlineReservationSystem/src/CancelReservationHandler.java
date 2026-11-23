
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
import java.util.HashMap;
import java.util.Map;

public class CancelReservationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405,
                    "{\"success\":false,\"message\":\"Method not allowed.\"}");
            return;
        }

        String username = SessionManager.getUsername(
                exchange.getRequestHeaders().getFirst("Cookie")
        );

        if (username == null) {
            sendJson(exchange, 401,
                    "{\"success\":false,\"message\":\"Please log in first.\"}");
            return;
        }

        try {
            String body;

            try (var input = exchange.getRequestBody()) {
                body = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8
                );
            }

            Map<String, String> form = parseForm(body);
            String pnr = form.getOrDefault("pnr", "").trim();

            if (pnr.isBlank()) {
                sendJson(exchange, 400,
                        "{\"success\":false,\"message\":\"Please provide a PNR.\"}");
                return;
            }

            try (Connection connection = DBConnection.getConnection()) {

                connection.setAutoCommit(false);

                try {
                    String trainNumber;
                    String status;

                    // Verify reservation ownership and current status.
                    String findSql =
                            "SELECT train_number, status FROM reservations " +
                            "WHERE pnr = ? AND username = ?";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(findSql)) {

                        statement.setString(1, pnr);
                        statement.setString(2, username);

                        try (ResultSet result = statement.executeQuery()) {

                            if (!result.next()) {
                                connection.rollback();

                                sendJson(exchange, 404,
                                        "{\"success\":false,\"message\":\"Reservation not found.\"}");
                                return;
                            }

                            trainNumber = result.getString("train_number");
                            status = result.getString("status");
                        }
                    }

                    if (!"CONFIRMED".equalsIgnoreCase(status)) {
                        connection.rollback();

                        sendJson(exchange, 409,
                                "{\"success\":false,\"message\":\"This reservation is already cancelled or cannot be cancelled.\"}");
                        return;
                    }

                    // Mark the reservation as cancelled.
                    String cancelSql =
                            "UPDATE reservations SET status = 'CANCELLED' " +
                            "WHERE pnr = ? AND username = ? AND status = 'CONFIRMED'";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(cancelSql)) {

                        statement.setString(1, pnr);
                        statement.setString(2, username);

                        if (statement.executeUpdate() != 1) {
                            connection.rollback();

                            sendJson(exchange, 409,
                                    "{\"success\":false,\"message\":\"Reservation could not be cancelled.\"}");
                            return;
                        }
                    }

                    // Restore the available seat.
                    String seatSql =
                            "UPDATE trains SET available_seats = available_seats + 1 " +
                            "WHERE train_number = ?";

                    try (PreparedStatement statement =
                                 connection.prepareStatement(seatSql)) {

                        statement.setString(1, trainNumber);

                        if (statement.executeUpdate() != 1) {
                            connection.rollback();

                            sendJson(exchange, 500,
                                    "{\"success\":false,\"message\":\"Could not restore the train seat.\"}");
                            return;
                        }
                    }

                    connection.commit();

                    sendJson(exchange, 200,
                            "{\"success\":true,\"message\":\"Reservation cancelled successfully.\"}");

                } catch (SQLException | RuntimeException e) {

                    connection.rollback();
                    throw e;
                }
            }

        } catch (SQLException | RuntimeException e) {

            System.out.println("Cancellation error: " + e.getMessage());

            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"Unable to cancel reservation.\"}");
        }
    }

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
