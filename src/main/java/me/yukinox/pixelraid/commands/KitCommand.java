package me.yukinox.pixelraid.commands;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.menus.KitMenu;

import org.bukkit.entity.Player;

public class KitCommand {
    private PixelRaid plugin;

    public KitCommand(PixelRaid plugin) {
        this.plugin = plugin;
    }

    public boolean execute(Player player) {
        KitMenu menu = new KitMenu(plugin);
        menu.open(player);
        return true;
    }
}
