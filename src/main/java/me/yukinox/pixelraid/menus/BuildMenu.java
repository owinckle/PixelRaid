package me.yukinox.pixelraid.menus;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.yukinox.pixelraid.PixelRaid;

public class BuildMenu {
	PixelRaid plugin;

	public BuildMenu(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public void load(Player player) {
		Inventory inventory = player.getInventory();
		inventory.clear();

		player.setGameMode(org.bukkit.GameMode.CREATIVE);

		List<String> items = plugin.config.getStringList("buildMenu.items");
		for (String item : items) {
			ItemStack itemStack = new ItemStack(Material.getMaterial(item));
			inventory.addItem(itemStack);
		}
	}
}
