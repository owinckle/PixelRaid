package me.yukinox.pixelraid.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.utils.BlockPos;

public class ClickListener implements Listener {
	private PixelRaid plugin;

	public ClickListener(PixelRaid plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if (event.getItem() == null || event.getItem().getType() == Material.AIR) {
			return;
		}

		if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getItem().getType() == Material.BLAZE_ROD) {
			handleClick(event.getPlayer(), event.getClickedBlock(), 1);
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType() == Material.BLAZE_ROD) {
			handleClick(event.getPlayer(), event.getClickedBlock(), 2);
		}
	}

	private void handleClick(Player player, Block block, int selection) {
		if (plugin.builders.get(player.getName()) == null) {
			return;
		}

		int x = block.getX();
		int y = block.getY();
		int z = block.getZ();
		BlockPos blockPos = new BlockPos(x, y, z);

		if (selection == 1) {
			plugin.builderSelection1.put(player.getName(), blockPos);
			player.sendMessage(
					ChatColor.GREEN + "[Pixel Raid] First position set to (" + x + ", " + y + ", " + z + ")");
		} else {
			plugin.builderSelection2.put(player.getName(), blockPos);
			player.sendMessage(
					ChatColor.GREEN + "[Pixel Raid] Second position set to (" + x + ", " + y + ", " + z + ")");
		}
	}
}
