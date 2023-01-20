package me.yukinox.pixelraid.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.Enums.Team;

public class ActionInZone {
	PixelRaid plugin;

	public ActionInZone(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean isInZone(Block block, Game game, Team team) {
		Location blockLocation = block.getLocation();
		int blockX = blockLocation.getBlockX();
		int blockZ = blockLocation.getBlockZ();
		int blockY = blockLocation.getBlockY() + 1; // Adding a 1 offset as the block locations is lowered by 1

		String teamString;
		if (team == Team.BLUE) {
			teamString = ".blue.zone.";
		} else {
			teamString = ".red.zone.";
		}

		int fromX = plugin.maps.getInt(game.getMap() + teamString + "from.x");
		int fromZ = plugin.maps.getInt(game.getMap() + teamString + "from.z");
		int fromY = plugin.maps.getInt(game.getMap() + teamString + "from.y");
		int toY = plugin.maps.getInt(game.getMap() + teamString + "to.y");
		int toX = plugin.maps.getInt(game.getMap() + teamString + "to.x");
		int toZ = plugin.maps.getInt(game.getMap() + teamString + "to.z");

		return blockX >= fromX && blockX <= toX && blockY >= fromY && blockY <= toY && blockZ >= fromZ && blockZ <= toZ;
	}
}
