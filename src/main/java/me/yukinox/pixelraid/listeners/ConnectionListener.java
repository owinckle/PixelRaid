package me.yukinox.pixelraid.listeners;

import me.yukinox.pixelraid.game.InventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.Enums.GameState;

public class ConnectionListener implements Listener {
	private PixelRaid plugin;

	public ConnectionListener(PixelRaid plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();

		// Verify if the player is in a game
		Game game = plugin.players.get(player.getName());
		if (game == null) {
			return;
		}

		if (game.gameState == GameState.WAITING_FOR_PLAYERS || game.gameState == GameState.PREPARATION) {
			if (game.gameState == GameState.PREPARATION) {
				game.gameBroadcast(ChatColor.RED + plugin.config.getString("messages.leftRaid").replace("{player}", player.getName()));
				game.cancelPreparation();
				game.removePlayer(player);
			}
		} else {
			game.gameBroadcast(ChatColor.RED + plugin.config.getString("messages.disconnected").replace("{player}", player.getName()));
			game.forceRemovePlayer(player);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		InventoryManager inventoryManager = new InventoryManager(plugin);
		inventoryManager.restoreInventory(player);
	}
}
