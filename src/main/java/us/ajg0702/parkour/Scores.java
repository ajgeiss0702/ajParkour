package us.ajg0702.parkour;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import us.ajg0702.parkour.game.Manager;

public class Scores {

	Main plugin;
	File storageConfigFile;
	YamlConfiguration storageConfig;

	File scoresFile;
	YamlConfiguration scores;
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
		String tablePrefix = storageConfig.getString("mysql.table");
		boolean useSSL = storageConfig.getBoolean("mysql.useSSL");
		boolean allowPublicKeyRetrieval = storageConfig.getBoolean("mysql.allowPublicKeyRetrieval");
		int minCount = storageConfig.getInt("mysql.minConnections");
		int maxCount = storageConfig.getInt("mysql.maxConnections");

		String sMethod = storageConfig.getString("method");


		if(sMethod.equalsIgnoreCase("mysql")) {
			try {
				initDatabase("mysql", ip, username, password, database, tablePrefix, useSSL, allowPublicKeyRetrieval, minCount, maxCount);
			} catch (Exception e) {
				plugin.getLogger().warning("Could not connect to database! Switching to sqlite storage. Error: ");
				e.printStackTrace();
				sMethod = "sqlite";
			}
		}
		if(sMethod.equalsIgnoreCase("sqlite")) {
			try {
				initSQLite(tablePrefix, minCount, maxCount);
			} catch(SQLException e) {
				plugin.getLogger().severe("Unable to create sqlite database. High scores will not work!");
				e.printStackTrace();
			}
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
		v.put("mysql.minConnections", 1);
		v.put("mysql.maxConnections", 10);

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
		if(ds == null) return null;
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			plugin.getLogger().warning("Unable to get sql connection:");
			e.printStackTrace();
			return null;
		}
	}

	private void initSQLite(String tablePrefix, int minConnections, int maxConnections) throws SQLException {
		initDatabase("sqlite", null, null, null, null, tablePrefix, false, false, minConnections, maxConnections);
	}
	private void initDatabase(String method, String ip, String username, String password, String database, String tablePrefix, boolean useSSL, boolean allowPublicKeyRetrieval, int minConnections, int maxConnections) throws SQLException {
		String url;
		if(method.equals("mysql")) {
			url = "jdbc:mysql://"+ip+"/"+database+"?useSSL="+useSSL+"&allowPublicKeyRetrieval="+allowPublicKeyRetrieval+"";
			hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
		} else {
			url = "jdbc:sqlite:"+plugin.getDataFolder().getAbsolutePath()+File.separator+"scores.db";
			hikariConfig.setDriverClassName("org.sqlite.JDBC");
		}
		hikariConfig.setJdbcUrl(url);
		hikariConfig.setUsername(username);
		hikariConfig.setPassword(password);
		hikariConfig.setMaximumPoolSize(maxConnections);
		hikariConfig.setMinimumIdle(minConnections);
		this.tablePrefix = tablePrefix;
		ds = new HikariDataSource(hikariConfig);
		ds.setLeakDetectionThreshold(60 * 1000);
		String oldTableName = storageConfig.getString("mysql.table");
		if(oldTableName != null) {
			convertFromOldSQL(oldTableName);
		}
		createTables();
		method = "mysql";
	}

	private void createTables() throws SQLException {
		Connection conn = getConnection();
		conn.createStatement().executeUpdate(
				"create table if not exists "+ tablePrefix +"players " +
						"(id VARCHAR(36) PRIMARY KEY, material TINYTEXT, name VARCHAR(17), gamesplayed INT)"
		);
		conn.createStatement().executeUpdate(
				"create table if not exists "+ tablePrefix +"scores " +
						"(id INT PRIMARY KEY AUTO_INCREMENT, area TINYTEXT, player VARCHAR(36), score INT, time INT)"
		);
		conn.close();
	}

	private void convertFromOldSQL(String oldTable) throws SQLException {
		plugin.getLogger().info("Starting database conversion (getting rid of the json in MySQL :puke:)");
		Connection conn = getConnection();
		conn.createStatement().executeUpdate("alter table "+oldTable+" rename "+oldTable+"_old");

		createTables();

		ResultSet oldData = conn.createStatement().executeQuery("select * from "+oldTable+"_old");
		while(oldData.next()) {
			UUID uuid = UUID.fromString(oldData.getString("id"));
			plugin.getLogger().info("Converting "+uuid.toString());
			String raw = oldData.getString("score");
			String name = oldData.getString("name");
			int time = oldData.getInt("time");
			String mat = oldData.getString("material");
			int gamesPlayed = oldData.getInt("gamesplayed");

			if(mat == null) {
				mat = "NULL";
			} else {
				mat = "'"+mat+"'";
			}

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
				plugin.getLogger().severe("An error occured when attempting convert a player's score:");
				e.printStackTrace();
				scores = new JSONObject();
			}

			for(Object a : scores.keySet()) {
				String area = (String) a;
				int score = (int) scores.get(a);

				if(area.equals("null")) {
					area = "overall";
				}

				int t = 0;
				if(area.equals("overall")) {
					t = time;
				}

				conn.createStatement().executeUpdate("insert into "+tablePrefix+"scores " +
						"(area, player, score, time) values" +
						"('"+area+"', '"+uuid+"', "+score+", "+t+")");
			}
			conn.createStatement().executeUpdate("insert into "+tablePrefix+"players" +
					"(id, material, name, gamesplayed) values" +
					"("+uuid.toString()+", "+mat+", "+name+", "+gamesPlayed+")");

		}

		storageConfig.set("mysql.table", null);
		try {
			storageConfig.save(storageConfigFile);
		} catch (IOException e) {
			plugin.getLogger().severe("Unable to remove old data from storage config. Conversion may happen again on next restart!");
			e.printStackTrace();
		}
		conn.close();
	}



	public int getHighScore(UUID uuid, String area) {
		if(area == null) {
			area = "overall";
		}
		try {
			Connection conn = getConnection();
			ResultSet rs = conn.createStatement().executeQuery(
					"select score from "+tablePrefix+"scores where player='"+uuid.toString()+"' and area='"+area+"'"
			);
			if(!rs.next()) {
				conn.close();
				return 0;
			}
			conn.close();
			return rs.getInt("score");
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
			ResultSet p = conn.createStatement().executeQuery(
					"select time from "+ tablePrefix +"scores where player='"+uuid.toString()+"' and area='"+area+"'"
			);
			if(!p.isBeforeFirst()) {
				return 0;
			}
			int r = p.getInt(1);
			conn.close();
			return r;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occured when attempting to get a players time:");
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
			if(ar == null) {
				ar = "overall";
			}
			try {
				Connection conn = getConnection();
				ResultSet r1 = conn.createStatement().executeQuery(
						"select id from "+ tablePrefix +"scores where player='"+uuid.toString()+"' and area='"+area+"'"
				);
				if(r1.isAfterLast()) {
					if(!(score == 0 && time == 0)) {
						conn.createStatement().executeUpdate("insert into "+ tablePrefix +"scores " +
								"(area, player, score, time) values " +
								"("+ar+", "+uuid.toString()+", "+score+", "+time+")");
					}
				} else {
					if(score == 0 && time == 0) {
						conn.createStatement().executeUpdate(
								"delete from `"+ tablePrefix +"scores` where player='"+uuid.toString()+"' and area='"+area+"'"
						);
					} else {
						conn.createStatement().executeUpdate(
								"update "+ tablePrefix +"scores set " +
										"score="+score+", time="+time +
										"where player='"+uuid.toString()+"' and area='"+area+"'"
						);
					}
				}
				conn.close();
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
			conn.createStatement().executeUpdate(
					"update "+tablePrefix+"players set material='"+mat+"' where id='"+uuid.toString()+"'"
			);
			conn.close();
		} catch(SQLException e) {
			plugin.getLogger().warning("Unable to set material for player:");
			e.printStackTrace();
		}
	}

	//HashMap<UUID, String> materialCache = new HashMap<>();
	public String getMaterial(UUID uuid) {
		try {
			Connection conn = getConnection();
			ResultSet r = conn.createStatement().executeQuery("select material from "+tablePrefix+"players where id='"+uuid.toString()+"'");
			if(!r.next()) {
				conn.close();
				return "RANDOM";
			}
			String mat = r.getString("material");
			if(mat == null) mat = "RANDOM";
			conn.close();
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
			ResultSet r = conn.createStatement().executeQuery("select name from "+ tablePrefix +"players where id='"+uuid.toString()+"'");
			if(!r.next()) {
				return null;
			}
			String re = r.getString("name");
			conn.close();
			return re;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occured when attempting to get a players name:");
			e.printStackTrace();
			return null;
		}
	}


	public void updateName(Player player) {
		UUID uuid = player.getUniqueId();
		try {
			Connection conn = getConnection();
			ResultSet r = conn.createStatement().executeQuery("select id from "+ tablePrefix +"players where id='"+uuid.toString()+"'");

			if(r.next()) {
				conn.createStatement().executeUpdate("update "+ tablePrefix +"players set name='"+player.getName()+"' where id='"+uuid.toString()+"'");
			} else {
				conn.createStatement().executeUpdate("insert into "+tablePrefix+"players" +
						"(id, material, name, gamesplayed) values" +
						"("+uuid.toString()+", NULL, "+player.getName()+", 0)");
			}
			conn.close();
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occurred while trying to update name for player " + player.getName()+":");
			e.printStackTrace();
		}

	}

	public int getGamesPlayed(UUID uuid) {
		try {
			Connection conn = getConnection();
			ResultSet r = conn.createStatement().executeQuery("select gamesplayed from "+ tablePrefix +"players where id='"+uuid.toString()+"'");
			if(!r.isBeforeFirst()) {
				conn.close();
				return 0;
			}
			int re = r.getInt("gamesplayed");
			conn.close();
			return re;
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occured when attempting to read from database:");
			e.printStackTrace();
			return -1;
		}
	}
	public void addToGamesPlayed(UUID uuid) {
		int newgp = getGamesPlayed(uuid)+1;
		try {
			Connection conn = getConnection();
			ResultSet r = conn.createStatement().executeQuery("select id from "+ tablePrefix +" where id='"+uuid.toString()+"'");

			if(r.isBeforeFirst()) {
				conn.createStatement().executeUpdate("update "+ tablePrefix +" set gamesplayed='"+newgp+"' where id='"+uuid.toString()+"'");
			}
			conn.close();
		} catch (SQLException e) {
			Bukkit.getLogger().severe("[ajParkour] An error occured while trying to update gamesplayed for uuid " + uuid +":");
			e.printStackTrace();
		}
	}

	public int migrate(String from) {
		if(method.equalsIgnoreCase(from)) {
			return 0;
		}
		if(from.equalsIgnoreCase("yaml")) {
			File sc = new File(plugin.getDataFolder(), "scores.yml");
			YamlConfiguration s = YamlConfiguration.loadConfiguration(sc);

			int count = 0;
			for(String key : s.getKeys(false)) {
				UUID uuid = UUID.fromString(key);
				//setScore(uuid, s.getInt(key), -1, "null");
				JSONObject o = getJsonObject(s.getString(key+".score"));
				for(Object k : o.keySet()) {
					setScore(uuid, Integer.parseInt(o.get(k)+""), s.getInt(key + ".time"), k+"");
				}
				count++;
			}
			return count;
		} else if(from.equalsIgnoreCase("mysql")) {
			String ip = storageConfig.getString("mysql.ip");
			String username = storageConfig.getString("mysql.username");
			String password = storageConfig.getString("mysql.password");
			String database = storageConfig.getString("mysql.database");
			String table = storageConfig.getString("mysql.table");
			boolean useSSL = storageConfig.getBoolean("mysql.useSSL");
			String url = "jdbc:mysql://"+ip+"/"+database+"?useSSL="+useSSL;
			try {
				Class.forName("org.gjt.mm.mysql.Driver");
			} catch(Exception e) {
				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			Connection con;
			try {
				con = DriverManager.getConnection(url, username, password);
				con.createStatement().executeUpdate("create table if not exists "+table+" (id VARCHAR(36), score BIGINT(255), name VARCHAR(17))");

				ResultSet r = con.createStatement().executeQuery("select id,score,time from "+ tablePrefix);
				if(r != null) {
					boolean next = r.isBeforeFirst();
					while(next) {
						UUID uuid = UUID.fromString(r.getString(1));
						JSONObject o = getJsonObject(r.getString(2));
						for(Object k : o.keySet()) {
							setScore(uuid, Integer.parseInt(o.get(k)+""), r.getInt(3), k.toString());
						}
						//setScore(UUID.fromString(r.getString(1)), r.getString(2), r.getInt(3), null);
						next = r.next();
					}

				}
				return r.getRow();
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
		} else if(from.equalsIgnoreCase("rogueparkour")) {
			String ip = storageConfig.getString("mysql.ip");
			String username = storageConfig.getString("mysql.username");
			String password = storageConfig.getString("mysql.password");
			String database = storageConfig.getString("mysql.database");
			String table = "RPScore";
			boolean useSSL = storageConfig.getBoolean("mysql.useSSL");
			String url = "jdbc:mysql://"+ip+"/"+database+"?useSSL="+useSSL;
			try {
				Class.forName("org.gjt.mm.mysql.Driver");
			} catch(Exception e) {
				try {
					Class.forName("com.mysql.jdbc.Driver");
				} catch (ClassNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			Connection con;
			try {
				con = DriverManager.getConnection(url, username, password);
				con.createStatement().executeUpdate("create table if not exists "+table+" (`id` int(11) NOT NULL AUTO_INCREMENT,`player` varchar(40) NOT NULL,`score` int(11) NOT NULL, PRIMARY KEY (`id`))");

				ResultSet r = con.createStatement().executeQuery("select * from "+table);
				if(r != null) {
					boolean next = r.isBeforeFirst();
					while(next) {
						//System.out.println("2");
						UUID uuid = UUID.fromString(r.getString(2));
						JSONObject o = getJsonObject(r.getString(3));
						for(Object k : o.keySet()) {
							System.out.println("moving score");
							setScore(uuid, Integer.parseInt(o.get(k)+""), -1, k.toString());
						}
						//setScore(UUID.fromString(r.getString(1)), r.getString(2), r.getInt(3), null);
						next = r.next();
					}

				}
				return r.getRow();
			} catch (SQLException e) {
				e.printStackTrace();
				return 0;
			}
		}
		return -1;
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
