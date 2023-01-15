package me.yukinox.pixelraid.commands;

import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.menus.JoinMenu;

public class JoinCommand {
	private PixelRaid plugin;

	public JoinCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean execute(Player player) {
		JoinMenu joinMenu = new JoinMenu(plugin);
		joinMenu.open(player);

		return true;
	}
}
