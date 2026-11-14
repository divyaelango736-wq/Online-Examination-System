package in.vcet.exam;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import io.github.cdimascio.dotenv.Dotenv;

public class Database {

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }
    }

    private static final Dotenv dotenv;

    static {
        Dotenv loaded = null;
        String[] possiblePaths = {".", "backend", "../backend"};
        for (String p : possiblePaths) {
            File f = new File(p, ".env");
            if (f.exists()) {
                loaded = Dotenv.configure().directory(p).ignoreIfMissing().load();
                break;
            }
        }
        if (loaded == null) {
            loaded = Dotenv.configure().directory(".").ignoreIfMissing().load();
        }
        dotenv = loaded;
    }

    private static String env(String key, String fallback) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            if (dotenv != null) {
                value = dotenv.get(key);
            }
        }
        return (value == null || value.isBlank()) ? fallback : value;
    }

    public static Connection getConnection() throws SQLException {
        String url = env("DB_URL", "jdbc:postgresql://localhost:5432/online_exam_db");
        String user = env("DB_USER", "postgres");
        String password = env("DB_PASSWORD", "D");

        return DriverManager.getConnection(url, user, password);
    }
}
