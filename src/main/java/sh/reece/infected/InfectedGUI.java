package sh.reece.infected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

public class InfectedGUI implements Listener {

	//private static Main plugin;
	public static Inventory control;
	public static String CPName;
	public static int rows;
	
	// all items which we give during the game
	private static HashMap<String, List<Object>> control_panel =
			new HashMap<String, List<Object>>();
	private final List<String> EMPTY = new ArrayList<String>();
	
	public InfectedGUI(Main instance) {
		//plugin = instance;
		
		rows = 6*9;
		CPName = Util.color("&lInfected Control");		
		
		control_panel.put("64x Wool", items("WOOL", 1, 0, "giveall wool 64"));
		control_panel.put("1x Bow", items("BOW", 1, 1, "giveall bow 1"));
		control_panel.put("8x Arrow", items("ARROW", 1, 2, "giveall arrow 8"));
		control_panel.put("1x Web", items("WEB", 1, 3, "giveall cobweb 1"));
		control_panel.put("1x Wooden Sword", items("WOOD_SWORD", 1, 4, "giveall woodensword 1"));
		control_panel.put("1x Stone Sword", items("STONE_SWORD", 1, 5, "giveall woodensword 1"));
		control_panel.put("1x Wooden Axe", items("WOOD_AXE", 1, 6, "giveall woodenaxe 1"));
		control_panel.put("1x Gold Axe", items("GOLD_AXE", 1, 7, "giveall gold_AXE 1"));
		control_panel.put("1x Golden Apple", items("GOLDEN_APPLE", 1, 8, "giveall golden_apple 1"));		
		control_panel.put("1x Instant Healing potion",items("POTION", 1, 9, "giveall healingpotion 1"));
		
		control_panel.put("Jump Boost (20 seconds x2)", items("RABBIT_FOOT", 1, 18, "effect @a minecraft:jump_boost 20 2"));
		control_panel.put("Speed (20 seconds x4)", 		items("SUGAR", 1, 19, "effect @a minecraft:speed 20 4"));
		
		control_panel.put("CLEAR ALL INVS", items("REDSTONE_BLOCK", 1, 35, "infected clearaliveinv"));
		
		createControlInv();
		
	}
	
	private ArrayList<Object> items(String mat, int amt, int slot, String Command) {
		try {
			return new ArrayList<Object>(Arrays.asList(mat, amt, slot, Command));
		} catch (Exception e) {
			Util.consoleMSG("material: " + mat + " could not be found for EVNTMinigames!");
			return new ArrayList<Object>(Arrays.asList("NETHER_STAR", amt, slot, Command));
		}			
	}
	
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		ItemStack clicked = event.getCurrentItem(); 
		if (event.getInventory() == null || clicked == null || clicked.getType() == Material.AIR) {
			return;
		}

		//Player player = (Player) event.getWhoClicked();
		String clickedName = event.getCurrentItem().getItemMeta().getDisplayName();
		
		// control panel - Items to give during event
		if(event.getView().getTitle().equalsIgnoreCase(CPName)) {	
			event.setCancelled(true);
			
			if(control_panel.containsKey(clickedName)) {				
				Util.coloredBroadcast("&a&l[✓] &aGiven &f" + ChatColor.stripColor(clickedName));
				Util.console((String) control_panel.get(clickedName).get(3));				
			}
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
	
	public void createControlInv() {
		control = Bukkit.createInventory(null, rows, CPName);			
		for(String name : control_panel.keySet()) {		
			try {
				Material mat = Material.valueOf((String) control_panel.get(name).get(0));
				int amt = (int) control_panel.get(name).get(1);
				int slot = (int) control_panel.get(name).get(2);				
				createDisplay(control, mat, amt, slot, name, EMPTY);
			} catch (Exception e) {
				Util.consoleMSG("Material " + name + " is not valid!");
			}
			
		}
	}
	
	
}
