package me.yukinox.pixelraid.commands;

import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.Enums;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.menus.JoinMenu;

public class JoinCommand {
	private PixelRaid plugin;

	public JoinCommand(PixelRaid plugin) {
		this.plugin = plugin;
	}

	public boolean execute(Player player) {
		Game game = plugin.players.get(player.getName());

		if (game == null || (game.gameState == Enums.GameState.WAITING_FOR_PLAYERS || game.gameState == Enums.GameState.PREPARATION)) {
			JoinMenu joinMenu = new JoinMenu(plugin);
			joinMenu.open(player);
		} else {
			player.sendMessage(ChatColor.RED + "[Pixel Raid] " + plugin.config.getString("messages.alreadyInGame"));
		}

		return true;
	}
}
