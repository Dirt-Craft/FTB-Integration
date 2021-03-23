package net.dirtcraft.ftbintegration.storage;

import net.dirtcraft.ftbintegration.handlers.forge.SpongePermissionHandler;
import net.dirtcraft.plugin.dirtdatabaselib.DirtDatabaseLib;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings({"SqlDialectInspection", "SqlNoDataSourceInspection"})
public class Database {

    protected Connection getConnection() {
        return DirtDatabaseLib.getConnection("ftbintegration", null);
    }

    private String getContext(){
        return SpongePermissionHandler.INSTANCE.getServerContext();
    }

    public void createRecord(UUID uuid) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("INSERT INTO Chunks (uuid, context, loaders, claims) VALUES (?, ?, ?, ?)")) {

            ps.setString(1, uuid.toString());
            ps.setString(2, getContext());
            ps.setInt(3, 0);
            ps.setInt(4, 0);
            ps.execute();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public boolean addClaims(UUID uuid, int amount) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE Chunks SET claims = claims + ? WHERE uuid = ? AND context = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, getContext());
            ps.executeUpdate();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean removeClaims(UUID uuid, int amount) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE Chunks SET claims = claims - ? WHERE uuid = ? AND context = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, getContext());
            ps.executeUpdate();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean setClaims(UUID uuid, int amount) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE Chunks SET claims = ? WHERE uuid = ? AND context = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, getContext());
            ps.executeUpdate();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean addLoaders(UUID uuid, int amount) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE Chunks SET loaders = loaders + ? WHERE uuid = ? AND context = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, getContext());
            ps.executeUpdate();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean removeLoaders(UUID uuid, int amount) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE Chunks SET loaders = loaders - ? WHERE uuid = ? AND context = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, getContext());
            ps.executeUpdate();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean setLoaders(UUID uuid, int amount) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("UPDATE Chunks SET loaders = ? WHERE uuid = ? AND context = ?")) {
            ps.setInt(1, amount);
            ps.setString(2, uuid.toString());
            ps.setString(3, getContext());
            ps.executeUpdate();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public Optional<ChunkData> getChunkData(UUID PID) {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM Chunks WHERE uuid = ? AND context = ?")) {
            ps.setString(1, PID.toString());
            ps.setString(2, getContext());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                int loaders = rs.getInt("loaders");
                int claims = rs.getInt("claims");
                return Optional.of(new ChunkData(loaders, claims));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    public static class ChunkData{
        public final int loaders;
        public final int claims;
        public ChunkData(int loaders, int claims){
            this.loaders = loaders;
            this.claims = claims;
        }
    }
}
