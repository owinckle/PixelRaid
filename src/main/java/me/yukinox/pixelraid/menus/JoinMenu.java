package me.yukinox.pixelraid.menus;

import java.util.ArrayList;

import me.yukinox.pixelraid.utils.Enums;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;

public class JoinMenu {
	private PixelRaid plugin;

	public JoinMenu(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public void open(Player player) {
		Inventory menu = Bukkit.createInventory(null, 9 * 2, plugin.config.getString("joinMenu.title"));

		for (Integer i = 1; i <= 10; i++) {
			ItemStack itemStack = new ItemStack(Material.getMaterial("IRON_SWORD"), i);
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(i + " vs " + i);
			itemStack.setItemMeta(itemMeta);
			menu.addItem(itemStack);
		}

		player.openInventory(menu);
	}

	public void handler(InventoryClickEvent event) {
		event.setCancelled(true);

		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		int selectedMode = event.getSlot();
		ArrayList<Game> games = plugin.games.computeIfAbsent(selectedMode, k -> new ArrayList<>());

		if (plugin.players.get(player.getName()) != null) {
			if (plugin.players.get(player.getName()).teamSize == selectedMode + 1) {
				player.sendMessage(ChatColor.RED + "[Pixel Raid] " + plugin.config.getString("messages.alreadyInQueue"));
				return;
			}
			plugin.players.get(player.getName()).removePlayer(player);
		}

		Game game;
		if (games.isEmpty()) {
			game = new Game(plugin, selectedMode + 1);
			games.add(game);
		} else {
			game = games.get(games.size() - 1);
		}

		if (game.teamSize * 2 > game.getTotalPlayer() && game.gameState == Enums.GameState.WAITING_FOR_PLAYERS) {
			game.addPlayer(player);
		} else {
			Game newGame = new Game(plugin, selectedMode + 1);
			games.add(newGame);
			newGame.addPlayer(player);
		}

		player.closeInventory();
	}
}
