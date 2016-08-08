package ru.luvas.hns.game;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.luvas.rmcs.utils.RListener;
import ru.luvas.rmcs.player.RPlayer;
import ru.luvas.rmcs.utils.Task;
import ru.luvas.rmcs.utils.UtilBlock;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.rmcs.utils.UtilPlayer;
import ru.luvas.hns.MainClass;
import ru.luvas.hns.ScoreboardUtil;
import ru.luvas.hns.commands.SetupMode;
import ru.luvas.hns.customs.GamePlayer;
import ru.luvas.hns.enums.GamePhase;
import ru.luvas.rmcs.shards.GameShard;
import ru.luvas.rmcs.utils.UtilAlgo;
import ru.luvas.rmcs.utils.UtilBungee;
import ru.luvas.rmcs.utils.UtilTitle;

/**
 *
 * @author RinesThaix
 */
@SuppressWarnings("unchecked")
public class GameListener extends RListener {
    
    @EventHandler
    public void AsyncPlayerChat(AsyncPlayerChatEvent e) {
        final Player p = e.getPlayer();
        if(e.getMessage().equalsIgnoreCase("gg") || e.getMessage().equalsIgnoreCase("гг")) {
            HnsShard game = (HnsShard) GameShard.getGameShard(p);
            if(game != null)
                Task.schedule(() -> game.gg(p));
        }
    }
    
    private static void kick(Player p, String cause) {
        UtilChat.ps("&a&lHide&Seek", p, cause);
        UtilBungee.sendPlayer(p, ru.luvas.rmcs.MainClass.getLobbyName());
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        HnsShard game = (HnsShard) GameShard.getGameShard(p);
        checkJoinPossibility: {
            if(game == null)
                break checkJoinPossibility;
            if(game.getPlayers().size() >= game.getPlayersMaximumAllowed() && !RPlayer.get(e.getPlayer()).isModerator()) {
                kick(p, "&cСервер полон!");
                break checkJoinPossibility;
            }
            if(game.getGamePhase() != GamePhase.WAITING && !RPlayer.get(e.getPlayer()).isModerator()) {
                kick(p, "&cИгра уже началась!");
                break checkJoinPossibility;
            }
            if(!LeaveManager.canLogin(e.getPlayer().getName()) && !RPlayer.get(e.getPlayer()).isModerator() && !RPlayer.get(e.getPlayer()).isYoutube()) {
                kick(p, "&cНедавно вы вышли из игры, будучи живы. В наказание, вы не можете играть в Hide&Seek в течение 3 минут.");
                break checkJoinPossibility;
            }
        }
        if(game != null)
            ScoreboardUtil.setupGameWaitingScoreboard(p);
        if(game != null && game.getGamePhase() != GamePhase.WAITING) {
            ScoreboardUtil.setupGameScoreboard(p);
            p.setGameMode(GameMode.SPECTATOR);
            return;
        }
        p.setGameMode(GameMode.SURVIVAL);
    }
    
    @EventHandler
    public void onKick(PlayerKickEvent e) {
        onQuit(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onQuit(PlayerQuitEvent e) {
        onQuit(e.getPlayer());
    }
    
    private void onQuit(Player p) {
        GamePlayer gp = GamePlayer.get(p);
        HnsShard game = (HnsShard) GameShard.getGameShard(p);
        if(game == null)
            return;
        GamePhase current = game.getGamePhase();
        if((current == GamePhase.PREGAME || current == GamePhase.INGAME) && gp.getTeam() != null)
            LeaveManager.addLeave(p.getName());
        if(gp.getTeam() != null) {
            GameTeams teams = game.getTeams();
            teams.getTeams().forEach(gt -> gt.quit(p));
            if(teams.getSeekers().getPlayers().isEmpty()) {
                GameTeam hiders = teams.getHiders();
                if(hiders.getPlayers().size() <= 1) {
                    game.getTimer().setTime(0);
                    game.endTheGame();
                }else {
                    List<Player> players = new ArrayList<>(hiders.getPlayers());
                    Player seeker = players.get(UtilAlgo.r(players.size()));
                    teams.getTeams().forEach(gt -> gt.quit(seeker));
                    teams.getSeekers().addAlly(seeker);
                    hiders.addEnemy(seeker);
                    if(current == GamePhase.PREGAME || game.getTimer().getTime() > Timer.SEEKER_FREE_TIME)
                        seeker.teleport(teams.getSeekers().getSpawn());
                    else
                        seeker.teleport(teams.getHiders().getSpawn());
                    UtilChat.ps("&a&lHide&Seek", seeker, "&cЕдинственный искатель вышел, и вы были избраны новым искателем!");
                    game.getPlayers().forEach(p2 -> {
                        UtilTitle.sendTitle(p2, "");
                        UtilTitle.sendSubtitle(p2, "&c&lИзбран новый искатель");
                    });
                }
            }else if(teams.getHiders().getPlayers().isEmpty())
                game.endTheGame();
        }
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(SetupMode.isSetupper(e.getPlayer()))
            return;
        e.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = false)
    public void onBlockPlace(BlockPlaceEvent e) {
        if(SetupMode.isSetupper(e.getPlayer()) || e.getPlayer().isOp()) {
            Material type = e.getBlock().getType();
            if(type == Material.BEACON) {
                FileConfiguration config = MainClass.getInstance().getConfig();
                List<String> spawns = config.getStringList("spawns");
                spawns.add(UtilBlock.locToStr(e.getBlock().getLocation()));
                config.set("spawns", spawns);
                MainClass.getInstance().saveConfig();
                UtilChat.ps("&a&lHide&Seek", e.getPlayer(), "&b%d&a-й спавн сохранен!", spawns.size());
                e.setCancelled(true);
            }
            return;
        }
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(!e.getPlayer().isOp() && (!e.hasItem() || (e.getItem().getType() != Material.IRON_SWORD && e.getItem().getType() != Material.MAGMA_CREAM))) {
            e.setCancelled(true);
            return;
        }
    }
    
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        e.setCancelled(true);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if(e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            Player damager = null;
            if(e.getDamager() instanceof Player)
                damager = (Player) e.getDamager();
            else if(e.getDamager() instanceof Projectile) {
                Projectile pj = (Projectile) e.getDamager();
                if(pj.getShooter() instanceof Player)
                    damager = (Player) pj.getShooter();
            }
            if(damager != null) {
                GamePlayer gp = GamePlayer.get(p);
                HnsShard game = (HnsShard) GameShard.getGameShard(p);
                if(gp.getTeam() == GamePlayer.get(damager).getTeam() || gp.getTeam() == game.getTeams().getSeekers()) {
                    e.setCancelled(true);
                    return;
                }
                e.setDamage(0d);
                p.getWorld().strikeLightningEffect(p.getLocation());
                GamePlayer.get(damager).addPlayerCaught(20);
                GameTeam hiders = game.getTeams().getHiders(), seekers = game.getTeams().getSeekers();
                game.getTeams().getTeams().forEach(gt -> gt.quit(p));
                hiders.addEnemy(p);
                seekers.addAlly(p);
                game.pb("&a&lHide&Seek", "%s &cнашел %s&c!", RPlayer.get(damager).getColoredName(), RPlayer.get(p).getColoredName());
                if(game.getTeams().getHiders().getPlayers().isEmpty()) {
                    game.endTheGame();
                    return;
                }
                p.teleport(game.getTeams().getHiders().getSpawn());
                UtilChat.ps("&a&lHide&Seek", p, "&cВы стали новым искателем!");
                game.getPlayers().forEach(p2 -> {
                    UtilTitle.sendTitle(p2, "");
                    UtilTitle.sendSubtitle(p2, "&c&lИскателей стало больше");
                });
                game.getTimer().setTime(game.getTimer().getTime() + Timer.ADDER);
                game.pb("&a&lHide&Seek", "&cОдин из прячущихся был пойман и превращен в очередного искателя!");
                game.pb("&a&lHide&Seek", "&cВремя поиска увеличено на %d секунд!", Timer.ADDER);
            }
        }
    }
    
    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(!(e.getEntity() instanceof Player))
            return;
        HnsShard game = (HnsShard) GameShard.getGameShard((Player) e.getEntity());
        if(game == null) {
            e.setCancelled(true);
            return;
        }
        if(e.getCause() != DamageCause.ENTITY_ATTACK || game.getGamePhase() != GamePhase.INGAME)
            e.setCancelled(true);
    }
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }
    
}
