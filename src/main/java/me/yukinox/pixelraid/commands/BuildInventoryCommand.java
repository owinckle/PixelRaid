package me.yukinox.pixelraid.commands;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.menus.BuildMenu;

import org.bukkit.entity.Player;

public class BuildInventoryCommand {
	private PixelRaid plugin;

	public BuildInventoryCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean execute(Player player) {
		BuildMenu menu = new BuildMenu(plugin);
		menu.load(player);
		return true;
	}
}
