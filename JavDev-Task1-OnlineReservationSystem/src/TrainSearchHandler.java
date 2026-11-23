
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

public class TrainSearchHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String username = SessionManager.getUsername(
                exchange.getRequestHeaders().getFirst("Cookie")
        );

        if (username == null) {
            send(exchange, 401, "{\"message\":\"Please log in first.\"}");
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            send(exchange, 405, "{\"message\":\"Method not allowed.\"}");
            return;
        }

        try {
            String query = exchange.getRequestURI().getRawQuery();

            String source = getParameter(query, "source");
            String destination = getParameter(query, "destination");

            if (source.isBlank() || destination.isBlank()) {
                send(exchange, 400,
                        "{\"message\":\"Enter both source and destination.\"}");
                return;
            }

            String sql = """
                SELECT train_number, train_name, source, destination,
                       departure_time, arrival_time, available_seats
                FROM trains
                WHERE LOWER(source) LIKE LOWER(?)
                  AND LOWER(destination) LIKE LOWER(?)
                  AND available_seats > 0
                ORDER BY departure_time
                """;

            StringBuilder json = new StringBuilder("[");
            boolean first = true;

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, "%" + source + "%");
                statement.setString(2, "%" + destination + "%");

                try (ResultSet result = statement.executeQuery()) {

                    while (result.next()) {

                        if (!first) {
                            json.append(",");
                        }

                        json.append("{")
                            .append("\"trainNumber\":\"").append(escape(result.getString("train_number"))).append("\",")
                            .append("\"trainName\":\"").append(escape(result.getString("train_name"))).append("\",")
                            .append("\"source\":\"").append(escape(result.getString("source"))).append("\",")
                            .append("\"destination\":\"").append(escape(result.getString("destination"))).append("\",")
                            .append("\"departureTime\":\"").append(escape(result.getString("departure_time"))).append("\",")
                            .append("\"arrivalTime\":\"").append(escape(result.getString("arrival_time"))).append("\",")
                            .append("\"availableSeats\":").append(result.getInt("available_seats"))
                            .append("}");

                        first = false;
                    }
                }
            }

            json.append("]");

            send(exchange, 200, json.toString());

        } catch (SQLException | RuntimeException e) {
            System.out.println("Train search error: " + e.getMessage());
            send(exchange, 500, "{\"message\":\"Unable to search for trains.\"}");
        }
    }

    private String getParameter(String query, String name) {

        if (query == null) {
            return "";
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2 && parts[0].equals(name)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8).trim();
            }
        }

        return "";
    }

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    private void send(HttpExchange exchange, int status, String response)
            throws IOException {

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
