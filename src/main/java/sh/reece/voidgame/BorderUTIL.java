package sh.reece.voidgame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.World;
import org.bukkit.block.Block;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;


public class BorderUTIL {
	// https://www.spigotmc.org/resources/particlenativeapi-1-7.76480/ ?

	// used when drawing borders, "color": <Locations of particles>
	public static HashMap<String, List<Location>> borders = new HashMap<String, List<Location>>();
	private static World world;
	
	public static int minY, maxY;
	private static int defY, defX, under24Y, under24X, under16Y, under16X, under8Y, under8X, particleSize;
	private static int Rvalue,Gvalue,Bvalue;
	// "red": [[105, 525], [100, 500]] - future optomization
	//public static HashMap<String, List<List<Integer>>> ColorXZCache = new HashMap<String, List<List<Integer>>>();
	
	private static Main plugin;
	public BorderUTIL(Main instance) {
		plugin = instance;
		Set<String> zonesColorsKeys = plugin.getConfig().getConfigurationSection("color").getKeys(false);

		// has to load first to get particle spawn points
		defY = plugin.getConfig().getInt("particles.default.y_step");
		defX = plugin.getConfig().getInt("particles.default.x_step");
		under24Y = plugin.getConfig().getInt("particles.under_24r.y_step");
		under24X = plugin.getConfig().getInt("particles.under_24r.x_step");		
		under16Y = plugin.getConfig().getInt("particles.under_16r.y_step");
		under16X = plugin.getConfig().getInt("particles.under_16r.x_step");
		under8Y = plugin.getConfig().getInt("particles.under_8r.y_step");
		under8X = plugin.getConfig().getInt("particles.under_8r.x_step");

		particleSize = plugin.getConfig().getInt("particles.particleSize");
		minY = plugin.getConfig().getInt("particles.minYParticleLevel");
		maxY = plugin.getConfig().getInt("particles.maxYParticleLevel");

		String[] RGBValues = plugin.getConfig().getString("particles.RGBValues").split(",");
		Rvalue = Integer.valueOf(RGBValues[0]);
		Gvalue = Integer.valueOf(RGBValues[1]);
		Bvalue = Integer.valueOf(RGBValues[2]);
		
		world = null;

		if(zonesColorsKeys.size() == 0) {
			Util.consoleMSG("[BorderUTIL] ERROR, no keys found for the color section.");	
			return;
		}

		// loops through config sections, and puts where all particles would be. Saves this to a hashmap.
		// Allows for use of drawBox("color") from config to work
		for(String key : zonesColorsKeys) {						
			loadNewColorToCache(key);
//			Util.consoleMSG("[BorderUTIL] " +key+ " loaded in border. Particles-" + borders.get(key).size());				
		}


	}

	public static void loadNewColorToCache(String colorKey) {
		Location l1 = Zones.getLocationFromConfig("color."+colorKey+".loc1");
		Location l2 =Zones.getLocationFromConfig("color."+colorKey+".loc2");
		if(l1 == null) {Util.consoleMSG("[BorderUTIL] l1 for " + colorKey + " was null");return;}

		// makles a cache for X Z locations "red": [[105, 525], [100, 500]]
		// future
//		List<List<Integer>> finalList = new ArrayList<List<Integer>>();
//		List<Integer> loc1 = new ArrayList<Integer>();
//		List<Integer> loc2 = new ArrayList<Integer>();
//		loc1.add(l1.getBlockX());
//		loc1.add(l1.getBlockZ());
//		loc2.add(l2.getBlockX());
//		loc2.add(l2.getBlockZ());
//		finalList.add(loc1);
//		finalList.add(loc2);
//		ColorXZCache.put(colorKey, finalList);
		
		int radius = zonesutil.getRadius(l1, l2);
		borders.put(colorKey, getAllPlacesToSpawnParticles(colorKey, l1, l2, radius));
	}

	public static List<Location> getAllPlacesToSpawnParticles(String color, Location loc1, Location loc2, int radius) {

		// Initial Loadup. used to get particles for config and load into List of locations.

		if(world == null) {
			world = loc1.getWorld();
		}

		int x = loc1.getBlockX();
		int z = loc1.getBlockZ();
		int x2 = loc2.getBlockX()+1;
		int z2 = loc2.getBlockZ()+1;		
		int ySize, kSize;

		// default spaces for each particle to be show / saved.
		// zSize: amt of blocks in Y direction bwtn particles
		// kSize: `` in X direction
		ySize=defY;
		kSize=defX;

		if(radius<=24) {
			ySize=under24Y;
			kSize=under24X;
		} 
		if(radius<=16) {
			ySize=under16Y;
			kSize=under16X;
		} 
		if(radius<=8) {
			ySize=under8Y;
			kSize=under8X;
		} 

		List<Location> borderLocations = new ArrayList<Location>();	
		for (int y = minY; y <= maxY; y+=ySize) {
			for(int k=0; k<=radius;k+=kSize) {	
				borderLocations.add(new Location(world, x2-k, y, z));
				borderLocations.add(new Location(world, x2-k, y, z2));
				borderLocations.add(new Location(world, x2, y, z+k));
				borderLocations.add(new Location(world, x, y, z+k));
			}
		} 		
		return borderLocations;
	}

	public static void drawBox(String color) { // color from config		
		// draws box based on preloaded config from file
		borders.get(color).stream().forEach(location -> spawnParticle(location));
	}
	
	public static void setBlocksAt255(String color, Material mat) { // color from config		
		// puts red glass at y level 255 from color		
		Location l1 = Zones.getLocationFromConfig("color."+color+".loc1");
		Location l2 =Zones.getLocationFromConfig("color."+color+".loc2");
		
		l1.setY(255);
		l2.setY(255);
		Cuboid cuboid = new Cuboid(l1, l2);
		
		for(Block block : cuboid) {
			Bukkit.getScheduler().runTaskLater(plugin, new Runnable(){
				public void run(){
					block.setType(mat); //set the block back to the original block
				}
			},5L);
		}

		
	}

	private static void spawnParticle(Location loc) {
		// https://www.spigotmc.org/threads/colored-particle-dust.113860/
		
		// so whenw e change maxY from Zones command (/zones maxy #), it does not lag more
		if(!(loc.getBlockY() > maxY) && !(loc.getBlockY() < minY)) {
			if(Util.isVersion1_8()) {
				//world.spigot().playEffect(loc, Effect.valueOf("HAPPY_VILLAGER"), 0, 0, 255, 0, 0, 50, 0, particleViewDistance);
			} else {
				//world.playEffect(loc, Effect.valueOf("VILLAGER_PLANT_GROW"), 1);
				
				DustOptions DUST = new Particle.DustOptions(Color.fromRGB(Rvalue, Gvalue, Bvalue), particleSize);
				world.spawnParticle(Particle.REDSTONE, loc, 0, 16.0D, 0.0D, 0.0D, 10.0D, DUST, true);
			}
				
		}
		//location.getWorld().spigot().playEffect(location, effect, id, data, offsetX, r, g, b, particleCount, radius);
	}



	// old backup
	//	public static void drawBox(Location loc1, Location loc2, int radius) {
	//		World world = loc1.getWorld();
	//		int x = loc1.getBlockX();
	//		int z = loc1.getBlockZ();
	//
	//		int x2 = loc2.getBlockX()+1;
	//		int z2 = loc2.getBlockZ()+1;
	//
	//		int iSize, kSize;
	//		if(radius<=16) {
	//			iSize=4;
	//			kSize=2;
	//		} else {
	//			iSize=9;
	//			kSize=3;
	//		}
	//		
	////		DustOptions DUST = new Particle.DustOptions(Color.RED, thickness);
	////		for (int i = 60; i <= 255; i+=iSize) {
	////			for(int k=0; k<=radius;k+=kSize) {
	////				world.spawnParticle(Particle.REDSTONE, x2-k, i, z, 0, 16.0D, 0.0D, 0.0D, 10.0D, DUST, true);
	////				world.spawnParticle(Particle.REDSTONE, x2, i, z+k, 0, 128.0D, 0.0D, 0.0D, 10.0D, DUST, true);				
	////				world.spawnParticle(Particle.REDSTONE, x, i, z+k, 0, 128.0D, 0.0D, 0.0D, 10.0D, DUST, true);
	////				world.spawnParticle(Particle.REDSTONE, x2-k, i, z2, 0, 256.0D, 0.0D, 0.0D, 10.0D, DUST, true);	
	////			}
	////		} 	
	//		
	//		
	//		//DustOptions DUST = new Particle.DustOptions(Color.RED, thickness);
	//		for (int i = 60; i <= 255; i+=iSize) {
	//			for(int k=0; k<=radius;k+=kSize) {
	////				world.spawnParticle(Particle.REDSTONE, x2-k, i, z, 0, 16.0D, 0.0D, 0.0D, 10.0D, DUST, true);
	////				world.spawnParticle(Particle.REDSTONE, x2, i, z+k, 0, 128.0D, 0.0D, 0.0D, 10.0D, DUST, true);				
	////				world.spawnParticle(Particle.REDSTONE, x, i, z+k, 0, 128.0D, 0.0D, 0.0D, 10.0D, DUST, true);
	////				world.spawnParticle(Particle.REDSTONE, x2-k, i, z2, 0, 256.0D, 0.0D, 0.0D, 10.0D, DUST, true);
	//				
	//				spawnParticle(new Location(world, x2-k, i, z));
	//				spawnParticle(new Location(world, x2, i, z+k));
	//				spawnParticle(new Location(world, x, i, z+k));
	//				spawnParticle(new Location(world, x2-k, i, z2));
	//				
	//			}
	//		} 
	//	}
	
	public static void setMinY(int minY) {
		BorderUTIL.minY = minY;
	}

	public static void setMaxY(int maxY) {
		BorderUTIL.maxY = maxY;
	}

}
