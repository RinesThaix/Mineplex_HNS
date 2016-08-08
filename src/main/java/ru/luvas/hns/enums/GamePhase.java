package ru.luvas.hns.enums;

import lombok.Getter;
import ru.luvas.rmcs.utils.UtilChat;

/**
 *
 * @author RinesThaix
 */
public enum GamePhase {
    WAITING("&eОжидание"),
    PREGAME("&eНачало игры"),
    INGAME("&aИгра идет"),
    ENDING("&cЗавершение"),
    RELOADING("&4Перезагрузка");
    
    @Getter
    private final String visualName;
    
    GamePhase(String visualName) {
        this.visualName = UtilChat.c(visualName);
    }
    
}
