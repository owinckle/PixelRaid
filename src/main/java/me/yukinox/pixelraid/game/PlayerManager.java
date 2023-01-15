package me.yukinox.pixelraid.game;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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

	public void saveInventory() {
		Player player = getPlayer();
		File inventoryFile = new File(plugin.getDataFolder() + File.separator + "inventories",
				player.getName() + ".yml");
		FileConfiguration inventoryConfig = YamlConfiguration.loadConfiguration(inventoryFile);
		inventoryConfig.set("inventory", player.getInventory().getContents());
		try {
			inventoryConfig.save(inventoryFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadInventory() {
		Player player = getPlayer();
		File inventoryFile = new File(plugin.getDataFolder() + File.separator + "inventories",
				player.getName() + ".yml");
		FileConfiguration inventoryConfig = YamlConfiguration.loadConfiguration(inventoryFile);

		if (!inventoryFile.exists()) {
			return;
		}
	}
}
