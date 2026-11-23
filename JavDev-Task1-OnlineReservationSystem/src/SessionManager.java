
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final ConcurrentHashMap<String, String> sessions =
            new ConcurrentHashMap<>();

    public static String createSession(String username) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, username);

        return "ORS_SESSION=" + token
                + "; Path=/; HttpOnly; SameSite=Strict";
    }

    public static String getUsername(String cookieHeader) {
        String token = getToken(cookieHeader);

        if (token == null) {
            return null;
        }

        return sessions.get(token);
    }

    public static void destroySession(String cookieHeader) {
        String token = getToken(cookieHeader);

        if (token != null) {
            sessions.remove(token);
        }
    }

    private static String getToken(String cookieHeader) {
        if (cookieHeader == null) {
            return null;
        }

        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);

            if (parts.length == 2
                    && parts[0].equals("ORS_SESSION")) {
                return parts[1];
            }
        }

        return null;
    }
}
