package me.yukinox.pixelraid.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.utils.Enums.Team;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
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

	// Scoreboard
	private Scoreboard scoreboard;
	private Objective objective;

	private Boolean teamChat = true;

	public PlayerManager(PixelRaid plugin, String name) {
		this.plugin = plugin;
		this.name = name;
	}

	public Player getPlayer() {
		Player player = Bukkit.getPlayer(name);
		return player;
	}

	public void toggleChat() {
		if (teamChat) {
			teamChat = false;
			sendMessage(ChatColor.GREEN, plugin.config.getString("messages.chatSwitchToGlobal"));
		} else {
			teamChat = true;
			sendMessage(ChatColor.GREEN, plugin.config.getString("messages.chatSwitchToTeam"));
		}
	}

	public Boolean isTeamChat() {
		return teamChat;
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
		scoreboard.getTeam("deathsString").setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.info.deaths").replace("{deaths}", deaths.toString())));
	}

	public void addKill() {
		kills++;
		scoreboard.getTeam("killsString").setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.info.kills").replace("{kills}", kills.toString())));
	}

	public void loadKit() {
		ConfigurationSection itemsSection = plugin.kits.getConfigurationSection("kits." + kit + ".items");
		Set<String> itemKeys = itemsSection.getKeys(false);

		for (String key : itemKeys) {
			ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
			String id = itemSection.getString("id");
			int amount = itemSection.getInt("amount");

			try {
				Material mat = Material.getMaterial(id);
				if (mat != null) {
					ItemStack item = new ItemStack(mat, amount);
					String enchant = itemSection.getString("enchant");
					Integer enchantLevel = itemSection.getInt("enchantLevel");
					if (enchant != null && enchantLevel != null) {
						Enchantment enchantment = Enchantment.getByName(enchant);
						if (enchantment != null) {
						item.addEnchantment(enchantment, enchantLevel);
						}
					}

					if (mat.name().endsWith("HELMET")) {
						getPlayer().getInventory().setHelmet(item);
					} else if (mat.name().endsWith("CHESTPLATE")) {
						getPlayer().getInventory().setChestplate(item);
					} else if (mat.name().endsWith("LEGGINGS")) {
						getPlayer().getInventory().setLeggings(item);
					} else if (mat.name().endsWith("BOOTS")) {
						getPlayer().getInventory().setBoots(item);
					} else {
						getPlayer().getInventory().addItem(item);
					}
				} else {
					System.out.println("[Pixel Raid] Item " + id + " doesn't exist.");
				}
			} catch (IllegalArgumentException e) {
				System.out.println("[Pixel Raid] Item " + id + " doesn't exist.");
			}
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

	public void updateFlagHealth(Team team, Integer health) {
		if (team == Team.BLUE) {
			scoreboard.getTeam("blueFlag").setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.flags.blue").replace("{hp}", health.toString())));
		} else {
			scoreboard.getTeam("redFlag").setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.flags.red").replace("{hp}", health.toString())));
		}
	}

	public void generateScoreboard() {
		scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		objective = scoreboard.registerNewObjective("stats", "dummy");
		Integer initFlagHealth = plugin.config.getInt("gameSettings.flagHealth");

		String title = ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.title"));
		String flagTitle = ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.flags.title"));

		org.bukkit.scoreboard.Team blueFlag = scoreboard.registerNewTeam("blueFlag");
		blueFlag.addEntry("");
		blueFlag.setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.flags.blue").replace("{hp}", initFlagHealth.toString())));

		org.bukkit.scoreboard.Team redFlag = scoreboard.registerNewTeam("redFlag");
		redFlag.addEntry(" ");
		redFlag.setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.flags.red").replace("{hp}", initFlagHealth.toString())));

		String infoTitle = ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.info.title"));
		String teamBlue = ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.info.teamBlue"));
		String teamRed = ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.info.teamRed"));

		org.bukkit.scoreboard.Team killsString = scoreboard.registerNewTeam("killsString");
		killsString.addEntry("  ");
		killsString.setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.info.kills").replace("{kills}", kills.toString())));

		org.bukkit.scoreboard.Team deathsString = scoreboard.registerNewTeam("deathsString");
		deathsString.addEntry("   ");
		deathsString.setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("scoreboard.info.deaths").replace("{deaths}", deaths.toString())));


		Score flagHealthSpacer = objective.getScore(flagTitle);
		Score blueFlagScore = objective.getScore(""); // 0
		Score redFlagScore = objective.getScore(" "); // 1
		Score infoSpacer = objective.getScore(infoTitle);
		Score killScore = objective.getScore("  "); // 2
		Score deathScore = objective.getScore("   "); // 3
		Score spacerScore1 = objective.getScore("    "); // 4
		Score spacerScore2 = objective.getScore("     "); // 5
		Score teamScore;

		if (team == Team.BLUE) {
			teamScore = objective.getScore(teamBlue);
		} else {
			teamScore = objective.getScore(teamRed);
		}

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		objective.setDisplayName(title);
		spacerScore1.setScore(9);
		flagHealthSpacer.setScore(8);
		blueFlagScore.setScore(7);
		redFlagScore.setScore(6);
		spacerScore2.setScore(5);
		infoSpacer.setScore(4);
		teamScore.setScore(3);
		killScore.setScore(2);
		deathScore.setScore(1);

		getPlayer().setScoreboard(scoreboard);
	}

	public void removeScoreboard() {
		try {
			objective.unregister();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
}
