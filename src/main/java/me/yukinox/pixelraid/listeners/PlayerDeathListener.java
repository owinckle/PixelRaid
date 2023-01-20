package me.yukinox.pixelraid.listeners;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.game.PlayerManager;
import me.yukinox.pixelraid.utils.Enums;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathListener implements Listener {
    PixelRaid plugin;

    public PlayerDeathListener(PixelRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Game game = plugin.players.get(player.getName());
        if (game == null) {
            return ;
        }

        if (event.getFinalDamage() >= player.getHealth()) {
            event.setCancelled(true);
            PlayerManager playerManager = game.getPlayerManager(player);
            playerManager.addDeath();

            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Player) {
                    Player killer = (Player) damager;
                    rewardKiller(killer, player, game);
                } else {
                    game.gameBroadcast(player.getName() + " died.");
                }
            } else if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
                Entity damager = ((EntityDamageByEntityEvent) event).getDamager();
                if (damager instanceof Arrow) {
                    Arrow arrow = (Arrow) damager;
                    if (arrow.getShooter() instanceof Player) {
                        Player killer = (Player) arrow.getShooter();
                        rewardKiller(killer, player, game);
                    }
                }
            } else {
                game.gameBroadcast(player.getName() + " died.");
            }

            // Reset player
            player.getInventory().clear();
            player.setHealth(player.getMaxHealth());
            player.setHealth(player.getMaxHealth());
			player.setSaturation(20);
			player.setFoodLevel(20);
			player.setFireTicks(0);
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

            // Teleport player
            String teamPath;
            if (playerManager.getTeam() == Enums.Team.BLUE) {
                teamPath = game.getMap() + ".blue.spawn.";
            } else {
                teamPath = game.getMap() + ".red.spawn.";
            }
            int fromX = plugin.maps.getInt(teamPath + "from.x");
            int fromY = plugin.maps.getInt(teamPath + "from.y");
            int fromZ = plugin.maps.getInt(teamPath + "from.z");
            int toX = plugin.maps.getInt(teamPath + "to.x");
            int toY = plugin.maps.getInt(teamPath + "to.y");
            int toZ = plugin.maps.getInt(teamPath + "to.z");

            double x = fromX + Math.random() * (toX - fromX);
            double y = fromY + Math.random() * (toY - fromY);
            double z = fromZ + Math.random() * (toZ - fromZ);

			Location spawn = new Location(Bukkit.getWorld(plugin.maps.getString(game.getMap() + ".world")), x, y, z);
			player.teleport(spawn);
            playerManager.loadKit();
        }
    }

    private void rewardKiller(Player killer, Player victim, Game game) {
         PlayerManager killerPlayerManager = game.getPlayerManager(killer);
         killerPlayerManager.addKill();
         ItemStack tnt = new ItemStack(Material.TNT, 1);
         killer.getInventory().addItem(tnt);
         killerPlayerManager.sendMessage(ChatColor.GREEN, plugin.config.getString("messages.tntReceived").replace("{player}", victim.getName()));
         game.gameBroadcast(ChatColor.RED +
                 plugin.config.getString("messages.killMessage")
                         .replace("{player}", victim.getName())
                         .replace("{killer}", killer.getName()));
    }
}
