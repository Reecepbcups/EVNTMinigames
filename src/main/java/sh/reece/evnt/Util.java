package sh.reece.evnt;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Strings;


public class Util {

	// server tools has a more up to date config file
	
	static ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

	private final static int CENTER_PX = 154;

	//Util.color(" &8[&r" + getProgressBar(TokensBal, tokensForReward, 40, '|', "&d", "&7") + "&8]  " + "&7&o((" + TokensBal + " / " + tokensForReward + "&7&o))");
	public String getProgressBar(int current, int max, int totalBars, char symbol, String string, String string2) {
		float percent = (float) current / max;
		int progressBars = (int) (totalBars * percent);
		return Strings.repeat("" + string + symbol, progressBars) + Strings.repeat("" + string2 + symbol, totalBars - progressBars);
	}

	// Not tested yet (2/7)
	public void fillSlots(Inventory inv, ItemStack item) {
		for (int i = 0; i < inv.getSize(); i++) {
			if (inv.getItem(i) == null || inv.getItem(i).getType().equals(Material.AIR))
				inv.setItem(i, item); 
		} 
	}
	
	public static boolean isVersion1_8() {
		// 1.8 uses: e.getInventory().getName()
		// 1.9+uses: e.getView().getTitle()
		return Bukkit.getServer().getClass().getPackage().getName().contains("1_8");
	}

	public static boolean cooldown(final HashMap<String, Date> CooldownHash, final Integer SecondCooldown, final String PlayerName, final String CooldownMessage) { 

		long CURRENT_TIME = new Date().getTime();

		if (CooldownHash.containsKey(PlayerName) && CooldownHash.get(PlayerName).getTime() >= CURRENT_TIME) {			
			Player p = Bukkit.getServer().getPlayer(PlayerName);
			Long timeLeft = ((CooldownHash.get(PlayerName).getTime() - CURRENT_TIME) / 1000);
			p.sendMessage(Util.color(CooldownMessage.replace("%timeleft%", timeLeft.toString())));
			//Util.console("bc return false. On cooldown");
			return false;

		} else {
			CooldownHash.remove(PlayerName); //Cooldown is over			
		}

		if(!(CooldownHash.containsKey(PlayerName))) {	
			long mil_cooldown = SecondCooldown * 1000;
			CooldownHash.put(PlayerName , new Date(CURRENT_TIME + mil_cooldown));	    	
		}

		return true;				

	}

	public int stringToInt(String value){return Integer.parseInt(value);}
	public String intToString(int value){return Integer.toString(value);}


	public static String color(String message) {return ChatColor.translateAlternateColorCodes('&', message);}
	
	public static void coloredMessage(Player player, String message) {		
		if(message.contains("\n")) {
			if(message.endsWith("\n")) {message+= " ";}
			
			for(String line : message.split("\n")) {
				player.sendMessage(color(line));
			}			
		} else {
			player.sendMessage(color(message));
		}
	}
	
	public static void coloredBroadcast(String msg) {Bukkit.broadcastMessage(Util.color(msg));}
	
	
	public static String UUIDToPlayername(String uuid){		
		if(uuid.length() == 36 && Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName() != null) {
			return Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName();
		}
		return null;
	}


	public static void console(String command) {
		Bukkit.dispatchCommand(console, command);
	}
	public static void consoleMSG(String consoleMsg) {Bukkit.getServer().getConsoleSender().sendMessage(Util.color(consoleMsg));}


	// item stuff
	public static ItemStack createItem(Material mat, int amt, int durability, String name) {
		ItemStack item = new ItemStack(mat, amt);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		//meta.setLore(lore); - List<String> lore
		if (durability != 0)
			item.setDurability((short)durability); 
		item.setItemMeta(meta);
		return item;
	}


	public static void remove(Inventory inv, Material mat, int amt) {
		int amount = 0;
		ItemStack[] arrayOfItemStack;
		int j = (arrayOfItemStack = inv.getContents()).length;
		for (int i = 0; i < j; i++) {
			ItemStack item = arrayOfItemStack[i];
			if (item != null && item.getType() == mat)
				amount += item.getAmount(); 
		} 
		inv.remove(mat);
		if (amount > amt)
			inv.addItem(new ItemStack[] { new ItemStack(mat, amount - amt) }); 
	}

	public static boolean isInt(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException numberFormatException) {
			return false;
		} 
	}

	public static int getTotalExperience(Player player) {
		int exp = Math.round(getExpAtLevel(player.getLevel()) * player.getExp());
		int currentLevel = player.getLevel();
		while (currentLevel > 0) {
			currentLevel--;
			exp += getExpAtLevel(currentLevel);
		} 
		if (exp < 0)
			exp = Integer.MAX_VALUE; 
		return exp;
	}

	public static int getExpAtLevel(int level) {
		if (level > 29)
			return 62 + (level - 30) * 7; 
		if (level > 15)
			return 17 + (level - 15) * 3; 
		return 17;
	}  




	public static boolean isArmour(Material m) {
		return Enchantment.PROTECTION_ENVIRONMENTAL.canEnchantItem(new ItemStack(m));
	}
	public static boolean isWeapon(Material m) {
		return Enchantment.DAMAGE_ALL.canEnchantItem(new ItemStack(m));
	}

	public static boolean isTool(Material m) {
		return Enchantment.DIG_SPEED.canEnchantItem(new ItemStack(m));
	}

	public static boolean isDiamond(Material m) {
		return m.toString().contains("DIAMOND");
	}

	public static boolean isGold(Material m) {
		return m.toString().contains("GOLD");
	}

	public static boolean isIron(Material m) {
		return m.toString().contains("IRON");
	}

	public static boolean isLeather(Material m) {
		return m.toString().contains("LEATHER");
	}

	public static boolean isChain(Material m) {
		return m.toString().contains("CHAIN");
	}

	public static boolean isSword(Material m) {
		return m.toString().contains("SWORD");
	}

	public static boolean isAxe(Material m) {
		return m.toString().endsWith("_AXE");
	}

	public static boolean isPickaxe(Material m) {
		return m.toString().contains("PICKAXE");
	}


}

