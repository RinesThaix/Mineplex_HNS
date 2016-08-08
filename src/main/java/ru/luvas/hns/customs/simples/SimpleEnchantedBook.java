package ru.luvas.hns.customs.simples;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import ru.luvas.rmcs.utils.UtilChat;

/**
 *
 * @author RinesThaix
 */
public class SimpleEnchantedBook extends ItemStack {
    
    public SimpleEnchantedBook(Object... args) {
        super(Material.ENCHANTED_BOOK, 1);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) getItemMeta();
        for(int i = 0; i < args.length; ++i) {
            if(args[i] instanceof Enchantment) {
                int level = (Integer) args[i + 1];
                meta.addStoredEnchant((Enchantment) args[i++], level, true);
            }else if(args[i] instanceof Integer)
                setAmount((Integer) args[i]);
        }
        meta.setDisplayName(UtilChat.c("&fКнига зачарований"));
        setItemMeta(meta);
    }

}
