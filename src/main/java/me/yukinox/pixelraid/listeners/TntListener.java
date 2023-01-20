package me.yukinox.pixelraid.listeners;

import me.yukinox.pixelraid.PixelRaid;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.ActionInZone;
import me.yukinox.pixelraid.utils.Enums;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class TntListener implements Listener {
    PixelRaid plugin;

    public TntListener(PixelRaid plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) {
            return;
        }

        List<Block> destroyed = event.blockList();
        Iterator iterator = destroyed.iterator();
        ActionInZone actionInZone = new ActionInZone(plugin);

        while (iterator.hasNext()) {
            Block block = (Block) iterator.next();

            for (Integer key : plugin.games.keySet()) {
                ArrayList<Game> gameList = plugin.games.get(key);

                for (Game game : gameList) {
                    if (game.gameState == Enums.GameState.RAID) {
                        if (actionInZone.isInZone(block, game, Enums.Team.BLUE)) {
                            if (block.getType() == Material.BEACON) {
                                iterator.remove();
                            }
                        } else if (actionInZone.isInZone(block, game, Enums.Team.RED)) {
                            if (block.getType() == Material.BEACON) {
                                iterator.remove();
                            }
                        } else {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }
}
