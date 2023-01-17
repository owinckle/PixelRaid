package me.yukinox.pixelraid.commands;

import me.yukinox.pixelraid.PixelRaid;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Executor implements CommandExecutor {
    private PixelRaid plugin;

    public Executor(PixelRaid plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        Player player = (Player) sender;
        DisplayHelpCommand displayHelpCommand = new DisplayHelpCommand();

        if (args.length == 0) {
            return displayHelpCommand.execute(player);

        }

        switch (args[0].toLowerCase()) {
            default:
                return displayHelpCommand.execute(player);
            case "reload":
                ReloadCommand reloadCommand = new ReloadCommand(plugin);
                return reloadCommand.execute(player);
            case "build":
                BuildCommand buildCommand = new BuildCommand(plugin);
                return buildCommand.execute(player);
            case "inventory":
                BuildInventoryCommand buildInventoryCommand = new BuildInventoryCommand(plugin);
                return buildInventoryCommand.execute(player);
            case "kit":
                KitCommand kitCommand = new KitCommand(plugin);
                return kitCommand.execute(player);
            case "join":
                JoinCommand joinCommand = new JoinCommand(plugin);
                return joinCommand.execute(player);
            case "leave":
                LeaveCommand leaveCommand = new LeaveCommand(plugin);
                return leaveCommand.execute(player);
            case "set":
                if (args.length < 3) {
                    return false;
                }

                SetCommand setCommand = new SetCommand(plugin);
                if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("map")) {
                        return setCommand.setMap(player, args[2]);

                    } else if (args[1].equalsIgnoreCase("spawn")) {
                        return setCommand.setSpawn(player, args[2]);
                    }
                } else if (args.length == 5) {
                    if (args[1].equalsIgnoreCase("team") && args[2].equalsIgnoreCase("zone")) {
                        return setCommand.setTeamZone(player, args[3], args[4]);
                    }
                }
                return false;
        }
    }
}
