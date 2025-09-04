package net.forgecraft.command;

import net.forgecraft.LCaRLP;
import net.forgecraft.database.DatabaseDriver;
import net.forgecraft.model.PlayerData;
import org.bukkit.Color;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.util.UUID;

public final class RegisterCommand implements CommandExecutor {
    private final DatabaseDriver databaseManager;

    public RegisterCommand(final DatabaseDriver databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Эта команда только для игроков!");
            return true;
        }

        final UUID playerId = player.getUniqueId();

        LCaRLP.getInstance().getLicenseController().checkPlayerLicense(player).thenAccept(isLicensed -> {
            if (isLicensed) {
                player.sendMessage(Color.GREEN + "Вы лицензионный игрок и не нуждаетесь в регистрации!");
                return;
            }

            if (args.length < 2) {
                player.sendMessage(Color.RED + "Использование: /register <пароль>");
                return;
            }

            if (databaseManager.isPlayerRegistered(playerId)) {
                player.sendMessage(Color.RED + "Вы уже зарегистрированы! Используйте /login <пароль>");
                return;
            }

            final String password = args[0];

            if (password.length() < 6) {
                player.sendMessage(Color.RED + "Пароль должен содержать 6 символов!");
                return;
            }

            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

            databaseManager.registerPlayer(new PlayerData(playerId, passwordHash, player.getName(), new Date()));
            databaseManager.updateAuthenticationStatus(playerId, true);

            player.sendMessage(Color.GREEN + "Регистрация успешна! Добро пожаловать!");
        });

        return true;
    }
}
