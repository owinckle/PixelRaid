package me.yukinox.pixelraid.listeners;

import me.yukinox.pixelraid.game.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
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

		Block block = event.getBlock();
		PlayerManager playerManager = game.getPlayerManager(player);
		if (game.gameState == GameState.BUILDING || game.gameState == GameState.RAID) {
			if (block != null && !plugin.config.getStringList("buildMenu.items").contains(block.getType().name())) {
				if (block.getType() == Material.TNT && game.gameState == GameState.RAID) {
					if (!playerManager.canPlaceTnt()) {
						event.setCancelled(true);
						playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.tntCooldown"));
						return;
					}
					block.getWorld().spawn(block.getLocation(), TNTPrimed.class);
					block.setType(Material.AIR);
					playerManager.updateTntCooldown();
				} else {
					event.setCancelled(true);
					playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.itemNotAllowed"));
				}
			} else {
				ActionInZone actionInZone = new ActionInZone(plugin);

				if (!actionInZone.isInZone(block, game, playerManager.getTeam())) {
					playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.cantBuild"));
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
