package sh.reece.voidgame;

// import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
// import org.bukkit.scheduler.BukkitRunnable;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;

public class zonesutil {	
	
	final ItemStack COMPASS = new ItemStack(Material.COMPASS);
	final ItemStack DIRT = new ItemStack(Material.DIRT, 64);
	
	public static Main plugin;
	public zonesutil(Main instance) {
		plugin = instance;
	}
	
	public static int getRadius(Location l1, Location l2) {
		return (l2.getBlockX()-l1.getBlockX())+1;
	}
	public static int getRadiusFromColor(String color) {		
		int l2 = Zones.getLocationFromConfig("color."+color+".loc2").getBlockX();
		int l1 = Zones.getLocationFromConfig("color."+color+".loc1").getBlockX();		
		return (l2-l1)+1;
	}
	
	public static void DealDamage(Player p) {
		if(p.getGameMode() == GameMode.SURVIVAL) {			
			p.damage(1);
			p.setFoodLevel(20);
			// playSound(p, "ENTITY_PLAYER_HURT");			
		} 
	}
	
	public static void playSound(Player p, String sound) {
		p.playSound(p.getLocation(), Sound.valueOf(sound), 1, 1);
	}
	
	public static void broadcastWinner(String name) {		
		if(Zones.isGameRunning) {
			// called from VoidPlayerDeath
			Bukkit.broadcast("ADMINS: Seems the zones winner was: " + name, Zones.Permission);
			Bukkit.broadcast("RUN: /zone winner " + name, Zones.Permission);
			Zones.StopEvent();
		}
	}
	
	public void giveCompassAndDirtToAlive() {
		// give player compass on border change if they do not have
		for(Player alivePlayer : Zones.alive) {
			if(!alivePlayer.getInventory().contains(Material.COMPASS)) {
				alivePlayer.getInventory().addItem(COMPASS);
			}
			if(!alivePlayer.getInventory().contains(Material.DIRT)) {
				alivePlayer.getInventory().addItem(DIRT);
			}
		}
	}
	
	
	public static void COMMANDS(Player p) {				
		Util.coloredMessage(p, " \n&b&n/zone&f <join, quit, alive, warpjail> \n ");
		
		if(isAdminPlayer(p)) {
			Util.coloredMessage(p, "&cADMIN COMMANDS:");
			
			Util.coloredMessage(p, " \n&f&lRUNNING EVENT");
			Util.coloredMessage(p, "&b&n/zone&f <start, stop, rules, setspawn, setjail>");
			Util.coloredMessage(p, "&b&n/zone&f <set, forcejoin, addall, stopchange>");
			Util.coloredMessage(p, "&b&n/zone&f <revive, tpdead, winner>");
			
			Util.coloredMessage(p, " \n&f&lTOGGLES");
			Util.coloredMessage(p, "&b&n/zone&f <togglepvp, togglefall>");
			Util.coloredMessage(p, "&b&n/zone&f <togglebuild, togglebreak>");
			Util.coloredMessage(p, "&b&n/zone&f <miny, maxy, speed>");
			
			Util.coloredMessage(p, " \n&f&lCONTROL PANELS");
			Util.coloredMessage(p, "&b&n/zone&f <controlpanel, switch>\n");
			
		}		
	}
	
	public static void teleportDeadToPlayer(Player p) {
		
		if(!isAdminPlayer(p)) {
			Util.coloredMessage(p, "&cYou are not an admin, and can not use this CMD.");
			return;
		}

		if(Zones.alive.size() == 0) {
			Util.coloredMessage(p, "&cThere is no one alive, so just tpall.");
			return;
		} else {
			Util.coloredMessage(p, "&aTeleporting dead players to you over 10 seconds...");
		}

		// for all online players
		for(Player target : Bukkit.getOnlinePlayers()) {

			// if target not a staffmember (we dont want to auto TP staff during dead tpall)
			if(!target.hasPermission("zones.staff")) {
				// if player not alive
				if(!Zones.alive.contains(target)) {					
					target.teleport(p.getLocation());
					target.sendMessage(Util.color("&c&l(!) Teleported all dead to " + p.getName()));																
				}
			}
		}
	}
	
	public static Boolean isAdminPlayer(Player p) {
		// checks player has admin permission
		if (p.hasPermission(Zones.Permission)) {
			return true;
		} 		
		return false;
	}
	
	public static void setLocToConfig(Player p, String key) {
		if(isAdminPlayer(p)) {  
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
	}
	
	
	
}
