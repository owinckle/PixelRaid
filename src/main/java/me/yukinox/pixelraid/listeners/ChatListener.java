package me.yukinox.pixelraid.listeners;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.game.PlayerManager;
import me.yukinox.pixelraid.utils.Enums;
import me.yukinox.pixelraid.utils.Enums.GameState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    PixelRaid plugin;

    public ChatListener(PixelRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Verify if the player is in a game
		Game game = plugin.players.get(player.getName());
		if (game == null) {
			return;
		}

        if (game.gameState == GameState.WAITING_FOR_PLAYERS || game.gameState == GameState.PREPARATION) {
            return ;
        }

        event.setCancelled(true);
        if (game.gameState == GameState.TEAM_SELECTION) {
            for (PlayerManager playerManager : game.players.values()) {
                playerManager.getPlayer().sendMessage(ChatColor.GREEN + "[Pixel Raid] " + player.getDisplayName() + ChatColor.RESET + " » " + event.getMessage());
            }
        }

        boolean isTeamChat = game.getPlayerManager(player).isTeamChat();
        Enums.Team thisPlayerTeam = game.getPlayerManager(player).getTeam();
        if (game.gameState == GameState.BUILDING || game.gameState == GameState.RAID) {
            for (PlayerManager playerManager : game.players.values()) {
                if (isTeamChat) {
                    if (thisPlayerTeam == playerManager.getTeam()) {
                        playerManager.getPlayer().sendMessage(ChatColor.GREEN + "[Pixel Raid] " + player.getDisplayName()  + " » " +  ChatColor.GREEN + event.getMessage());
                    }
                } else {
                    playerManager.getPlayer().sendMessage(ChatColor.GREEN + "[Pixel Raid] " + player.getDisplayName()  + " » " +  ChatColor.RESET + event.getMessage());
                }
            }
        }
    }
}
