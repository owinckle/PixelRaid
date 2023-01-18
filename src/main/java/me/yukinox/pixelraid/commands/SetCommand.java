package me.yukinox.pixelraid.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.utils.BlockPos;

public class SetCommand {
	PixelRaid plugin;

	public SetCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	private boolean isBuilder(Player player) {
		if (plugin.builders.get(player.getName()) == null) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] You need to be in build mode.");
			return false;
		}
		return true;
	}

	private boolean isPositionValid(Player player, BlockPos selection1, BlockPos selection2) {
		if (selection1 == null || selection2 == null) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] Please first select the map's area.");
			return false;
		}
		return true;
	}

	private void setCoords(String source, BlockPos selection1, BlockPos selection2) {
		int x1 = Math.min(selection1.x, selection2.x);
		int y1 = Math.min(selection1.y, selection2.y);
		int z1 = Math.min(selection1.z, selection2.z);
		int x2 = Math.max(selection1.x, selection2.x);
		int y2 = Math.max(selection1.y, selection2.y);
		int z2 = Math.max(selection1.z, selection2.z);

		plugin.maps.set(source + ".from.x", x1);
		plugin.maps.set(source + ".from.y", y1);
		plugin.maps.set(source + ".from.z", z1);
		plugin.maps.set(source + ".to.x", x2);
		plugin.maps.set(source + ".to.y", y2);
		plugin.maps.set(source + ".to.z", z2);
		plugin.saveMaps();
	}

	public boolean setMap(Player player, String mapName) {
		if (!isBuilder(player)) {
			return false;
		}

		if (plugin.maps.getString(mapName) != null) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] A map with this name already exists.");
			return false;
		}

		BlockPos selection1 = plugin.builderSelection1.get(player.getName());
		BlockPos selection2 = plugin.builderSelection2.get(player.getName());
		if (!isPositionValid(player, selection1, selection2)) {
			return false;
		}

		setCoords(mapName, selection1, selection2);
		plugin.maps.set(mapName + ".world", player.getWorld().getName());
		plugin.saveMaps();
		player.sendMessage(ChatColor.GREEN + "[Pixel Raid] " + mapName + " has been created.");
		return true;
	}

	public boolean setSpawn(Player player, String mapName) {
		if (!isBuilder(player)) {
			return false;
		}

		if (plugin.maps.getString(mapName) == null) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] No map found with the name " + ChatColor.RED + mapName);
			return false;
		}

		BlockPos selection1 = plugin.builderSelection1.get(player.getName());
		BlockPos selection2 = plugin.builderSelection2.get(player.getName());
		if (!isPositionValid(player, selection1, selection2)) {
			return false;
		}

		selection1.y++;
		selection2.y++;
		setCoords(mapName + ".spawn", selection1, selection2);
		player.sendMessage(ChatColor.GREEN + "[Pixel Raid] Spawn for " + ChatColor.GOLD + mapName + ChatColor.GREEN
				+ " has been set.");
		return true;
	}

	public boolean setTeamZone(Player player, String color, String mapName) {
		if (!isBuilder(player)) {
			return false;
		}

		if (plugin.maps.getString(mapName) == null) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] No map found with the name " + ChatColor.RED + mapName);
			return false;
		}

		BlockPos selection1 = plugin.builderSelection1.get(player.getName());
		BlockPos selection2 = plugin.builderSelection2.get(player.getName());
		if (!isPositionValid(player, selection1, selection2)) {
			return false;
		}

		if (!color.equals("blue") && !color.equals("red")) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] Only teams available are RED and BLUE");
			return false;
		}

		selection1.y++;
		selection2.y++;
		setCoords(mapName + "." + color + ".zone", selection1, selection2);

		player.sendMessage(
				ChatColor.GREEN + "[Pixel Raid] Team " + color + " team's zone for " + ChatColor.GOLD + mapName
						+ ChatColor.GREEN
						+ " has been set.");
		return true;
	}
}
