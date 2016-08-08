package ru.luvas.hns;

import com.comphenix.packetwrapper.WrapperPlayServerScoreboardObjective;
import com.comphenix.packetwrapper.WrapperPlayServerScoreboardScore;
import com.comphenix.protocol.wrappers.EnumWrappers;
import java.util.Collection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import ru.luvas.rmcs.player.RPlayer;
import ru.luvas.rmcs.servertypes.hub.HubScoreboard;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.hns.customs.GamePlayer;
import ru.luvas.hns.enums.PluginMode;
import ru.luvas.hns.game.HnsShard;
import ru.luvas.rmcs.MainScoreboard;
import ru.luvas.rmcs.shards.GameShard;

/**
 *
 * @author RinesThaix
 */
public class ScoreboardUtil {
    
    @Getter
    private final static Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    
    public static void init() {
        scoreboard.getObjectives().forEach(Objective::unregister);
        HubScoreboard.setObjectiveName("HNS");
        Objective obj = scoreboard.registerNewObjective("HNS", "dummy");
        if(MainClass.getPluginMode() == PluginMode.LOBBY)
            MainScoreboard.setupAnimation(obj, "Hide&Seek", MainScoreboard.AnimationGamma.AQUA);
        else {
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);
            obj.setDisplayName(UtilChat.c("&a&lHide&Seek"));
            preloadGlobalScoreboard();
        }
    }
    
    public static void preloadGlobalScoreboard() {
        setupScore("&r&r&r", 7);
        setupScore("&r&r", 5);
        setupScore("Режим: &a%s", 3, MainClass.getGameType().getName());
        setupScore("&r", 2);
        setupScore("&6Наш сайт: &bwww.dms.yt", 1);
    }
    
    public static void setupGameWaitingScoreboard(Player p) {
        HnsShard game = (HnsShard) GameShard.getGameShard(p);
        int needed = game.getPlayersMaximumAllowed();
        needed = needed * 3 >> 2;
        sendScore(p, "Необходимо игроков: &a%d", 6, needed);
        sendScore(p, "Карта: &a%s", 4, game.getMap().getName());
    }
    
    public static void setupGameScoreboard(HnsShard game) {
        int needed = game.getPlayersMaximumAllowed();
        needed = needed * 3 >> 2;
        final int fneeded = needed;
        game.getPlayers().forEach(p -> {
            takeScore(p, "Необходимо игроков: &a%d", 6, fneeded);
            sendScore(p, "&r&r&r&r", 9);
            sendScore(p, "Прячущихся осталось: &a%d", 8, game.getTeams().getHiders().getPlayers().size());
            updateKills(p);
        });
    }
    
    public static void setupGameScoreboard(Player p) {
        HnsShard game = (HnsShard) GameShard.getGameShard(p);
        if(game == null)
            return;
        int needed = game.getPlayersMaximumAllowed();
        needed = needed * 3 >> 2;
        takeScore(p, "Необходимо игроков: &a%d", 6, needed);
        sendScore(p, "&r&r&r&r", 9);
        sendScore(p, "Прячущихся осталось: &a%d", 8, game.getTeams().getHiders().getPlayers().size());
        updateKills(p);
    }
    
    public static void updateGameScoreboard(HnsShard game) {
        String score = UtilChat.c("Прячущихся осталось: &a%d", game.getTeams().getHiders().getPlayers().size() + 1);
        game.getPlayers().forEach(p -> {
            takeScore(p, score, 8);
            sendScore(p, "Прячущихся осталось: &a%d", 8, game.getTeams().getHiders().getPlayers().size());
        });
    }
    
    public static void setupLobbyScoreboard(Player p) {
        GamePlayer gp = GamePlayer.get(p);
        setupScore("&r&r&r", 9);
        sendScore(p, "Пойманных игроков: &a" + gp.getPlayersCaught(), 8, true);
        sendScore(p, "Успешных пряток: &a" + gp.getWinsAsHider(), 7, true);
        sendScore(p, "Серебро: &a" + gp.getCoins(), 6, true);
        sendScore(p, "Золото: &a" + RPlayer.get(p).getCoins().getCoins(), 5, true);
        setupScore("&r&r", 4);
        sendScore(p, "Сервер: &a" + ru.luvas.rmcs.MainClass.getServerName(), 3, true);
        setupScore("&r", 2);
        setupScore("&6Наш сайт: &bwww.dms.yt", 1);
    }
    
    public static void updateSilver(Player p, int previous, int new_v) {
        String prev = "Серебро: &a" + previous;
        int value = 6;
        sendScore(p, prev, value, false);
        sendScore(p, "Серебро: &a" + new_v, value, true);
    }
    
    public static void updateKills(Player p) {
        GamePlayer gp = GamePlayer.get(p);
        int caught = gp.getThisGameCaught();
        sendScore(p, "Вы поймали: &a" + (caught - 1), 6, false);
        sendScore(p, "Вы поймали: &a" + caught, 6, true);
    }
    
    private static void setupScore(String name, int value, Object... args) {
        scoreboard.getObjective(DisplaySlot.SIDEBAR).getScore(UtilChat.c(name, args)).setScore(value);
    }
    
    public static void sendScore(Player p, String name, int value) {
        sendScore(p, name, value, true);
    }
    
    public static void sendScore(Player p, String name, int value, Object... args) {
        sendScore(p, String.format(name, args), value, true);
    }
    
    public static void takeScore(Player p, String name, int value, Object... args) {
        sendScore(p, String.format(name, args), value, false);
    }

    public static void sendScore(Player p, String name, int value, boolean add) {
        WrapperPlayServerScoreboardScore wrapper = new WrapperPlayServerScoreboardScore();
        wrapper.setObjectiveName("HNS");
        wrapper.setScoreboardAction(add ? EnumWrappers.ScoreboardAction.CHANGE : EnumWrappers.ScoreboardAction.REMOVE);
        wrapper.setScoreName(UtilChat.c(name));
        wrapper.setValue(value);
        wrapper.sendPacket(p);
    }
    
    public static void setDisplayName(String name, Collection<Player> players) {
        WrapperPlayServerScoreboardObjective wrapper = new WrapperPlayServerScoreboardObjective();
        wrapper.setMode(2);
        wrapper.setName("HNS");
        wrapper.setDisplayName(name);
        players.forEach(wrapper::sendPacket);
    }
    
}
