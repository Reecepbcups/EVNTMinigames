package sh.reece.voidgame;

//import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;

public class zoneEvents implements Listener {

	public static Main plugin;
	private boolean infiniteDirtConfig;
	
	
	public zoneEvents(Main instance) {
		plugin = instance;		
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);		
		infiniteDirtConfig = plugin.getConfig().getBoolean("infiniteDirt");
	}
	
	// on leave & kick, if player in alive, remove from alive. remove from event
	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {		
		if(!Zones.isGameRunning) {return;} // if game is not running dont do this
		if(Zones.isQueueRunning) {return;} // if queue is running, do not remove players
		Player p = e.getPlayer();
		
		Zones.removePlayerFromGame(p);
		checkAliveCount(p);
		Bukkit.broadcast(Util.color("&7&o(( Admins, " + p.getName() + " quit the game ))"), Zones.Permission);
	}
	@EventHandler
	public void playerKick(PlayerKickEvent e) {
		if(!Zones.isGameRunning) {return;} // if game is not running dont do this
		if(Zones.isQueueRunning) {return;} // if queue is running, do not remove players
		
		Player p = e.getPlayer();
		Zones.removePlayerFromGame(p);
		checkAliveCount(p);
		Bukkit.broadcast(Util.color("&7&o(( Admins, " + p.getName() + " was kicked from the game ))"), Zones.Permission);
	}

	@EventHandler
	public void playerDeath(PlayerDeathEvent e) {
		if(!Zones.isGameRunning) {return;} // if game is not running dont do this
		if(Zones.isQueueRunning) {return;} // if queue is running, do not remove players
		// this should fix /kill while queue?
		
		Player p = e.getEntity().getPlayer();		
		Zones.removePlayerFromGame(p);
		checkAliveCount((Player) e.getEntity());		 	
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoin(PlayerJoinEvent e) {		
		if(!Zones.isGameRunning) {return;} // if game is not running dont do this
		if(Zones.isQueueRunning) {return;} // if queue is running, do not remove players		
		tpPlayerToJail(e.getPlayer());
	}
	
	public void checkAliveCount(Player p) {
		int left = Zones.alive.size();
		plugin.logFile("# "+left+"Death " + p.getName());
		
		switch (left) {		
		case 50:
			plugin.logFile("50 Left: " + Zones.alive.toString());
			break;					
		case 25:
			plugin.logFile("25 Left: " + Zones.alive.toString());
			break;				
		case 20:
			plugin.logFile("20 Left: " + Zones.alive.toString());
			break;	
		case 15:
			plugin.logFile("15 Left: " + Zones.alive.toString());
			break;
		case 10:
			plugin.logFile("10 Left: " + Zones.alive.toString());
			break;
		case 8:
			plugin.logFile("8 Left: " + Zones.alive.toString());
			break;
		case 6:
			plugin.logFile("6 Left: " + Zones.alive.toString());
			break;
		case 4:
			plugin.logFile("4 Left: " + Zones.alive.toString());
			break;
		case 2:
			zonesutil.broadcastWinner(Zones.alive.get(0).getName());
			break;
			
		case 1:
			zonesutil.broadcastWinner(Zones.alive.get(0).getName());
			break;
		case 0:
			zonesutil.broadcastWinner(p.getName());
			break;
		default:
			break;
		}
	}
	
	
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e) {
		if(!Zones.isGameRunning) {return;}
		if(Zones.isQueueRunning) {return;} 
		
		if(!Zones.alive.contains(e.getPlayer())) {
			//e.setRespawnLocation(Zones.jail);			
			tpPlayerToJail(e.getPlayer());
	           
		}
		
	}
	
	public void tpPlayerToJail(Player p) {
		new BukkitRunnable() {
            @Override
            public void run() {	            	
            	p.teleport(Zones.jail);
            }	           
        }.runTaskLater(plugin, 10L);
	}
	
	@EventHandler
	public void onPlayerGettingHurtInQueue(EntityDamageByEntityEvent e) {
		if(Zones.isQueueRunning) {
			if(e.getEntity() instanceof Player) {
				if(Zones.queue.contains((Player) e.getEntity())) {
					Util.coloredMessage((Player) e.getDamager(), "&cYou can not damage in queue!");
					e.setCancelled(true);
				}
				
			}
		}
	}
	
	
	@EventHandler
	public void BlockBreaking(BlockBreakEvent e) {
		Player p = e.getPlayer();

		// if queue is running do not allow block placement
		if(Zones.isQueueRunning) {
			if(Zones.queue.contains(p)) {
				Util.coloredMessage(e.getPlayer(), "\n&4(( No Breaking )) &cThe event has not yet started");				
				e.setCancelled(true);
			}		
			return;
		}
		
		if(Zones.isGameRunning) {
			// allow block mine if player has placed that block
			if(Zones.alive.contains(p)) {
				if(!Zones.placedBlocks.contains(e.getBlock().getLocation())) {
					Util.coloredMessage(p, "&cYou can only break blocks which players have placed!");
					e.setCancelled(true);
				}
			} 
		}
	}
	
	
	
	final ItemStack DIRT = new ItemStack(Material.DIRT);
	@EventHandler
	public void BlockPlacement(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		// if queue is running do not allow block placement
		
		
		// if queue is running,
		if(Zones.isQueueRunning) {
			if(Zones.queue.contains(p)) {
				Util.coloredMessage(p, "\n&4(( No Placement )) &cThe event has not yet started");				
				e.setCancelled(true);
			}	
			return;
		}
		
		// if game is running 
		if(Zones.isGameRunning) {			
			if(Zones.alive.contains(p)) {
				Zones.placedBlocks.add(e.getBlock().getLocation());
				
				// if dirt block, allow them to have unlimited usage
				if(infiniteDirtConfig && e.getBlock().getType() == Material.DIRT) {	
					if(Util.isVersion1_8()) {
						if(p.getInventory().getItemInHand().isSimilar(DIRT)) {
							p.getInventory().getItemInHand().setAmount(64);
						}	
					} else {
						if(p.getInventory().getItemInMainHand().isSimilar(DIRT)) {
							p.getInventory().getItemInMainHand().setAmount(64);
						}
					}
									
				}
				
			}			
		}
	}

	
	@EventHandler // No auto-health regen when player is in game
	public void disableRegenOfHealth(EntityRegainHealthEvent e) {		
		if(e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			if(Zones.alive.contains(p)) {
				
				// if it is none, dont do anything
				if(Zones.CurrentColor == "none") {
					return;
				}
				
				// done so we we can cache the location when changing borders in the headTake bukkit runnable
				Location loc1 = Zones.getLocationFromConfig("color."+Zones.CurrentColor+".loc1");
				Location loc2 = Zones.getLocationFromConfig("color."+Zones.CurrentColor+".loc2");
				if(!Zones.isPlayerInsideTheirBorder(p, loc1, loc2)) {
					e.setCancelled(true);
				}
			}
		}
	}
	@EventHandler // No auto-health regen when player is in game
	public void disableFoodLoss(FoodLevelChangeEvent e) {		
		if(e.getEntity() instanceof Player) {
			if(Zones.alive.contains((Player) e.getEntity())) {
				e.setCancelled(true);
			} 
		}
	}
	
	
	
}
