package me.yukinox.pixelraid.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.menus.JoinMenu;
import me.yukinox.pixelraid.menus.KitMenu;
import me.yukinox.pixelraid.menus.TeamMenu;

public class InventoryListener implements Listener {
	private PixelRaid plugin;

	public InventoryListener(PixelRaid plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		String menuTitle = event.getInventory().getTitle();

		if (menuTitle.equals(plugin.config.getString("kitMenu.title"))) {
			KitMenu menu = new KitMenu(plugin);
			menu.handler(event);
		} else if (menuTitle.equals(plugin.config.getString("joinMenu.title"))) {
			JoinMenu menu = new JoinMenu(plugin);
			menu.handler(event);
		} else if (menuTitle.equals(plugin.config.getString("teamMenu.title"))) {
			TeamMenu menu = new TeamMenu(plugin);
			menu.handler(event);
		}
	}

	@EventHandler
	void onInventoryClose(InventoryCloseEvent event) {
		String menuTitle = event.getInventory().getTitle();

		if (menuTitle.equals(plugin.config.getString("kitMenu.title"))) {
			KitMenu menu = new KitMenu(plugin);
			menu.handleClose(event);
		} else if (menuTitle.equals(plugin.config.getString("teamMenu.title"))) {
			TeamMenu menu = new TeamMenu(plugin);
			menu.handleClose(event);
		}
	}
}
