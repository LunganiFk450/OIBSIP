
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        try {

            // Create HTTP server
            HttpServer server = HttpServer.create(
                    new InetSocketAddress(8080), 0
            );

            // =================================
            // API ENDPOINTS
            // =================================

            server.createContext("/api/login", new LoginHandler());

            server.createContext("/api/trains", new TrainSearchHandler());

            server.createContext("/api/book", new BookingHandler());

            server.createContext("/api/reservations", new ReservationHandler());

            server.createContext("/api/cancel", new CancelReservationHandler());

            server.createContext("/api/logout", new LogoutHandler());


            // =================================
            // LOGIN PAGE
            // =================================

            server.createContext("/", (HttpExchange exchange) -> {

                // Only allow GET requests
                if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                    exchange.getResponseHeaders().set("Allow", "GET");
                    exchange.sendResponseHeaders(405, -1);
                    exchange.close();
                    return;
                }

                Path page = Path.of("web", "index.html");

                if (!Files.exists(page)) {

                    String error = "Login page not found.";

                    byte[] bytes = error.getBytes(StandardCharsets.UTF_8);

                    exchange.sendResponseHeaders(404, bytes.length);

                    try (var output = exchange.getResponseBody()) {
                        output.write(bytes);
                    }

                    return;
                }

                byte[] content = Files.readAllBytes(page);

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "text/html; charset=UTF-8"
                );

                exchange.getResponseHeaders().set(
                        "Cache-Control",
                        "no-store"
                );

                exchange.sendResponseHeaders(200, content.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(content);
                }
            });


            // =================================
            // PROTECTED DASHBOARD
            // =================================

            server.createContext("/dashboard", (HttpExchange exchange) -> {

                // Check logged-in user
                String username = SessionManager.getUsername(
                        exchange.getRequestHeaders().getFirst("Cookie")
                );

                // Redirect unauthenticated users
                if (username == null) {

                    exchange.getResponseHeaders().set(
                            "Location",
                            "/"
                    );

                    exchange.sendResponseHeaders(302, -1);

                    exchange.close();

                    return;
                }

                // Only allow GET requests
                if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {

                    exchange.getResponseHeaders().set("Allow", "GET");

                    exchange.sendResponseHeaders(405, -1);

                    exchange.close();

                    return;
                }

                Path page = Path.of("web", "dashboard.html");

                if (!Files.exists(page)) {

                    String error = "Dashboard page not found.";

                    byte[] bytes = error.getBytes(StandardCharsets.UTF_8);

                    exchange.sendResponseHeaders(404, bytes.length);

                    try (var output = exchange.getResponseBody()) {
                        output.write(bytes);
                    }

                    return;
                }

                byte[] content = Files.readAllBytes(page);

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "text/html; charset=UTF-8"
                );

                // Prevent browser from caching protected dashboard
                exchange.getResponseHeaders().set(
                        "Cache-Control",
                        "no-store"
                );

                exchange.sendResponseHeaders(200, content.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(content);
                }
            });


            // =================================
            // START SERVER
            // =================================

            server.setExecutor(null);

            server.start();

            System.out.println("================================");
            System.out.println("          RAILCONNECT");
            System.out.println("     ONLINE RESERVATION SYSTEM");
            System.out.println("================================");

            System.out.println("Server running at:");
            System.out.println("http://localhost:8080");

            System.out.println("--------------------------------");
            System.out.println("Available API endpoints:");
            System.out.println("/api/login");
            System.out.println("/api/trains");
            System.out.println("/api/book");
            System.out.println("/api/reservations");
            System.out.println("/api/cancel");
            System.out.println("/api/logout");
            System.out.println("--------------------------------");

            System.out.println("Server started successfully.");

        } catch (IOException | RuntimeException e) {

            System.out.println("Server error: " + e.getMessage());

            
        }
    }
}
