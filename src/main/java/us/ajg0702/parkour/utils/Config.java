package us.ajg0702.parkour.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
/**
 * A wrapper class for bukkit's built-in config system that supports comments.
 * Does not support lists because im lazy. But it does support csv lists
 * @author ajgeiss0702 (Aiden Geiss)
 *
 */
public class Config {
	File file;
	YamlConfiguration yml;
	Plugin plugin;
	String pluginName;
	String header = "";
	boolean isSet = false;
	List<Map<String, Object>> entries = new ArrayList<Map<String, Object>>();

	public Config(Plugin pl, String fileName) {
		plugin = pl;
		pluginName = pl.getDescription().getName();
		file = new File(pl.getDataFolder(), fileName);
		yml = YamlConfiguration.loadConfiguration(file);
	}
	
	/**
	 * Gets a comma-seperated list
	 * @param key The key of the comma-seperated list
	 * @return a {@link java.util.List List} of the values in the comma-seperated list
	 */
	public List<String> getCommaList(String key) {
		String raw = yml.getString(key);
		String[] rawlist = raw.split(",");
		List<String> output = new ArrayList<>();
		for(String s : rawlist) {
			if(!s.isEmpty()) {
				output.add(s);
			}
		}
		return output;
	}
	public Object get(String key) {
		return yml.get(key);
	}
	public String getString(String key) {
		return yml.getString(key);
	}
	public int getInt(String key) {
		return yml.getInt(key);
	}
	public boolean getBoolean(String key) {
		return yml.getBoolean(key);
	}
	
	
	public boolean inCommaList(String key, String value) {
		List<String> cl = getCommaList(key);
		for(String s : cl ) {
			if(value.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	
	public void reload() {
		yml = YamlConfiguration.loadConfiguration(file);
		setEntries();
	}
	
	public void addEntry(String name, Object value) {
		addEntry(name, value, "");
	}
	public void addEntry(String name, Object value, String comment) {
		if(isSet) {
			Bukkit.getLogger().warning("[ajUtils] Plugin '"+pluginName+"' attempted to add entry after entries have been set! (attempted to set '"+name+"')");
			return;
		}
		Map<String, Object> e = new HashMap<String, Object>();
		e.put("name", name);
		e.put("value", value);
		e.put("comment", comment);
		entries.add(e);
	}
	
	public void setHeader(String head) {
		header = head;
	}
	
	public void setEntries() {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				String add = "";
				String[] comments = header.split("\n");
				String commentsParsed = "";
				for(String comment : comments) {
					if(comment.trim().isEmpty()) continue;
					commentsParsed += "#"+comment+"\n";
				}
				add += commentsParsed;
				commentsParsed = null;
				for(Map<String, Object> e : entries) {
					String name = (String) e.get("name");
					if(!yml.isSet(name)) {
						String[] comments1 = ((String) e.get("comment")).split("\n");
						String commentsParsed1 = "";
						for(String comment : comments1) {
							if(comment.trim().isEmpty()) continue;
							commentsParsed1 += "#"+comment+"\n";
						}
						add += commentsParsed1;
						add += e.get("name")+": " + e.get("value")+"\n\n";
					}
				}

				add.replaceAll("#\\n", "");
				
				if(!add.equals("")) {
					try {
						File dir = plugin.getDataFolder();
						if(!dir.exists()) {
							dir.mkdirs();
						}
						file.createNewFile();
						Files.write(Paths.get(file.getPath()), add.getBytes(), StandardOpenOption.APPEND);
						yml = YamlConfiguration.loadConfiguration(file);
					} catch (IOException e1) {
						Bukkit.getLogger().severe("[ajUtils] Failed to save config file for plugin '"+pluginName+"'! More details:");
						e1.printStackTrace();
					}
				}
				isSet = true;
			}
		}, 0);
	}
}
