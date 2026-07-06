package us.ajg0702.parkour;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import us.ajg0702.parkour.game.Manager;
import us.ajg0702.parkour.top.TopEntry;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Scores {

	Main plugin;
	File storageConfigFile;
	YamlConfiguration storageConfig;

	String tablePrefix;

	String method;

	private final HikariConfig hikariConfig = new HikariConfig();
	HikariDataSource ds;


	public Scores(Main pl) {
		plugin = pl;
		storageConfigFile = new File(pl.getDataFolder(), "storage.yml");
		storageConfig = YamlConfiguration.loadConfiguration(storageConfigFile);

		checkStorageConfig();

		String ip = storageConfig.getString("mysql.ip");
		String username = storageConfig.getString("mysql.username");
		String password = storageConfig.getString("mysql.password");
		String database = storageConfig.getString("mysql.database");
		String tablePrefix = storageConfig.getString("mysql.tablePrefix");
		String tx_isolation = storageConfig.getString("mysql.tx_isolation");
		boolean useSSL = storageConfig.getBoolean("mysql.useSSL");
		boolean allowPublicKeyRetrieval = storageConfig.getBoolean("mysql.allowPublicKeyRetrieval");
		int minCount = storageConfig.getInt("mysql.minConnections");
		int maxCount = storageConfig.getInt("mysql.maxConnections");

		String sMethod = storageConfig.getString("method");


		if(sMethod.equalsIgnoreCase("mysql")) {
			try {
				initDatabase("mysql", ip, username, password, database, tablePrefix, useSSL, allowPublicKeyRetrieval, minCount, maxCount, tx_isolation);
			} catch (Exception e) {
				plugin.getLogger().warning("Could not connect to database! Switching to sqlite storage. Error: ");
				e.printStackTrace();
				sMethod = "sqlite";
			}
		}
		if(sMethod.equalsIgnoreCase("sqlite") || sMethod.equalsIgnoreCase("yaml")) {
			try {
				initDatabase(sMethod, null, null, null, null, tablePrefix, false, false, minCount, maxCount, tx_isolation);
			} catch(SQLException e) {
				plugin.getLogger().severe("Unable to create sqlite database. High scores will not work!");
				e.printStackTrace();
			}
		}

		if(method == null) {
			plugin.getLogger().severe("Unable to find database method! Check storage.yml as you most likely put in an invalid storage method.");
		}
	}





	private void checkStorageConfig() {
		Map<String, Object> v = new HashMap<>();
		v.put("method", "sqlite");
		v.put("mysql.ip", "127.0.0.1:3306");
		v.put("mysql.username", "");
		v.put("mysql.password", "");
		v.put("mysql.database", "");
		v.put("mysql.tablePrefix", "ajparkour_");
		v.put("mysql.allowPublicKeyRetrieval", false);
		v.put("mysql.useSSL", false);
		v.put("mysql.minConnections", 2);
		v.put("mysql.maxConnections", 10);
		v.put("mysql.tx_isolation", "");

		boolean save = false;

		storageConfig.options().header("\n\nThis file tells the plugin where it\n"
				+ "should store player high scores.\n\n"
				+ "The method option can either be 'sqlite' or 'mysql'.\n"
				+ "If it is mysql, you must configure the mysql section below.\n\n ");
		for(String key : v.keySet()) {
			if(!storageConfig.isSet(key)) {
				storageConfig.set(key, v.get(key));
				save = true;
			}
		}
		if(save) {
			try {
				storageConfig.save(storageConfigFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}


	public Connection getConnection() {
		try {
			if(method.equals("sqlite") || method.equals("yaml")) {
				if(sqliteConn == null || sqliteConn.isClosed()) {
					sqliteConn = DriverManager.getConnection(url);
				}
				return sqliteConn;
			}
			if(ds == null) return null;
			return ds.getConnection();
		} catch (SQLException e) {
			plugin.getLogger().warning("Unable to get sql connection:");
			e.printStackTrace();
			return null;
		}
	}

	Connection sqliteConn;
	String url;
	private void initDatabase(String method, String ip, String username, String password, String database, String tablePrefix, boolean useSSL, boolean allowPublicKeyRetrieval, int minConnections, int maxConnections, String tx_isolation) throws SQLException {
		if(method.equals("mysql")) {
			url = "jdbc:mysql://"+ip+"/"+database+"?useSSL="+useSSL+"&allowPublicKeyRetrieval="+allowPublicKeyRetrieval+"&characterEncoding=utf8";
			hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
			hikariConfig.setJdbcUrl(url);
			hikariConfig.setUsername(username);
			hikariConfig.setPassword(password);
			hikariConfig.setMaximumPoolSize(maxConnections);
			hikariConfig.setMinimumIdle(minConnections);

			if(!tx_isolation.isEmpty()) {
				hikariConfig.setTransactionIsolation(tx_isolation);
			}

			this.tablePrefix = tablePrefix;
			ds = new HikariDataSource(hikariConfig);
			ds.setLeakDetectionThreshold(60 * 1000);
		} else {
			url = "jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"scores.db";
			try {
				Class.forName("org.sqlite.JDBC");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
			}

			sqliteConn = DriverManager.getConnection(url);
		}
		this.method = method;

		String oldTableName = storageConfig.getString("mysql.table");
		if(oldTableName != null && method.equalsIgnoreCase("mysql")) {
			convertFromOldSQL(oldTableName);
		}
		createTables();
		if(method.equalsIgnoreCase("yaml")) {
			convertFromYaml();
		}
		if(method.equalsIgnoreCase("yaml")) {
			this.method = "sqlite";
		}
	}

	public void disable() {
		if(ds != null) {
			ds.close();
		}
		if(sqliteConn != null) {
			try {
				sqliteConn.close();
			} catch (SQLException ignored) {}
		}
	}

	private void createTables() throws SQLException {
		String autoIncrement = "AUTO_INCREMENT";
		String integer = "INT";
		if(method.equalsIgnoreCase("sqlite") || method.equalsIgnoreCase("yaml")) {
			autoIncrement = "AUTOINCREMENT";
			integer = "INTEGER";
		}
		Connection conn = getConnection();
		conn.createStatement().executeUpdate(
				"create table if not exists "+ tablePrefix +"players " +
						"(id VARCHAR(36) PRIMARY KEY, material TINYTEXT, name VARCHAR(17), gamesplayed INT)"
		);
		conn.createStatement().executeUpdate(
				"create table if not exists "+ tablePrefix +"scores " +
						"(id "+integer+" PRIMARY KEY "+autoIncrement+", area TINYTEXT, player VARCHAR(36), score INT, time INT)"
		);
		closeConn(conn);
	}


	private void closeConn(Connection conn, ResultSet... resultSets) throws SQLException {
		if(method.equalsIgnoreCase("mysql")) {
			conn.close();
		}
		for(ResultSet rs : resultSets) {
			rs.close();
		}
	}

	private void convertFromOldSQL(String oldTable) throws SQLException {
		plugin.getLogger().info("Starting database conversion (getting rid of the json in MySQL :puke:)");
		Connection conn = getConnection();

		ResultSet teCheck = conn.createStatement().executeQuery("show tables like '"+oldTable+"_old'");
		if(teCheck.next()) {
			plugin.getLogger().info("Conversion seems to already be done. Marking it as done.");
			storageConfig.set("mysql.table", null);
			try {
				storageConfig.save(storageConfigFile);
			} catch (IOException e) {
				plugin.getLogger().severe("Unable to remove old data from storage config. Conversion may happen again on next restart!");
				e.printStackTrace();
			}
			return;
		}
		teCheck.close();
		conn.createStatement().executeUpdate("alter table "+oldTable+" rename "+oldTable+"_old");

		createTables();

		ResultSet oldData = conn.createStatement().executeQuery("select * from "+oldTable+"_old");
		while(oldData.next()) {
			UUID uuid = UUID.fromString(oldData.getString("id"));
			plugin.getLogger().info("Converting " + uuid.toString());
			String raw = oldData.getString("score");
			String name = oldData.getString("name");
			int time = oldData.getInt("time");
			String mat = oldData.getString("material");
			int gamesPlayed = oldData.getInt("gamesplayed");

			insertJsonData(uuid, name, raw, time, mat, gamesPlayed);
		}
		storageConfig.set("mysql.table", null);
		try {
			storageConfig.save(storageConfigFile);
		} catch (IOException e) {
			plugin.getLogger().severe("Unable to remove old data from storage config. Conversion may happen again on next restart!");
			e.printStackTrace();
		}
		closeConn(conn, oldData);
	}




	public void convertFromYaml() throws SQLException {
		File ymlFile = new File(plugin.getDataFolder(), "scores.yml");
		YamlConfiguration yml = YamlConfiguration.loadConfiguration(ymlFile);
		plugin.getLogger().info("Starting yaml conversion (moving to sqlite)");

		for(String rawUUID : yml.getKeys(false)) {
			ConfigurationSection oldData = yml.getConfigurationSection(rawUUID);
			UUID uuid = UUID.fromString(rawUUID);
			plugin.getLogger().info("Converting "+uuid.toString());
			String raw = oldData.getString("score");
			int time = oldData.getInt("time", 0);
			String mat = oldData.getString("material");
			int gamesPlayed = oldData.getInt("gamesplayed", 0);

			insertJsonData(uuid, null, raw, time, mat, gamesPlayed);
		}

		storageConfig.set("method", "sqlite");
		try {
			storageConfig.save(storageConfigFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public void insertJsonData(UUID uuid, String name, String raw, int time, String mat, int gamesPlayed) throws SQLException {
		Connection conn = getConnection();

		JSONObject scores;

		if(raw == null) {
			raw = "{}";
		}
		if(isInt(raw)) {
			raw = "{\"null\":"+raw+"}";
		}
		try {
			scores = (JSONObject) new JSONParser().parse(raw);
		} catch(Exception e) {
			plugin.getLogger().severe("An error occurred when attempting convert a player's score:");
			e.printStackTrace();
			scores = new JSONObject();
		}

		int largest = 0;
		int largestTime = 0;
		boolean insertedOverall = false;

		PreparedStatement psScore = conn.prepareStatement("insert into "+tablePrefix+"scores " +
				"(area, player, score, time) values" +
				"(?, ?, ?, ?)");
		for(Object a : scores.keySet()) {
			String area = (String) a;
			int score = Math.round((long)scores.get(a));

			if(area.equals("null")) {
				area = "overall";
			}

			int t = 0;
			if(area.equals("overall")) {
				t = time;
				insertedOverall = true;
			} else if(score > largest) {
				largest = score;
				largestTime = time;
			}

			psScore.setString(1, area);
			psScore.setString(2, uuid.toString());
			psScore.setInt(3, score);
			psScore.setInt(4, t);
			psScore.executeUpdate();
		}
		if(!insertedOverall) {
			psScore.setString(1, "overall");
			psScore.setString(2, uuid.toString());
			psScore.setInt(3, largest);
			psScore.setInt(4, largestTime);
			psScore.executeUpdate();
		}
		psScore.close();

		PreparedStatement psPlayer = conn.prepareStatement("insert into "+tablePrefix+"players" +
				"(id, material, name, gamesplayed) values" +
				"(?, ?, ?, ?)");
		psPlayer.setString(1, uuid.toString());
		psPlayer.setString(2, mat);
		psPlayer.setString(3, name);
		psPlayer.setInt(4, gamesPlayed);
		psPlayer.executeUpdate();
		psPlayer.close();

		closeConn(conn);
	}

	public int getHighScore(UUID uuid, String area) {
		if(area == null) {
			area = "overall";
		}
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(
					"select score from "+tablePrefix+"scores where player=? and area=?"
			);
			ps.setString(1, uuid.toString());
			ps.setString(2, area);
			ResultSet rs = ps.executeQuery();
			if(!rs.next()) {
				rs.close();
				ps.close();
				closeConn(conn);
				return 0;
			}
			int s = rs.getInt("score");
			rs.close();
			ps.close();
			closeConn(conn);
			return s;
		} catch(SQLException e) {
			plugin.getLogger().warning("Unable to get score for "+uuid.toString()+":");
			e.printStackTrace();
			return -1;
		}
	}

	public int getTime(UUID uuid, String area) {
		if(area == null) {
			area = "overall";
		}
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(
					"select time from "+ tablePrefix +"scores where player=? and area=?"
			);
			ps.setString(1, uuid.toString());
			ps.setString(2, area);
			ResultSet p = ps.executeQuery();
			if(!p.isBeforeFirst()) {
				p.close();
				ps.close();
				closeConn(conn);
				return 0;
			}
			int r = p.getInt(1);
			p.close();
			ps.close();
			closeConn(conn);
			return r;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occurred when attempting to get a players time:");
			e.printStackTrace();
			return -1;
		}
	}
	public int getTime(UUID uuid) {
		return getTime(uuid, null);
	}


	public void setScore(UUID uuid, int score, int time, final String area) {
		Runnable r = () -> {
			String ar = area;
			if(ar == null || ar.equals("null")) {
				ar = "overall";
			}
			try {
				Connection conn = getConnection();
				PreparedStatement psSelect = conn.prepareStatement(
						"select id from "+ tablePrefix +"scores where player=? and area=?"
				);
				psSelect.setString(1, uuid.toString());
				psSelect.setString(2, ar);
				ResultSet r1 = psSelect.executeQuery();
				if(!r1.next()) {
					r1.close();
					if(!(score == 0 && time == 0)) {
						PreparedStatement psInsert = conn.prepareStatement("insert into "+ tablePrefix +"scores " +
								"(area, player, score, time) values " +
								"(?, ?, ?, ?)");
						psInsert.setString(1, ar);
						psInsert.setString(2, uuid.toString());
						psInsert.setInt(3, score);
						psInsert.setInt(4, time);
						psInsert.executeUpdate();
						psInsert.close();
					}
				} else {
					r1.close();
					if(score == 0 && time == 0) {
						PreparedStatement psDelete = conn.prepareStatement(
								"delete from `"+ tablePrefix +"scores` where player=? and area=?"
						);
						psDelete.setString(1, uuid.toString());
						psDelete.setString(2, ar);
						psDelete.executeUpdate();
						psDelete.close();
					} else {
						PreparedStatement psUpdate = conn.prepareStatement(
								"update "+ tablePrefix +"scores set " +
										"score=?, time=? " +
										"where player=? and area=?"
						);
						psUpdate.setInt(1, score);
						psUpdate.setInt(2, time);
						psUpdate.setString(3, uuid.toString());
						psUpdate.setString(4, ar);
						psUpdate.executeUpdate();
						psUpdate.close();
					}
				}

				psSelect.close();
				closeConn(conn);
				if(!ar.equals("overall")) {
					if(score > getHighScore(uuid, null)) {
						setScore(uuid, score, time, null);
					}
				}
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] Unable to set score for a player:");
				e.printStackTrace();
			}

		};
		if(Manager.getInstance().pluginDisabling) {
			r.run();
		} else {
			Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
		}
	}

	public void setMaterial(UUID uuid, String mat) {
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(
					"update "+tablePrefix+"players set material=? where id=?"
			);
			ps.setString(1, mat);
			ps.setString(2, uuid.toString());
			ps.executeUpdate();
			ps.close();
			closeConn(conn);
		} catch(SQLException e) {
			plugin.getLogger().warning("Unable to set material for player:");
			e.printStackTrace();
		}
	}

	//HashMap<UUID, String> materialCache = new HashMap<>();
	public String getMaterial(UUID uuid) {
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("select material from "+tablePrefix+"players where id=?");
			ps.setString(1, uuid.toString());
			ResultSet r = ps.executeQuery();
			if(!r.next()) {
				r.close();
				ps.close();
				closeConn(conn);
				return "RANDOM";
			}
			String mat = r.getString("material");
			if(mat == null) mat = "RANDOM";
			r.close();
			ps.close();
			closeConn(conn);
			return mat;
		} catch(SQLException e) {
			plugin.getLogger().warning("Unable to get block material for player:");
			e.printStackTrace();
			return "RANDOM";
		}
	}

	public String getName(UUID uuid) {
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("select name from "+ tablePrefix +"players where id=?");
			ps.setString(1, uuid.toString());
			ResultSet r = ps.executeQuery();
			if(!r.next()) {
				r.close();
				ps.close();
				closeConn(conn);
				return null;
			}
			String re = r.getString("name");
			r.close();
			ps.close();
			closeConn(conn);
			return re;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occurred when attempting to get a players name:");
			e.printStackTrace();
			return null;
		}
	}


	public void updateName(Player player) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			UUID uuid = player.getUniqueId();
			try {
				Connection conn = getConnection();
				PreparedStatement psSelect = conn.prepareStatement("select id from "+ tablePrefix +"players where id=?");
				psSelect.setString(1, uuid.toString());
				ResultSet r = psSelect.executeQuery();

				if(r.next()) {
					PreparedStatement psUpdate = conn.prepareStatement("update "+ tablePrefix +"players set name=? where id=?");
					psUpdate.setString(1, player.getName());
					psUpdate.setString(2, uuid.toString());
					psUpdate.executeUpdate();
					psUpdate.close();
				} else {
					PreparedStatement psInsert = conn.prepareStatement("insert into "+tablePrefix+"players" +
							"(id, material, name, gamesplayed) values" +
							"(?, NULL, ?, 0)");
					psInsert.setString(1, uuid.toString());
					psInsert.setString(2, player.getName());
					psInsert.executeUpdate();
					psInsert.close();
				}
				r.close();
				psSelect.close();
				closeConn(conn);
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] An error occurred while trying to update name for player " + player.getName()+":");
				e.printStackTrace();
			}
		});
	}

	/**
	 * It is recommended to use TopManager#getTop instead of this method.
	 * @param position The position to fetch
	 * @return The TopEntry for the requested position
	 */
	public TopEntry getTopPosition(int position, String area) {
		if(area == null) {
			area = "overall";
		}
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement(
					"select * from "+tablePrefix+"scores where area=? order by score desc limit ?,1"
			);
			ps.setString(1, area);
			ps.setInt(2, position - 1);
			ResultSet r = ps.executeQuery();
			if(!r.next()) {
				r.close();
				ps.close();
				closeConn(conn);
				return new TopEntry(position, "--", -1, -1);
			}
			UUID uuid = UUID.fromString(r.getString("player"));
			int score = r.getInt("score");
			int time = r.getInt("time");
			r.close();
			ps.close();
			closeConn(conn);
			String name = getName(uuid);

			if(name == null) {
				name = Bukkit.getOfflinePlayer(uuid).getName();
			}
			if(name == null) {
				name = "Unknown";
			}
			return new TopEntry(position, name, score, time);

		} catch(SQLException e) {
			plugin.getLogger().warning("An error occurred while trying to get a top score:");
			e.printStackTrace();
			return new TopEntry(position, "Error", -1, -1);
		}
	}

	public int getGamesPlayed(UUID uuid) {
		try {
			Connection conn = getConnection();
			PreparedStatement ps = conn.prepareStatement("select gamesplayed from "+ tablePrefix +"players where id=?");
			ps.setString(1, uuid.toString());
			ResultSet r = ps.executeQuery();
			if(!r.next()) {
				r.close();
				ps.close();
				closeConn(conn);
				return 0;
			}
			int re = r.getInt("gamesplayed");
			r.close();
			ps.close();
			closeConn(conn);
			return re;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occurred when attempting to read from database:");
			e.printStackTrace();
			return -1;
		}
	}
	public void addToGamesPlayed(UUID uuid) {
		int newGP = getGamesPlayed(uuid)+1;
		try {
			Connection conn = getConnection();
			PreparedStatement psSelect = conn.prepareStatement("select id from "+ tablePrefix +"players where id=?");
			psSelect.setString(1, uuid.toString());
			ResultSet r = psSelect.executeQuery();

			if(r.next()) {
				PreparedStatement psUpdate = conn.prepareStatement("update "+ tablePrefix +"players set gamesplayed=? where id=?");
				psUpdate.setInt(1, newGP);
				psUpdate.setString(2, uuid.toString());
				psUpdate.executeUpdate();
				psUpdate.close();
			}
			r.close();
			psSelect.close();
			closeConn(conn);
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occurred while trying to update gamesplayed for uuid " + uuid +":");
			e.printStackTrace();
		}
	}




	private boolean isInt(String str) {
	    try {
	        Integer.parseInt(str);
	        return true;
	    } catch (NumberFormatException nfe) {
	        return false;
	    }
	}



}
