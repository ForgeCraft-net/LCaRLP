package net.forgecraft.database;

import net.forgecraft.LCaRLP;
import net.forgecraft.constants.Constants;
import net.forgecraft.model.PlayerData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Date;
import java.util.UUID;

public final class DatabaseDriver {
    private final String url;

    private Connection connection;

    public DatabaseDriver(final String databaseName) {
        this.url = Constants.BASE_DATABASE_URL + LCaRLP.getInstance().getDataFolder().getAbsolutePath() + "/" + databaseName;
    }

    public boolean connect() {
        try {
            if (!LCaRLP.getInstance().getDataFolder().exists()) {
                if (!LCaRLP.getInstance().getDataFolder().mkdirs())
                    throw new RuntimeException("[ERROR]: Failed to create configurations folders.");
            }

            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(this.url);
            return !this.connection.isClosed() && !this.connection.isReadOnly();
        } catch (SQLException | ClassNotFoundException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
        }
        return false;
    }

    public boolean disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean createTables() {
        try (final Statement stmt = connection.createStatement()) {
            stmt.execute(Constants.CREATE_PLAYERS_TABLE);
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
        return true;
    }

    public boolean isPlayerRegistered(final UUID uuid) {
        final String sql = "SELECT COUNT(*) FROM players WHERE uuid = ?";

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            final ResultSet rs = pstmt.executeQuery();

            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
    }

    public void registerPlayer(final PlayerData playerData) {
        final String sql = "INSERT INTO players(uuid, username, password_hash) " + "VALUES(?, ?, ?)";

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, playerData.getUuid().toString());
            pstmt.setString(2, playerData.getUsername());
            pstmt.setString(3, playerData.getPasswordHash());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public boolean verifyPassword(final UUID uuid, final String password) {
        final String sql = "SELECT password_hash FROM players WHERE uuid = ?";

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            final ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return BCrypt.checkpw(password, storedHash);
            }
            return false;
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
    }

    public void updateAuthenticationStatus(final UUID uuid, final boolean isAuthenticated) {
        final String sql = "UPDATE players SET is_authenticated = ?, last_login = ? WHERE uuid = ?";

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, isAuthenticated ? 1 : 0);
            pstmt.setTimestamp(2, Timestamp.from(new Date().toInstant()));
            pstmt.setString(3, uuid.toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public boolean isAuthenticated(final UUID uuid) {
        final String sql = "SELECT is_authenticated FROM players WHERE uuid = ?";

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            final ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt("is_authenticated") == 1;
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
    }

    public void updateLicenseStatus(final UUID uuid, final String username, final boolean isLicensed) {
        final String updateSql = "UPDATE players SET is_licensed = ? WHERE uuid = ?";

        try (final PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
            updateStmt.setInt(1, isLicensed ? 1 : 0);
            updateStmt.setString(2, uuid.toString());
            updateStmt.executeUpdate();
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
        }
    }

    public boolean isLicensed(final UUID uuid) {
        final String sql = "SELECT is_licensed FROM players WHERE uuid = ?";

        try (final PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            final ResultSet rs = pstmt.executeQuery();

            return rs.next() && rs.getInt("is_licensed") == 1;
        } catch (SQLException e) {
            LCaRLP.getInstance().getLogger().severe(e.getMessage());
            return false;
        }
    }
}
