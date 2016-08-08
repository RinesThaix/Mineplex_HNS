package ru.luvas.hns;

import ru.luvas.hns.commands.SetupMode;
import ru.luvas.hns.commands.ForceStart;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.luvas.rmcs.fakenpcs.CoinsTransfererNpc;
import ru.luvas.rmcs.servertypes.lobby.FastQueuer;
import ru.luvas.rmcs.utils.GameTop;
import ru.luvas.hns.customs.GamePerkManager;
import ru.luvas.hns.customs.GamePlayer;
import ru.luvas.hns.enums.GameType;
import ru.luvas.hns.enums.PluginMode;
import ru.luvas.hns.game.GameListener;
import ru.luvas.hns.game.HnsShard;
import ru.luvas.hns.lobby.LobbyEngine;
import ru.luvas.multiutils.MultiUtils;
import ru.luvas.rmcs.utils.EnchantmentAutoLapis;
import ru.luvas.rmcs.utils.configurations.ConfigurationGroup;

/**
 *
 * @author RinesThaix
 */
public class MainClass extends JavaPlugin {
    
    @Getter
    private static PluginMode pluginMode;
    
    @Getter
    private static GameType gameType;
    
    @Getter
    private static MainClass instance;
    
    private static ConfigurationGroup cg;
    
    private final static Set<HnsShard> games = new HashSet<>();
    
    @Override
    public void onEnable() {
        cg = ru.luvas.rmcs.MainClass.getConfigurationManager().getConfigurationGroup(this);
        instance = this;
        FileConfiguration config = getConfig();
        if(!config.isSet("plugin-mode")) {
            config.set("plugin-mode", "game");
            config.set("game-type", "classic");
            saveConfig();
        }
        
        MultiUtils.getGamesConnector().query(
                "CREATE TABLE IF NOT EXISTS " + 
                "hidenseek" +
                " (" +
                "player_name varchar(16) NOT NULL," +
                "players_caught int(6) NOT NULL DEFAULT 0," +
                "wins_as_hider int(6) NOT NULL DEFAULT 0," +
                "coins int(9) NOT NULL DEFAULT 0," +
                "perks text(0) NOT NULL," +
                "PRIMARY KEY (player_name), UNIQUE(player_name)" +
                ") ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci"
        );
        MultiUtils.getGamesConnector().query(
                "CREATE TABLE IF NOT EXISTS " + 
                "hidenseek_leavers" +
                " (" +
                "player_name varchar(16) NOT NULL," +
                "leave_time bigint(18) NOT NULL DEFAULT 0," +
                "PRIMARY KEY (player_name), UNIQUE(player_name)" +
                ") ENGINE=InnoDB CHARACTER SET utf8 COLLATE utf8_general_ci"
        );
        pluginMode = PluginMode.valueOf(config.getString("plugin-mode").toUpperCase());
        if(pluginMode == PluginMode.GAME) {
            gameType = GameType.valueOf(config.getString("game-type").toUpperCase());
            int shards = 1;
            if(config.isSet("shards"))
                shards = config.getInt("shards");
            for(int i = 1; i <= shards; ++i)
                games.add(new HnsShard(i));
            new GameListener();
            new EnchantmentAutoLapis();
        }else {
            LobbyEngine.init();
        }
        ScoreboardUtil.init();
        new GlobalListener();
        GamePerkManager.init();
        
        new ForceStart(this);
        new SetupMode(this);
        new GameTop("maintop", "hidenseek", "player_name", "players_caught", new String[]{"убийство", "убийства", "убийств"});
        CoinsTransfererNpc.setTransferer(new CoinsTransfererNpc.CoinsTransferer() {

            @Override
            public int getGameCoins(Player p) {
                return GamePlayer.get(p).getCoins();
            }

            @Override
            public void changeGameCoins(Player p, int amount) {
                GamePlayer.get(p).changeCoins(amount);
            }
            
        });
        new FastQueuer("sw", 5, Arrays.asList(new FastQueuer.SubQueue[] {
            new FastQueuer.SubQueue("sw", Material.DIAMOND_SWORD, "&bКлассика", Arrays.asList(new String[]{
                "&7Классический и всеми любимый",
                "&7SkyWars без всяких ненужных",
                "&7наворотов ;)"
            }), false, 2, 3),
            new FastQueuer.SubQueue("lsw", Material.SPONGE, "LuckyBlocks", Arrays.asList(new String[]{
                "&7Совсем другой взгляд на то,",
                "&7каким может быть SkyWars, - ",
                "&7это скай варс с лаки блоками!"
            }), false, 2, 7),
            new FastQueuer.SubQueue("sw_magic", Material.MONSTER_EGG, 96, "&bКарта Magic", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 21"
            }), true, 3, 2),
            new FastQueuer.SubQueue("sw_galaxy", Material.MONSTER_EGG, 94, "&bКарта Galaxy", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 16"
            }), true, 3, 3),
            new FastQueuer.SubQueue("sw_angelic", Material.MONSTER_EGG, 91, "&bКарта Angelic", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 16"
            }), true, 3, 4),
            new FastQueuer.SubQueue("sw_flowers", Material.MONSTER_EGG, 101, "&bКарта Flowers", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 16"
            }), true, 4, 2),
            new FastQueuer.SubQueue("sw_birdhouse", Material.MONSTER_EGG, 100, "&bКарта Birdhouse", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 16"
            }), true, 4, 3),
            new FastQueuer.SubQueue("sw_swords", Material.MONSTER_EGG, 51, "&bКарта Swords", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 12"
            }), true, 4, 4),
            new FastQueuer.SubQueue("sw_snakes", Material.MONSTER_EGG, 50, "&bКарта Snakes", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 12"
            }), true, 5, 2),
            new FastQueuer.SubQueue("sw_paris", Material.MONSTER_EGG, 93, "&bКарта Paris", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 12"
            }), true, 5, 3),
            new FastQueuer.SubQueue("sw_atlantis", Material.MONSTER_EGG, 54, "&bКарта Atlantis", Arrays.asList(new String[]{
                "&7Режим: Классический",
                "&7Игроков: 12"
            }), true, 5, 4),
            new FastQueuer.SubQueue("lsw_winter", Material.MONSTER_EGG, 56, "Карта Winter", Arrays.asList(new String[]{
                "&7Режим: LuckyBlocks",
                "&7Игроков: 16"
            }), true, 3, 6),
            new FastQueuer.SubQueue("lsw_western", Material.MONSTER_EGG, 98, "Карта Western", Arrays.asList(new String[]{
                "&7Режим: LuckyBlocks",
                "&7Игроков: 12"
            }), true, 3, 7),
            new FastQueuer.SubQueue("lsw_trees", Material.MONSTER_EGG, 68, "Карта Trees", Arrays.asList(new String[]{
                "&7Режим: LuckyBlocks",
                "&7Игроков: 12"
            }), true, 3, 8),
            new FastQueuer.SubQueue("lsw_sweets", Material.MONSTER_EGG, 57, "Карта Sweets", Arrays.asList(new String[]{
                "&7Режим: LuckyBlocks",
                "&7Игроков: 12"
            }), true, 4, 6),
            new FastQueuer.SubQueue("lsw_forest", Material.MONSTER_EGG, 55, "Карта Forest", Arrays.asList(new String[]{
                "&7Режим: LuckyBlocks",
                "&7Игроков: 12"
            }), true, 4, 7),
            new FastQueuer.SubQueue("lsw_art", Material.MONSTER_EGG, 61, "Карта Art", Arrays.asList(new String[]{
                "&7Режим: LuckyBlocks",
                "&7Игроков: 12"
            }), true, 4, 8),
        }));
    }
    
    @Override
    public void onDisable() {
        games.forEach(HnsShard::forcefullyEndTheGame);
    }
    
    public static FileConfiguration getConfig(String name) {
        return cg.getConfig(name);
    }
    
    public static void saveConfig(String name) {
        cg.saveConfig(name);
    }

}
