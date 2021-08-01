package us.ajg0702.parkour;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class Messages {
	
	static Messages instance = null;
	
	File file = new File(Bukkit.getPluginManager().getPlugin("ajParkour").getDataFolder(), "messages.yml");
	YamlConfiguration msgs = YamlConfiguration.loadConfiguration(file);
	
	public String get(String key) {
		return get(key, null);
	}
	
	List<String> noprefix = Arrays.asList(
			"score",
			"placeholders.stats.no-data",
			"placeholders.current.no-data",
			"palceholders.stats.time-format",
			"fall.force.reasons.teleport",
			"fall.force.reasons.afk",
			"gui.selector.title",
			"gui.selector.items.random.title",
			"gui.selector.items.random.lore",
			"gui.selector.items.selected.lore",
			"gui.selector.items.nextpage.name",
			"gui.selector.items.prevpage.name"
			);
	
	public String get(String key, Player p) {
		String raw;
		if(msgs.isSet(key)) {
			raw = msgs.getString(key);
			if(!noprefix.contains(key) && !key.equals("prefix")) {
				raw = get("prefix")+raw;
			}
		} else {
			raw = "&4| &cCould not find the message '" + key + "'! &4| "; 
		}
		if(plugin.papi) {
			raw = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(p, raw);
		}
		raw = ChatColor.translateAlternateColorCodes('&', raw);
		return raw;
	}
	
	public String color(String text) {
		return ChatColor.translateAlternateColorCodes('&', text);
	}
	
	public void reload() {
		msgs = YamlConfiguration.loadConfiguration(file);
	}
	
	Main plugin;
	
	public static Messages getInstance() {
		return instance;
	}
	
	
	public Messages(Main pl) {
		this.plugin = pl;
		instance = this;
		Map<String, String> msgDefaults = new LinkedHashMap<>();
		msgDefaults.put("prefix", "");
		msgDefaults.put("alreadyin", "&cYou are already in parkour!");
		msgDefaults.put("start.score", "&aStarted parkour! &7Try to beat your high-score of &r{SCORE}&7!");
		msgDefaults.put("start.first", "&aStarted parkour!");
		msgDefaults.put("noperm", "&cYou do not have permission to do that!");
		msgDefaults.put("score", "&7Score &r&l{SCORE}");
		msgDefaults.put("beatrecord", "&aCongrats!&7 You beat your previous score of &f{SCORE}&7!");
		msgDefaults.put("beatrecord-ingame", "&aCongrats! &7You just beat your previous highscore! Keep going!");
		msgDefaults.put("disabledworld", "&cYou cannot start parkour from this world!");
		
		msgDefaults.put("block.potion", "&cNope!");
		msgDefaults.put("block.place", "&cNope!");
		msgDefaults.put("block.break", "&cNope!");
		msgDefaults.put("numberformatexception", "&cThat is not a valid number!");
		msgDefaults.put("areafull", "&cCould not start parkour because the area is full! &7Try again later.");
		
		msgDefaults.put("must-be-ingame", "&cYou must be in-game to do that!");
		msgDefaults.put("not-in-game", "&cYou must do this command from the console!");
		msgDefaults.put("couldnt-find-player", "&cCould not find player {PLAYER}");
		msgDefaults.put("list.header", "&9Players playing parkour:");
		msgDefaults.put("list.format", "&6{NAME} &7- &e{SCORE} jumps");
		msgDefaults.put("list.none", "&7None");
		msgDefaults.put("nobodys-played-yet", "&cNobody has played parkour yet!");
		msgDefaults.put("first-position", "&eFirst position set!");
		msgDefaults.put("second-position", "&eSecond position set!");
		msgDefaults.put("setup-done", "&aYou can now do &2/ajParjour start&a to do parkour!");
		msgDefaults.put("reload.usage", "&cUsage: /{CMD} reload <file>&7 Files: {POSS}");
		msgDefaults.put("fallpos.set", "&aFall position set!");
		msgDefaults.put("fallpos.removed", "&aFall position removed!");
		msgDefaults.put("top.header", "&9Top Scores:");
		msgDefaults.put("top.header-area", "&9Top Scores for {AREA}:");
		msgDefaults.put("top.format", "&a{#}. &6{NAME} &e{SCORE} jumps");
		
		msgDefaults.put("fall.normal", "&cYou fell! &7Your score was &r{SCORE}");
		msgDefaults.put("fall.force.base", "&cYour parkour game was ended because ");
		msgDefaults.put("fall.force.reasons.teleport", "you teleported");
		msgDefaults.put("fall.force.reasons.afk", "you went AFK.");
		
		msgDefaults.put("errors.blocknotair.base", "&c&lAn error occured:&r&c The block is not air!");
		msgDefaults.put("errors.blocknotair.player", "&6Tell an admin, and try again later.");
		msgDefaults.put("errors.blocknotair.admin", "&6Make sure the parkour area is completly air, and that the plugin has been correctly set up by reading /{CMD} setup.");
		
		msgDefaults.put("errors.notsetup.base", "&cajParkour has not been set up yet.");
		msgDefaults.put("errors.notsetup.player", "&6Ask an admin to set it up!");
		msgDefaults.put("errors.notsetup.admin", "&6For info on how to set it up, do /{CMD} setup");
		
		msgDefaults.put("errors.too-many-players", "&cThere are too many players in parkour right now. Try again later.");
		
		msgDefaults.put("migrate.more-args", "&cPlease provide the source to migrate from!");
		msgDefaults.put("migrate.success", "&aMigrated {COUNT} scores!");
		msgDefaults.put("migrate.error", "&cSomething went wrong. There is probably more information in the console.");
		
		msgDefaults.put("placeholders.stats.no-data", "---");
		msgDefaults.put("placeholders.current.no-data", "0");
		msgDefaults.put("placeholders.stats.time-format", "{m}m {s}s");

		msgDefaults.put("commands.help.header", "&aajParkour made by &2ajgeiss0702&a!");
		msgDefaults.put("commands.help.start", "&6 /{CMD} start &7- &9Start parkour!");
		msgDefaults.put("commands.help.startarea", "&6 /{CMD} start <area> &7- &9Start parkour in a certain area.");
		msgDefaults.put("commands.help.startothers", "&6 /{CMD} start <player> [area] &7- &9Start another player's parkour in a certain area.");
		msgDefaults.put("commands.help.top", "&6 /{CMD} top &7- &9See the top 10 players!");
		msgDefaults.put("commands.help.list", "&6 /{CMD} list &7- &9See everyone that is playing parkour, and their current scores.");
		msgDefaults.put("commands.help.setup", "&6 /{CMD} setup &7- &9Commands for setting up the plugin. &7(required permssion: ajparkour.setup)");
		msgDefaults.put("commands.help.portals", "&6 /{CMD} portals &7- &9Commands for setting up portals.");
		msgDefaults.put("commands.help.areas", "&6 /{CMD} areas &7- &9List all loaded areas");
		
		msgDefaults.put("commands.setup.header", "&aajParkour&2 setup commands");
		msgDefaults.put("commands.setup.create", "&6 /{CMD} setup create &7- &9Create an area");
		msgDefaults.put("commands.setup.pos1", "&6 /{CMD} setup pos1 &7- &9Set the first position");
		msgDefaults.put("commands.setup.pos2", "&6 /{CMD} setup pos2 &7- &9Set the second position");
		msgDefaults.put("commands.setup.we", "&6 /{CMD} setup we &7- &9Select first and second position with worldedit");
		msgDefaults.put("commands.setup.fallpos", "&6 /{CMD} setup fallpos &7- &9Set fall position!");
		msgDefaults.put("commands.setup.edit", "&6 /{CMD} edit &7- &9Load an existing area into the editing queue.");
		msgDefaults.put("commands.setup.info", "&6 /{CMD} setup info &7- &9Show which points you have set so far!");
		msgDefaults.put("commands.setup.diff", "&6 /{CMD} setup difficulty &7- &9Set the area's difficulty!");
		msgDefaults.put("commands.setup.max", "&6 /{CMD} setup max &7- &9Set the maximum number of people allowed in the area!");
		msgDefaults.put("commands.setup.save", "&6 /{CMD} setup save &7- &9Saves and enabled the area. (This should be done last)");
		
		msgDefaults.put("commands.portals.header", "&aajParkour&2 portal commands");
		msgDefaults.put("commands.portals.list", "&6 /{CMD} portals list &7- &9Lists all portals.");
		msgDefaults.put("commands.portals.create", "&6 /{CMD} portals create &7- &9Create a portal.");
		msgDefaults.put("commands.portals.remove", "&6 /{CMD} portals remove &7- &9Remove a portal.");
		
		msgDefaults.put("commands.listareas.header", "&9Loaded parkour areas:");
		msgDefaults.put("commands.listareas.format", "&7 - &a{NAME} &7Difficulty: &9{DIFFICULTY}");
		msgDefaults.put("commands.listareas.none", "&7No areas loaded!");
		
		msgDefaults.put("not-from-console", "&cYou cannot run this command from the console!");
		
		msgDefaults.put("setup.no-name", "&cYou must name your area!");
		msgDefaults.put("setup.invalid-name", "&cYou can't name your arena that.");
		msgDefaults.put("setup.edit.no-name", "&cPlease enter the name of an area to edit!");
		msgDefaults.put("setup.edit.loaded", "&aArea loaded for editing! Re-set positions using /{CMD} setup");
		msgDefaults.put("setup.edit.no-area", "&cAn area with that name does not exist!");
		msgDefaults.put("setup.need-to-create", "&aYou must create an area before editing it!");
		msgDefaults.put("setup.we.not-installed", "&cYou need to have WorldEdit installed to use this command!&7 Try using the built-in pos1 and pos2 setup commands.");
		msgDefaults.put("setup.we.incomplete-selection", "&cYou must make a complete worldedit selection before doing this!");
		msgDefaults.put("setup.created", "&aArea created and added to the editing queue. See &2/ajParkour setup&a or view the plugin page for steps to set it up!");
		msgDefaults.put("setup.already-creating", "&cAn arena is currently being set up. &aPlease either save that arena, or run this command again to override. (&c&lNOTE:&r&c Overriding will erase all of the data of the arena being set up before!)");
		msgDefaults.put("setup.info.header", "&6Colors: &aSet &7Not set (optional) &cNot set (required)");
		msgDefaults.put("setup.set.pos1", "&aFirst position set!");
		msgDefaults.put("setup.set.pos2", "&aSecond position set!");
		msgDefaults.put("setup.set.fallpos", "&aFall position set!");
		msgDefaults.put("setup.set.we", "&aPositions set!");
		msgDefaults.put("setup.set.diff", "&aDifficulty set!");
		msgDefaults.put("setup.set.max", "&aMax players set!");
		msgDefaults.put("setup.max.need-args", "&cPlease enter a number!");
		msgDefaults.put("setup.diff.not-valid", "&cInvalid difficulty! &7Valid difficulties are &feasy&7, &fmedium&7, &fhard&7, &fexpert&7, and &fbalanced");
		msgDefaults.put("setup.diff.need-args", "&cPlease enter a difficulty! &7Valid difficulties are &feasy&7, &fmedium&7, &fhard&7, &fexpert&7, and &fbalanced&7.\n&aBalanced difficulty will make the parkour get harder as the player gets higher scores.");
		msgDefaults.put("setup.save.not-done", "&cYou have not set all of the required settings! &7Check &f/{CMD} setup info&7 to see what you have left.");
		msgDefaults.put("setup.save.success", "&aArena {NAME} saved and loaded!");
		msgDefaults.put("setup.area.too-small", "&cThe area you just created does not meet the minimum reccomended size of 20x20x20!\n&7You can continue creating the area, but know you may run into issues with players running into each other, and possibly parkour going outside the area!\n&7Your area: {l}x{w}x{h} (XxZxY)");
		
		
		msgDefaults.put("portals.list.header", "&2Parkour Portals");
		msgDefaults.put("portals.list.format.area", "&2{NAME} &7- &a{COORDS} &7- &a{AREA}");
		msgDefaults.put("portals.list.format.no-area", "&2{NAME} &7- &a{COORDS}");
		msgDefaults.put("portals.create.help", "&cUsage: /{CMD} portals create <name> [area]");
		msgDefaults.put("portals.create.success", "&aCreated a portal named '{NAME}' at your current location! &7You might have to restart the server for the portal to start working.");
		msgDefaults.put("portals.remove.help", "&cPlease enter the name of the portal to be removed!");
		msgDefaults.put("portals.remove.cannot-find", "&cCannot find a portal with that name! Make sure you spelled it correctly, and that you put the same caps too.");
		msgDefaults.put("portals.remove.success", "&aRemoved portal '{NAME}'!");
		msgDefaults.put("portals.remove.no-portals", "&cNo portals have been set up yet! Please create portals before removing them.");
		
		msgDefaults.put("gui.selector.title", "Block Selector");
		msgDefaults.put("gui.selector.items.random.title", "&rRandom Blocks");
		msgDefaults.put("gui.selector.items.random.lore", "&7Will pick a random block.");
		msgDefaults.put("gui.selector.items.selected.lore", "&aCurrently Selected");
		msgDefaults.put("gui.selector.items.nextpage.name", "&a&lNext Page");
		msgDefaults.put("gui.selector.items.prevpage.name", "&a&lPrevious Page");
		
		msgDefaults.put("items.blockselector.name", "&aBlock Selector");
		
		msgDefaults.put("start.no-area-player", "&cNo arena or player could be found called '{INPUT}'!");
		msgDefaults.put("start.unknown-player", "&cCannot find that player!");
		msgDefaults.put("start.unknown-area", "&cCannot find that area!");
		msgDefaults.put("start.started-other", "&aStarted parkour for {PLY}!");
		msgDefaults.put("start.started-other-area", "&aStarted parkour for &l&f{PLY}&r&a on area &l&f{AREA}&r&a!");
		
		msgDefaults.put("commands.not-on-whitelist", "&cYou cannot use this command while on parkour!");
		
		msgDefaults.put("reset.usage", "&cUsage: /{CMD} reset <player>");
		msgDefaults.put("reset.success", "&aReset score of {PLAYER} to 0");
		
		msgDefaults.put("blockselector.openedfor", "&aOpened block selector for {PLAYER}");
		
		
		msgDefaults.put("areas.remove.help", "&cPlease enter the name of the area to be removed!");
		msgDefaults.put("areas.remove.cannot-find", "&cCannot find an area with that name! Make sure you spelled it correctly, and that you put the same caps too.");
		msgDefaults.put("areas.remove.success", "&aRemoved area '{NAME}'!");
		
		for(String key : msgDefaults.keySet()) {
			if(!msgs.isSet(key)) {
				msgs.set(key, msgDefaults.get(key));
			}
		}
		
		Map<String, String> mv = new HashMap<>();
		mv.put("top-header", "top.header");
		mv.put("list-header", "list.header");
		mv.put("block-potion", "block.potion");
		mv.put("startfirst", "start.first");
		mv.put("fall.force.afk", "fall.force.reasons.afk");
		
		for(String key : mv.keySet()) {
			if(msgs.isSet(key)) {
				msgs.set(mv.get(key), msgs.getString(key));
				if(key.contains(mv.get(key))) {
					msgs.set(key, null);
				}
			}
		}
		
		msgs.options().header("\n\nThis is the messsages file.\nYou can change any messages that are in this file\n\nIf you want to reset a message back to the default,\ndelete the entire line the message is on and restart the server.\n\t\n\t");
		try {
			msgs.save(file);
		} catch (IOException e) {
			Bukkit.getLogger().warning("[ajParkour] Could not save messages file!");
		}
	}
}
