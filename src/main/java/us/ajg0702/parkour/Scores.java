package us.ajg0702.parkour;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

public class Scores {
	
	Main plugin;
	File storageConfigFile;
	YamlConfiguration storageConfig;
	
	File scoresFile;
	YamlConfiguration scores;
	Connection conn;
	String tablename;
	
	String method;
	

	public Scores(Main pl) {
		plugin = pl;
		storageConfigFile = new File(pl.getDataFolder(), "storage.yml");
		storageConfig = YamlConfiguration.loadConfiguration(storageConfigFile);
		
		checkStorageConfig();
		
		
		if(storageConfig.getString("method").equalsIgnoreCase("mysql")) {
			String ip = storageConfig.getString("mysql.ip");
			String username = storageConfig.getString("mysql.username");
			String password = storageConfig.getString("mysql.password");
			String database = storageConfig.getString("mysql.database");
			String table = storageConfig.getString("mysql.table");
			boolean useSSL = storageConfig.getBoolean("mysql.useSSL");
			try {
				initDatabase(ip, username, password, database, table, useSSL);
			} catch (Exception e) {
				System.err.println("Could not connect to database! Switching to file storage. Error: " + e.getMessage());
				initYaml();
			}
		} else {
			initYaml();
		}
	}
	
	
	public HashMap<String, Double> getTopScores() {
		return getTopScores(true);
	}
	
	
	/**
     * For getting all players scores for sorting.
     *
     * @param  nameKeys
     *         Whether or not to return player names instead of uuids
     *
     * @return map with a list of all scores.
     */
	public HashMap<String, Double> getTopScores(boolean nameKeys) {
		HashMap<String, Double> map = new HashMap<String, Double>();
		if(!nameKeys) {
			for(UUID uuid : this.getPlayers()) {
				map.put(uuid.toString(), Double.valueOf(this.getScore(uuid)));
			}
			return map;
		}
		for(UUID uuid : this.getPlayers()) {
			String n = this.getName(uuid);
			if(n == null) {
				n = "-Unknown-";
				//Bukkit.getLogger().warning("[ajParkour] Could not find name for a person! This probably means the person's name is missing from the databse, or the player has never joined before.");
			}
			map.put(n, Double.valueOf(this.getScore(uuid)));
		}
		//Bukkit.getLogger().info("Returned "+map.toString()+" scores");
		return map;
	}
	
	public LinkedHashMap<String, Double> getSortedScores(boolean nameKeys) {
		LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();
		if(!nameKeys) {
			for(UUID uuid : this.getPlayers(true)) {
				map.put(uuid.toString(), Double.valueOf(this.getScore(uuid)));
			}
			return map;
		}
		for(UUID uuid : this.getPlayers()) {
			String n = this.getName(uuid);
			if(n == null) {
				n = "-Unknown-";
				//Bukkit.getLogger().warning("[ajParkour] Could not find name for a person! This probably means the person's name is missing from the databse, or the player has never joined before.");
			}
			map.put(n, Double.valueOf(this.getScore(uuid)));
		}
		if(method.equalsIgnoreCase("yaml")) {
			map = plugin.sortByValue(map);
		}
		//Bukkit.getLogger().info("Returned "+map.toString()+" scores");
		return map;
	}
	
	private void checkStorageConfig() {
		Map<String, Object> v = new HashMap<String, Object>();
		v.put("method", "yaml");
		v.put("mysql.ip", "127.0.0.1:3306");
		v.put("mysql.username", "");
		v.put("mysql.password", "");
		v.put("mysql.database", "");
		v.put("mysql.table", "ajparkour_scores");
		v.put("mysql.useSSL", false);
		
		boolean save = false;
		
		storageConfig.options().header("\n\nThis file tells the plugin where it\n"
				+ "should store player high scores.\n\n"
				+ "The method option can either be 'yaml' or 'mysql'.\n"
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
	
	private void initYaml() {
		method = "yaml";
		scoresFile = new File(plugin.getDataFolder(), "scores.yml");
		scores = YamlConfiguration.loadConfiguration(scoresFile);
		scores.options().header("\n\nThis is the scores file.\nEveryone's high scores are stored here.\n\nTheres not really any reason to edit this file.\n \n ");
		
		boolean save = false;
		for(String key : scores.getKeys(false)) {
			Object value = scores.get(key);
			if(value instanceof Integer) {
				scores.set(key+".score", value);
				save = true;
			}
		}
		if(save) {
			try {
				scores.save(scoresFile);
			} catch (IOException e) {
				Bukkit.getLogger().severe("[ajParkour] Unable to save scores file:");
				e.printStackTrace();
			}
		}
	}
	
	private void initDatabase(String ip, String username, String password, String database, String table, boolean useSSL) throws Exception {
		String url = "jdbc:mysql://"+ip+"/"+database+"?useSSL="+useSSL+"&autoReconnect=true";
		Class.forName("org.gjt.mm.mysql.Driver");
		tablename = table;
		conn = DriverManager.getConnection(url, username, password);
		conn.createStatement().executeUpdate("create table if not exists "+tablename+" (id VARCHAR(36), score BIGINT(255), name VARCHAR(17))");
		method = "mysql";
		try {
			conn.createStatement().executeUpdate("alter table "+tablename+" add column time INT(255) after name");
			conn.createStatement().executeUpdate("alter table "+tablename+" add column material TINYTEXT after time");
		} catch(Exception e) {}
	}
	
	public int getScore(UUID uuid) {
		if(method.equals("yaml")) {
			
			return scores.getInt(uuid.toString()+".score", -1);
			
		} else if(method.equals("mysql")) {
			try {
				ResultSet p = conn.createStatement().executeQuery("select score from "+tablename+" where id='"+uuid.toString()+"'");
				int size = 0;
				if(p != null) {
					p.last();
					size = p.getRow();
				}
				if(size == 0) {
					return -1;
				}
				p.first();
				return p.getInt(1);
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] An error occured when attempting to read from database:");
				e.printStackTrace();
				return -1;
			}
		}
		Bukkit.getLogger().severe("[ajParkour] getScore() could not find a method!");
		return -1;
	}
	
	public int getTime(UUID uuid) {
		if(method.equals("yaml")) {
			
			return scores.getInt(uuid.toString()+".time", -1);
			
		} else if(method.equals("mysql")) {
			try {
				ResultSet p = conn.createStatement().executeQuery("select time from "+tablename+" where id='"+uuid.toString()+"'");
				int size = 0;
				if(p != null) {
					p.last();
					size = p.getRow();
				}
				if(size == 0) {
					return -1;
				}
				p.first();
				return p.getInt(1);
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] An error occured when attempting to read from database:");
				e.printStackTrace();
				return -1;
			}
		}
		Bukkit.getLogger().severe("[ajParkour] getTime() could not find a method!");
		return -1;
	}
	
	private void saveYaml() {
		try {
			scores.save(scoresFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("[ajParkour] Unable to save scores file:");
			e.printStackTrace();
		}
	}
	
	public void setScore(UUID uuid, int score, int secs) {
		if(method.equals("yaml")) {
			scores.set(uuid.toString()+".score", score);
			scores.set(uuid.toString()+".time", secs);
			if(score == 0 && secs == 0) {
				scores.set(uuid.toString(), null);
			}
			saveYaml();
		} else if(method.equals("mysql")) {
			try {
				ResultSet r = conn.createStatement().executeQuery("select * from "+tablename+" where id='"+uuid.toString()+"'");
				int size = 0;
				if(r != null) {
					r.last();
					size = r.getRow();
				}
				if(size <= 0) {
					if(!(score == 0 && secs == 0)) {
						conn.createStatement().executeUpdate("insert into "+tablename+" (id, score, name, time) "
								+ "values ('"+uuid+"', "+score+", '"+Bukkit.getOfflinePlayer(uuid).getName()+"', "+secs+")");
					}
				} else {
					if(score == 0 && secs == 0) {
						conn.createStatement().executeUpdate("delete from `"+tablename+"` where id="+uuid.toString());
					} else {
						conn.createStatement().executeUpdate("update "+tablename+" set score="+score + ",time="+secs+" where id='"+uuid.toString()+"'");
					}
				}
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] Unable to set score for a player:");
				e.printStackTrace();
			}
		}
	}
	
	public void setMaterial(UUID uuid, String mat) {
		if(method.equals("yaml")) {
			scores.set(uuid.toString()+".material", mat.toString());
			saveYaml();
		} else if(method.equals("mysql")) {
			try {
				ResultSet r = conn.createStatement().executeQuery("select * from "+tablename+" where id='"+uuid.toString()+"'");
				int size = 0;
				if(r != null) {
					r.last();
					size = r.getRow();
				}
				if(size <= 0) {
					conn.createStatement().executeUpdate("insert into "+tablename+" (id, score, name, material) "
						+ "values ('"+uuid+"', 0, "+Bukkit.getOfflinePlayer(uuid)+", "+mat.toString()+")");
				} else {
					conn.createStatement().executeUpdate("update "+tablename+" set material=\"" + mat + "\" where id='"+uuid.toString()+"'");
				}
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] Unable to set material for a player:");
				e.printStackTrace();
			}
		}
	}
	
	public String getMaterial(UUID uuid) {
		if(method.equals("yaml")) {
			
			return scores.getString(uuid.toString()+".material", "RANDOM");
			
		} else if(method.equals("mysql")) {
			try {
				ResultSet p = conn.createStatement().executeQuery("select material from "+tablename+" where id='"+uuid.toString()+"'");
				int size = 0;
				if(p != null) {
					p.last();
					size = p.getRow();
				}
				if(size == 0) {
					return "RANDOM";
				}
				p.first();
				return p.getString(1);
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] An error occured when attempting to read from database:");
				e.printStackTrace();
				return "RANDOM";
			}
		}
		Bukkit.getLogger().severe("[ajParkour] getMaterial() could not find a method!");
		return "RANDOM";
	}
	
	public String getName(UUID uuid) {
		if(method.equals("yaml")) {
			
			return scores.getString(uuid.toString()+".name", Bukkit.getOfflinePlayer(uuid).getName());
			
		} else if(method.equals("mysql")) {
			try {
				ResultSet r = conn.createStatement().executeQuery("select name from "+tablename+" where id='"+uuid.toString()+"'");
				int size = 0;
				if(r != null) {
					r.last();
					size = r.getRow();
				}
				if(size <= 0) {
					return null;
				}
				return r.getString("name");
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] An error occured when attempting to read from database:");
				e.printStackTrace();
				return null;
			}
		}
		Bukkit.getLogger().severe("[ajParkour] getName() could not find a method!");
		return null;
	}
	
	
	public List<UUID> getPlayers() {
		return getPlayers(false);
	}
	public List<UUID> getPlayers(boolean sort) {
		if(method.equals("yaml")) {
			List<UUID> uuids = new ArrayList<UUID>();
			for(String key : scores.getKeys(false)) {
				uuids.add(UUID.fromString(key));
			}
			return uuids;
		} else if(method.equals("mysql")) {
			int size = 0;
			try {
				ResultSet r = null;
				if(sort) {
					r = conn.createStatement().executeQuery("select id, score from "+tablename+" order by score DESC;");
				} else {
					r = conn.createStatement().executeQuery("select id, score from "+tablename+";");
				}
				List<UUID> uuids = new ArrayList<UUID>();
				if(r != null) {
					r.last();
					size = r.getRow();
				}
				if(size > 0) {
					int i = size;
					r.first();
					while(i > 0) {
						//Bukkit.getLogger().info(i+"");
						uuids.add(UUID.fromString(r.getString(1)));
						i--;
						if(i > 0) {
							r.next();
						}
					}
				}
				return uuids;
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] An error occured while trying to get list of ("+size+") scores:");
				e.printStackTrace();
				return null;
			}
		}
		Bukkit.getLogger().severe("[ajParkour] getPlayers() could not find a method!");
		return null;
	}
	
	public void updateName(UUID uuid) {
		//System.out.println("updateName("+uuid.toString()+")");
		String newname = Bukkit.getPlayer(uuid).getName();
		if(method.equals("yaml")) {
			scores.set(uuid.toString()+".name", newname);
			saveYaml();
		} else if(method.equals("mysql")) {
			try {
				ResultSet r = conn.createStatement().executeQuery("select id from "+tablename+" where id='"+uuid.toString()+"'");
				int size = 0;
				if(r != null) {
					r.last();
					size = r.getRow();
				}
				
				if(size > 0) {
					conn.createStatement().executeUpdate("update "+tablename+" set name='"+newname+"' where id='"+uuid.toString()+"'");
				} else {
					//System.out.println("No name to update for " + newname);
				}
			} catch (SQLException e) {
				Bukkit.getLogger().severe("[ajParkour] An error occured while trying to update name for player " + newname+":");
				e.printStackTrace();
			}
			
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
				setScore(uuid, s.getInt(key), -1);
				count++;
			}
			sc = null;
			s = null;
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
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			Connection con;
			try {
				con = DriverManager.getConnection(url, username, password);
				con.createStatement().executeUpdate("create table if not exists "+table+" (id VARCHAR(36), score BIGINT(255), name VARCHAR(17))");
				
				ResultSet r = con.createStatement().executeQuery("select id,score,time from "+tablename);
				int size = 0;
				if(r != null) {
					r.last();
					size = r.getRow();
					
					int i = size;
					while(i > 0) {
						setScore(UUID.fromString(r.getString(1)), r.getInt(2), r.getInt(3));
						i--;
						r.next();
					}
					
				}
				return size;
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return -1;
	}
	
	

}
