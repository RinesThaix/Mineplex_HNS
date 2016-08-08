package ru.luvas.hns.game;

import java.util.HashSet;
import java.util.Set;
import lombok.Getter;

/**
 *
 * @author 0xC0deBabe <iam@kostya.sexy>
 */
public class GameTeams {
    
    @Getter
    private final HnsShard game;
    
    @Getter
    private final Set<GameTeam> teams = new HashSet<>();
    
    @Getter
    private final GameTeam hiders, seekers;
    
    public GameTeams(HnsShard game) {
        this.game = game;
        this.hiders = new GameTeam(this, game);
        this.seekers = new GameTeam(this, game);
    }

}
