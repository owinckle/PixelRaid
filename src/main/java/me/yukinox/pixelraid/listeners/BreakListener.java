package me.yukinox.pixelraid.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
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
	}
}
