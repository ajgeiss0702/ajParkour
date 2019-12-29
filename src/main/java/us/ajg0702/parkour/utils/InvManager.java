package us.ajg0702.parkour.utils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
/**
 * A class to save and restore inventories. The two methods are modified versions from Shadow48402 on spigot
 * @author Spigot user 25936 (Shadow48402): https://www.spigotmc.org/members/shadow48402.25936/
 *
 */
public class InvManager {

	public static void saveInventory(Player p) throws IOException {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("ajParkour");
        File f = new File(plugin.getDataFolder().getAbsolutePath(), "inventories/"+p.getUniqueId().toString() + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        c.set("inventory.armor", p.getInventory().getArmorContents());
        c.set("inventory.content", p.getInventory().getContents());
        c.save(f);
    }

    @SuppressWarnings("unchecked")
    public static void restoreInventory(Player p) throws IOException {
    	Plugin plugin = Bukkit.getPluginManager().getPlugin("ajParkour");
    	File f = new File(plugin.getDataFolder().getAbsolutePath(), "inventories/"+p.getUniqueId().toString() + ".yml");
        FileConfiguration c = YamlConfiguration.loadConfiguration(f);
        ItemStack[] content = ((List<ItemStack>) c.get("inventory.armor")).toArray(new ItemStack[0]);
        p.getInventory().setArmorContents(content);
        content = ((List<ItemStack>) c.get("inventory.content")).toArray(new ItemStack[0]);
        p.getInventory().setContents(content);
    }

}
