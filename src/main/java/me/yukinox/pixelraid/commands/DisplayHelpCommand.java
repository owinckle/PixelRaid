package me.yukinox.pixelraid.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DisplayHelpCommand {

    public boolean execute(Player player) {
        player.sendMessage(ChatColor.GOLD + "/raid join <teamSize>");
        return true;
    }
}
