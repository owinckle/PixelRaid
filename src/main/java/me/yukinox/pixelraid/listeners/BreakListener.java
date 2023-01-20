package me.yukinox.pixelraid.listeners;

import me.yukinox.pixelraid.game.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.ActionInZone;
import me.yukinox.pixelraid.utils.Enums.GameState;

public class BreakListener implements Listener {
	PixelRaid plugin;

	public BreakListener(PixelRaid plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();

		// Builder interactions
		if (plugin.builders.get(player.getName()) != null) {
			if (player.getItemInHand().getType() == Material.BLAZE_ROD) {
				event.setCancelled(true);
			}
		}

		// In game interactions
		Game game = plugin.players.get(player.getName());
		if (game == null) {
			return;
		}

		if (game.gameState == GameState.TEAM_SELECTION) {
			event.setCancelled(true);
		}

		if (game.gameState == GameState.BUILDING || game.gameState == GameState.RAID) {
			ActionInZone actionInZone = new ActionInZone(plugin);
			PlayerManager playerManager = game.getPlayerManager(player);

			if (!actionInZone.isInZone(event.getBlock(), game, playerManager.getTeam())) {
				playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.cantBreak"));
				event.setCancelled(true);
			} else if (event.getBlock().getType() == Material.BEACON) {
				playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.selfDestroyFlag"));
				event.setCancelled(true);
			}
		}
	}
}
