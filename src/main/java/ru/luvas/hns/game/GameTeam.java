package ru.luvas.hns.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import ru.luvas.rmcs.scoreboards.SCTeam;
import ru.luvas.hns.ScoreboardUtil;
import ru.luvas.hns.customs.GamePlayer;

/**
 *
 * @author RinesThaix
 */
public class GameTeam {
    
    @Getter
    private final HnsShard game;

    @Getter
    private final Set<Player> players = new HashSet<>();
    
    private SCTeam friends, enemies;
    
    @Setter
    @Getter
    private Location spawn;
    
    GameTeam(GameTeams teams, HnsShard game) {
        this.game = game;
        teams.getTeams().add(this);
        clear();
    }
    
    public void clear() {
        friends = new SCTeam("friends", "&a", "", new ArrayList<>());
        enemies = new SCTeam("enemies", "&c", "", new ArrayList<>());
        players.clear();
    }
    
    public void quit(Player p) {
        if(!players.contains(p)) {
            enemies.removePlayer(players, p);
            return;
        }
        players.remove(p);
        friends.removePlayer(players, p);
        ScoreboardUtil.updateGameScoreboard(game);
    }
    
    public boolean isInTeam(Player p) {
        return players.contains(p);
    }
    
    public void addAlly(Player p) {
        players.add(p);
        friends.addPlayer(players, p);
        enemies.create(p);
        GamePlayer.get(p).setTeam(this);
    }
    
    public void addAllies(Collection<Player> players) {
        this.players.addAll(players);
        friends.addPlayer(this.players, players.toArray(new Player[players.size()]));
        enemies.create(players);
        players.stream().map(GamePlayer::get).forEach(gp -> gp.setTeam(this));
    }
    
    public void addEnemy(Player p) {
        enemies.addPlayerSilently(players, p);
    }
    
    public void addEnemies(Collection<Player> players) {
        enemies.addPlayerSilently(this.players, players.toArray(new Player[players.size()]));
    }
    
}
