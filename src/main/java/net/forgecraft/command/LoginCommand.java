package net.forgecraft.command;

import net.forgecraft.LCaRLP;
import net.forgecraft.database.DatabaseDriver;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class LoginCommand implements CommandExecutor {
    private final DatabaseDriver databaseManager;

    public LoginCommand(final DatabaseDriver databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String s, final String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        final UUID playerId = player.getUniqueId();

        LCaRLP.getInstance().getLicenseController().checkPlayerLicense(player).thenAccept(isLicensed -> {
            if (isLicensed) {
                player.sendMessage(Color.GREEN + "Вы лицензионный игрок и не нуждаетесь в авторизации!");
                return;
            }

            if (args.length < 1) {
                player.sendMessage(Color.RED + "Использование: /login <пароль>");
                return;
            }

            if (!databaseManager.isPlayerRegistered(playerId)) {
                player.sendMessage(Color.RED + "Вы еще не зарегистрированы! Используйте /register <пароль>");
                return;
            }

            String password = args[0];

            if (databaseManager.verifyPassword(playerId, password)) {
                databaseManager.updateAuthenticationStatus(playerId, true);
                player.sendMessage(Color.GREEN + "Авторизация успешна! Добро пожаловать!");
            } else {
                player.sendMessage(Color.RED + "Неверный пароль!");
            }
        });

        return true;
    }
}
