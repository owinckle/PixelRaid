package me.yukinox.pixelraid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import me.yukinox.pixelraid.listeners.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.yukinox.pixelraid.commands.Executor;
import me.yukinox.pixelraid.game.Game;
import me.yukinox.pixelraid.utils.BlockPos;

public final class PixelRaid extends JavaPlugin {

    // Configuration
    public FileConfiguration config;
    public FileConfiguration kits;
    public FileConfiguration maps;

    // Files
    private File kitsFile;
    private File mapsFile;

    // Game
    public HashMap<Integer, ArrayList<Game>> games;
    public HashMap<String, Game> players;
    public HashSet<String> activeMaps;

    // Build
    public HashMap<String, Player> builders;
    public HashMap<String, BlockPos> builderSelection1;
    public HashMap<String, BlockPos> builderSelection2;

    @Override
    public void onEnable() {
        loadConfig();
        loadKits();
        loadMaps();
        loadGame();

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new ClickListener(this), this);
        getServer().getPluginManager().registerEvents(new BreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractionsListener(this), this);
        getServer().getPluginManager().registerEvents(new BuildPhaseListener(this), this);
        getServer().getPluginManager().registerEvents(new TntListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        getCommand("raid").setExecutor(new Executor(this));
    }

    @Override
    public void onDisable() {
        saveMaps();
    }

    private void loadConfig() {
        config = getConfig();
    }

    public void loadKits() {
        kitsFile = new File(getDataFolder(), "kits.yml");
        kits = YamlConfiguration.loadConfiguration(kitsFile);

        if (!kitsFile.exists()) {
            kitsFile.getParentFile().mkdirs();
            saveResource("kits.yml", false);
        }
    }

    private void loadMaps() {
        mapsFile = new File(getDataFolder(), "maps.yml");
        maps = YamlConfiguration.loadConfiguration(mapsFile);

        if (!mapsFile.exists()) {
            mapsFile.getParentFile().mkdirs();
            saveResource("maps.yml", false);
        }
    }

    public void saveMaps() {
        try {
            maps.save(mapsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadGame() {
        games = new HashMap<Integer, ArrayList<Game>>();
        activeMaps = new HashSet<String>();
        players = new HashMap<String, Game>();
        builders = new HashMap<String, Player>();
        builderSelection1 = new HashMap<String, BlockPos>();
        builderSelection2 = new HashMap<String, BlockPos>();
    }
}
