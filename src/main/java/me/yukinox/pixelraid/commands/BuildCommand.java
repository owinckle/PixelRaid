package me.yukinox.pixelraid.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.yukinox.pixelraid.PixelRaid;

public class BuildCommand {
	PixelRaid plugin;

	public BuildCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean execute(Player player) {
		if (!player.hasPermission("pixelraid.admin") && !player.isOp()) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] You do not have the permissions to do that.");
			return false;
		}

		if (plugin.builders.get(player.getName()) == null) {
			plugin.builders.put(player.getName(), player);
			ItemStack wand = new ItemStack(Material.BLAZE_ROD);
			player.setItemInHand(wand);
			player.sendMessage(ChatColor.GREEN + "[Pixel Raid] Build mode enabled.");
		} else {
			plugin.builders.remove(player.getName());
			player.sendMessage(ChatColor.RED + "[Pixel Raid] Build mode disabled.");
		}
		return true;
	}
}
