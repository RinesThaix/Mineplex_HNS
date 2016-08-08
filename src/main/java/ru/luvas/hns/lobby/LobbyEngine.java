package ru.luvas.hns.lobby;

import ru.luvas.hns.customs.GamePlayer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.luvas.rmcs.utils.RListener;
import ru.luvas.rmcs.player.RPlayer;
import ru.luvas.rmcs.queues.QueueHub;
import ru.luvas.rmcs.utils.Cooldowns;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.rmcs.utils.inventory.InventoryManager;
import ru.luvas.rmcs.utils.inventory.RButton;
import ru.luvas.rmcs.utils.inventory.RInventory;
import ru.luvas.rmcs.utils.inventory.RItem;
import ru.luvas.rmcs.utils.inventory.SimpleItemStack;
import ru.luvas.rmcs.utils.items.ActionType;
import ru.luvas.rmcs.utils.items.UsableItem;
import ru.luvas.hns.ScoreboardUtil;

/**
 *
 * @author RinesThaix
 */
@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LobbyEngine extends RListener {

    private static RInventory main;
    private static ItemStack item;
    private final static RButton back = new RButton(Material.ARROW, "Назад", Arrays.asList(new String[]{
    
        "&7Нажми, чтобы вернуться в",
        "&7главное меню."
        
    })) {

        @Override
        public void onClick(Player p, int slot) {
            InventoryManager.openInventory(p, main);
        }
        
    };
    
    public static void init() {
        main = new RInventory("Магазин SkyWars", 3);
//        main.addItem(new RButton(Material.DOUBLE_PLANT, "Бонусы", Arrays.asList(new String[]{
//        
//            "&7Нажми, чтобы просмотреть",
//            "&7активные для тебя бонусы."
//            
//        })) {
//
//            @Override
//            public void onClick(Player p, int slot) {
//                InventoryManager.openInventory(p, getBonusesInventory(p));
//            }
//            
//        }, 5, 5);
        main.addItem(new RButton(Material.STAINED_GLASS_PANE, 14, "&cСкоро...", new ArrayList<String>()) {

            @Override
            public void onClick(Player p, int slot) {
                //Nothing is here
            }
            
        }, 2, 5);
        main.addItem(new RButton(Material.STAINED_GLASS_PANE, 14, "&cСкоро...", new ArrayList<String>()) {

            @Override
            public void onClick(Player p, int slot) {
                //Nothing is here
            }
            
        }, 2, 7);
        main.addItem(new RButton(Material.DIAMOND_SWORD, "Игровые классы", Arrays.asList(new String[]{
        
            "&7Нажми, чтобы просмотреть имеющиеся",
            "&7у тебя игровые классы или купить новые."
            
        })) {

            @Override
            public void onClick(Player p, int slot) {
                
            }
            
        }, 2, 3);
//        main.addItem(new RButton(Material.STAINED_GLASS, "Стиль клетки", Arrays.asList(new String[]{
//        
//            "&7Нажми, чтобы просмотреть",
//            "&7имещиеся у тебя стили клетки."
//            
//        })) {
//
//            @Override
//            public void onClick(Player p, int slot) {
//                InventoryManager.openInventory(p, getCagesInventory(p));
//            }
//            
//        }, 5, 6);
        
        item = new SimpleItemStack(Material.EMERALD, "&6Магазин Hide&Seek", Arrays.asList(new String[]{
        
            "&7Нажми правой кнопкой мыши,",
            "&7держа меня в руке, чтобы",
            "&7открыть внутреигровой магазин :)"
            
        }));
        new UsableItem(item, ActionType.RIGHT) {

            @Override
            public void onUse(Player p, ActionType actionType) {
//                InventoryManager.openInventory(p, main);
                UtilChat.ps("&a&lHide&Seek", p, "&cА это пока в разработке :(");
            }
            
        };
        new LobbyEngine();
        InventoryManager.addBlockedSlot(2);
        new LobbyListener();
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.getInventory().setItem(2, item);
        ScoreboardUtil.setupLobbyScoreboard(p);
    }
    
}
