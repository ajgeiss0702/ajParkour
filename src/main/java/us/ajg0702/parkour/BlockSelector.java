package us.ajg0702.parkour;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import us.ajg0702.parkour.game.PkArea;
import us.ajg0702.parkour.utils.VersionSupport;

public class BlockSelector implements Listener {

	private Map<Player, Inventory> plys = new HashMap<>();
	Main plugin;
	Messages msgs;
	Scores scores;
	
	private YamlConfiguration blocks;
	private File blocksFile;
	
	List<String> types;
	
	List<String> defBlocksList;
	
	public BlockSelector(Main pl) {
		plugin = pl;
		msgs = pl.msgs;
		scores = pl.scores;

		reloadTypes();
	}
	
	public void reloadTypes() {
		defBlocksList = Arrays.asList(
				"BLUE_GLAZED_TERRACOTTA",
				"ORANGE_GLAZED_TERRACOTTA",
				"WHITE_GLAZED_TERRACOTTA",
				"LIGHT_BLUE_GLAZED_TERRACOTTA",
				"YELLOW_GLAZED_TERRACOTTA",
				"LIME_GLAZED_TERRACOTTA",
				"PINK_GLAZED_TERRACOTTA",
				"GRAY_GLAZED_TERRACOTTA",
				"CYAN_GLAZED_TERRACOTTA",
				"PURPLE_GLAZED_TERRACOTTA",
				"BROWN_GLAZED_TERRACOTTA",
				"GREEN_GLAZED_TERRACOTTA",
				"RED_GLAZED_TERRACOTTA",
				"BLACK_GLAZED_TERRACOTTA",
				"MOSSY_COBBLESTONE",
				"NETHER_BRICKS",
				"QUARTZ_BLOCK",
				"QUARTZ_PILLAR",
				"BLACK_WOOL",
				"BLUE_WOOL",
				"BROWN_WOOL",
				"CYAN_WOOL",
				"GRAY_WOOL",
				"GREEN_WOOL",
				"LIGHT_BLUE_WOOL",
				"LIGHT_GRAY_WOOL",
				"LIME_WOOL",
				"MAGENTA_WOOL",
				"ORANGE_WOOL",
				"PINK_WOOL",
				"PURPLE_WOOL",
				"RED_WOOL",
				"WHITE_WOOL",
				"YELLOW_WOOL");
		if(VersionSupport.getMinorVersion() <= 12) {
			defBlocksList = Arrays.asList(
					"MOSSY_COBBLESTONE",
					"NETHER_BRICK",
					"QUARTZ_BLOCK",
					"WOOD",
					"WOOL:true",
					"RED_SANDSTONE",
					"SMOOTH_BRICK",
					"BRICK");
		}
		blocksFile = new File(plugin.getDataFolder(), "blocks.yml");
		blocks = YamlConfiguration.loadConfiguration(blocksFile);
		blocks.options().header("This is where you can set what blocks to use.\nHere is a guide for the special things you can do in this file: https://ajgeiss72.gitbook.io/ajparkour/blocks\nHere is a list of block types for each version made by brc: https://wiki.brcdev.net/Materials\n Make sure to only use blocks (not items) or it will break!");
		if(!blocks.isSet("blocks")) {
			YamlConfiguration oldconfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
			if(oldconfig.isSet("blocks")) {
				blocks.set("blocks", oldconfig.get("blocks"));
			} else {
				blocks.set("blocks", defBlocksList);
			}
			
			saveBlocks();
		}
		
		/*if(!blocks.isSet("present_heads")) {
			blocks.set("present_heads", Arrays.asList(
					"6a6c5b03-8d52-42d8-b9af-db0b20f61672",
					"4e30e7dc-40fc-4a4d-bf23-d6a9747e35bb",
					"c5f12b47-ccd1-403d-be28-4b04764fe87c",
					"493e008d-efe7-494a-b438-4dcec6715afe",
					"f1eb7cad-e2c0-4e9e-8aad-1eae21d5fd95",
					"156b251b-12e0-4829-a130-a61b53ba7720",
					"5b8f1665-aed5-485e-be9e-4129a377d3fd",
					"b544a15b-8ee2-45e4-8236-1122312f51fb",
					"5b8f1665-aed5-485e-be9e-4129a377d3fd"
					));
			saveBlocks();
		}*/
		
		
		types = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		List<String> mats = (List<String>) blocks.getList("blocks", new ArrayList<>());
		
		for(String mat : mats) {
			Material t = null;
			try {
				t = Material.valueOf(mat.split(";")[0].split(":")[0]);
			} catch(IllegalArgumentException e) {}
			
			if(t != null) {
				types.add(mat);
			} else {
				Bukkit.getLogger().warning("[ajParkour] Could not find material '"+mat+"'! Make sure it exists in the server version you are using!");
			}
		}
		if(types.size() <= 0) {
			types.add("STONE");
			Bukkit.getLogger().warning("[ajParkour] None of the materials in blocks.yml are valid! Falling back to stone.");
		}
	}
	
	private void saveBlocks() {
		try {
			blocks.save(blocksFile);
		} catch (IOException e) {
			Bukkit.getLogger().severe("[ajParkour] Unable to save blocks file!");
			e.printStackTrace();
		}
	}
	
	public Inventory openSelector(Player ply) {
		Inventory inv = Bukkit.createInventory(ply, 54, msgs.get("gui.selector.title", ply));
		
		ply.openInventory(inv);
		plys.put(ply, inv);
		inv = addBlocks(inv);
		return inv;
	}
	
	@SuppressWarnings("deprecation")
	private Inventory addBlocks(Inventory inv) {
		Material randomMat = Material.valueOf(plugin.config.getString("random-item"));
		String rawselected = plugin.scores.getMaterial(((Player)inv.getViewers().get(0)).getUniqueId());
		if(rawselected == null) {
			rawselected = "random";
		}
		int selectedd = -1;
		Material selected;
		if(rawselected.equalsIgnoreCase("random")) {
			selected = randomMat;
		} else {
			selected = Material.valueOf(rawselected.split(":")[0]);
			if(rawselected.split(":").length > 1) {
				selectedd = (!rawselected.split(":")[1].equalsIgnoreCase("true")) ? Integer.valueOf(rawselected.split(":")[1]) : -1;
			}
		}
		
		ItemStack randomI = new ItemStack(randomMat, 1);
		ItemMeta randomImeta = randomI.getItemMeta();
		randomImeta.setDisplayName(msgs.get("gui.selector.items.random.title"));
		randomImeta.setLore(Arrays.asList(msgs.get("gui.selector.items.random.lore").split("\n")));
		if(selected.equals(randomMat)) {
			randomImeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
			if(VersionSupport.getMinorVersion() >= 8) {
				randomImeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
		}
		randomI.setItemMeta(randomImeta);
		
		inv.setItem(4, randomI);
		
		int s = 0;
		for(String rm : types) {
			String m = rm;
			int d = -1;
			if(rm.indexOf("FLOWER_POT") != -1) {
				m = "FLOWER_POT_ITEM";
			}
			if(m.equals(randomMat.toString())) continue;
			s++;
			int slot = 8+s;
			ItemStack i;
			if(VersionSupport.getMinorVersion() > 12) {
				i = new ItemStack(Material.valueOf(m.split(":")[0]), 1);
			} else {
				
				String[] parts = m.split(":");
				if(parts.length > 1 && !parts[1].equalsIgnoreCase("true")) {
					d = Integer.valueOf(parts[1]);
				}
				i = new ItemStack(Material.valueOf(m.split(":")[0]), 1, (short)d, (byte)((d == -1) ? 0 : d));
			}
			//Bukkit.getLogger().info(m+" == "+selected+"    &&    "+d+" == "+selectedd);
			if(m.split(":")[0].equals(selected.toString()) && d == selectedd) {
				ItemMeta iMeta = i.getItemMeta();
				iMeta.addEnchant(Enchantment.DAMAGE_ALL, 1, false);
				if(VersionSupport.getMinorVersion() >= 8) {
					iMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
				}
				iMeta.setLore(Arrays.asList(msgs.get("gui.selector.items.selected.lore").split("\n")));
				i.setItemMeta(iMeta);
			}
			inv.setItem(slot, i);
		}
		
		return inv;
	}
	
	
	@EventHandler
	public void onInvClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		ItemStack clicked = e.getCurrentItem();
		
		Inventory inv = e.getInventory();
		if(!inv.equals(plys.get(p))) {
			return;
		}
		e.setCancelled(true);
		if(clicked == null || clicked.getType() == null || clicked.getType() == Material.AIR || p.getUniqueId() == null) {
			return;
		}
		if(clicked.getType().toString().equals("FLOWER_POT_ITEM")) {
			clicked.setType(Material.FLOWER_POT);
		}
		String matname = null; 
		if(e.getSlot() >= 9) {
			matname = types.get(e.getSlot()-9);
		} else {
			matname = "random";
		}
		scores.setMaterial(p.getUniqueId(), matname);
		inv.clear();
		plys.put(p, addBlocks(inv));
		e.setCancelled(true);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		Inventory inv = e.getInventory();
		if(inv.equals(plys.get(p))) {
			plys.remove(p);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<UUID> getPresents() {
		List<String> r = (List<String>) blocks.getList("present_heads");
		List<UUID> o = new ArrayList<>();
		for(String u : r) {
			o.add(UUID.fromString(u));
		}
		return o;
	}
	
	public String getBlock(Player p, PkArea area) {
		String raw = scores.getMaterial(p.getUniqueId());
		if(raw == null || raw.equalsIgnoreCase("random") || raw.equalsIgnoreCase(plugin.config.getString("random-item"))) {
			List<String> ftypes = new ArrayList<>();
			for(String b : types) {
				if(b.indexOf(';') != -1) {
					String a = b.split(";")[1];
					b = b.split(";")[0];
					String[] parts = a.split(",");
					boolean isIn = false;
					for(String pa : parts) {
						if(pa.equalsIgnoreCase(area.getName())) {
							isIn = true;
							break;
						}
					}
					
					if(!isIn) {
						//System.out.println("removing "+b+" because area not "+area.getName()+" ("+a+")");
						continue;
					}
					//System.out.println("not removing "+b+" because area "+area.getName()+" ("+a+")");
				}
				//System.out.println("adding "+b);
				ftypes.add(b);
			}
			int max = ftypes.size()-1;
			if(max < 0) {
				max = 0;
			}
			int i = Main.random(0, max);
			//String dbug = "";
			//for(String ft : ftypes) { dbug += ft+", "; }
			//System.out.println("max: "+max+" r: "+i+" dbug: "+dbug);
			return ftypes.get(i).toString();
		} else {
			return raw;
		}
	}

}
