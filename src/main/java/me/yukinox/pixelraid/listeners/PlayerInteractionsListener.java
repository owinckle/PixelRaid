package me.yukinox.pixelraid.listeners;

import me.yukinox.pixelraid.game.PlayerManager;
import me.yukinox.pixelraid.utils.ActionInZone;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.Enums.GameState;
import org.bukkit.event.player.PlayerInteractEvent;
import me.yukinox.pixelraid.utils.Enums.Team;

public class PlayerInteractionsListener implements Listener {
	PixelRaid plugin;

	public PlayerInteractionsListener(PixelRaid plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
			return;
		}

		Player damager = (Player) event.getDamager();
		Player damaged = (Player) event.getEntity();

		Game damagerGame = plugin.players.get(damager.getName());
		Game damagedGame = plugin.players.get(damaged.getName());

		if (damagerGame != damagedGame) {
			return;
		}

		Game game = damagerGame;
		if (game == null) {
			return;
		}

		if (game.gameState == GameState.BUILDING || game.gameState == GameState.TEAM_SELECTION) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null) return ;
		if (event.getClickedBlock().getType() != Material.BEACON) return;

		Block flag = event.getClickedBlock();
		Player player = event.getPlayer();
		Game game = plugin.players.get(player.getName());

		if (game == null || flag == null) {
			return ;
		}

		if (game.gameState != GameState.RAID) {
			return ;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			event.setCancelled(true);
		} else {
			PlayerManager playerManager = game.getPlayerManager(player);
			ActionInZone actionInZone = new ActionInZone(plugin);


			if (playerManager.getTeam() == Team.BLUE) {
				if (actionInZone.isInZone(flag, game, Team.RED)) {
					game.damageFlag(Team.RED);
				} else {
					playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.selfDestroyFlag"));
				}
			} else {
				if (actionInZone.isInZone(flag, game, Team.BLUE)) {
					game.damageFlag(Team.BLUE);
				} else {
					playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.selfDestroyFlag"));
				}
			}
		}
	}
}
