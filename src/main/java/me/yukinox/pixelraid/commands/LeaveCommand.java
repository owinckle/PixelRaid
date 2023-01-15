package me.yukinox.pixelraid.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;

public class LeaveCommand {
	PixelRaid plugin;

	public LeaveCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean execute(Player player) {
		Game game = plugin.players.get(player.getName());
		if (game != null) {
			game.removePlayer(player);
			return true;
		} else {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] You're not in a raid.");
			return false;
		}
	}
}
