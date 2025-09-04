package net.forgecraft.constants;

public class Constants {
    public static final String BASE_DATABASE_URL = "jdbc:sqlite:";
    public static final String CREATE_PLAYERS_TABLE = "CREATE TABLE IF NOT EXISTS players (" +
            "uuid VARCHAR(50) PRIMARY KEY NOT NULL, " +
            "username VARCHAR(16) NOT NULL, " +
            "password_hash TEXT, " +
            "is_licensed BOOLEAN DEFAULT 0, " +
            "is_authenticated BOOLEAN DEFAULT 0, " +
            "created_at TIMESTAMP NOT NULL DEFAULT current_timestamp, " +
            "last_login TIMESTAMP NOT NULL DEFAULT current_timestamp" +
            ");";
}
