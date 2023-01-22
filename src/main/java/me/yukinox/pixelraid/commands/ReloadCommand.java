package me.yukinox.pixelraid.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;

public class ReloadCommand {
	private PixelRaid plugin;

	public ReloadCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean execute(Player player) {
		if (player.hasPermission("pixelraid.admin") || player.isOp()) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] Reloading...");
			plugin.reloadConfig();
			plugin.config = plugin.getConfig();
			plugin.loadKits();
			player.sendMessage(ChatColor.GREEN + "[Pixel Raid] Reload complete.");
			return true;
		} else {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] You do not have the permissions to use this command.");
			return false;
		}
	}
}
