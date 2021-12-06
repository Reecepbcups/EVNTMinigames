package sh.reece.voidgame;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
// import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
// import org.bukkit.inventory.ItemStack;
// import org.bukkit.scheduler.BukkitRunnable;
// import org.bukkit.scheduler.BukkitScheduler;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;

public class Zones implements Listener, CommandExecutor {

	public static Main plugin;
	//private FileConfiguration MainConfig, config;
	public static String Permission; //, FILENAME;
	//public static WorldBorderApi border;

	public static Location jail, spawn;

	public static Boolean isGameRunning, isQueueRunning;
	private static Boolean isChangingBorders;
	public static ArrayList<Player> alive, queue;
	public static ArrayList<Location> placedBlocks;



	public static int delayBorderDamageTime; // delayBorderDamage
	public static int teleportSyncCooldown;
	public static int checkUserInBorderRunnableID, startGameRunnableID, borderShowID, nextBorder, 
	delayedDamageBorder, broadcastingRulesRunnable, rulesRunnableIdx;
	
	private static Location currentBorderLocation; // used for when reviving someone

	public static HashMap<String, Location> settingColorLocsForConfig;
	public static Random random;
	public static String CurrentColor;
	private int queuetime;
	public static Boolean pvpAllowDeny, falldmgAllowDeny;

	
	// time to repeat border change speed
	public int repeatTime;
	
	// zone automate? seperate area maybe?
	
	// On Creation of Particles, get the top Y values in all of those blocks which are not air.
	// then create particles only above those Y values for the same X and Z locations on creation. 
	// helps reduce particle output

	public Zones(Main instance) {
		plugin = instance;
		new ZonesGUI(instance);			
		
		plugin.getCommand("zone").setExecutor(this);		
		
		// check if this is null if it is do not allow game to start
		spawn = getLocationFromConfig("spawn");
		// this is the inital spawn jail (( also used in train))
		jail = getLocationFromConfig("jail");

		queuetime = plugin.getConfig().getInt("zones.queuetime");
		Permission = "zones.use";
		repeatTime = plugin.getConfig().getInt("particles.repeatTime");
		
		queue = new ArrayList<Player>();
		alive = new ArrayList<Player>();

		isGameRunning = false;
		isQueueRunning = false;
		isChangingBorders = false;

		random = new Random();

		CurrentColor="none";

		placedBlocks = new ArrayList<Location>();
		settingColorLocsForConfig = new HashMap<String, Location>();

		// loads all key colors into location List for use (N vs N^2).
		// called bc it gets config from plugin, and gets all keys
		new BorderUTIL(plugin);

		// handles all event related items for queues and such
		new zoneEvents(plugin);

	}
	
	
	

	public void showParticalBorder(String color) { // where color is the thing in the config
		
//		BorderUTIL.makeRedGlassAbove(color);	
		borderShowID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {               
				//BorderUTIL.drawBox(l1, l2, radius);				
				BorderUTIL.drawBox(color);
				
			}			
		}, 0*20L, repeatTime*20L);
	}


	public static void stopShowingBorder() {
		Bukkit.getScheduler().cancelTask(borderShowID);
	}

	
	// sends message to players, border changing to COLOR in 30 seconds.
	public void changeBorderLocation(String color) {
		// GUI, click yellow. sign for number of how quickly (( default 30))
		int delay = 0;
		if(!isGameRunning) { return; }

		// if previous color was something, set that area to barriers first
		if(CurrentColor != "none") {
			BorderUTIL.setBlocksAt255(CurrentColor, Material.BARRIER);
		}
		
		// set new global current color
		CurrentColor = color;
		stopShowingBorder();

		// stop border runnable
		if(color.toLowerCase().equalsIgnoreCase("none")) {	
			Util.coloredBroadcast("\n&c&l[!] Zone has been removed!\n ");
			return;
		}
		
		Location l1 = getLocationFromConfig("color."+color+".loc1");
		Location l2 = getLocationFromConfig("color."+color+".loc2");
		if(l1 == null) {
			return;
		}

		isChangingBorders = true;
		//int radius = zonesutil.getRadius(l1, l2);

		// sets new border to have glass above at 255
		if(Util.isVersion1_8()) {
			BorderUTIL.setBlocksAt255(CurrentColor, Material.valueOf("STAINED_GLASS_PANE"));
		} else {
			BorderUTIL.setBlocksAt255(CurrentColor, Material.RED_STAINED_GLASS_PANE);
		}
				
		
		int xMidpoint = (l1.getBlockX()+l2.getBlockX())/2;
		int zMidpoint = (l1.getBlockZ()+l2.getBlockZ())/2;

		
		
		// resets their border to none. They can roam the entire map
		nextBorder = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {

				// stop precious border before making new one
				Bukkit.getScheduler().cancelTask(borderShowID);	

				// start new border for server
				// showParticalBorder(l1, l2, radius); // old
				showParticalBorder(color);
				
				// currentBorder used as a check when reviving players
				currentBorderLocation = new Location(l1.getWorld(), xMidpoint, 0, zMidpoint);
				//world.strikeLightningEffect(l1.getWorld());
				
				String title1 = Util.color("&bNEXT ZONE");
				String title2 = Util.color(color);
				alive.stream().forEach(p -> p.sendTitle(title1, title2));
				
				String zoneLoc = "\n      &b&l[&k&l!&b&l] &f&lZONE &b&lX: " + xMidpoint + " Z: " + zMidpoint + "\n                " + color;
				
				alive.forEach(p -> p.setCompassTarget(currentBorderLocation));
				Util.coloredBroadcast(zoneLoc);
				
				//Util.coloredMessage(p, "&7&o(( Damage will begin in 45 Seconds ))"); // moved to delayBorderDamage				
				delayBorderDamage();
			}
		}, delay*20L);
	}


	public void delayBorderDamage() {
		// Called by "changeBorderLocation"
		delayBorderDamageTime = 60;

		delayedDamageBorder = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {

				if(delayBorderDamageTime <= 0) {
					isChangingBorders=false;  					
					alive.forEach(p -> Util.coloredMessage(p, "&b&l[&k&l!&b&l] &f&lYou will now start taking damage"));
					
					
					Bukkit.getScheduler().cancelTask(delayedDamageBorder);	
					return;
				}
				
				alive.forEach(p -> Util.coloredMessage(p, "    &7&o(( Damage begins in "+delayBorderDamageTime+" Seconds ))"));
				delayBorderDamageTime-=10;

			}
			// repeat dmg every 10 sec
		}, 0, 10*20L);
	}
	
	public void stopBorderChange(Player whoStopped) {
		
		if(zonesutil.isAdminPlayer(whoStopped)) {
			isChangingBorders=false;
			Bukkit.getScheduler().cancelTask(delayedDamageBorder);
			
			Util.coloredBroadcast("&c&l[&k&l!&c&l] "+whoStopped.getName()+" &f&lStopped the zone change");
			
			changeBorderLocation("none");
		}
	}

	// optomised this
	public static Boolean isPlayerInsideTheirBorder(Player p, Location loc1, Location loc2) {
		if(!isGameRunning) {return true;} 

				
		// if there is no border, do NOT damage
		if(CurrentColor.toLowerCase().equalsIgnoreCase("none")) {
			//Util.consoleMSG("[isPlayerInsideTheirBorder] Current Color: " + CurrentColor);
			return true;
		} 
		
		if(isChangingBorders) { // if we are on cooldown as we change borders,
			// dont harm them!
			return true;
		}

		// one or both values are not set in config
		if(loc1 == null || loc2 == null) {
			return true;
		}
		
		// get players X Z coords
		int px = p.getLocation().getBlockX();
		int pz = p.getLocation().getBlockZ();

		// if player is between both block locations // greater than 6, less than 8  
		if((px >= loc1.getBlockX() && px <= loc2.getBlockX()) && (pz >= loc1.getBlockZ() && pz <= loc2.getBlockZ())) {        				
			return true;  //p.sendMessage("You are safe!");      	
		} else {    			
			return false; // if false => deal damage
		}

	}



	public static void removePlayerFromGame(Player p) {
		// removes player from game, then broadcast to all players left in	
		if(!isGameRunning) {return;}		

		if(alive.contains(p)) {
			alive.remove(p);
			p.getInventory().clear();
			
			Util.coloredBroadcast("&c[&4-&c] &n"+p.getName()+"&c has died. "+alive.size()+" remain!");
			
		}		
	}

	public void initQueueStart() {	
		isGameRunning = true;
		// when this is true, do not allow block placement
		isQueueRunning = true;

		Util.coloredBroadcast("\n&b&lZONE EVENT STARTING IN &3&l&n"+queuetime+"&b&l seconds.");
		Util.coloredBroadcast("       &7&o(( Join via &a&n/zone join&7&o ))\n ");	

		// clear any alive from previously
		alive.clear();
		queue.clear();
		placedBlocks.clear();

		startGameRunnableID = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			@Override
			public void run(){
				START_GAME();
			}}, queuetime*20L);
	}
	public void START_GAME() {	
		int secondsPerHeartTake = plugin.getConfig().getInt("zones.secondsPerHeartTake");
		
		int gracePeriod = plugin.getConfig().getInt("zones.gracePeriod");
		isQueueRunning = false;
		isChangingBorders=false;
		teleportSyncCooldown=0; // used in the Teleport runnable
		
		for(Player p : queue) {				
			alive.add(p); // dont clear QUEUE it breaks here.

			Util.coloredMessage(p, "\n     &9&k*&b&k*&9&k* &b&l&nZONE MINIGAME STARTED&r &9&k*&b&k*&9&k*\n&7&o(( no border yet, you will not take damage ))\n ");	

			if(gracePeriod != 0) 
				Util.coloredMessage(p, "\n&c&oGrace period: &c&n" + gracePeriod/20 + " seconds");	
	
			p.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.DIRT, 64));						
			p.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.COMPASS, 1));
			p.setCompassTarget(p.getLocation());

			p.setHealth(20.0);
			p.setFoodLevel(20);
		}
		// could maybe clear queue here, but really not needed TBH

		// After starting grace period, this runs every "secondsPerHeartTake" seconds
		checkUserInBorderRunnableID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() {  

				// get locations from config to check if player is inside
				Location loc1 = getLocationFromConfig("color."+CurrentColor+".loc1");
				
				// if location 1 is not null, then we can get Location 2 and loop through all people
				// using cached locations
				if(loc1 != null) {
					Location loc2 = getLocationFromConfig("color."+CurrentColor+".loc2");
					
					for(Player p : alive) {					
						if(isPlayerInsideTheirBorder(p, loc1, loc2) == false && isChangingBorders==false) {
							zonesutil.DealDamage(p);
						}         		            		
					}
				} else {
					Util.consoleMSG("Location 1 is null from getLocationFromConfig. Color: " + CurrentColor);
				}
				
				
			}
		}, gracePeriod*20L, secondsPerHeartTake*20L);


				
	}


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {				
		Player p = (Player) sender;

		if (args.length == 0) {
			// if no args && player has admin perm, open GUI

			if(zonesutil.isAdminPlayer(p)) {
				p.openInventory(ZonesGUI.zonesinv);
				return true;
			}			
			zonesutil.COMMANDS(p);
			return true;
		}

		//player commands
		switch(args[0].toLowerCase()){ // void args[0]
		case "join":
			JoinQueue(p);
			return true;
		case "leave":
		case "quit":
			removePlayerFromGame(p);
			return true;
		case "warpjail":
			p.teleport(jail);
			Util.coloredMessage(p, "&a&l[!] &fTeleported to the Jail");
			return true;
		case "alive":
			String msg = alive.size() + " ";
			for(Player play : alive) {
				msg+=play.getDisplayName() + ", ";
			}
			Util.coloredMessage(p, msg);
			return true;
		}

		// admin commands
		switch(args[0].toLowerCase()){		
		case "start":
			if(spawn == null) {
				Util.coloredMessage(p, "&cSpawn is not set, /zone setspawn");
				return true;
			}
			adminStartEvent(p);	
			return true;
		case "stop":
			adminStopEvent(p);
			return true;
			
		case "maxy":
			if(zonesutil.isAdminPlayer(p)) {
				if(args.length==2) {
					if(Util.isInt(args[1])) {
						BorderUTIL.setMaxY(Integer.valueOf(args[1]));
					} else {
						Util.coloredMessage(p, args[1]+" is not a number!");
					}
				} else {
					Util.coloredMessage(p, "/zones maxy <y value>");
				}
			}
			return true;
		case "miny":
			if(zonesutil.isAdminPlayer(p)) {
				if(args.length==2) {
					if(Util.isInt(args[1])) {
						BorderUTIL.setMinY(Integer.valueOf(args[1]));
					} else {
						Util.coloredMessage(p, args[1]+" is not a number!");
					}
				} else {
					Util.coloredMessage(p, "/zones miny <y value>");
				}
			}
			return true;
			
		case "repeat":
		case "speed":
			if(zonesutil.isAdminPlayer(p)) {
				if(args.length==2) {
					if(Util.isInt(args[1])) {
						repeatTime = (Integer.valueOf(args[1]));
						Util.coloredMessage(p, "Set speed for border to " + args[1]);
						showParticalBorder(CurrentColor);
					} else {
						Util.coloredMessage(p, args[1]+" is not a number!");
					}
				} else {
					Util.coloredMessage(p, "/zones speed <seconds>");
				}
			}
			return true;
			
		case "stopchange":
			stopBorderChange(p);
			return true;

		case "sound":
			if(args.length!=2) {
				p.sendMessage("Usage: /zones sound SOUND");
				return true;
			}
			zonesutil.playSound(p, args[1]);
			return true;	

		case "controlpanel":	
		case "cp":
			if(zonesutil.isAdminPlayer(p)) {
				ZonesGUI.colorSwitchGUI();
				p.openInventory(ZonesGUI.controlPanelInv);
			}
			return true;

		case "togglepvp":	
			if(zonesutil.isAdminPlayer(p)) {
				String toDo = "rg flag zones pvp ";
				if(pvpAllowDeny == null || pvpAllowDeny == false) {
					pvpAllowDeny = true;
					toDo += "allow";												
					Util.coloredBroadcast("\n&2[!] &aPVP has been enabled in the arena!");
					
				} else {
					pvpAllowDeny = false;
					toDo += "deny";		
					Util.coloredBroadcast("\n&4[!] &cPVP has been disabled in the arena!");
				}				
				p.performCommand(toDo);
			}
			return true;

		case "togglefall":	
		case "togglefalldamage":
			if(zonesutil.isAdminPlayer(p)) {
				String toDo = "rg flag zones fall-damage ";
				if(falldmgAllowDeny == null || falldmgAllowDeny == false) {
					falldmgAllowDeny = true;
					toDo += "allow";									
					Util.coloredBroadcast("\n&2[!] &aFall Damage has been enabled in the arena!");
				} else {
					falldmgAllowDeny = false;
					toDo += "deny";					
					Util.coloredBroadcast("\n&4[!] &cFall Damage has been disabled in the arena!");
				}				
				p.performCommand(toDo);
			}
			return true;
			
		case "togglebuild":	 
		case "togglebuilding":
			if(zonesutil.isAdminPlayer(p)) {
				String toDo = "rg flag zones block-place ";
				if(falldmgAllowDeny == null || falldmgAllowDeny == false) {
					falldmgAllowDeny = true;
					toDo += "allow";	
					Util.coloredBroadcast("\n&2[!] &aBuilding has been enabled in the area!");
				} else {
					falldmgAllowDeny = false;
					toDo += "deny";					
					Util.coloredBroadcast("\n&4[!] &cBuilding has been disabled in the area!");
				}				
				p.performCommand(toDo);
			}
			return true;
			
		case "togglebreak":	
		case "togglebreaking":
			if(zonesutil.isAdminPlayer(p)) {
				String toDo = "rg flag zones block-break ";
				if(falldmgAllowDeny == null || falldmgAllowDeny == false) {
					falldmgAllowDeny = true;
					toDo += "allow";		
					Util.coloredBroadcast("\n&4[!] &cBreaking blocks has been enabled in the area!");
				} else {
					falldmgAllowDeny = false;
					toDo += "deny";					
					Util.coloredBroadcast("\n&4[!] &cBreaking blocks has been disabled in the area!");
				}				
				p.performCommand(toDo);
			}
			return true;

		case "tpdead":
			zonesutil.teleportDeadToPlayer(p);
			return true;			
			
		case "set":
		case "setloc":
			if(!(args.length >= 2)) {
				p.sendMessage("/zone set COLOR");
			} else {
				setConfigColorLocations(args[1], p);
			}
			return true;

		case "delete":
			if(!(args.length >= 2)) {
				p.sendMessage("/zone delete COLOR");
			} else {
				
				if(plugin.getConfig().contains("color."+args[1])) {
					plugin.getConfig().set("color."+args[1], null);
					//BorderUTIL.borders.remove(args[1]);
					plugin.saveConfig();
				}
			}
			return true;	
			
		case "rules":
			// rules of game to all online
			if(zonesutil.isAdminPlayer(p)) {
				broadcastRules();
			}			
			return true;	

		case "change":	
		case "switch":	
		case "switchinv":
			if(!(args.length >= 2)) {
				ZonesGUI.colorSwitchGUI();
				p.openInventory(ZonesGUI.switchInv);
				return true;
			}			
			if(isChangingBorders == true || isQueueRunning == true) {	
				// add this
				p.sendMessage("You can not change borders while its changing or in queue");
				return true;
			}
			if(CurrentColor.equalsIgnoreCase(args[1])) {
				p.sendMessage("The border is already: "+args[1]);
				return true;
			}

			changeBorderLocation(args[1]);	
			return true;	

		case "winner":	
		case "broadcastwinner":	
			// /zones winner IGN
			if(zonesutil.isAdminPlayer(p)) {
				if(args.length == 2) {					
					Util.coloredBroadcast(" \n    &3✧&b✧&3✧   &b&lZONE WINNER: &f&n" + args[1] + "&r   &3✧&b✧&3✧\n ");
				} else {
					Util.coloredMessage(p, "/zones winner IGN");
				}		
				return true;	
			}
			
		case "forcejoin":
			forceJoin(p);				
			return true;

		case "revive":
			if(args.length < 2) {
				Util.coloredMessage(p, "&c/zone revive <player>");
				return true;
			}
			revivePlayer(p, args[1]);				
			return true;

		case "gui":
			openGUI(p);	
			return true;



		case "reload":
			p.performCommand("plugman reload "+plugin.getDescription().getName());
			return true;

		case "setspawn":			
			zonesutil.setLocToConfig(p, "spawn");
			return true;
		case "setjail":			
			zonesutil.setLocToConfig(p, "jail");
			return true;

		case "addall":			
			addAll(p);
			return true;

		default:
			zonesutil.COMMANDS(p);
			return true;		
		}
	}

	public void revivePlayer(Player p, String revivedPlayer) {
		
		if(!zonesutil.isAdminPlayer(p)) {
			Util.coloredMessage(p, "&cYou are not an admin, and can not use this CMD.");
			return;
		}
		
		if(isQueueRunning) {
			Util.coloredMessage(p, "&cYou can not revive in queue, just have them /zone join");
			return;
		}
		if(!isGameRunning) {
			Util.coloredMessage(p, "&cThere is no game running D:");
			return;
		}
		
		// only applies on revive when game has not set location yet
		if(currentBorderLocation == null) {
			Util.coloredMessage(p, "currentBorderLocation is null, which means this is probably the before the game starts. "
					+ "Reviving anyways");
		}

		Player targetReviver = Bukkit.getPlayer(revivedPlayer);
		if(targetReviver == null) {
			Util.coloredMessage(p, "&cPlayer " + revivedPlayer + " does not seem to be online!");
			return;
		}

		if(alive.contains(targetReviver)) {
			Util.coloredMessage(p, "&cPlayer is already in the game!");
		} else {
			alive.add(targetReviver);
			Bukkit.broadcastMessage("&a[✓] REVIVED "+revivedPlayer+" back into zones!");
			
			//targetReviver.spigot().respawn();
			targetReviver.teleport(p.getLocation());
			targetReviver.setGameMode(GameMode.SURVIVAL);
			targetReviver.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.COMPASS, 1));

		}
		  	
	}

	public static void saveLocationToFile(String key, World w, int x, int y, int z, float yaw, float pitch) {
		plugin.getConfig().set(key, w.getName()+";"+x+";"+y+";"+z+";"+yaw+";"+pitch);
		plugin.saveConfig();
	}



	public static void setConfigColorLocations(String color, Player p) {
		
		if(!zonesutil.isAdminPlayer(p)) {
			Util.coloredMessage(p, "&cYou are not an admin, and can not use this CMD.");
			return;
		}
		
		// setting the locations for the sections in the right order
		Location loc = p.getLocation();
		World world = loc.getWorld();

		// if the array doesnt have color in it yet (queued)
		if(!settingColorLocsForConfig.containsKey(color)) {
			Util.coloredMessage(p, "\n&a[!] Go to other corner and run this CMD again!");
			settingColorLocsForConfig.put(color, loc);
			return;
		}
		
		Location l1 = settingColorLocsForConfig.get(color); // temp cache for the set command
		Location l2 = loc;
		
		// NOTE when setting new locations, make sure that both X and Z are lower then location 2
		// if not, swap the values before saving.
		l1 = new Location(world, Math.min(l1.getX(), l2.getX()), 0, Math.min(l1.getZ(), l2.getZ()));
		l2 = new Location(world, Math.max(l1.getX(), l2.getX()), 255, Math.max(l1.getZ(), l2.getZ()));
		
		// when setting locations from one way to another, it likes to break sometimes idk why
		if(l1.getBlockX() == l2.getBlockX() || l1.getBlockZ() == l2.getBlockZ()) {
			Util.coloredMessage(p, "&c[!] ERROR, Run this command again here, then retrace your steps");
			settingColorLocsForConfig.clear();
			return;
		}

		plugin.getConfig().set("color."+color+".loc1", world.getName()+";"+(int)l1.getBlockX()+";"+0+";"+(int)l1.getBlockZ());
		plugin.getConfig().set("color."+color+".loc2", world.getName()+";"+(int)l2.getBlockX()+";"+255+";"+(int)l2.getBlockZ());	
		plugin.getConfig().set("color."+color+".material", "NETHER_STAR");
		plugin.saveConfig();		

		settingColorLocsForConfig.clear();
		BorderUTIL.loadNewColorToCache(color); // loads color to memory
		
		Util.coloredMessage(p, "\n&a[!] &cZone: " + color + " set!");		
	}


	// -= CONFIGURATION METHODS =-
	public static Location getLocationFromConfig(String yaml_key) {	
		
		
		if(!plugin.getConfig().contains(yaml_key)) {	
			// if none, just return spawn
			if(CurrentColor.toLowerCase().equalsIgnoreCase("none")) {
				if(spawn != null) {
					return spawn;
				} else {
					return null;
				}
				
			} 
			
			Bukkit.broadcast("ADMINS: " + yaml_key + " not set in ZONE minigame", Permission);
			Bukkit.broadcast(Util.color("&7&oOnly players with " + Permission + " can see this"), Permission);
			return null;
		}

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


	// Admin Commands
	public void forceJoin(Player p) {
		if(zonesutil.isAdminPlayer(p)) {			
			if(isGameRunning) {
				if(alive.contains(p)) {
					Util.coloredMessage(p, "&cYou are already in the game!");
				} else {
					alive.add(p);
				}
			} else {
				Util.coloredMessage(p, "&cThere is no game running");
			}
		} 	
	}

	public void addAll(Player p) {
		if(zonesutil.isAdminPlayer(p)) {			
			if(isGameRunning) {
				for(Player oPlayer : Bukkit.getOnlinePlayers()) {

					// if player=staff, dont auto add them.
					if(oPlayer.hasPermission("zones.staff")) {
						Util.coloredMessage(oPlayer, 
								"&c[!] Zones addall happened but you are a staff... &f&n/zone join &cto join");
					} else {
						if(!queue.contains(oPlayer)) {
							JoinQueue(oPlayer);
						}
					}					
				}
			} else {
				Util.coloredMessage(p, "&cThere is no game running");
			}
		} 	
	}


	public void adminStartEvent(Player p) {
		// called from commands / GUI (( /void start ))
		if(zonesutil.isAdminPlayer(p)) {  // p has adminPerm 
			if(isGameRunning == false) {	
				initQueueStart();		// start queue for all players						
				JoinQueue(p);	// and join it	 
				plugin.logFile("\n\n"+plugin.timeStamp+" ZONES EVENT STARTED!");
			} else {
				Util.coloredMessage(p, "\n&c[!] A Game is already running!");
			}													
		}
	}
	public void adminStopEvent(Player p) {
		// called from commands / GUI (( /void stop ))		
		if(zonesutil.isAdminPlayer(p)) {  
			if(isGameRunning == true) {										
				Util.coloredBroadcast("&4&l&n[!]&c&l "+p.getName() + "&c has ended the zone event");
				BorderUTIL.setBlocksAt255(CurrentColor, Material.BARRIER);
				StopEvent();
				plugin.logFile(plugin.timeStamp+" "+p.getName()+" ended event\n\n");
			}  else {
				p.sendMessage("no game is running");
			}													
		}
	}
	public void openGUI(Player p) {
		if(zonesutil.isAdminPlayer(p)) {			
			p.openInventory(ZonesGUI.zonesinv);
		} 
	}

	// core functions
	public static void StopEvent() {		
		
		Bukkit.getOnlinePlayers().forEach(p -> p.getInventory().clear());
		alive.clear();
		queue.clear();
		isGameRunning = false;
		isQueueRunning = false;
		isChangingBorders = false;
		CurrentColor="none";

		Bukkit.getScheduler().cancelTask(checkUserInBorderRunnableID);
		Bukkit.getScheduler().cancelTask(startGameRunnableID);
		Bukkit.getScheduler().cancelTask(borderShowID);	
		Bukkit.getScheduler().cancelTask(nextBorder);	
		Bukkit.getScheduler().cancelTask(delayedDamageBorder);	
		Bukkit.getScheduler().cancelTask(teleportSyncCooldown); 
		Bukkit.getScheduler().cancelTask(broadcastingRulesRunnable);

		placedBlocks.forEach(l -> l.getBlock().setType(Material.AIR));

	}
	public void JoinQueue(Player p) {
		if(!isGameRunning) {
			Util.coloredMessage(p, "\n&c&l[!] No Game is running!");	
			return;
		}
		if(!isQueueRunning) {
			Util.coloredMessage(p, "\n&c&l[!] Game is already running!");	
			return;
		}
		if(!queue.contains(p)) {
			if(p.isDead()) {
				p.spigot().respawn();
			}			
			
			p.getInventory().clear();
			p.getPlayer().setGameMode(GameMode.SURVIVAL);

			Util.coloredMessage(p, "     &a&lADDED TO ZONE QUEUE! &7&o(("+(queue.size()+1)+"))\n");
			for(Player announcePlayerJoin : queue) {
				Util.coloredMessage(announcePlayerJoin, "&7&o[&a&o+&7&o] " + p.getName() + " joined the queue");
			}		
			queue.add(p);
			Util.coloredMessage(p, "&4(!) Teleporting... &7&o(Up to 5 seconds)"); 

			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {		
					p.teleport(spawn);
				}
			},random.nextInt(5) * 20L);
			// adds a little cooldown each time a player does this.
		}


	}


	private static int rulesDelay = 0;
	private static int rulesID;
	public static void broadcastRules() {
		String[] messages = {
		"&c[&k!&c] &fZONE EVENT IS ABOUT TO BEGIN!",
		"&c[&k!&c] &fVideo Settings > Animations > Particles:all!",
		"&c[&k!&c] Make sure Particles are turned on!",
		"&c[&k!&c] &fVideo Settings > Animations > Particles:all!",

		"&c[&f1&c] You will be given UNLIMITED dirt, The sky is the limit!",
		"&c[&f2&c] Your compass will point to the next zone when there is one",
		"&c[&f3&c] PVP and Fall damage will be disabled at the start",
		"        &7&o(( These will turn on later ))",
		"&c[&f4&c] Lava Will Rise as the game mode goes on",
		"&c[&f5&c] ZONES will appear as green borders from the ground to sky",
		"&c[&f6&c] Revivals are up to staffs discretion.",
		"&c[&f7&c] &fBest of luck to all! Staff will take over from here...",
		};
		
		rulesID = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
			@Override
			public void run() { 
				if(rulesDelay>=messages.length){
					Bukkit.getScheduler().cancelTask(rulesID);
					return;
				} else {
					Util.coloredBroadcast(messages[rulesDelay]  + "\n ");
					rulesDelay+=1;
				}
			}
		}, 0L,  120L);
	}
}
