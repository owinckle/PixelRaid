package me.yukinox.pixelraid.commands;

import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import net.md_5.bungee.api.ChatColor;

public class ReloadCommand {
	private PixelRaid plugin;

	public ReloadCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean execute(Player player) {
		if (player.hasPermission("pixelraid.admin") || player.isOp()) {
			player.sendMessage(ChatColor.RED + "[PIXEL RAID] Reloading...");
			plugin.reloadConfig();
			plugin.config = plugin.getConfig();
			player.sendMessage(ChatColor.GREEN + "[PIXEL RAID] Reload complete.");
			return true;
		} else {
			player.sendMessage(ChatColor.RED + "[PIXEL RAID] You do not have the permissions to use this command.");
			return false;
		}
	}
}
