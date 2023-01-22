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

        // Declaring commands
        DisplayHelpCommand displayHelpCommand = new DisplayHelpCommand();
        ReloadCommand reloadCommand = new ReloadCommand(plugin);
        BuildCommand buildCommand = new BuildCommand(plugin);
        LeaveCommand leaveCommand = new LeaveCommand(plugin);
        SetCommand setCommand = new SetCommand(plugin);
        JoinCommand joinCommand = new JoinCommand(plugin);
        KitCommand kitCommand = new KitCommand(plugin);

        if (args.length == 0) {
            return joinCommand.execute(player);
        }

        switch (args[0].toLowerCase()) {
            default:
            case "help":
                return displayHelpCommand.execute(player);
            case "reload":
                return reloadCommand.execute(player);
            case "build":
                return buildCommand.execute(player);
            case "leave":
                return leaveCommand.execute(player);
            case "stop":
                return false;
            case "kit":
                if (args.length != 2) {
                    return false;
                }
                return kitCommand.execute(player, args[1]);
            case "set":
                if (args.length < 3) {
                    return false;
                }

                if (args.length == 3) {
                    if (args[1].equalsIgnoreCase("map")) {
                        return setCommand.setMap(player, args[2]);

                    } else if (args[1].equalsIgnoreCase("spawn")) {
                        return setCommand.setSpawn(player, args[2]);
                    }
                } else if (args.length == 5) {
                    if (args[1].equalsIgnoreCase("team") && args[2].equalsIgnoreCase("zone")) {
                        return setCommand.setTeamZone(player, args[3], args[4]);
                    } else if (args[1].equalsIgnoreCase("team") && args[2].equalsIgnoreCase("spawn")) {
                        return setCommand.setTeamSpawn(player, args[3], args[4]);
                    }
                }
                return false;
        }
    }
}
