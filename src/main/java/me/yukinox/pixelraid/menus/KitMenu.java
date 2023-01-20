package me.yukinox.pixelraid.menus;

import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.game.PlayerManager;

public class KitMenu {
	private PixelRaid plugin;

	public KitMenu(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public void open(Player player) {
		Inventory menu = Bukkit.createInventory(null, 9, plugin.config.getString("kitMenu.title"));

		Set<String> kits = plugin.kits.getConfigurationSection("kits").getKeys(false);
		for (String kit : kits) {
			ConfigurationSection kitSection = plugin.kits.getConfigurationSection("kits." + kit);

			ItemStack itemStack = new ItemStack(Material.getMaterial(kitSection.getString("iconId")));
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setDisplayName(kit);
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

		String selectedKit = item.getItemMeta().getDisplayName();
		Game game = plugin.players.get(player.getName());
		if (game == null) {
			return;
		}
		game.setPlayerKit(player, selectedKit);
		player.closeInventory();
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
		if (playerManager.getKit() != null) {
			return;
		}

		Set<String> kits = plugin.kits.getConfigurationSection("kits").getKeys(false);
		Random rand = new Random();
		int randomIndex = rand.nextInt(kits.size());
		game.setPlayerKit(player, (String) kits.toArray()[randomIndex]);
	}
}
