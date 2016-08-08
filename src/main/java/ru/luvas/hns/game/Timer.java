package ru.luvas.hns.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import ru.luvas.rmcs.utils.Task;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.hns.ScoreboardUtil;
import ru.luvas.hns.enums.GamePhase;
import ru.luvas.rmcs.utils.UtilTitle;

/**
 *
 * @author RinesThaix
 */
@RequiredArgsConstructor
public class Timer {
    
    public final static int
            GAME_TIME = 300,
            ADDER = 30,
            SEEKER_FREE_TIME = 255;
    
    private final HnsShard game;
    
    @Setter
    @Getter
    private int time = 60;
    
    @Setter
    private boolean seekerLeftHisRoom = false;

    protected void init() {
        Task.schedule(() -> {
            GamePhase current = game.getGamePhase();
            switch(current) {
                case WAITING: {
                    if(--time == 0) {
                        if(game.getPlayers().size() >= game.getPlayersMaximumAllowed() * 3 >> 2)
                            game.switchPhase(GamePhase.PREGAME);
                        else {
                            time = 60;
                           game.pb("&a&lHide&Seek", "&cНедостаточно игроков для начала игры!");
                        }
                    }else if(game.getPlayers().size() >= game.getPlayersMaximumAllowed())
                        game.switchPhase(GamePhase.PREGAME);
                    else if(time % 10 == 0 || time <= 5)
                        game.pb("&a&lHide&Seek", "&eОжидание завершится через &a%d &eсекунд.", time);
                    break;
                }case PREGAME: {
                    if(--time == 0)
                        game.switchPhase(GamePhase.INGAME);
                    break;
                }case INGAME: {
                    --time;
                    if(time == SEEKER_FREE_TIME && !seekerLeftHisRoom) {
                        seekerLeftHisRoom = true;
                        game.pb("&a&lHide&Seek", "&c&lИскатель вышел на охоту!");
                        game.getPlayers().forEach(p -> {
                            UtilTitle.sendTitle(p, "");
                            UtilTitle.sendSubtitle(p, "&c&lИскатель вышел на охоту");
                        });
                        Location spawn = game.getTeams().getHiders().getSpawn();
                        game.getTeams().getSeekers().getPlayers().forEach(p -> p.teleport(spawn));
                    }
                    if(time % 30 == 0 || time < 15) {
                        if(time == 0) {
                            game.endTheGame();
                            return;
                        }
                        game.pb("&a&lHide&Seek", "&e&lИгра завершится через %d %s.", time, getSeconds(time));
                    }
                    break;
                }case ENDING: {
                    if(--time == 0)
                        game.switchPhase(GamePhase.RELOADING);
                    break;
                }
            }
            if(time < 0)            
                time = 1;
            int minutes = time / 60, seconds = time % 60;
            int hours = minutes / 60;
            minutes %= 60;
            String hh = format(hours), mm = format(minutes), ss = format(seconds);
            ScoreboardUtil.setDisplayName(UtilChat.c("&7%s:%s:%s &8| %s", hh, mm, ss, current.getVisualName()), game.getPlayers());
        }, 0l, 20l);
    }
    
    private static String format(int time) {
        return time < 10 ? "0" + time : time + "";
    }
    
    private static String getSeconds(int time) {
        int o1 = time % 10, o2 = time % 100;
        if(o1 == 1 && o2 != 11)
            return "секунду";
        if(o1 >= 2 && o1 <= 4 && (o2 < 10 || o2 > 20))
            return "секунды";
        return "секунд";
    }
    
}
