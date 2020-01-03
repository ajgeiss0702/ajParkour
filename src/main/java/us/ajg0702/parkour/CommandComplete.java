package us.ajg0702.parkour;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import us.ajg0702.parkour.game.PkArea;

public class CommandComplete implements TabCompleter {
	Main plugin;
	public CommandComplete(Main plugin) {
		this.plugin = plugin;
	}

	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		List<String> mainCommands = new ArrayList<>();
		mainCommands.add("start");
		mainCommands.add("list");
		mainCommands.add("version");
		mainCommands.add("top");
		if(sender.hasPermission("ajparkour.update")) mainCommands.add("update");
		if(sender.hasPermission("ajparkour.setup")) { mainCommands.add("setup"); mainCommands.add("edit");};
		if(sender.hasPermission("ajparkour.reload")) mainCommands.add("reload");
		if(sender.hasPermission("ajparkour.setup") || sender.hasPermission("ajparkour.portals")) mainCommands.add("portals");
		
		if(args.length == 1) {
			List<String> finalList = new ArrayList<>();
        	for(String command : mainCommands) {
            	if(command.startsWith(args[0].toLowerCase())) {
            		finalList.add(command);
            	}
            }
        	return finalList;
		} else if(args.length == 2) {
			switch(args[0].toLowerCase()) {
				case "edit":
					List<String> areas = new ArrayList<>();
					for(PkArea p : plugin.man.getAreas()) {
						areas.add(p.getName().toLowerCase());
					}
					List<String> finalList = new ArrayList<>();
		        	for(String command : areas) {
		            	if(command.startsWith(args[1].toLowerCase())) {
		            		finalList.add(command);
		            	}
		            }
		        	return finalList;
				case "setup":
					List<String> sc = new ArrayList<>();
					sc.add("create");
					sc.add("pos1");
					sc.add("pos2");
					sc.add("we");
					sc.add("difficulty");
					sc.add("info");
					sc.add("fallpos");
					sc.add("save");
					List<String> finalL = new ArrayList<>();
		        	for(String command : sc) {
		            	if(command.startsWith(args[1].toLowerCase())) {
		            		finalL.add(command);
		            	}
		            }
		        	return finalL;
			}
		}
		
		
		
		return null;
	}

}
