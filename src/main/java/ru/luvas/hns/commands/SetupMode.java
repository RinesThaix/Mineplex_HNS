package ru.luvas.hns.commands;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.luvas.multiutils.perms.PermissionGroup;
import ru.luvas.rmcs.commands.SpigotCommand;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.hns.MainClass;
import ru.luvas.hns.enums.PluginMode;

/**
 *
 * @author RinesThaix
 */
public class SetupMode extends SpigotCommand {
    
    private final static Set<String> setuppers = new HashSet<>();
    
    public static boolean isSetupper(Player p) {
        return setuppers.contains(p.getName());
    }

    public SetupMode(JavaPlugin plugin) {
        super(plugin, "setupmode", PermissionGroup.OWNER, "/setupmode");
        unavailableFromConsole();
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if(MainClass.getPluginMode() != PluginMode.GAME) {
            UtilChat.ps("&a&lHide&Seek", sender, "&cДействие возможно только на игровом сервере!");
            return;
        }
        String name = sender.getName();
        if(setuppers.contains(name)) {
            setuppers.remove(name);
            UtilChat.ps("&a&lHide&Seek", sender, "&aВы больше не настраиваете карту.");
        }else {
            setuppers.add(name);
            UtilChat.ps("&a&lHide&Seek", sender, "&aВы вошли в режим настройки карты.");
        }
    }

}
