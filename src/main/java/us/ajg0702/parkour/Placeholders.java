package us.ajg0702.parkour;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import us.ajg0702.parkour.game.PkArea;
import us.ajg0702.parkour.game.PkPlayer;
import us.ajg0702.parkour.top.TopEntry;
import us.ajg0702.parkour.top.TopManager;

/**
 * This class will be registered through the register-method in the 
 * plugins onEnable-method.
 */
public class Placeholders extends PlaceholderExpansion {

    private final Main plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public Placeholders(Main plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convenience do we return the author from the plugin.yml
     * 
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest 
     * method to obtain a value if a placeholder starts with our 
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "ajpk";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convenience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }



    /**
     * This is the method called when a placeholder with our identifier 
     * is found and needs a value.
     * <br>We specify the value identifier in this method.
     * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
     *
     * @param  player
     *         A Player.
     * @param  identifier
     *         A String containing the identifier/value.
     *
     * @return possibly-null String of the requested identifier.
     */
    @Override
    public String onPlaceholderRequest(Player player, String identifier){
    	identifier = identifier.replaceAll("_nocache", "");

		if(identifier.matches("stats_top_name_[1-9][0-9]*$")) {
			int number = Integer.parseInt(identifier.split("stats_top_name_")[1]);
			TopEntry pos = TopManager.getInstance().getTop(number, null);

			if(pos.getName().equalsIgnoreCase("--")) {
				return plugin.msgs.get("placeholders.stats.no-data", player);
			}

			return pos.getName();
		}
		if(identifier.matches("stats_top_name_[1-9][0-9]*_.+$")) {
			int number = Integer.parseInt(identifier.split("_")[3]);
			String area = identifier.split("_")[4];
			TopEntry pos = TopManager.getInstance().getTop(number, area);

			if(pos.getName().equalsIgnoreCase("--")) {
				return plugin.msgs.get("placeholders.stats.no-data", player);
			}

			return pos.getName();
		}




		if(identifier.matches("stats_top_score_[1-9][0-9]*$")) {
			int number = Integer.parseInt(identifier.split("stats_top_score_")[1]);
			TopEntry pos = TopManager.getInstance().getTop(number, null);

			if(pos.getName().equalsIgnoreCase("--")) {
				return plugin.msgs.get("placeholders.stats.no-data", player);
			}

			return pos.getScore()+"";
		}
		if(identifier.matches("stats_top_score_[1-9][0-9]*_.+$")) {
			int number = Integer.parseInt(identifier.split("_")[3]);
			String area = identifier.split("_")[4];
			TopEntry pos = TopManager.getInstance().getTop(number, area);

			if(pos.getName().equalsIgnoreCase("--")) {
				return plugin.msgs.get("placeholders.stats.no-data", player);
			}

			return pos.getScore()+"";
		}



		if(identifier.matches("stats_gamesplayed")) {
			return plugin.scores.getGamesPlayed(player.getUniqueId())+"";
		}




		if(identifier.matches("stats_top_time_[1-9][0-9]*$")) {
			int number = Integer.parseInt(identifier.split("stats_top_time_")[1]);
			TopEntry pos = TopManager.getInstance().getTop(number, null);

			int time = pos.getTime();

			if(pos.getName().equalsIgnoreCase("--") || pos.getTime() < 0) {
				return plugin.msgs.get("placeholders.stats.no-data", player);
			}

			int min = time / 60;
			int sec = time % 60;

			return plugin.msgs.get("placeholders.stats.time-format", player)
					.replaceAll("\\{m}", min+"")
					.replaceAll("\\{s}", sec+"");
		}
        if(identifier.matches("stats_top_time_[1-9][0-9]*_.+$")) {
        	int number = Integer.parseInt(identifier.split("_")[3]);
        	String area = identifier.split("_")[4];
			TopEntry pos = TopManager.getInstance().getTop(number, area);

			int time = pos.getTime();

			if(pos.getName().equalsIgnoreCase("--") || pos.getTime() < 0) {
				return plugin.msgs.get("placeholders.stats.no-data", player);
			}

			int min = time / 60;
			int sec = time % 60;

            return plugin.msgs.get("placeholders.stats.time-format", player)
            		.replaceAll("\\{m}", min+"")
            		.replaceAll("\\{s}", sec+"");
        }



		if(identifier.equals("current")) {
			if(player == null) return "0";
			PkPlayer p = plugin.man.getPlayer(player);
			if(p != null) {
				return p.getScore()+"";
			} else {
				return plugin.msgs.get("placeholders.current.no-data");
			}
		}

		if(identifier.equals("jumping")) {
			return plugin.man.getTotalPlayers()+"";
		}
		if(identifier.equals("jumping_.+")) {
			String areaRaw = identifier.split("_")[1];
			PkArea area = plugin.man.getArea(areaRaw);
			if(area == null) {
				return "!";
			}
			return plugin.man.getPlayerCounts(area)+"";
		}


		if(identifier.equals("stats_highscore")) {
			if(player == null) {
				return "0";
			}
			return TopManager.getInstance().getHighScore(player, null)+"";
		}

		if(identifier.matches("stats_highscore_.+$")) {
			if(player == null) {
				return "0";
			}

			String area = identifier.split("_")[2];
			return plugin.scores.getHighScore(player.getUniqueId(), area)+"";
		}


		return null;
    }


}
