package me.yukinox.pixelraid.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.Enums.GameState;

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

		if (game.gameState == GameState.BUILDING || game.gameState == GameState.TEAM_SELECTION) {
			event.setCancelled(true);
		}
	}
}
