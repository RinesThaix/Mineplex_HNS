package ru.luvas.hns.customs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import ru.luvas.hns.MainClass;
import ru.luvas.hns.enums.GameType;

/**
 *
 * @author RinesThaix
 */
public class GamePerkManager {
    
    private final static Map<GameType, List<GamePerk>> perks = new HashMap<>();
    
    @Getter
    private final static Set<GamePerk> globalPerks = new HashSet<>();

    public static void init() {
        
    }
    
    private static void add(GamePerk gp) {
        GameType type = gp.getGameType();
        if(type == null) {
            globalPerks.add(gp);
            return;
        }
        List<GamePerk> list = perks.get(type);
        if(list == null) {
            list = new ArrayList<>();
            perks.put(type, list);
        }
        list.add(gp);
    }
    
    public static List<GamePerk> getPerks() {
        return getPerks(MainClass.getGameType());
    }
    
    public static List<GamePerk> getPerks(GameType gameType) {
        if((gameType.ordinal() & 1) == 1)
            gameType = GameType.values()[gameType.ordinal() - 1];
        return perks.get(gameType);
    }
    
    public static List<GamePerk> getAllPerks() {
        List<GamePerk> all = new ArrayList<>();
        perks.values().forEach(all::addAll);
        return all;
    }
    
}
