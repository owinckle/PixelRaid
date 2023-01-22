package me.yukinox.pixelraid.commands;

import me.yukinox.pixelraid.PixelRaid;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class KitCommand {
    PixelRaid plugin;

    public KitCommand(PixelRaid plugin) {
        this.plugin = plugin;
    }

    public boolean execute(Player player, String kit) {
        if (plugin.kits.getString("kits." + kit) == null) {
            player.sendMessage(ChatColor.RED + "[Pixel Raid] This it doesn't exist.");
            return false;
        }

        ConfigurationSection itemsSection = plugin.kits.getConfigurationSection("kits." + kit + ".items");
		Set<String> itemKeys = itemsSection.getKeys(false);

        Inventory displayInventory = Bukkit.createInventory(null, 9 * 3, "Kit " + kit);
        for (String key : itemKeys) {
			ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
			String id = itemSection.getString("id");
			int amount = itemSection.getInt("amount");

            try {
                Material mat = Material.getMaterial(id);

                if (mat != null) {
                    ItemStack item = new ItemStack(mat, amount);
                    String enchant = itemSection.getString("enchant");
                    Integer enchantLevel = itemSection.getInt("enchantLevel");
                    if (enchant != null && enchantLevel != null) {
                        Enchantment enchantment = Enchantment.getByName(enchant);
                        if (enchantment != null) {
                        item.addEnchantment(enchantment, enchantLevel);
                        }
                    }

                    displayInventory.addItem(item);
                }
            } catch (IllegalArgumentException e) {
				System.out.println("[Pixel Raid] Item " + id + " doesn't exist.");
			}
		}

        player.openInventory(displayInventory);
        return true;
    }
}
