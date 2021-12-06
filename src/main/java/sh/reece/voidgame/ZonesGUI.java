package sh.reece.voidgame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;

public class ZonesGUI implements Listener {

	private static Main plugin;
	public static Inventory zonesinv, switchInv, controlPanelInv;
	public String perm, InvName;
	public static String switchInvName, controlPanelName;
	public static int rows;
	
	private HashMap<String, List<Object>> main_menu = 
			new HashMap<String, List<Object>>();
	
	private HashMap<String, List<Object>> control_panel = 
			new HashMap<String, List<Object>>();
	
	private List<String> empty = new ArrayList<String>();
	
	public ZonesGUI(Main instance) {
		plugin = instance;
		rows = 6*9;

		InvName = Util.color("&lZones GUI");
		switchInvName = Util.color("&lSwitch Section");
		controlPanelName = Util.color("&lControl Panel");
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

		// 1.8 GUI items - should really use xseries but oh well
		String start= null, setspawn= null, givewoodaxe = null, givecobweb= null, 
				givewoodsword= null, givegoldaxe= null,giveironshovel= null, givegoldshovel= null, givediamondshovel = null;
		if(Util.isVersion1_8()) {
			start = "GREEN_RECORD";
			setspawn = "SAPLING";			
			givewoodaxe = "WOOD_AXE";
			givecobweb = "WEB";
			givewoodsword = "WOOD_SWORD";
			givegoldaxe =  "GOLD_AXE";
			giveironshovel = "IRON_SPADE";
			givegoldshovel = "GOLD_SPADE";		
			givediamondshovel = "DIAMOND_SPADE";
		} else {
			start = "GREEN_WOOL";
			setspawn = "OAK_SAPLING";			
			givewoodaxe = "WOODEN_AXE";
			givecobweb = "COBWEB";
			givewoodsword = "WOODEN_SWORD";
			givegoldaxe = "GOLDEN_AXE";
			giveironshovel = "IRON_SHOVEL";
			givegoldshovel = "GOLDEN_SHOVEL";
			givediamondshovel = "DIAMOND_SHOVEL";
		}
				

		// main zones menu items
		main_menu.put("ADD ALL", 				items("DIAMOND", 1, 1, "zone addall"));		
		main_menu.put("SET JAIL", 				items("BEDROCK", 1, 3, "zone setjail"));	
		main_menu.put("STOP", 					items("REDSTONE_BLOCK", 1, 9, "zone stop"));
		main_menu.put("START", 					items(start, 1, 10, "zone start"));
		main_menu.put("SET SPAWN", 				items(setspawn, 1, 12, "zone setspawn"));
		main_menu.put("SWITCH LOCATION",		items("MAP", 1, 14, "zone switchinv"));		
		main_menu.put("STOP ZONE CHANGE", 		items("BARRIER", 1, 16, "zone stopchange"));
		main_menu.put("RULES | HOW-TO", 		items("BOOK", 1, 28, "zone rules"));				
		main_menu.put("TOGGLE ZONE PVP", 		items("DIAMOND_SWORD", 1, 30, "zone togglepvp"));
		main_menu.put("TOGGLE ZONE FALL-DAMAGE",items("FEATHER", 1, 31, "zone togglefall"));
		main_menu.put("TOGGLE ZONE BUILDING",       items("DIRT", 1, 39, "zone togglebuild")); 
		main_menu.put("TOGGLE ZONE BREAKING", 		items("DIAMOND_PICKAXE", 1, 40, "zone togglebreak"));		
		main_menu.put("CONTROL PANEL", 			items("COMPASS", 1, 34, "zone controlpanel"));
		main_menu.put("/zone revive PLAYER", 	items("NETHER_STAR", 1, 53, "zone revive"));
				
		control_panel.put("1x Iron Helmet", 		items("IRON_HELMET", 1, 0, "giveall iron_helmet 1"));
		control_panel.put("1x Iron Chestplate", 	items("IRON_CHESTPLATE", 1, 1, "giveall iron_chestplate 1"));
		control_panel.put("1x Iron Leggings", 	items("IRON_LEGGINGS", 1, 2, "giveall iron_leggings 1"));
		control_panel.put("1x Iron Boots", 		items("IRON_BOOTS", 1, 3, "giveall iron_boots 1"));
		control_panel.put("1x Wooden Axe", 		items(givewoodaxe, 1, 4, "giveall "+givewoodaxe+" 1"));
		control_panel.put("1x Golden Apple", 		items("GOLDEN_APPLE", 1, 5, "giveall golden_apple 1"));
		control_panel.put("64x Dirt (Unlimited)", 			items("DIRT", 1, 6, "giveall dirt 64"));		
		control_panel.put("1x Instant Healing potion",items("POTION", 1, 7, "giveall healingpotion 1"));
		control_panel.put("1x Diamond Helm", 		items("DIAMOND_HELMET", 1, 8, "giveall diamond_helmet 1"));
		control_panel.put("1x Diamond Chest", 	items("DIAMOND_CHESTPLATE", 1, 9, "giveall diamond_chestplate 1"));
		control_panel.put("1x Diamond legs", 		items("DIAMOND_LEGGINGS", 1, 10, "giveall diamond_leggings 1"));
		control_panel.put("1x Diamond boots", 	items("DIAMOND_BOOTS", 1, 11, "giveall diamond_boots 1"));
		control_panel.put("1x Diamond shovel", 	items(givediamondshovel, 1, 12, "giveall "+givediamondshovel+" 1"));
		control_panel.put("1x Diamond axe", 		items("DIAMOND_AXE", 1, 13, "giveall diamond_axe 1"));
		control_panel.put("8x Arrow",			 	items("ARROW", 1, 14, "giveall arrow 8"));
		control_panel.put("1x Bow", 				items("BOW", 1, 15, "giveall bow 1"));
		control_panel.put("1x Web", 				items(givecobweb, 1, 16, "giveall "+givecobweb+" 1"));
		control_panel.put("1x Compass", 				items("COMPASS", 1, 17, "giveall compass 1"));
		control_panel.put("1x Wooden Sword", 				items(givewoodsword, 1, 18, "giveall "+givewoodsword+" 1"));
		control_panel.put("1x Stone Sword", 				items("STONE_SWORD", 1, 19, "giveall woodensword 1"));
		control_panel.put("1x Diamond Sword", 				items("DIAMOND_SWORD", 1, 20, "giveall diamondsword 1"));
		control_panel.put("1x lava Bucket", 				items("LAVA_BUCKET", 1, 21, "giveall lavabucket 1"));
		
		control_panel.put("1x Iron Axe", 				items("IRON_AXE", 1, 22, "giveall IRON_AXE 1"));
		control_panel.put("1x Gold Axe", 				items(givegoldaxe, 1, 23, "giveall "+givegoldaxe+" 1"));
		control_panel.put("1x Iron Shovel", 	items(giveironshovel, 1, 24, "giveall "+giveironshovel+" 1"));
		control_panel.put("1x Gold Shovel", 	items(givegoldshovel, 1, 25, "giveall "+givegoldshovel+" 1"));
		
		control_panel.put("Jump Boost (20 seconds x2)", 	items("RABBIT_FOOT", 1, 27, "effect @a minecraft:jump_boost 20 2"));
		control_panel.put("Speed (20 seconds x4)", 		items("SUGAR", 1, 28, "effect @a minecraft:speed 20 4"));
		control_panel.put("Weakness (30sec)", 				items("POTION", 1, 29, "effect @a minecraft:weakness 30"));
		control_panel.put("Slowness (30sec)", 				items("POTION", 1, 30, "effect @a minecraft:slowness 30"));
		control_panel.put("Blindness (30sec)", 				items("POTION", 1, 31, "effect @a minecraft:blindness 30"));
		control_panel.put("Absorption (30sec)", 				items("POTION", 1, 32, "effect @a minecraft:absorption 30"));
		control_panel.put("Invis (30sec)", 				items("POTION", 1, 33, "effect @a minecraft:invisibility 30"));
		
		control_panel.put("CLEAR ALL INVS", 				items("REDSTONE", 1, 34, "minecraft:clear @a"));
		createInvs();
		colorSwitchGUI();				
	}

	private ArrayList<Object> items(String mat, int amt, int slot, String Command) {
		try {
			Material.valueOf(mat);
			return new ArrayList<Object>(Arrays.asList(mat, amt, slot, Command));
		} catch (Exception e) {
			Util.consoleMSG("material: " + mat + " could not be found for EVNTMinigames!");
			return new ArrayList<Object>(Arrays.asList("NETHER_STAR", amt, slot, Command));
		}			
	}
	
	public void createInvs() {
		zonesinv = Bukkit.createInventory(null, rows, InvName);			
		for(String name : main_menu.keySet()) {				
			Material mat = Material.valueOf((String) main_menu.get(name).get(0));
			int amt = (int) main_menu.get(name).get(1);
			int slot = (int) main_menu.get(name).get(2);			
			createDisplay(zonesinv, mat, amt, slot, name, empty);
		}
		
		controlPanelInv = Bukkit.createInventory(null, rows, controlPanelName);			
		for(String name : control_panel.keySet()) {		
			try {
				Material mat = Material.valueOf((String) control_panel.get(name).get(0));
				int amt = (int) control_panel.get(name).get(1);
				int slot = (int) control_panel.get(name).get(2);				
				createDisplay(controlPanelInv, mat, amt, slot, name, empty);
			} catch (Exception e) {
				Util.consoleMSG("Material " + name + " is not valid!");
			}
			
		}
	}

	public static void colorSwitchGUI() {
		Set<String> keys = plugin.getConfig().getConfigurationSection("color").getKeys(false);			
		switchInv = Bukkit.createInventory(null, rows, switchInvName);				

		// add a default none border in gui 
		createDisplay(switchInv, Material.BARRIER, 1, 0, "&fnone", new ArrayList<String>());

		if(keys == null) {
			return;
		}
		
		int i = 1;
		for(String key : keys) { 
			
			// skips test key
			if(key.equalsIgnoreCase("testplaceholderdontuse")) {
				continue;
			}
			
			String name = "&f"+key;
			
			Material mat = Material.NETHER_STAR;			
			if(plugin.getConfig().contains("color."+key+".material")) {
				mat = Material.valueOf(plugin.getConfig().getString("color."+key+".material"));
			}
			
			Location l1 = Zones.getLocationFromConfig("color."+key+".loc1");
			Location l2 = Zones.getLocationFromConfig("color."+key+".loc2");
			int radius = zonesutil.getRadius(l1, l2);

			createDisplay(switchInv, mat, radius, i, name, new ArrayList<String>());
			i+=1;
		} 		
	}


	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack clicked = event.getCurrentItem(); 
		if (event.getInventory() == null || clicked == null || clicked.getType() == Material.AIR) {
			return;
		}

		Player player = (Player) event.getWhoClicked();
		String clickedName = event.getCurrentItem().getItemMeta().getDisplayName();
		
		// control panel - Items to give during event
		if(event.getView().getTitle().equalsIgnoreCase(controlPanelName)) {	
			event.setCancelled(true);
			
			if(control_panel.containsKey(clickedName)) {				
				Util.coloredBroadcast("&a&l[✓] &aGiven &f" + ChatColor.stripColor(clickedName));
				Util.console((String) control_panel.get(clickedName).get(3));				
			}
		}
		
		// main inv
		if(event.getView().getTitle().equalsIgnoreCase(InvName)) {	
			event.setCancelled(true);
			if(main_menu.containsKey(clickedName)) {
				player.performCommand((String) main_menu.get(clickedName).get(3));
			}			
		}	

		// switch zones location
		if(event.getView().getTitle().equalsIgnoreCase(switchInvName)) {
			event.setCancelled(true);
			player.closeInventory();
			player.performCommand("zone switch " + ChatColor.stripColor(clickedName));
		}
	}

	public static void createDisplay(Inventory inv, Material material, int numOfItems, int Slot, String name, List<String> list) {
		ArrayList<String> Lore = new ArrayList<String>();

		ItemStack item = new ItemStack(material, numOfItems);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(Util.color(name));

		String DefaultLoreColor = "&f";

		for(String l : list) {			
			if(!DefaultLoreColor.equalsIgnoreCase("")) {
				l = DefaultLoreColor + l;
			}						
			Lore.add(Util.color(l));
		}

		meta.setLore(Lore);
		item.setItemMeta(meta);

		inv.setItem(Slot, item); 

	}

}
