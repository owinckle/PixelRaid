package me.yukinox.pixelraid.utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.game.PlayerManager;
import me.yukinox.pixelraid.utils.Enums.Team;

public class ActionInZone {
	PixelRaid plugin;

	public ActionInZone(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean isInZone(Block block, Game game, Player player) {
		PlayerManager playerManager = game.getPlayerManager(player);
		Location blockLocation = block.getLocation();
		int blockX = blockLocation.getBlockX();
		int blockZ = blockLocation.getBlockZ();

		String teamString;
		if (playerManager.getTeam() == Team.BLUE) {
			teamString = ".blue.zone.";
		} else {
			teamString = ".red.zone.";
		}
		int fromX = plugin.getConfig().getInt(game.getMap() + teamString + "from.x");
		int fromZ = plugin.getConfig().getInt(game.getMap() + teamString + "from.z");
		int toX = plugin.getConfig().getInt(game.getMap() + teamString + "to.x");
		int toZ = plugin.getConfig().getInt(game.getMap() + teamString + "to.z");

		int minX = Math.min(fromX, toX);
		int maxX = Math.max(fromX, toX);
		int minZ = Math.min(fromZ, toZ);
		int maxZ = Math.max(fromZ, toZ);

		return blockX >= minX && blockX <= maxX && blockZ >= minZ && blockZ <= maxZ;
	}
}
