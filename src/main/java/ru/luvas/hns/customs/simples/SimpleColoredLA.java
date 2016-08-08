package ru.luvas.hns.customs.simples;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import ru.luvas.rmcs.utils.UtilChat;

/**
 *
 * @author RinesThaix
 */
public class SimpleColoredLA extends ItemStack {

    public SimpleColoredLA(Material type, Color color, String name) {
        super(type, 1);
        LeatherArmorMeta lam = (LeatherArmorMeta) getItemMeta();
        lam.setColor(color);
        lam.setDisplayName(UtilChat.c("&f%s", name));
        setItemMeta(lam);
    }
    
}
