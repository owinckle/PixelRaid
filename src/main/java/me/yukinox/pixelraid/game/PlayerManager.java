package me.yukinox.pixelraid.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.utils.Enums.Team;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;

public class PlayerManager {
	private PixelRaid plugin;

	// Info
	private String name;
	private Team team = null;

	// Stats
	private Integer kills = 0;
	private Integer deaths = 0;
	private String kit = null;
	private Boolean tntPlaced = false;
	private BukkitTask tntCooldownTask;

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

	public Boolean canPlaceTnt() {
		return !tntPlaced;
	}

	public void updateTntCooldown() {
		tntPlaced = true;
		tntCooldownTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int countdown = plugin.config.getInt("gameSettings.tntCooldown");

			public void run() {
				if (countdown <= 0) {
					tntPlaced = false;
					tntCooldownTask.cancel();
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	public void setKit(String kit) {
		this.kit = kit;
	}

	public String getKit() {
		return kit;
	}

	public void addDeath() {
		deaths++;
	}

	public void addKill() {
		kills++;
	}

	public void loadKit() {
		ConfigurationSection itemsSection = plugin.kits.getConfigurationSection("kits." + kit + ".items");
		Set<String> itemKeys = itemsSection.getKeys(false);

		for (String key : itemKeys) {
			ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
			String id = itemSection.getString("id");
			int amount = itemSection.getInt("amount");
			ItemStack item = new ItemStack(Material.getMaterial(id), amount);
			getPlayer().getInventory().addItem(item);
		}
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

	public void giveReward(Integer killReward, Integer winReward, Boolean won) {
		if (won) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + getPlayer().getName() + " " + winReward);
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + getPlayer().getName() + " " + killReward * kills);
	}
}
