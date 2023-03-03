package me.yukinox.pixelraid.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.menus.BuildMenu;
import me.yukinox.pixelraid.menus.KitMenu;
import me.yukinox.pixelraid.menus.TeamMenu;
import me.yukinox.pixelraid.utils.Enums.GameState;
import me.yukinox.pixelraid.utils.Enums.Team;

public class Game {
	private PixelRaid plugin;

	public HashMap<String, PlayerManager> players = new HashMap<String, PlayerManager>();

	public Integer teamSize;
	private HashMap<String, PlayerManager> blueTeam = new HashMap<String, PlayerManager>();
	private HashMap<String, PlayerManager> redTeam = new HashMap<String, PlayerManager>();
	private Integer redTeamFlag;
	private Integer blueTeamFlag;
	private String map = null;;
	private Integer teamSelectionDuration;
	private Integer preGameCountdown;
	private Integer buildPhaseDuration;
	private Integer raidPhaseDuration;
	private Integer killReward;
	private Integer winReward;

	private BukkitTask preGameTask;
	private BukkitTask teamSelectionTask;
	private BukkitTask buildPhaseTask;
	private BukkitTask raidPhaseTask;
	public GameState gameState = GameState.WAITING_FOR_PLAYERS;

	public Game(PixelRaid plugin, Integer teamSize) {
		this.plugin = plugin;
		this.teamSize = teamSize;
		this.teamSelectionDuration = plugin.config.getInt("gameSettings.teamSelectionDuration");
		this.preGameCountdown = plugin.config.getInt("gameSettings.preGameCountdown");
		this.buildPhaseDuration = plugin.config.getInt("gameSettings.buildPhaseDuration") * 60;
		this.raidPhaseDuration = plugin.config.getInt("gameSettings.raidPhaseDuration") * 60;
		this.killReward = plugin.config.getInt("gameSettings.killReward");
		this.winReward = plugin.config.getInt("gameSettings.winReward");
		this.blueTeamFlag = plugin.config.getInt("gameSettings.flagHealth");
		this.redTeamFlag = plugin.config.getInt("gameSettings.flagHealth");
	}

	public void addPlayer(Player player) {
		// Verify if the player is not already in a game
		if (plugin.players.get(player.getName()) != null) {
			removePlayer(player);
		}

		// Add the player to the game
		PlayerManager newPlayer = new PlayerManager(plugin, player.getName());
		players.put(player.getName(), newPlayer);

		// Add the player to the global player hashmap
		plugin.players.put(player.getName(), this);

		gameBroadcast(teamSize + "vs" + teamSize + ": " + ChatColor.GREEN + players.size() + "/" + (teamSize * 2));

		if (players.size() == teamSize * 2) {
			startPreparation();
		}
	}

	public void removePlayer(Player player) {
		// Verify if the player is not already in game, if he is, cancel the command
		if (gameState != GameState.WAITING_FOR_PLAYERS && gameState != GameState.PREPARATION) {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] " + plugin.config.getString("messages.alreadyInGame"));
			return;
		}

		players.remove(player.getName());
		plugin.players.remove(player.getName());

		if (players.size() == 0) {
			deleteGame();
		}

		player.sendMessage(ChatColor.RED + "[Pixel Raid] " + plugin.config.getString("messages.leftQueue"));
		gameBroadcast(teamSize + "vs" + teamSize + ": " + ChatColor.GREEN + players.size() + "/" + (teamSize * 2));
	}

	public void forceRemovePlayer(Player player) {
		players.remove(player.getName());
		plugin.players.remove(player.getName());
	}

	private void startPreparation() {
		gameState = GameState.PREPARATION;

		gameBroadcast(plugin.config.getString("messages.gameStarting").replace("{seconds}", preGameCountdown.toString()));
		preGameTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			Integer countdown = preGameCountdown;

			public void run() {
				if (countdown <= 0) {
					initGame();
					return;
				}

				if (players.size() < teamSize * 2) {
					cancelPreparation();
					return;
				}

				if (countdown <= 5 && countdown > 0) {
					gameBroadcast(plugin.config.getString("messages.gameStarting").replace("{seconds}", countdown.toString()));
				}

				countdown--;
			}
		}, 0L, 20L);
	}

	public void cancelPreparation() {
		gameState = GameState.WAITING_FOR_PLAYERS;
		preGameTask.cancel();
	}

	private void initGame() {
		preGameTask.cancel();

		// Select a map
		Set<String> maps = plugin.maps.getKeys(false);
		maps.removeAll(plugin.activeMaps);
		if (maps.isEmpty()) {
			gameBroadcast(plugin.config.getString("messages.noMapAvailable"));
			deleteGame();
			return;
		}

		Random rand = new Random();
		int randomIndex = rand.nextInt(maps.size());
		map = (String) maps.toArray()[randomIndex];
		plugin.activeMaps.add(map);

		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();
			InventoryManager inventoryManager = new InventoryManager(plugin);

			// Teleports the player to the spawn
			double x = plugin.maps.getInt(map + ".spawn.from.x") + Math.random()
					* (plugin.maps.getInt(map + ".spawn.to.x") - plugin.maps.getInt(map + ".spawn.from.x"));
			double y = plugin.maps.getInt(map + ".spawn.from.y") + Math.random()
					* (plugin.maps.getInt(map + ".spawn.to.y") - plugin.maps.getInt(map + ".spawn.from.y"));
			double z = plugin.maps.getInt(map + ".spawn.from.z") + Math.random()
					* (plugin.maps.getInt(map + ".spawn.to.z") - plugin.maps.getInt(map + ".spawn.from.z"));
			Location spawn = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);

			inventoryManager.saveInventory(player);
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.setLevel(0);
			player.setExp(0);
			player.setTotalExperience(0);
			player.setGameMode(GameMode.SURVIVAL);
			player.teleport(spawn);

		}
		startTeamSelectionPhase();
	}

	private void startTeamSelectionPhase() {
		gameState = GameState.TEAM_SELECTION;
		gameBroadcast(plugin.config.getString("messages.selectTeam").replace("{seconds}", teamSelectionDuration.toString()));

		TeamMenu teamMenu = new TeamMenu(plugin);
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();
			player.setHealth(player.getMaxHealth());
			player.setSaturation(20);
			player.setFoodLevel(20);
			player.setFireTicks(0);
			player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
			teamMenu.open(player);
		}

		teamSelectionTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			Integer countdown = teamSelectionDuration;

			public void run() {
				if (countdown <= 0) {
					for (PlayerManager playerManager : players.values()) {
						Player player = playerManager.getPlayer();
						if (playerManager.getTeam() == null) {
							if (blueTeam.size() < teamSize) {
								setTeam(player, Team.BLUE);
							} else {
								setTeam(player, Team.RED);
							}
						}

						if (player.getOpenInventory() != null) {
							player.closeInventory();
						}
					}
					teamSelectionTask.cancel();
					startBuildPhase();
					return;
				}

				if (countdown <= 5) {
					gameBroadcast(plugin.config.getString("messages.countdown").replace("{seconds}", countdown.toString()));
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void startBuildPhase() {
		gameState = GameState.BUILDING;
		int offset = plugin.config.getInt("gameSettings.flagOffset");

		// Generate the flag
		Random rand = new Random();
		int fromX = plugin.maps.getInt(map + ".blue.zone.from.x");
		int toX = plugin.maps.getInt(map + ".blue.zone.to.x");
		int fromZ = plugin.maps.getInt(map + ".blue.zone.from.z");
		int toZ = plugin.maps.getInt(map + ".blue.zone.to.z");
		int y = plugin.maps.getInt(map + ".blue.zone.from.y") - 1;

		int x = fromX + offset + rand.nextInt(toX - fromX - (offset * 2) + 1);
		int z = fromZ + offset + rand.nextInt(toZ - fromZ - (offset * 2) + 1);
		Location blueLoc = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);
		blueLoc.getBlock().setType(Material.BEACON);

		fromX = plugin.maps.getInt(map + ".red.zone.from.x");
		toX = plugin.maps.getInt(map + ".red.zone.to.x");
		fromZ = plugin.maps.getInt(map + ".red.zone.from.z");
		toZ = plugin.maps.getInt(map + ".red.zone.to.z");
		y = plugin.maps.getInt(map + ".red.zone.from.y") - 1;

		x = fromX + offset + rand.nextInt(toX - fromX - (offset * 2) + 1);
		z = fromZ + offset + rand.nextInt(toZ - fromZ - (offset * 2) + 1);
		Location redLoc = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);
		redLoc.getBlock().setType(Material.BEACON);

		KitMenu kitMenu = new KitMenu(plugin);
		BuildMenu buildMenu = new BuildMenu(plugin);
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();
			playerManager.generateScoreboard();
			player.setGameMode(GameMode.CREATIVE);
			buildMenu.load(player);
			playerManager.teleportToSpawn(map);
			kitMenu.open(player);
		}

		Integer buildDuration = buildPhaseDuration / 60;
		gameBroadcast(plugin.config.getString("messages.buildStart").replace("{minutes}", buildDuration.toString()));

		buildPhaseTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			Integer countdown = buildPhaseDuration;

			public void run() {
				if (countdown <= 0) {
					buildPhaseTask.cancel();
					startRaidPhase();
				}

				if (countdown <= 5 && countdown > 0) {
					gameBroadcast(plugin.config.getString("messages.countdown").replace("{seconds}", countdown.toString()));
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void startRaidPhase() {
		gameState = GameState.RAID;
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();

			player.getInventory().clear();
			player.setGameMode(GameMode.SURVIVAL);

			String teamPath;
            if (playerManager.getTeam() == Team.BLUE) {
                teamPath = map + ".blue.spawn.";
            } else {
                teamPath = map + ".red.spawn.";
            }
            int fromX = plugin.maps.getInt(teamPath + "from.x");
            int fromY = plugin.maps.getInt(teamPath + "from.y");
            int fromZ = plugin.maps.getInt(teamPath + "from.z");
            int toX = plugin.maps.getInt(teamPath + "to.x");
            int toY = plugin.maps.getInt(teamPath + "to.y");
            int toZ = plugin.maps.getInt(teamPath + "to.z");

            double x = fromX + Math.random() * (toX - fromX);
            double y = fromY + Math.random() * (toY - fromY);
            double z = fromZ + Math.random() * (toZ - fromZ);

			Location spawn = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);
			player.teleport(spawn);
			playerManager.loadKit();
		}

		Integer raidDuration = raidPhaseDuration / 60;
		gameBroadcast(plugin.config.getString("messages.raidStart").replace("{minutes}", raidDuration.toString()));
		raidPhaseTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			Integer countdown = raidPhaseDuration;

			public void run() {
				if (countdown <= 0) {
					raidPhaseTask.cancel();
					endGame();
				}

				if (countdown <= 5 && countdown > 0) {
					gameBroadcast(plugin.config.getString("messages.countdown").replace("{seconds}", countdown.toString()));
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void endGame() {
		gameBroadcast(plugin.config.getString("messages.gameOver"));

		Team winningTeam;
		if (blueTeamFlag == redTeamFlag) {
			gameBroadcast(plugin.config.getString("messages.draw"));
			winningTeam = null;
		} else if (blueTeamFlag > redTeamFlag) {
			gameBroadcast(plugin.config.getString("messages.blueWin"));
			winningTeam = Team.BLUE;
		} else {
			gameBroadcast(plugin.config.getString("messages.redWin"));
			winningTeam = Team.RED;
		}

		if (winningTeam == null) {
			winReward = winReward / 2;
		}
		for (PlayerManager playerManager : players.values()) {
			playerManager.removeScoreboard();
			playerManager.giveReward(killReward, winReward, playerManager.getTeam() == winningTeam || winningTeam == null);
			playerManager.getPlayer().getInventory().clear();
			playerManager.getPlayer().getInventory().setArmorContents(null);

			InventoryManager inventoryManager = new InventoryManager(plugin);
			inventoryManager.restoreInventory(playerManager.getPlayer());
		}

		resetZones();
		deleteGame();
	}

	public int getTotalPlayer() {
		return players.size();
	}

	private void resetZones() {
		int blueFromX = plugin.maps.getInt(map + ".blue.zone.from.x");
		int blueFromY = plugin.maps.getInt(map + ".blue.zone.from.y") - 1;
		int blueFromZ = plugin.maps.getInt(map + ".blue.zone.from.z");
		int blueToX = plugin.maps.getInt(map + ".blue.zone.to.x");
		int blueToY = plugin.maps.getInt(map + ".blue.zone.to.y") - 1;
		int blueToZ = plugin.maps.getInt(map + ".blue.zone.to.z");

		int redFromX = plugin.maps.getInt(map + ".red.zone.from.x");
		int redFromY = plugin.maps.getInt(map + ".red.zone.from.y") - 1;
		int redFromZ = plugin.maps.getInt(map + ".red.zone.from.z");
		int redToX = plugin.maps.getInt(map + ".red.zone.to.x");
		int redToY = plugin.maps.getInt(map + ".red.zone.to.y") - 1;
		int redToZ = plugin.maps.getInt(map + ".red.zone.to.z");

		for (int x = blueFromX; x <= blueToX; x++) {
			for (int y = blueFromY; y <= blueToY; y++) {
				for (int z = blueFromZ; z <= blueToZ; z++) {
					Location blockLocation = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);
					Block currentBlock = blockLocation.getBlock();
					currentBlock.setType(Material.AIR);
				}
			}
		}

		for (int x = redFromX; x <= redToX; x++) {
			for (int y = redFromY; y <= redToY; y++) {
				for (int z = redFromZ; z <= redToZ; z++) {
					Location blockLocation = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);
					Block currentBlock = blockLocation.getBlock();
					currentBlock.setType(Material.AIR);
				}
			}
		}
	}

	public void damageFlag(Team team) {
		Integer newFlagHealth;
		if (team == Team.BLUE) {
			blueTeamFlag--;
			newFlagHealth = blueTeamFlag;
		} else {
			redTeamFlag--;
			newFlagHealth = redTeamFlag;
		}

		for (PlayerManager playerManager : players.values()) {
			playerManager.updateFlagHealth(team, newFlagHealth);
		}

		if (blueTeamFlag == 0 || redTeamFlag == 0) {
			endGame();
		}
	}

	public void setTeam(Player player, Team team) {
		PlayerManager playerManager = getPlayerManager(player);
		if (team == Team.BLUE) {
			if (blueTeam.size() < teamSize) {
				playerManager.setTeam(Team.BLUE);
				blueTeam.put(player.getName(), playerManager);
				playerManager.sendMessage(ChatColor.GREEN, plugin.config.getString("messages.joinBlue"));
				playerManager.getPlayer().closeInventory();
			} else {
				playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.teamFull"));
			}
		} else if (team == Team.RED) {
			if (redTeam.size() < teamSize) {
				playerManager.setTeam(Team.RED);
				redTeam.put(player.getName(), playerManager);
				playerManager.sendMessage(ChatColor.GREEN, plugin.config.getString("messages.joinRed"));
				playerManager.getPlayer().closeInventory();
			} else {
				playerManager.sendMessage(ChatColor.RED, plugin.config.getString("messages.teamFull"));
			}
		}
	}

	public void setPlayerKit(Player player, String kit) {
		PlayerManager playerManager = getPlayerManager(player);
		playerManager.setKit(kit);
		playerManager.sendMessage(ChatColor.GREEN, plugin.config.getString("messages.kitSelected").replace("{kit}", kit));
	}

	public void gameBroadcast(String message) {
		for (PlayerManager playerManager : players.values()) {
			playerManager.sendMessage(ChatColor.GOLD, message);
		}
	}

	public PlayerManager getPlayerManager(Player player) {
		return players.get(player.getName());
	}

	public String getMap() {
		return map;
	}

	private void deleteGame() {
		for (PlayerManager playerManager : players.values()) {
			plugin.players.remove(playerManager.getPlayer().getName());
			playerManager.getPlayer().performCommand("spawn");
		}

		int key = teamSize - 1;
		if (plugin.games.get(key) != null) {
			ArrayList<Game> gameList = plugin.games.get(key);
			gameList.remove(this);
			if (gameList.isEmpty()) {
				plugin.games.remove(key);
			}
		}

		plugin.activeMaps.remove(map);
	}
}
