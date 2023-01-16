package me.yukinox.pixelraid.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.utils.Enums.Team;

public class PlayerManager {
	private PixelRaid plugin;

	// Info
	private String name;
	private Team team = null;

	// Stats
	private Integer kills = 0;
	private Integer deaths = 0;
	private String kit = null;

	public PlayerManager(PixelRaid plugin, String name) {
		this.plugin = plugin;
		this.name = name;
	}

	public Player getPlayer() {
		Player player = Bukkit.getPlayer(name);
		return player;
	}

	public void sendMessage(ChatColor color, String message) {
		Player player = getPlayer();
		if (player != null) {
			player.sendMessage(color + "[Pixel Raid] " + message);
		}
	}

	public void setKit(String kit) {
		this.kit = kit;
	}

	public String getKit() {
		return kit;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public void teleportToSpawn(String map) {
		Location spawn;
		double x;
		double y;
		double z;
		if (getTeam() == Team.BLUE) {
			x = plugin.maps.getInt(map + ".blue.zone.from.x") + Math.random()
					* (plugin.maps.getInt(map + ".blue.zone.to.x") - plugin.maps.getInt(map + ".blue.zone.from.x"));
			y = plugin.maps.getInt(map + ".blue.zone.from.y") + Math.random()
					* (plugin.maps.getInt(map + ".blue.zone.to.y") - plugin.maps.getInt(map + ".blue.zone.from.y"));
			z = plugin.maps.getInt(map + ".blue.zone.from.z") + Math.random()
					* (plugin.maps.getInt(map + ".blue.zone.to.z") - plugin.maps.getInt(map + ".blue.zone.from.z"));
		} else {
			x = plugin.maps.getInt(map + ".red.zone.from.x") + Math.random()
					* (plugin.maps.getInt(map + ".red.zone.to.x") - plugin.maps.getInt(map + ".red.zone.from.x"));
			y = plugin.maps.getInt(map + ".red.zone.from.y") + Math.random()
					* (plugin.maps.getInt(map + ".red.zone.to.y") - plugin.maps.getInt(map + ".red.zone.from.y"));
			z = plugin.maps.getInt(map + ".red.zone.from.z") + Math.random()
					* (plugin.maps.getInt(map + ".red.zone.to.z") - plugin.maps.getInt(map + ".red.zone.from.z"));
		}

		spawn = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);
		getPlayer().teleport(spawn);
	}
}
