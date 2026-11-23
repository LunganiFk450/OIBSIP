
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReservationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
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

        String sql =
                "SELECT pnr, passenger_name, train_number, travel_class, " +
                "travel_date, source, destination, booking_date, status " +
                "FROM reservations WHERE username = ? " +
                "ORDER BY booking_date DESC";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    if (!first) {
                        json.append(",");
                    }

                    first = false;

                    json.append("{")
                        .append("\"pnr\":\"").append(escapeJson(result.getString("pnr"))).append("\",")
                        .append("\"passengerName\":\"").append(escapeJson(result.getString("passenger_name"))).append("\",")
                        .append("\"trainNumber\":\"").append(escapeJson(result.getString("train_number"))).append("\",")
                        .append("\"travelClass\":\"").append(escapeJson(result.getString("travel_class"))).append("\",")
                        .append("\"travelDate\":\"").append(escapeJson(result.getString("travel_date"))).append("\",")
                        .append("\"source\":\"").append(escapeJson(result.getString("source"))).append("\",")
                        .append("\"destination\":\"").append(escapeJson(result.getString("destination"))).append("\",")
                        .append("\"bookingDate\":\"").append(escapeJson(result.getString("booking_date"))).append("\",")
                        .append("\"status\":\"").append(escapeJson(result.getString("status"))).append("\"")
                        .append("}");
                }
            }

            json.append("]");

            sendJson(exchange, 200,
                    "{\"success\":true,\"reservations\":" + json + "}");

        } catch (SQLException | RuntimeException e) {

            System.out.println("Reservation history error: " + e.getMessage());

            sendJson(exchange, 500,
                    "{\"success\":false,\"message\":\"Unable to retrieve reservations.\"}");
        }
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
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
