package net.forgecraft.controller;

import net.forgecraft.LCaRLP;
import net.forgecraft.database.DatabaseDriver;
import net.forgecraft.model.PlayerData;
import net.forgecraft.remote.MojangAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LicenseController {
    private final DatabaseDriver databaseManager;

    public LicenseController(final DatabaseDriver databaseManager) {
        this.databaseManager = databaseManager;
    }

    public CompletableFuture<Boolean> checkPlayerLicense(final Player player) {
        final CompletableFuture<Boolean> result = new CompletableFuture<>();
        final UUID playerId = player.getUniqueId();
        final String playerName = player.getName();

        if (LCaRLP.getInstance().getServer().getOnlineMode()) {
            databaseManager.updateLicenseStatus(playerId, playerName, true);
            result.complete(true);
            return result;
        }

        if (databaseManager.isLicensed(playerId)) {
            result.complete(true);
            return result;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final boolean isLegal = MojangAPI.checkPlayer(playerName, playerId);
                    LCaRLP.getInstance().getLogger().info(playerName + ": " + isLegal);
                    if (isLegal) databaseManager.registerPlayer(new PlayerData(playerId, "", playerName, new Date()));
                    databaseManager.updateLicenseStatus(playerId, playerName, isLegal);

                    result.complete(isLegal);
                } catch (Exception e) {
                    LCaRLP.getInstance().getLogger().warning("Ошибка при проверке лицензии для " + playerName + ": " + e.getMessage());
                    result.complete(false);
                }
            }
        }.runTaskAsynchronously(LCaRLP.getInstance());

        return result;
    }
}
