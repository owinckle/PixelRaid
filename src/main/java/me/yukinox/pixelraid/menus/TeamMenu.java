package me.yukinox.pixelraid.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.game.PlayerManager;
import me.yukinox.pixelraid.utils.Enums.Team;

public class TeamMenu {
	PixelRaid plugin;

	public TeamMenu(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public void open(Player player) {
		Inventory menu = Bukkit.createInventory(null, 9, plugin.config.getString("teamMenu.title"));

		for (Integer i = 1; i <= 10; i++) {
			ItemStack blueWool = new ItemStack(Material.WOOL, 1, (short) 11);
			ItemStack redWool = new ItemStack(Material.WOOL, 1, (short) 14);

			ItemMeta blueWoolMeta = blueWool.getItemMeta();
			ItemMeta redWoolMeta = redWool.getItemMeta();

			blueWoolMeta.setDisplayName("Blue");
			redWoolMeta.setDisplayName("Red");

			blueWool.setItemMeta(blueWoolMeta);
			redWool.setItemMeta(redWoolMeta);
			menu.setItem(3, blueWool);
			menu.setItem(5, redWool);
		}

		player.openInventory(menu);
	}

	public void handler(InventoryClickEvent event) {
		event.setCancelled(true);

		ItemStack item = event.getCurrentItem();
		if (item == null || item.getType() == Material.AIR) {
			return;
		}

		String itemName = item.getItemMeta().getDisplayName();
		Player player = (Player) event.getWhoClicked();
		Game game = plugin.players.get(player.getName());

		if (itemName.equals("Blue")) {
			game.setTeam(player, Team.BLUE);
		} else {
			game.setTeam(player, Team.RED);
		}
	}

	public void handleClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player)) {
			return;
		}

		Player player = (Player) event.getPlayer();
		Game game = plugin.players.get(player.getName());

		if (game == null) {
			return;
		}

		PlayerManager playerManager = game.getPlayerManager(player);
		if (playerManager.getTeam() != null) {
			return;
		}

		game.setTeam(player, null);
	}
}