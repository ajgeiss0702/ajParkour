	package us.ajg0702.parkour.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.regex.Matcher;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/*
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
*/

import us.ajg0702.parkour.Main;
import us.ajg0702.parkour.Messages;

public class Updater implements Listener {

	Main pl = null;
	static Updater instance = null;
	
	Messages msgs;
	
	boolean ready = false;
	boolean updateAvailable = false;
	boolean alreadyDownloaded = false;
	
	String latestVersion = "";
	String currentVersion = "";
	
	String lines;
	
	boolean enabled;
	
	private Updater(Main pl) {
		instance = this;
		enabled = pl.config.getBoolean("enable-updater");
		if(!enabled) return;
		this.pl = pl;
		msgs = Messages.getInstance();
		lines = msgs.color("&7&m                                                         &r");
		pl.getServer().getPluginManager().registerEvents(this, pl);
		
		currentVersion = pl.getDescription().getVersion().split("-")[0];
		
		check();
		Bukkit.getScheduler().scheduleSyncRepeatingTask(pl, this::check,5*60*20, (long)3600*20); // checks for an update every hour
	}
	
	public void check() {
		if(!enabled || alreadyDownloaded) return;
		Bukkit.getScheduler().runTaskAsynchronously(pl, () -> {
			try {
				//URL url = new URL("https://api.spiget.org/v2/resources/60909/versions?size=1&sort=-releaseDate");
				URL url = new URL("https://ajg0702.us/pl/ap/updates/getversion.php");
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.addRequestProperty("User-Agent", "ajParkour/"+currentVersion);// Set User-Agent

				// If you're not sure if the request will be successful,
				// you need to check the response code and use #getErrorStream if it returned an error code
				InputStream inputStream = connection.getInputStream();
				/*InputStreamReader reader = new InputStreamReader(inputStream); // old update checker for spiget

				// This could be either a JsonArray or JsonObject
				JsonElement element = new JsonParser().parse(reader);
				JsonArray o = element.getAsJsonArray();

				latestVersion = o.get(0).getAsJsonObject().get("name").getAsString();*/

				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				String line;

				StringBuilder string = new StringBuilder();
				while ((line = br.readLine()) != null) {
				string.append(line);
				}

				latestVersion = string.toString();

				if(latestVersion.isEmpty()) return;



				String[] parts = latestVersion.split("\\.");
				String[] curparts = currentVersion.split("\\.");

				//System.out.println("latest: "+latestVersion+" ("+parts.length+") cur: "+currentVersion+" ("+curparts.length+")");

				int i = 0;
				for(String part : parts) {
					if(i >= curparts.length) {
						break;
					}
					int newver = Integer.parseInt(part);
					int curver = Integer.parseInt(curparts[i]);
					if(newver > curver) {
						if(i != 0) {
							int newverlast = Integer.parseInt(parts[i-1]);
							int currentverlast = Integer.parseInt(curparts[i-1]);
							if(newverlast >= currentverlast) {
								updateAvailable = true;
								break;
							}
						} else {
							updateAvailable = true;
							break;
						}
					}
					i++;
				}

				/*int latestInt = Integer.valueOf(join(parts, ""));
				int currentInt = Integer.valueOf(join(curparts, ""));

				if(latestInt > currentInt) {
					updateAvailable = true;
				}*/

				if(updateAvailable && !ready) {
					Bukkit.getLogger().info(msgs.color("[ajParkour] An update is available! ("+latestVersion+") Do &7/ajParkour update&r to download it!"));
				} else if(!ready) {
					Bukkit.getLogger().info("[ajParkour] You are up to date! ("+latestVersion+")");
				}
				ready = true;
			} catch (IOException e) {
				pl.getLogger().warning("Unable to check for an update! "+e.getMessage());
			}
		});
	}
	
	public static Updater getInstance() {
		return instance;
	}
	public static Updater getInstance(Main plugin) {
		if(instance == null) {
			new Updater(plugin);
		}
		return instance;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(!enabled || alreadyDownloaded) return;
		if(ready && updateAvailable && pl.config.getBoolean("notify-update")) {
			if(!e.getPlayer().hasPermission("ajparkour.update")) return;
			Bukkit.getScheduler().runTaskLater(pl, () -> e.getPlayer().sendMessage(lines+msgs.color("\n\n  &aAn update is available for ajParkour!\n  &2You can download it using /ajParkour update\n\n"+lines)), 20); // wait a second to send the message to try to make it at the bottom of all the other plugin messages
		}
	}
	
	public void downloadUpdate(CommandSender p) {
		if(!enabled) {
			p.sendMessage(msgs.color("&cThe updater is disabled."));
			return;
		}
		if(!p.hasPermission("ajparkour.update")) {
			p.sendMessage(msgs.get("noperm"));
			return;
		}
		if(!updateAvailable || alreadyDownloaded) {
			p.sendMessage(msgs.color("&cThere is no update to download!"));
			return;
		}
		String curjarname = "ajParkour-"+currentVersion+".jar";
		String[] slashparts = pl.getDataFolder().toString().split(Matcher.quoteReplacement(File.separator));
		StringBuilder pluginspath = new StringBuilder();
		int i = 0;
		for(String part : slashparts) {
			pluginspath.append(part).append(File.separator);
			i++;
			if(i+1 >= slashparts.length) break;
		}
		File oldjar = new File(pluginspath+curjarname);
		if(!oldjar.exists()) {
			Bukkit.getLogger().warning("[ajParkour] Unable to find jar "+pluginspath+curjarname);
			p.sendMessage(msgs.color("&cUnable to find old jar! &7Please make sure it matches the format &fajParkour-VERSION.jar&7!"));
			return;
		}
		
		try {
			URL website = new URL("https://ajg0702.us/pl/ap/updates/downloads/ajParkour-"+latestVersion+".jar");
			//URL website = new URL("https://api.spiget.org/v2/resources/60909/versions/latest/download");
			HttpURLConnection con = (HttpURLConnection) website.openConnection();
			con.addRequestProperty("User-Agent", "ajParkour/"+currentVersion);
			con.setInstanceFollowRedirects(true);
			HttpURLConnection.setFollowRedirects(true);
			
			
			boolean redirect = false;
			int status = con.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
			}
			
			if (redirect) {

				// get redirect url from "location" header field
				String newUrl = con.getHeaderField("Location");

				// get the cookie if need, for login
				String cookies = con.getHeaderField("Set-Cookie");

				// open the new connnection again
				con = (HttpURLConnection) new URL(newUrl).openConnection();
				con.setRequestProperty("Cookie", cookies);
				con.addRequestProperty("User-Agent", "ajParkour/"+currentVersion);
										
				System.out.println("Redirect to URL : " + newUrl);

			}
			
			
			redirect = false;
			status = con.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
			}
			
			if (redirect) {

				// get redirect url from "location" header field
				String newUrl = con.getHeaderField("Location");
				
				// get the cookie if need, for login
				String cookies = con.getHeaderField("Set-Cookie");

				// open the new connnection again
				con = (HttpURLConnection) new URL(newUrl).openConnection();
				con.setRequestProperty("Cookie", cookies);
				con.setRequestProperty("Connection", "Connection: close");
				con.addRequestProperty("User-Agent", "ajParkour/"+currentVersion);
				//con.addRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
										
				System.out.println("Redirect to URL : " + newUrl);

			}
			
			ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
			FileOutputStream fos = new FileOutputStream(pluginspath+"ajParkour-"+latestVersion+".jar");
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			oldjar.delete();
			p.sendMessage(msgs.color("&aSuccess! &7Restart the server and the new version will be ready!"));
			updateAvailable = false;
			alreadyDownloaded = true;
		} catch(Exception e) {
			p.sendMessage(msgs.color("&cAn error occured while trying to download the newest version. Check console for more info"));
			e.printStackTrace();
		}
		
		
		
	}

}
