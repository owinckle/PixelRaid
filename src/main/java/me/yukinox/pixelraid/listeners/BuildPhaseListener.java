package me.yukinox.pixelraid.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.ActionInZone;
import me.yukinox.pixelraid.utils.Enums.GameState;

public class BuildPhaseListener implements Listener {
	PixelRaid plugin;

	public BuildPhaseListener(PixelRaid plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();

		Game game = plugin.players.get(player.getName());
		if (game == null) {
			return;
		}

		ItemStack item = event.getItemInHand();
		if (game.gameState == GameState.BUILDING || game.gameState == GameState.RAID) {
			if (item != null && !plugin.config.getStringList("buildMenu.items").contains(item.getType().name())) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "[Pixel Raid] You're not allowed to use this item.");
			} else {
				ActionInZone actionInZone = new ActionInZone(plugin);

				if (!actionInZone.isInZone(event.getBlock(), game, player)) {
					player.sendMessage(ChatColor.RED + "[Pixel Raid] You can't build here!");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onBlockDrop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
}
