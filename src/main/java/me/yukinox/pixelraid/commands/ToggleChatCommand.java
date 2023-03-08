package me.yukinox.pixelraid.commands;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.game.PlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ToggleChatCommand {
    PixelRaid plugin;

    public ToggleChatCommand(PixelRaid plugin) {
        this.plugin = plugin;
    }

    public boolean execute(Player player) {
        Game game = plugin.players.get(player.getName());
		if (game == null) {
            player.sendMessage(ChatColor.GOLD + plugin.config.getString("messages.notInRaid"));
            return false;
		}

        PlayerManager playerManager = game.getPlayerManager(player);
        playerManager.toggleChat();
        return true;
    }
}
