package ru.luvas.hns.customs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.luvas.multiutils.MultiUtils;
import ru.luvas.multiutils.structures.CacheMap;
import ru.luvas.rmcs.player.RPlayer;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.hns.MainClass;
import ru.luvas.hns.ScoreboardUtil;
import ru.luvas.hns.enums.PluginMode;
import ru.luvas.hns.game.GameTeam;
import ru.luvas.hns.game.HnsShard;
import ru.luvas.rmcs.shards.GameShard;

/**
 *
 * @author RinesThaix
 */
public class GamePlayer {
    
    private final static CacheMap<String, GamePlayer> players = new CacheMap<>(600l);
    
    @Getter
    private final static Map<HnsShard, Set<GamePlayer>> thisGamePlayers = new HashMap<>();
    
    public static GamePlayer get(String name) {
        GamePlayer gp = players.get(name.toLowerCase());
        if(gp != null)
            return gp;
        gp = new GamePlayer(name);
        players.put(name.toLowerCase(), gp);
        if(MainClass.getPluginMode() == PluginMode.GAME) {
            HnsShard game = (HnsShard) GameShard.getGameShard(name);
            if(game != null) {
                Set<GamePlayer> players = thisGamePlayers.get(game);
                if(players == null) {
                    players = new HashSet<>();
                    thisGamePlayers.put(game, players);
                }
                players.add(gp);
            }
        }
        return gp;
    }
    
    public static GamePlayer get(Player player) {
        return get(player.getName());
    }
    
    public static void invalidate(String name) {
        players.remove(name.toLowerCase());
    }
    
    public static Collection<GamePlayer> getAll() {
        return players.values();
    }
    
    @Getter
    private final String name;
    
    @Getter
    private int playersCaught = 0;
    
    @Getter
    private int winsAsHider = 0;
    
    @Getter
    private int coins = 0;
    
    @Getter
    private Map<GamePerk, Integer> perks = null;
    
    @Setter
    @Getter
    private GameTeam team;
    
    @Getter
    private int thisGameCaught = 0;
    
    public GamePlayer(String name) {
        this.name = name;
        try(ResultSet set = MultiUtils.getGamesConnector().query("SELECT * FROM hidenseek WHERE player_name='" + name + "'")) {
            if(set.next()) {
                playersCaught = set.getInt("players_caught");
                winsAsHider = set.getInt("wins_as_hider");
                coins = set.getInt("coins");
                String s = set.getString("perks");
                perks = new HashMap<>();
                if(s.length() > 1)
                    for(String gname : s.split(" ")) {
                        String[] spl = gname.split("\\$");
                        for(GamePerk gp : GamePerkManager.getAllPerks())
                            if(gp.getName().equals(spl[0])) {
                                perks.put(gp, Integer.parseInt(spl[1]));
                                break;
                            }
                    }
            }else {
                MultiUtils.getGamesConnector().addToQueue("INSERT INTO hidenseek "
                        + "(player_name, perks) "
                        + "VALUES ('%s', '')", name);
                perks = new HashMap<>();
            }
        }catch(SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    public void addPlayerCaught(int coins) {
        ++thisGameCaught;
        ++playersCaught;
        int dc = getCoinsAffectedByModifier(coins);
        this.coins += dc;
        Player p = getPlayer();
        if(p != null) {
            ScoreboardUtil.updateKills(p);
            UtilChat.s(p, "&6+%d серебра", dc);
        }
        MultiUtils.getGamesConnector().addToQueue("UPDATE hidenseek SET players_caught=players_caught + 1, coins=%d WHERE player_name='%s'", this.coins, name);
    }
    
    public void addWinAsHider(int coins) {
        ++winsAsHider;
        Player p = getPlayer();
        int dc = getCoinsAffectedByModifier(coins);
        this.coins += dc;
        if(p != null)
            UtilChat.s(p, "&6+%d серебра", dc);
        MultiUtils.getGamesConnector().addToQueue("UPDATE hidenseek SET wins_as_hider=wins_as_hider + 1, coins=%d WHERE player_name='%s'", this.coins, name);
    }
    
    public void changeCoins(int amount) {
        coins += amount;
        if(amount > 0 && getPlayer() != null)
            UtilChat.s(getPlayer(), "&6+%d серебра", amount);
        if(MainClass.getPluginMode() == PluginMode.LOBBY && getPlayer() != null)
            ScoreboardUtil.updateSilver(getPlayer(), coins - amount, coins);
        MultiUtils.getGamesConnector().addToQueue("UPDATE hidenseek SET coins=%d WHERE player_name='%s'", coins, name);
    }
    
    public void addPerk(GamePerk perk, int level) {
        perks.put(perk, level);
        updatePerks();
    }
    
    private void updatePerks() {
        StringBuilder sb = new StringBuilder();
        for(GamePerk gp : perks.keySet())
            sb.append(gp.getName()).append("$").append(perks.get(gp)).append(" ");
        MultiUtils.getGamesConnector().addToQueue("UPDATE hidenseek SET perks='%s' WHERE player_name='%s'", sb.toString().trim(), name);
    }
    
    private int getCoinsAffectedByModifier(int amount) {
        return (int) (amount * getCoinsModifier());
    }
    
    private double getCoinsModifier() {
        return RPlayer.get(name).getCoinsMultiplier();
    }
    
    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

}
