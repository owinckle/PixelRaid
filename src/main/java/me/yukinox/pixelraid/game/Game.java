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

	private HashMap<String, PlayerManager> players;

	public Integer teamSize;
	private HashMap<String, PlayerManager> blueTeam;
	private HashMap<String, PlayerManager> redTeam;
	private Integer preGameCountdown;
	private Integer buildPhaseDuration;
	private Integer raidPhaseDuration;
	private Integer killReward;
	private Integer winReward;

	private BukkitTask preGameTask;
	private BukkitTask teamSelectionTask;
	private BukkitTask buildPhaseTask;
	private BukkitTask raidPhaseTask;

	public GameState gameState;

	public Game(PixelRaid plugin, Integer teamSize) {
		this.plugin = plugin;
		this.teamSize = teamSize;
		this.preGameCountdown = plugin.config.getInt("gameSettings.preGameCountdown");
		this.buildPhaseDuration = plugin.config.getInt("gameSettings.buildPhaseDuration") * 60;
		this.raidPhaseDuration = plugin.config.getInt("gameSettings.raidPhaseDuration") * 60;
		this.killReward = plugin.config.getInt("gameSettings.killReward");
		this.winReward = plugin.config.getInt("gameSettings.winReward");
		this.gameState = GameState.WAITING_FOR_PLAYERS;

		players = new HashMap<String, PlayerManager>();
		blueTeam = new HashMap<String, PlayerManager>();
		redTeam = new HashMap<String, PlayerManager>();
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

		preGameTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			int countdown = preGameCountdown;

			public void run() {
				if (countdown <= 0) {
					startGame();
					return;
				}

				if (players.size() < teamSize * 2) {
					cancelPreparation();
					return;
				}

				gameBroadcast("Game starting in " + countdown + " seconds");
				countdown--;
			}
		}, 0L, 20L);
	}

	public void cancelPreparation() {
		gameState = GameState.WAITING_FOR_PLAYERS;
		preGameTask.cancel();
	}

	private void startGame() {
		gameState = GameState.TEAMS_SELECTION;
		preGameTask.cancel();

		// Select a map
		Set<String> maps = plugin.maps.getKeys(false);
		maps.removeAll(plugin.activeMaps);
		Random rand = new Random();
		int randomIndex = rand.nextInt(maps.size());
		String map = (String) maps.toArray()[randomIndex];

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
			player.setGameMode(GameMode.ADVENTURE);
			player.teleport(spawn);

		}
		startTeamSelectionPhase();
	}

	private void startTeamSelectionPhase() {
		gameBroadcast("You have 30 seconds to select a team.");

		TeamMenu teamMenu = new TeamMenu(plugin);
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();
			teamMenu.open(player);
		}

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
		// Opens the kit menu
		KitMenu menu = new KitMenu(plugin);
		for (PlayerManager playerManager : players.values()) {
			Player player = playerManager.getPlayer();
			menu.open(player);
			player.setGameMode(GameMode.CREATIVE);
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

				if (countdown <= 5) {
					gameBroadcast(countdown + " seconds remaining.");
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void startRaidPhase() {

		gameBroadcast("Raid phase has started, you have " + (raidPhaseDuration / 60) + " minutes!");
		raidPhaseTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
			// int countdown = raidPhaseDuration * 60;
			int countdown = 10;

			public void run() {
				if (countdown <= 0) {
					endGame();
					raidPhaseTask.cancel();
				}

				if (countdown <= 5) {
					gameBroadcast(countdown + " seconds remaining.");
				}
				countdown--;
			}
		}, 0L, 20L);
	}

	private void endGame() {
		gameBroadcast("Game is over!");

	}

	public void setTeam(Player player, Team team) {
		PlayerManager playerManager = getPlayerManager(player);
		if (team == Team.BLUE) {
			playerManager.setTeam(Team.BLUE);
			blueTeam.put(player.getName(), playerManager);
			playerManager.sendMessage(ChatColor.GREEN, "You joined the blue team.");
		} else if (team == Team.RED) {
			playerManager.setTeam(Team.RED);
			redTeam.put(player.getName(), playerManager);
			playerManager.sendMessage(ChatColor.GREEN, "You joined the red team.");
		} else if (team == null) {
			if (blueTeam.size() < teamSize) {
				playerManager.setTeam(Team.BLUE);
			} else {
				playerManager.setTeam(Team.RED);
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
		int key = teamSize - 1;
		ArrayList<Game> gameList = plugin.games.get(key);
		gameList.remove(this);
		if (gameList.isEmpty()) {
			plugin.games.remove(key);
		}
	}
}
