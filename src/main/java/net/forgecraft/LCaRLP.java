package net.forgecraft;

import lombok.Getter;
import net.forgecraft.command.LoginCommand;
import net.forgecraft.command.RegisterCommand;
import net.forgecraft.controller.AuthListener;
import net.forgecraft.controller.LicenseController;
import net.forgecraft.database.DatabaseDriver;
import net.forgecraft.listener.PlayerJoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class LCaRLP extends JavaPlugin {
    private DatabaseDriver databaseDriver;

    @Getter
    private LicenseController licenseController;

    public static LCaRLP getInstance() {
        return JavaPlugin.getPlugin(LCaRLP.class);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.databaseDriver = new DatabaseDriver(this.getConfig().getString("database.filename", "players.db"));
        this.databaseDriver.connect();
        this.databaseDriver.createTables();

        this.licenseController = new LicenseController(this.databaseDriver);

        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this.licenseController, this.databaseDriver), this);
        this.getServer().getPluginManager().registerEvents(new AuthListener(this.databaseDriver), this);

        Objects.requireNonNull(this.getCommand("register")).setExecutor(new RegisterCommand(this.databaseDriver));
        Objects.requireNonNull(this.getCommand("login")).setExecutor(new LoginCommand(this.databaseDriver));

        this.getLogger().info("Плагин проверки лицензии включен и работает с SQLite!");
    }

    @Override
    public void onDisable() {
        if (this.databaseDriver != null) this.databaseDriver.disconnect();
    }
}
