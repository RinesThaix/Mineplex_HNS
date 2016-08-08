package ru.luvas.hns;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.luvas.rmcs.utils.RListener;
import ru.luvas.rmcs.utils.UtilPlayer;
import ru.luvas.hns.customs.GamePlayer;

/**
 *
 * @author RinesThaix
 */
public class GlobalListener extends RListener {
    
    GlobalListener() {
        for(World w : Bukkit.getWorlds()) {
            w.setTime(0l);
            w.setGameRuleValue("doDaylightCycle", "false");
            w.setThundering(false);
            w.setWeatherDuration(Integer.MAX_VALUE);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        GamePlayer.get(p);
        UtilPlayer.resetPlayer(p);
        UtilPlayer.resetPlayerInventory(p);
        p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        GamePlayer.invalidate(p.getName());
    }
    
}
