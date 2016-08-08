package ru.luvas.hns.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import ru.luvas.multiutils.perms.PermissionGroup;
import ru.luvas.rmcs.commands.SpigotCommand;
import ru.luvas.rmcs.utils.UtilChat;
import ru.luvas.hns.MainClass;
import ru.luvas.hns.enums.GamePhase;
import ru.luvas.hns.enums.PluginMode;
import ru.luvas.hns.game.HnsShard;
import ru.luvas.rmcs.shards.GameShard;

/**
 *
 * @author RinesThaix
 */
public class ForceStart extends SpigotCommand {

    public ForceStart(JavaPlugin plugin) {
        super(plugin, "forcestart", PermissionGroup.ADMINISTRATOR, "/forcestart");
    }

    @Override
    public void handle(CommandSender sender, String[] args) {
        if(MainClass.getPluginMode() != PluginMode.GAME) {
            UtilChat.ps("&a&lHide&Seek", sender, "&cДействие возможно только на игровом сервере!");
            return;
        }
        HnsShard game = (HnsShard) GameShard.getGameShard(sender.getName());
        if(game == null) {
            UtilChat.ps("&a&lHide&Seek", sender, "&cВы не находитесь ни на одном из игровых шардов!");
            return;
        }
        UtilChat.ps("&a&lHide&Seek", sender, "&aЗапускаю игру на вашем шарде (&b%d&a)!", game.getId());
        game.switchPhase(GamePhase.PREGAME);
    }

}
