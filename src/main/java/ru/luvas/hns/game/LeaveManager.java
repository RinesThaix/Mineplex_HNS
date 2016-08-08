package ru.luvas.hns.game;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import ru.luvas.multiutils.MultiUtils;
import ru.luvas.rmcs.utils.TempHashMap;

/**
 *
 * @author RinesThaix
 */
public class LeaveManager {
    
    private final static ReadWriteLock lock = new ReentrantReadWriteLock();
    private final static TempHashMap<String, Long> leaves = new TempHashMap<>(60l);

    public static boolean canLogin(String player) {
        lock.readLock().lock();
        try {
            if(leaves.containsKey(player.toLowerCase()))
                return System.currentTimeMillis() - leaves.get(player.toLowerCase()) > 180000l;
        }finally {
            lock.readLock().unlock();
        }
        try(ResultSet set = MultiUtils.getGamesConnector().query("SELECT leave_time FROM hidenseek_leavers WHERE player_name='" + player + "'")) {
            if(set.next()) {
                long leave = set.getLong(1);
                lock.writeLock().lock();
                try {
                    leaves.put(player.toLowerCase(), leave);
                    return System.currentTimeMillis() - leave > 180000l;
                }finally {
                    lock.writeLock().unlock();
                }
            }else
                MultiUtils.getGamesConnector().addToQueue("INSERT INTO hidenseek_leavers (player_name) VALUES ('%s')", player);
        }catch(SQLException ex) {
            ex.printStackTrace();
        }
        return true;
    }
    
    public static void addLeave(String player) {
        MultiUtils.getGamesConnector().addToQueue("UPDATE hidenseek_leavers SET leave_time=%d WHERE player_name='%s'", System.currentTimeMillis(), player);
    }
    
}
