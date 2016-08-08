package ru.luvas.hns.game;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import ru.luvas.rmcs.utils.RListener;
import ru.luvas.rmcs.utils.UtilBungee;
import ru.luvas.rmcs.utils.inventory.InventoryManager;
import ru.luvas.rmcs.utils.inventory.SimpleItemStack;
import ru.luvas.rmcs.utils.items.ActionType;
import ru.luvas.rmcs.utils.items.UsableItem;
import ru.luvas.hns.enums.GamePhase;
import ru.luvas.rmcs.MainClass;
import ru.luvas.rmcs.shards.GameShard;

/**
 *
 * @author RinesThaix
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class KitSelector extends RListener {
    
    private static boolean initialized = false;

    public static void init(HnsShard game) {
        InventoryManager.addBlockedSlot(game, 8);
        if(initialized)
            return;
        initialized = true;
        new KitSelector();
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        HnsShard game = (HnsShard) GameShard.getGameShard(p);
        if(game == null)
            return;
        if(game.getGamePhase() != GamePhase.WAITING)
            return;
        if(lobbyLeaveItem == null)
            preloadLobbyLeaveItem();
        p.getInventory().setItem(8, lobbyLeaveItem);
    }
    
    private static ItemStack lobbyLeaveItem = null;
    
    private static void preloadLobbyLeaveItem() {
        lobbyLeaveItem = new SimpleItemStack(Material.MAGMA_CREAM, "&4&lВыйти в лобби!");
        new UsableItem(lobbyLeaveItem, ActionType.RIGHT) {
            
            @Override
            public void onUse(Player p, ActionType actionType) {
                UtilBungee.sendPlayer(p, MainClass.getLobbyName());
            }
            
        };
    }
    
}
