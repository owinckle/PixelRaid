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

		int fromX = plugin.maps.getInt(game.getMap() + teamString + "from.x");
		int fromZ = plugin.maps.getInt(game.getMap() + teamString + "from.z");
		int toX = plugin.maps.getInt(game.getMap() + teamString + "to.x");
		int toZ = plugin.maps.getInt(game.getMap() + teamString + "to.z");

		return blockX >= fromX && blockX <= toX && blockZ >= fromZ && blockZ <= toZ;
	}
}
