package ru.luvas.hns.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author RinesThaix
 */
@AllArgsConstructor
public enum GameType {
    
    CLASSIC("Hide&Seek", "hns");
    
    @Getter
    private final String name;
    
    @Getter
    private final String queue;
}
