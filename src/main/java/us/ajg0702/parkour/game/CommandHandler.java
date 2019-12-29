package us.ajg0702.parkour.game;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import us.ajg0702.parkour.Main;
import us.ajg0702.parkour.Messages;

public class CommandHandler implements Listener {
	
	Main pl;
	
	File cmdFile;
	YamlConfiguration cmd;
	
	Messages msgs;
	
	List<String> cmds;

	public CommandHandler(Main pl) {
		this.pl = pl;
		msgs = pl.msgs;
		
		pl.getServer().getPluginManager().registerEvents(this, pl);
		
		cmdFile = new File(pl.getDataFolder(), "commands.yml");
		cmd = YamlConfiguration.loadConfiguration(cmdFile);
		
		cmd.options().header("This file lists commands that will be allowed in the parkour. Any commands other than these will be blocked.\nIf you just set it to [], this feature will be disabled.");
		if(!cmd.isSet("whitelist")) {
			cmd.set("whitelist", new ArrayList<String>());
			saveCmd();
		}
		
		reload();
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Reloads the comand whitelist.
	 */
	public void reload() {
		cmds = (List<String>) cmd.getList("whitelist");
	}
	
	private void saveCmd() {
		try {
			cmd.save(cmdFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("[ajParkour] Unable to save commands.yml:");
			e.printStackTrace();
		}
	}
	
	
	@EventHandler
	public void command(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		if(pl.man.inParkour(p)) {
			if(!cmd.contains(e.getMessage().split(" ")[0].replace("/", ""))) {
				e.setCancelled(true);
				p.sendMessage(msgs.get("commands.not-on-whitelist"));
			}
		}
	}

}
