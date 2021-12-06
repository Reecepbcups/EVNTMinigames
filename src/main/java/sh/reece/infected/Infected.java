package sh.reece.infected;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;

public class Infected {
	// Infected - Deathrun 2.0
	
	/*
	 * Description: The classic Infected/Deathdash event 
	 * where all players are let loose on a big map and a 
	 * killer tries to kill people until the last one standing wins,
	 *  but with a **twist**. If you die, you will respawn as a death/infedcetd 
	 */
	
	private static List<Player> alive = new ArrayList<Player>();
	private static List<Player> infected = new ArrayList<Player>();
	
	// when player dies from alive, they are put here for next respawn.then cleared
	private static List<Player> transitionQueue = new ArrayList<Player>();
	
	private static Main plugin;
	
	private static Location jail, spawn;
	
	private static boolean queueOpen = false;
	private static boolean gameRunning = false;
	private static boolean gameFrozen = false;

	private static int queueDelay = 5;
	private static int rulesDelay = 0;
	private static int rulesID;
	private static Random r;
	
	public static String permission = "infected.staff";
	
	public Infected(Main instance) {
		plugin = instance;	
		
		new InfectedUTIL(plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new InfectedEvents(plugin), plugin);			
		instance.getCommand("infected").setExecutor(new InfectedCMDs(plugin));
		Bukkit.getServer().getPluginManager().registerEvents(new InfectedGUI(plugin), plugin);
		
		setJail(InfectedUTIL.getLocationFromConfig("jail"));
		setSpawn(InfectedUTIL.getLocationFromConfig("spawn"));	
		
		r = new Random();
		
		queueDelay = plugin.getConfig().getInt("infected.queuetime");
		if(!Util.isInt(queueDelay+"")) {
			queueDelay = 30;
		}
		
		
	}
	
	
	
	// /infected join
	// actions = "JOIN", "LEAVE"
	public static void gameAction(Player p, String action) {
		//p.getInventory().clear();
		//p.getInventory().setContents(null);		
		switch (action.toUpperCase()) {
		case "JOIN":			
			if(!isQueueOpen()) {
				Util.coloredMessage(p, "&c[&7!&c] The Infected queue is not open!");
				return;
			}	
			
			p.setHealth(p.getMaxHealth());
			if(alive.contains(p)) {
				Util.coloredMessage(p, "&cYou already joined the queue!");
			} else {
				alive.add(p);
				Util.coloredBroadcast("&7[&2&l+&7] &a&o"+p.getName()+" has joined the queue. &7&o(("+alive.size()+"&7&o))");
				tpToSpawn(p);
			}
			break;
			
		case "LEAVE":			
			if(!isGameRunning()) {
				Util.coloredMessage(p, "&c[&7!&c] The Infected game is not running!");
				return;
			}		
			
			alive.remove(p);
			infected.remove(p);
			p.setHealth(p.getMaxHealth());
			Util.coloredMessage(p, "&7[&c&l+&7] &c&oRemoved from the Infected Game" + alive.size());	
			tpToJail(p);
			break;
			
		default:
			break;
		}
	}
	
	
	// /infected start
	public static void START_GAME(Player p) {		
		if(isGameRunning() || isQueueOpen()) {
			Util.coloredMessage(p, "&c[!] There is a game already running OR Queue is running in prep for event!");
			return;
		}		
		
		Util.coloredBroadcast("\n&c&lINFECTED EVENT STARTING IN &4&l&n"+queueDelay+"&c&l seconds&7.");
		Util.coloredBroadcast("         &7&o(( Join via &f&n/infected join&7&o ))\n ");	
		
		// starts queue wait
		setQueueOn(true);
			
		//Util.console("minecraft:clear @a");	-- done when players JOIN the game
		//Util.console("heal *");	
		
		// where ~ is X and Z of getSpawn();
		// /spreadplayers ~ ~ 10 15 false
		
		new BukkitRunnable() {			
			@Override
			public void run() {
				setGameRunning(true);
				setQueueOn(false);	
				
				Util.coloredBroadcast("\n       &8&k*&7&k*&f&k* &c&l&nINFECTED MINIGAME STARTED&r &f&k*&7&k*&8&k*\n&7&o(( &f&oFirst player has been set as an infected &7&o))\n ");								
				randomlySelectInfected();
				// maybe we do a delay here and send big red titles
				// of who is the infected, and give a countdown for when they get released	
			}
		}.runTaskLater(plugin, queueDelay*20L);						
	}
	

	
	public static void STOP_GAME() {
		setQueueOn(false);
		setGameRunning(false);
		setGameFrozen(false);
		
		alive.clear();
		infected.clear();
		Bukkit.getOnlinePlayers().stream().forEach(player ->InfectedUTIL.resetPlayer(player));		
		//Util.console("minecraft:clear @a");	// done ^^ there now		
		
		Bukkit.getScheduler().cancelTask(InfectedUTIL.runnableFrozenID);
		InfectedUTIL.playerLocs.clear();

		if(isGameRunning()) {
			Util.coloredBroadcast("&4&l&n[!]&c&l The Infected Event has ended");
		} 
	}
	
	
	public static void randomlySelectInfected() {
		
		Player infectedPlayer = alive.get(r.nextInt(alive.size()-1));
		setAsInfected(infectedPlayer);			
	}
	

	// sets and revives players based on t/f toggle
	public static void revivePlayer(Player p, String type, Boolean teleport) {
		// where type "alive", "Infected"
		p.getInventory().clear();		
		
		switch (type.toLowerCase()) {
		case "alive":
		case "a":
			InfectedUTIL.resetPlayer(p);
			alive.add(p);
			infected.remove(p); // bug fix for when setting player back
			
			// used when reviving a player, otherwise its false to set
			if(teleport) {
				p.teleport(getSpawn());
				Util.coloredMessage(p, "&aREVIVED AS ALIVE! &7- &fTeleported to spawn");
			} else {
				Util.coloredMessage(p, "&aYou have been set as alive by a staff!");
			}
			
			
			break;
		case "infected":
		case "infect":
		case "i":
			InfectedUTIL.resetPlayer(p);
			setAsInfected(p);
			
			if(teleport) {
				p.teleport(getSpawn());
				Util.coloredMessage(p, "&cREVIVED AS AN INFECTED! &7- &fTeleported to spawn");
				
				if(isGameFrozen()) {
					InfectedUTIL.playerLocs.put(p, getSpawn());
				}
				
			} else {
				Util.coloredMessage(p, "&cYou have been set to a Infected by a staff!");
			}
			break;

		default:
			break;
		}
	}
	
	public static void forceJoin(Player p, String type) {
		// where type "alive", "infecterd" - allows staff to play if they join late
		
		switch (type.toLowerCase()) {
		case "alive":
		case "a":
			InfectedUTIL.resetPlayer(p);
			alive.add(p);			
			tpToSpawn(p);
			
			break;
		case "infected":
		case "infect":
		case "i":
			InfectedUTIL.resetPlayer(p);
			setAsInfected(p);
			tpToSpawn(p);
			break;
		default:
			break;
		}
		
	}
	
	
	public static void setAsInfected(Player p) {
		// remove from Alive list
		// add to infected list
		InfectedUTIL.resetPlayer(p);
		
		// removes player from alive, and adds to infected
		if(alive.contains(p)) {
			alive.remove(p);
		}
		if(!infected.contains(p)) {
			infected.add(p);
		}

		// https://bukkit.org/threads/setting-armor.235947/
				
		
		ItemStack boots = InfectedUTIL.getColoredItem(new ItemStack(Material.LEATHER_BOOTS));
		ItemStack leggings = InfectedUTIL.getColoredItem(new ItemStack(Material.LEATHER_LEGGINGS));
		ItemStack chest = InfectedUTIL.getColoredItem(new ItemStack(Material.LEATHER_CHESTPLATE));
		ItemStack helm = InfectedUTIL.getColoredItem(new ItemStack(Material.LEATHER_HELMET));		
		p.getInventory().setBoots(boots);
		p.getInventory().setLeggings(leggings);
		p.getInventory().setChestplate(chest);
		p.getInventory().setHelmet(helm);
		
		ItemStack sword = new ItemStack(Material.valueOf("WOOD_SWORD"));
		ItemMeta itemMeta = sword.getItemMeta();
		//itemMeta.spigot().setUnbreakable(true);
		itemMeta.setDisplayName(Util.color("&8✦&c&lINFECTED MATERIAL&8✦"));
		sword.setItemMeta(itemMeta);
		
		p.getInventory().setItemInHand(sword);
		p.setWalkSpeed((float) 0.15);
		
		// used for PlayerRespawnEvent from alive -> infected. 
		Infected.removeFromTransitionQueue(p);
		Util.coloredMessage(p, "&4[!] &c&lYOU HAVE BEEN INFECTED!!!");
		Util.coloredMessage(p, "&7&o(( Make sure to attack alive players ))");
		
		//Util.coloredBroadcast("&7[&4!&7] &c&l" +infectedPlayer.getName()+" has been infected!");
		
		
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {		
				p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 2));
				//p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, Integer.MAX_VALUE, 1));
				p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
			}
		}, 10L);
	}
	

	
	
	// infected rules (admin)
	public static void broadcastRules() {
		String[] messages = {
		" \n&7✧&f✧&8✧ &f&lINFECTERD EVENT IS ABOUT TO BEGIN! &7✧&f✧&8✧",

		"&7[&f&l1&7] &f&lYou will be given a limited set of materials, you must scavenge to stay alive",
		"&7[&f&l2&7] &f&lAs players die, you will be turned Infected yourself",
		"&7[&f&l3&7] &f&lAs a mutant, you have slowness, but make up for it in strength",
		"&7[&f&l4&7] &c&lIf you die as an Infectant, you must wait for a revival to come back",
		
		"&7[&f&l5&7] &a&lThe top alive will be rewarded as per our discord announcement",
		"&7[&f&l6&7] &a&lThe Infector with the most kills will also be rewarded",
		" \n&7[&f&l!&7] &a&lBest of luck!",
		};
		
		rulesID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() { 
				if(rulesDelay>=messages.length){
					Bukkit.getScheduler().cancelTask(rulesID);
					rulesDelay = 0;
					return;
				} else {
					Util.coloredBroadcast(messages[rulesDelay]  + "\n ");
					rulesDelay+=1;
				}
			}
		}, 0L,  160L);
	}
	
	public static void tpToJail(Player p) {
		p.teleport(getJail());
	}
	
	public static void tpToSpawn(Player p) {
		// has a 3 second delay so its not spamming / crashing
		Util.coloredMessage(p, "&4(!) Teleporting to spawn... &7&o(Up to 3 seconds)");			
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {		
				p.teleport(getSpawn());	
			}
		},r.nextInt(3)*20L);
		
	}
	
	public static List<Player> getAlive() {
		return alive;
	}

	public static List<Player> getInfected() {
		return infected;
	}

	public static boolean isQueueOpen() {
		return queueOpen;
	}

	public static void setAlive(List<Player> alive) {
		Infected.alive = alive;
	}

	public static void setInfected(List<Player> infected) {
		Infected.infected = infected;
	}

	public static void setQueueOn(boolean QueueBool) {
		queueOpen = QueueBool;
	}
	
	public static boolean isGameRunning() {
		return gameRunning;
	}

	public static void setGameRunning(boolean gameRunning) {
		Infected.gameRunning = gameRunning;
	}
	
	// might have to add an if contains statement
	public static void removeInfected(Player infected) {
		Infected.infected.remove(infected);
	}
	public static void removeAlive(Player player) {
		Infected.alive.remove(player);
	}
	
	
	public static Boolean isPlayerStaff(Player p) {
		return p.hasPermission(permission);
	}

	public static Location getJail() {
		return jail;
	}
	public static void setJail(Location jail) {
		Infected.jail = jail;
	}

	public static Location getSpawn() {
		return spawn;
	}
	public static void setSpawn(Location spawn) {
		Infected.spawn = spawn;
	}


	public static boolean isGameFrozen() {
		return gameFrozen;
	}
	public static void setGameFrozen(boolean gameFrozen) {
		Infected.gameFrozen = gameFrozen;
	}


	public static List<Player> getTransitionQueue() {
		return transitionQueue;
	}
//	public static void setTransitionQueue(List<Player> transitionQueue) {
//		Infected.transitionQueue = transitionQueue;
//	}
	public static void addToTransitionQueue(Player p) {
		Infected.transitionQueue.add(p);
	}
	public static void removeFromTransitionQueue(Player player) {
		Infected.transitionQueue.remove(player);
	}
}
