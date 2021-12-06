package sh.reece.infected;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;
import sh.reece.voidgame.Zones;

public class InfectedUTIL {

	public static Main plugin;
	public InfectedUTIL(Main instance) {
		plugin = instance;
	}
	
	
	public static void setLocToConfig(Player p, String key) {
		
		key = "infected."+key;
		
		//		if(isAdminPlayer(p)) {  
		int x, y, z;
		Location pL = p.getLocation();

		World w = pL.getWorld();
		x = (int)pL.getX();
		y = (int)pL.getY();
		z = (int)pL.getZ();		
		float yaw = pL.getYaw();
		float pitch = pL.getPitch();

		Zones.saveLocationToFile(key, w, x, y, z, yaw, pitch);			
		//Zones.spawn = pL;

		if(key.equalsIgnoreCase("jail")) {
			Zones.jail = pL;
		}
		if(key.equalsIgnoreCase("spawn")) {
			Zones.spawn = pL;
		}
		p.sendMessage("\nSet new "+key+": " +x+" "+y+" "+z);
	}
	
	
	public static Location getLocationFromConfig(String yaml_key) {	
		
		yaml_key = "infected."+yaml_key;
		
		if(!plugin.getConfig().contains(yaml_key)) {				
			Bukkit.broadcast("ADMINS: " + yaml_key + " not set in setAsInfected section!", Infected.permission);
			Bukkit.broadcast(Util.color("&7&oOnly players with "+Infected.permission+" can see this"), Infected.permission);
			return null;
		}

		
//		Util.consoleMSG("infected getConfig " + yaml_key);
		String[] loc = plugin.getConfig().getString(yaml_key).split(";");

		if(yaml_key.equalsIgnoreCase("spawn") || yaml_key.equalsIgnoreCase("jail")) {			
			return new Location(Bukkit.getWorld(loc[0]), 
					Double.parseDouble(loc[1]), 
					Double.parseDouble(loc[2])+0.1, 
					Double.parseDouble(loc[3]), 
					Float.parseFloat(loc[4]), 
					Float.parseFloat(loc[5]));
		}

		return new Location(Bukkit.getWorld(loc[0]), 
				Double.parseDouble(loc[1]), 
				Double.parseDouble(loc[2]), 
				Double.parseDouble(loc[3]));		
	}
	
	public static Boolean checkIfWehaveAWinner(Player removePlayer) {		
		Infected.removeAlive(removePlayer);
		if(Infected.getAlive().size() == 1) {
			announceWinner(Infected.getAlive().get(0).getName());
			return true;
		}
		return false;
	}

	// /zones winner IGN	
	public static void announceWinner(String name) {
		Util.coloredBroadcast(" \n    &7✧&f✧&8✧   &A&lINFECTED WINNER: &f&n" + name + "&r   &8✧&f✧&7✧\n ");

		// game stats
		// most kills
		Integer highestScore = null;
		String person = null;
		for (Player entry : InfectedEvents.KillCountStats.keySet()){
			int personsPoints = InfectedEvents.KillCountStats.get(entry);
			if (highestScore == null || personsPoints > highestScore){
				highestScore = personsPoints;
				person = entry.getName();
			}
		}

		if(person != null && highestScore != null) {
			Util.coloredBroadcast(" \n&fGame Stats: &f&n");
			Util.coloredBroadcast("    &7Most Kills: &f"+person+" &n"+highestScore+"&r");
		}
		Infected.STOP_GAME();

	}						
				
	
	public static ItemStack getColoredItem(ItemStack stack) {		
		LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();
	    meta.setColor(Color.LIME);
	    stack.setItemMeta(meta);		
	    stack.addUnsafeEnchantment(Enchantment.DURABILITY, 10);	
		return stack;
	}
	
	public static HashMap<Player, Location> playerLocs = new HashMap<Player, Location>();
	public static int runnableFrozenID;
	public static void freezeGame(Player p) {
		if(!Infected.isGameRunning()) {
			Util.coloredMessage(p, "infected is not running!");
			return;
		}
		
		if(Infected.isGameFrozen()) {
			Infected.setGameFrozen(false);
			Util.coloredBroadcast("&7[&a+&7] &aEveryone unfrozen!");					
			Bukkit.getScheduler().cancelTask(runnableFrozenID);
			playerLocs.clear();
			
		} else {
			Infected.setGameFrozen(true);
			Util.coloredBroadcast("&7[&c+&7] &cEveryone has been frozen in place!");	

			Infected.getAlive().stream().forEach(alive -> playerLocs.put(alive, alive.getLocation().getBlock().getLocation()));
			Infected.getInfected().stream().forEach(alive -> playerLocs.put(alive, alive.getLocation().getBlock().getLocation()));
			
			runnableFrozenID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {				
				@Override
				public void run() {
					for(Player pName : playerLocs.keySet()) {						
						if(pName.getLocation().distance(playerLocs.get(pName)) >= 2.0) {
							Util.coloredMessage(pName, "&c[!] Dont try moving, the game is frozen.");
							pName.teleport(playerLocs.get(pName));
						}
					}
				}
			}, 3*20L, 3*20L);
			
		}		
	}
	
	public static void removeFromFrozenList(Player p) {
		// remove player from list, used if a player dies while frozen somehow
		playerLocs.remove(p);
	}
	
	public static void resetPlayer(Player p) {
		
		// resets players inv, armour, walk, removes ingame things, and clears all
		// potion effects
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		p.setWalkSpeed((float) 0.2);	
		p.setGameMode(GameMode.SURVIVAL);
		p.setHealth(p.getMaxHealth());
		p.setFoodLevel(20);
		
		Infected.removeInfected(p);
		Infected.removeAlive(p);
		for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());		
	}
	

}
