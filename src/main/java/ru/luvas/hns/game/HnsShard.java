package ru.luvas.hns.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.luvas.hns.MainClass;
import ru.luvas.hns.ScoreboardUtil;
import ru.luvas.hns.customs.GamePlayer;
import ru.luvas.hns.enums.GamePhase;
import ru.luvas.hns.enums.GameType;
import ru.luvas.multiutils.structures.RIterator;
import ru.luvas.rmcs.player.RPlayer;
import ru.luvas.rmcs.queues.GameShardQueue;
import ru.luvas.rmcs.shards.GameShard;
import ru.luvas.rmcs.utils.SimplePotionEffect;
import ru.luvas.rmcs.utils.Task;
import ru.luvas.rmcs.utils.UtilAlgo;
import ru.luvas.rmcs.utils.UtilBungee;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.rmcs.utils.UtilPlayer;
import ru.luvas.rmcs.utils.UtilTitle;
import ru.luvas.rmcs.utils.inventory.InventoryManager;

/**
 *
 * @author 0xC0deBabe <iam@kostya.sexy>
 */
public class HnsShard extends GameShard {
    
    private final GameShardQueue queue;
    
    @Getter
    private final Timer timer;
    
    @Getter
    private final GameTeams teams = new GameTeams(this);
    
    private final Set<Player> ggs = new HashSet<>();
    
    @Getter
    private GamePhase gamePhase = GamePhase.WAITING;
    
    @Getter
    private final int playersMaximumAllowed;
    
    @Getter
    private long gameStarted = 0l;
    
    @Getter
    private GameMap map;

    public HnsShard(int id) {
        super(id);
        FileConfiguration config = MainClass.getConfig("shard-" + id);
        if(!config.isSet("map-name")) {
            config.set("map-name", "Unknown");
            config.set("spawns", new ArrayList<>());
            config.set("max-players", 16);
            MainClass.saveConfig("shard-" + id);
        }
        playersMaximumAllowed = config.getInt("max-players");
        map = new GameMap(config.getString("map-name"), config.getStringList("spawns"));
        addWorlds(map.getSpawns().stream().map(Location::getWorld).collect(Collectors.toSet()));
        timer = new Timer(this);
        timer.init();
        KitSelector.init(this);
        queue = GameShardQueue.init(this, playersMaximumAllowed);
        queue.addQueue(getGameType().getQueue());
        queue.addQueue(getGameType().getQueue() + "_" + map.getName().toLowerCase().replace(" ", ""));
        getWorlds().stream().map(World::getEntities)
                .forEach(l -> l.stream().filter(e -> !(e instanceof Player)).forEach(Entity::remove));
    }

    public static GameType getGameType() {
        return MainClass.getGameType();
    }
    
    public void gg(Player p) {
        if(gamePhase == GamePhase.ENDING && !ggs.contains(p)) {
            ggs.add(p);
            GamePlayer.get(p).changeCoins(10);
        }
    }
    
    public void forcefullyEndTheGame() {
        pb("&a&lHide&Seek", "&4&lЭта игра продолжается слишком много времени, посему победитель не определен, и она будет завершена.");
        getTeams().getTeams().clear();
        endTheGame();
    }
    
    private boolean ended = false;
    
    public void endTheGame() {
        if(ended || getGamePhase()!= GamePhase.INGAME)
            return;
        ended = true;
        if(getTeams().getTeams().isEmpty()) {
            switchPhase(GamePhase.RELOADING);
            return;
        }
        GameTeam winner = timer.getTime() == 0 ? getTeams().getHiders() : getTeams().getSeekers();
        String wname = winner == getTeams().getHiders() ? "&a&lПрячущиеся" : "&c&lИскатели";
        b("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        b(getNiceString("&lHide&Seek"));
        b("");
        b(getNiceString("&e&lПобедили %s", wname));
        b("");
        b("&a&l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        if(winner == getTeams().getHiders()) {
            winner.getPlayers().forEach(p -> {
                GamePlayer.get(p).addWinAsHider(50);
                if(UtilAlgo.r(15) == 0)
                    RPlayer.get(p).addRandomChest();
            });
        }
        switchPhase(GamePhase.ENDING);
    }
    
    private static String getNiceString(String name, Object... args) {
        return getNiceString(String.format(name, args));
    }
    
    private static String getNiceString(String name) {
        name = UtilChat.c(name);
        String sname = UtilChat.s(name);
        int length = sname.length() >> 1;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 37 - length; ++i)
            sb.append(" ");
        sb.append(name);
        return sb.toString();
    }
    
    public void switchPhase(GamePhase new_phase) {
        gamePhase = new_phase;
        switch(new_phase) {
            case PREGAME: {
                RIterator<Location> spawns = map.getSpawnsIterator();
                if(spawns.size() != 2)
                    throw new IllegalArgumentException("Wrong configuration file! There should be 2 spawns (for hiders & for seekers).");
                getTeams().getHiders().setSpawn(spawns.next());
                getTeams().getSeekers().setSpawn(spawns.next());
                queue.update(0);
                sortPlayersIntoTeams();
                pb("&a&lHide&Seek", "&aИгра начнется через 10 секунд!");
                getPlayers().forEach(p -> UtilTitle.sendTitle(p, "&a&lHide&Seek"));
                getPlayers().forEach(p -> UtilTitle.sendSubtitle(p, "&eИгра скоро начнется!"));
                timer.setTime(10);
                break;
            }case INGAME: {
                PotionEffect jump = new SimplePotionEffect(PotionEffectType.JUMP, 6);
                PotionEffect speed1 = new SimplePotionEffect(PotionEffectType.SPEED, 2);
                PotionEffect speed2 = new SimplePotionEffect(PotionEffectType.SPEED, 4);
                for(Player p : getPlayers()) {
                    UtilPlayer.resetPlayer(p);
                    UtilPlayer.resetPlayerInventory(p);
                    GamePlayer gp = GamePlayer.get(p);
                    GameTeam team = gp.getTeam();
                    if(team == null)
                        continue;
                    p.addPotionEffect(jump);
                    p.addPotionEffect(team == getTeams().getHiders() ? speed1 : speed2);
                    Location spawn = team.getSpawn();
                    Location tpTo = spawn.clone();
                    tpTo.setY(tpTo.getBlockY() + 1);
                    tpTo.setX(tpTo.getX() + 0.5d);
                    tpTo.setZ(tpTo.getZ() + 0.5d);
                    p.teleport(tpTo);
                    UtilTitle.sendTitle(p, "&a&lИгра началась!");
                    UtilTitle.sendSubtitle(p, "&eВремя охоты начинается..");
                }
                InventoryManager.removeBlockedSlot(this, 0);
                InventoryManager.removeBlockedSlot(this, 8);
                gameStarted = System.currentTimeMillis();
                pb("&a&lHide&Seek", "&a&lИгра началась!");
                timer.setTime(Timer.GAME_TIME);
                break;
            }case ENDING: {
                timer.setTime(15);
                break;
            }case RELOADING: {
                getPlayers().forEach(p -> UtilBungee.sendPlayer(p, ru.luvas.rmcs.MainClass.getLobbyName()));
                timer.setTime(3);
                Task.schedule(() -> restore(), 40l);
                break;
            }
        }
    }
    
    private void sortPlayersIntoTeams() {
        List<Player> players = new ArrayList<>(getPlayers());
        Player seeker = players.get(UtilAlgo.r(players.size()));
        players.remove(seeker);
        getTeams().getHiders().addAllies(players);
        getTeams().getHiders().addEnemy(seeker);
        getTeams().getSeekers().addAlly(seeker);
        getTeams().getSeekers().addEnemies(players);
        UtilChat.ps("&a&lHide&Seek", seeker, "&c&lВы - Искатель!");
        UtilTitle.sendTitle(seeker, "&c&lВы - Искатель");
        players.forEach(p -> {
            UtilChat.ps("&a&lHide&Seek", p, "&a&lВы прячитесь :3");
            UtilTitle.sendTitle(p, "&a&lВы прячитесь :3");
        });
        setupTeams();
    }
    
    private void setupTeams() {
        int i = 0;
        for(GameTeam team : getTeams().getTeams()) {
            if(++i > 2) {
                team.getPlayers().forEach(p -> p.kickPlayer(UtilChat.c("&cНа вас не хватило места :(")));
                continue;
            }
        }
        ScoreboardUtil.setupGameScoreboard(this);
    }
    
    @Override
    public void restore() {
//        super.restore();  we don't need to restore all used worlds, cause players are unable to modify them
        ggs.clear();
        gamePhase = GamePhase.WAITING;
        gameStarted = 0l;
        ended = false;
        timer.setTime(60);
        timer.setSeekerLeftHisRoom(false);
        getTeams().getTeams().forEach(GameTeam::clear);
        GamePlayer.getThisGamePlayers().get(this).clear();
        FileConfiguration config = MainClass.getConfig("shard-" + getId());
        map = new GameMap(config.getString("map-name"), config.getStringList("spawns")); //to preload game world
        InventoryManager.addBlockedSlot(this, 0);
        InventoryManager.addBlockedSlot(this, 8);
        queue.reinit(playersMaximumAllowed);
        queue.addQueue(getGameType().getQueue());
        queue.addQueue(getGameType().getQueue() + "_" + map.getName().toLowerCase().replace(" ", ""));
    }

}
