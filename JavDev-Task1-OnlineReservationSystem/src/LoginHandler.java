
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

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405,
                    "{\"success\":false,\"message\":\"Method not allowed\"}");
            return;
        }

        try {
            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Map<String, String> data = parseFormData(body);

            String username = data.getOrDefault("username", "").trim();
            String password = data.getOrDefault("password", "");

            if (username.isEmpty() || password.isEmpty()) {
                sendResponse(exchange, 400,
                        "{\"success\":false,\"message\":\"Enter your login details.\"}");
                return;
            }

            String sql = "SELECT id FROM users WHERE username = ? AND password = ?";

            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, username);
                statement.setString(2, password);

                try (ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        String cookie = SessionManager.createSession(username);

                        exchange.getResponseHeaders().add("Set-Cookie", cookie);

                        sendResponse(exchange, 200,
                                "{\"success\":true,\"message\":\"Login successful!\"}");
                    } else {
                        sendResponse(exchange, 401,
                                "{\"success\":false,\"message\":\"Invalid username or password.\"}");
                    }
                }
            }

        } catch (SQLException | RuntimeException e) {
            System.out.println("Login error: " + e.getMessage());

            sendResponse(exchange, 500,
                    "{\"success\":false,\"message\":\"A server error occurred.\"}");
        }
    }

    private Map<String, String> parseFormData(String body) {

        Map<String, String> data = new HashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            if (parts.length == 2) {
                String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                data.put(key, value);
            }
        }

        return data;
    }

    private void sendResponse(HttpExchange exchange, int status, String response)
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
