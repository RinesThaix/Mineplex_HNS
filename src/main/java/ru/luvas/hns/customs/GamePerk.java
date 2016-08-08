package ru.luvas.hns.customs;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.rmcs.utils.inventory.SimpleItemStack;
import ru.luvas.hns.enums.GameType;

/**
 *
 * @author RinesThaix
 */
public class GamePerk {

    @Getter
    private final String name;
    
    @Getter
    private final String visibleName;
    
    private final String description;
    
    @Getter
    private final GameType gameType;
    
    @Getter
    private final int levels;
    
    private final Material icon;
    
    private final int[] costs;
    
    private final int[] modifiers;
    
    public GamePerk(String name, String visibleName, String description, GameType gameType, Material icon, int levels, int... data) {
        this.name = name;
        this.visibleName = UtilChat.c(visibleName);
        this.description = description;
        this.gameType = gameType;
        this.levels = levels;
        this.costs = new int[levels];
        this.modifiers = new int[levels];
        for(int i = 0; i < levels; ++i)
            costs[i] = data[i];
        for(int i = levels; i < data.length; ++i)
            modifiers[i - levels] = data[i];
        this.icon = icon;
    }
    
    public ItemStack getIcon(int level) {
        if(level == 0)
            level = 1;
        List<String> lore = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        String[] spl = getDescription(level).split(" ");
        for(int i = 0; i < spl.length; ++i) {
            if(!sb.toString().isEmpty())
                sb.append(" ");
            sb.append(spl[i]);
            if(sb.length() >= 25) {
                lore.add(UtilChat.c("&7%s", sb.toString()));
                sb = new StringBuilder();
            }
        }
        if(!sb.toString().isEmpty())
            lore.add(UtilChat.c("&7%s", sb.toString()));
        return new SimpleItemStack(icon, visibleName, lore);
    }
    
    public String getDescription(int level) {
        return levels == 1 ? description : String.format(description, modifiers[level - 1]);
    }
    
    public int getCost(int level) {
        return costs[level - 1];
    }
    
    public int getModifier(int level) {
        return modifiers[level - 1];
    }
    
}
