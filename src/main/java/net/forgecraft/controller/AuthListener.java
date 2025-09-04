package net.forgecraft.controller;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.forgecraft.LCaRLP;
import net.forgecraft.database.DatabaseDriver;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class AuthListener implements Listener {
    private final DatabaseDriver databaseManager;

    private final Map<UUID, Boolean> authCache = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> licenseCache = new ConcurrentHashMap<>();

    public AuthListener(final DatabaseDriver databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();

        CompletableFuture.runAsync(() -> {
            try {
                final boolean isLicensed = databaseManager.isLicensed(playerId);
                licenseCache.put(playerId, isLicensed);

                final boolean isAuthenticated = databaseManager.isAuthenticated(playerId);
                authCache.put(playerId, isAuthenticated);

                LCaRLP.getInstance().getServer().getScheduler().runTask(LCaRLP.getInstance(), () -> {
                    if (isLicensed) {
                        player.sendMessage(Color.GREEN + "Добро пожаловать! Ваша лицензия подтверждена.");
                    } else {
                        if (isAuthenticated) {
                            player.sendMessage(Color.GREEN + "Добро пожаловать!");
                        } else {
                            if (databaseManager.isPlayerRegistered(playerId)) {
                                player.sendMessage(Color.YELLOW + "Пожалуйста, авторизуйтесь: /login <пароль>");
                            } else {
                                player.sendMessage(Color.YELLOW + "Пожалуйста, зарегистрируйтесь: /register <пароль>");
                            }
                        }
                    }
                });
            } catch (Exception e) {
                LCaRLP.getInstance().getLogger().severe("Ошибка при проверке статуса игрока " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final UUID playerId = event.getPlayer().getUniqueId();
        authCache.remove(playerId);
        licenseCache.remove(playerId);
    }

    private boolean shouldAllowAction(Player player) {
        final UUID playerId = player.getUniqueId();

        if (player.hasPermission("licensecheck.bypass")) {
            return false;
        }

        boolean isLicensed = false, isAuthenticated = false;
        if (licenseCache.containsKey(playerId)) {
            isLicensed = databaseManager.isLicensed(playerId);
            licenseCache.put(playerId, isLicensed);
        }

        if (authCache.containsKey(playerId)) {
            isAuthenticated = databaseManager.isAuthenticated(playerId);
            authCache.put(playerId, isAuthenticated);
        }

        return !isLicensed && !isAuthenticated;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (shouldAllowAction(player)) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);

                if (databaseManager.isPlayerRegistered(player.getUniqueId())) {
                    player.sendMessage(Color.RED + "Вы должны сначала авторизоваться! Используйте /login <пароль>");
                } else {
                    player.sendMessage(Color.RED + "Вы должны сначала зарегистрироваться! Используйте /register <пароль>");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        final String message = event.getMessage().toLowerCase();

        if (message.startsWith("/login") || message.startsWith("/register") || message.startsWith("/l") || message.startsWith("/reg")) {
            return;
        }

        if (shouldAllowAction(player)) {
            event.setCancelled(true);

            if (databaseManager.isPlayerRegistered(player.getUniqueId())) {
                player.sendMessage(Color.RED + "Вы должны сначала авторизоваться! Используйте /login <пароль>");
            } else {
                player.sendMessage(Color.RED + "Вы должны сначала зарегистрироваться! Используйте /register <пароль>");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (shouldAllowAction(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Color.RED + "Вы должны сначала авторизоваться!");
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (shouldAllowAction(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Color.RED + "Вы должны сначала авторизоваться!");
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (shouldAllowAction(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        if (shouldAllowAction(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Color.RED + "Вы должны сначала авторизоваться!");
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (shouldAllowAction(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            if (shouldAllowAction(player)) {
                event.setCancelled(true);
                player.sendMessage(Color.RED + "Вы должны сначала авторизоваться!");
            }
        }

        if (event.getEntity() instanceof Player player) {
            if (shouldAllowAction(player)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (shouldAllowAction(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Color.RED + "Вы должны сначала авторизоваться!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (shouldAllowAction(event.getPlayer())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(Color.RED + "Вы должны сначала авторизоваться!");
        }
    }
}
