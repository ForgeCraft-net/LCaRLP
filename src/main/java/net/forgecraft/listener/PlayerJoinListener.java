package net.forgecraft.listener;

import net.forgecraft.controller.LicenseController;
import net.forgecraft.database.DatabaseDriver;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public final class PlayerJoinListener implements Listener {
    private final LicenseController licenseChecker;
    private final DatabaseDriver databaseManager;

    public PlayerJoinListener(final LicenseController licenseChecker, final DatabaseDriver databaseManager) {
        this.licenseChecker = licenseChecker;
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();

        if (player.hasPermission("licensecheck.bypass")) {
            player.sendMessage(Color.GREEN + "Вы имеете право обхода проверки лицензии.");
            databaseManager.updateAuthenticationStatus(playerId, true);
            return;
        }

        licenseChecker.checkPlayerLicense(player).thenAccept(isLicensed -> {
            if (isLicensed) {
                databaseManager.updateAuthenticationStatus(playerId, true);
                player.sendMessage(Color.GREEN + "Добро пожаловать! Ваша лицензия подтверждена.");
            } else {
                if (databaseManager.isPlayerRegistered(playerId)) {
                    player.sendMessage(Color.RED + "Пожалуйста, авторизуйтесь: /login <пароль>");
                } else {
                    player.sendMessage(Color.RED + "Пожалуйста, зарегистрируйтесь: /register <пароль> <email>");
                }
                databaseManager.updateAuthenticationStatus(playerId, false);
            }
        });
    }
}
