package ru.luvas.hns.game;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Location;
import ru.luvas.multiutils.structures.RIterator;
import ru.luvas.rmcs.utils.UtilBlock;

/**
 *
 * @author RinesThaix
 */
@SuppressWarnings("unchecked")
public class GameMap {

    @Getter
    private final String name;
    
    @Getter
    private final List<Location> spawns;
    
    public GameMap(String name, List<String> spawns) {
        this.name = name;
        this.spawns = spawns.stream().map(UtilBlock::strToLoc).collect(Collectors.toList());
    }
    
    public RIterator<Location> getSpawnsIterator() {
        return new RIterator<>(spawns);
    }
    
}
