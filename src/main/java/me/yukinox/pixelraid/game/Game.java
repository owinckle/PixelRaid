package me.yukinox.pixelraid.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.menus.KitMenu;
import me.yukinox.pixelraid.menus.TeamMenu;
import me.yukinox.pixelraid.utils.Enums.GameState;
import me.yukinox.pixelraid.utils.Enums.Team;

public class Game {
	private PixelRaid plugin;

	private HashMap<String, PlayerManager> players = new HashMap<String, PlayerManager>();

	public Integer teamSize;
	private HashMap<String, PlayerManager> blueTeam = new HashMap<String, PlayerManager>();
	private HashMap<String, PlayerManager> redTeam = new HashMap<String, PlayerManager>();
	private String map = null;;
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
		this.preGameCountdown = plugin.config.getInt("gameSettings.preGameCountdown");
		this.buildPhaseDuration = plugin.config.getInt("gameSettings.buildPhaseDuration") * 60;
		this.raidPhaseDuration = plugin.config.getInt("gameSettings.raidPhaseDuration") * 60;
		this.killReward = plugin.config.getInt("gameSettings.killReward");
		this.winReward = plugin.config.getInt("gameSettings.winReward");
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
			player.sendMessage(ChatColor.RED + "[Pixel Raid] You're already in a game.");
			return;
		}

		players.remove(player.getName());
		plugin.players.remove(player.getName());

		if (players.size() == 0) {
			deleteGame();
		}

		player.sendMessage(ChatColor.RED + "[Pixel Raid] You left the queue.");
		gameBroadcast(teamSize + "vs" + teamSize + ": " + ChatColor.GREEN + players.size() + "/" + (teamSize * 2));
	}

	private void startPreparation() {
		gameState = GameState.PREPARATION;

		gameBroadcast("Game starting in " + preGameCountdown);
		preGameTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int countdown = preGameCountdown;

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
					gameBroadcast("Game starting in " + countdown + " seconds");
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
		Random rand = new Random();
		int randomIndex = rand.nextInt(maps.size());
		map = (String) maps.toArray()[randomIndex];

		Location spawn;
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();

			// Teleports the player to the spawn
			double x = plugin.maps.getInt(map + ".spawn.from.x") + Math.random()
					* (plugin.maps.getInt(map + ".spawn.to.x") - plugin.maps.getInt(map + ".spawn.from.x"));
			double y = plugin.maps.getInt(map + ".spawn.from.y") + Math.random()
					* (plugin.maps.getInt(map + ".spawn.to.y") - plugin.maps.getInt(map + ".spawn.from.y"));
			double z = plugin.maps.getInt(map + ".spawn.from.z") + Math.random()
					* (plugin.maps.getInt(map + ".spawn.to.z") - plugin.maps.getInt(map + ".spawn.from.z"));
			spawn = new Location(Bukkit.getWorld(plugin.maps.getString(map + ".world")), x, y, z);
			player.getInventory().clear();
			player.setGameMode(GameMode.SURVIVAL);
			player.teleport(spawn);

		}
		startTeamSelectionPhase();
	}

	private void startTeamSelectionPhase() {
		gameState = GameState.TEAM_SELECTION;
		gameBroadcast("You have 30 seconds to select a team.");

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

		gameBroadcast("Team selection has started, you have 30 seconds!");
		teamSelectionTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int countdown = 30;

			public void run() {
				if (countdown <= 0) {
					for (PlayerManager playerManager : players.values()) {
						Player player = playerManager.getPlayer();
						if (blueTeam.get(player.getName()) == null && redTeam.get(player.getName()) == null) {
							playerManager.setTeam(null);
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
					gameBroadcast(countdown + " seconds remaining.");
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void startBuildPhase() {
		gameState = GameState.BUILDING;

		// Opens the kit menu
		KitMenu menu = new KitMenu(plugin);
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();
			player.getInventory().clear();
			// player.setGameMode(GameMode.CREATIVE);
			menu.open(player);
			playerManager.teleportToSpawn(map);
		}

		gameBroadcast("Build phase has started, you have " + (buildPhaseDuration / 60) + " minutes!");

		buildPhaseTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			// int countdown = buildPhaseDuration * 60;
			int countdown = 10;

			public void run() {
				if (countdown <= 0) {
					startRaidPhase();
					buildPhaseTask.cancel();
				}

				if (countdown <= 5 && countdown > 0) {
					gameBroadcast(countdown + " seconds remaining.");
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void startRaidPhase() {
		gameState = GameState.RAID;
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();
			player.setGameMode(GameMode.SURVIVAL);
			playerManager.teleportToSpawn(map);
		}

		gameBroadcast("Raid phase has started, you have " + (raidPhaseDuration / 60) + " minutes!");
		raidPhaseTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			// int countdown = raidPhaseDuration * 60;
			int countdown = 10;

			public void run() {
				if (countdown <= 0) {
					endGame();
					raidPhaseTask.cancel();
				}

				if (countdown <= 5 && countdown > 0) {
					gameBroadcast(countdown + " seconds remaining.");
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void endGame() {
		gameBroadcast("Game is over!");
		// deleteGame();
	}

	public void setTeam(Player player, Team team) {
		PlayerManager playerManager = getPlayerManager(player);
		if (team == Team.BLUE) {
			if (blueTeam.size() < teamSize) {
				playerManager.setTeam(Team.BLUE);
				blueTeam.put(player.getName(), playerManager);
				playerManager.sendMessage(ChatColor.GREEN, "You joined the blue team.");
				playerManager.getPlayer().closeInventory();
			} else {
				playerManager.sendMessage(ChatColor.RED, "This team is full.");
			}
		} else if (team == Team.RED) {
			if (redTeam.size() < teamSize) {
				playerManager.setTeam(Team.RED);
				redTeam.put(player.getName(), playerManager);
				playerManager.sendMessage(ChatColor.GREEN, "You joined the red team.");
				playerManager.getPlayer().closeInventory();
			} else {
				playerManager.sendMessage(ChatColor.RED, "This team is full.");
			}
		} else if (team == null) {
			if (blueTeam.size() < teamSize) {
				playerManager.setTeam(Team.BLUE);
				playerManager.sendMessage(ChatColor.GREEN, "You joined the blue team.");
			} else {
				playerManager.setTeam(Team.RED);
				playerManager.sendMessage(ChatColor.GREEN, "You joined the red team.");
			}
		}
	}

	public void setPlayerKit(Player player, String kit) {
		PlayerManager playerManager = getPlayerManager(player);
		playerManager.setKit(kit);
		playerManager.sendMessage(ChatColor.GREEN, "You've selected the " + kit + " kit.");
	}

	public void gameBroadcast(String message) {
		for (PlayerManager playerManager : players.values()) {
			playerManager.sendMessage(ChatColor.GOLD, message);
		}
	}

	public PlayerManager getPlayerManager(Player player) {
		return players.get(player.getName());
	}

	private void deleteGame() {
		for (PlayerManager playerManager : players.values()) {
			plugin.players.remove(playerManager.getPlayer().getName());
		}

		int key = teamSize - 1;
		ArrayList<Game> gameList = plugin.games.get(key);
		gameList.remove(this);
		if (gameList.isEmpty()) {
			plugin.games.remove(key);
		}
	}
}
