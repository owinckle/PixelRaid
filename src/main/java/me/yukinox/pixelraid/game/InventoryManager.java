package me.yukinox.pixelraid.game;

import me.yukinox.pixelraid.PixelRaid;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class InventoryManager {
    private PixelRaid plugin;
    private File folder;

    public InventoryManager(PixelRaid plugin) {
        this.plugin = plugin;
        initFolder();
    }

    private void initFolder() {
        folder = new File(plugin.getDataFolder(), "inventories");
        if (!folder.exists())
            folder.mkdirs();
    }

    public File getPlayerFile(Player player) {
        return new File(folder, player.getName());
    }

    public void saveInventory(Player player) {
        File playerInventoryFile = getPlayerFile(player);
        YamlConfiguration playerInventory = new YamlConfiguration();

        playerInventory.set("inventory", player.getInventory().getContents());
        playerInventory.set("armor", player.getInventory().getArmorContents());
        playerInventory.set("exp", player.getExp());
        playerInventory.set("level", player.getLevel());
        try {
            playerInventory.save(playerInventoryFile);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private FileConfiguration getPlayerInventory(Player p) {
        File playerFile = getPlayerFile(p);
        if (!playerFile.exists())
            return null;

        YamlConfiguration playerInventory = new YamlConfiguration();
        try {
            playerInventory.load(playerFile);
            return playerInventory;
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void deletePlayerFile(Player player) {
        File playerFile = getPlayerFile(player);
        if (playerFile.exists())
            playerFile.delete();
    }

    public void restoreInventory(Player player) {
        FileConfiguration playerInventory = getPlayerInventory(player);
        if (playerInventory == null) {
            return ;
        }

        List<ItemStack> contentList = (List<ItemStack>) playerInventory.get("inventory");
        ItemStack[] inventoryContent = contentList.toArray(new ItemStack[contentList.size()]);
        player.getInventory().setContents(inventoryContent);

        List<ItemStack> armorList = (List<ItemStack>) playerInventory.get("armor");
        ItemStack[] armorContents = armorList.toArray(new ItemStack[armorList.size()]);
        player.getInventory().setArmorContents(armorContents);

        player.setExp((float)playerInventory.getDouble("exp"));
        player.setLevel(playerInventory.getInt("level"));

        deletePlayerFile(player);
    }
}
