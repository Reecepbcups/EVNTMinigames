package sh.reece.infected;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;
import net.md_5.bungee.api.ChatColor;

public class InfectedEvents implements Listener {

	public Main plugin;
	public InfectedEvents(Main plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if(Infected.isQueueOpen()) {
			Util.coloredMessage(e.getPlayer(), "&c[&7!&c] No placing blocks while queing!");
			e.setCancelled(true);
			return;
		}
		
		if(Infected.isGameRunning()) {
			if(Infected.isGameFrozen()) {
				Util.coloredMessage(e.getPlayer(), "The game is frozen! You can not do that");
				e.setCancelled(true);
			}
		}

	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if(Infected.isQueueOpen()) {
			Util.coloredMessage(e.getPlayer(), "&c[&7!&c] No breaking blocks while queing!");
			e.setCancelled(true);
			return;
		}

		if(Infected.isGameRunning()) {
			
			if(Infected.isGameFrozen()) {
				Util.coloredMessage(e.getPlayer(), "The game is frozen! You can not do that");
				e.setCancelled(true);
			}
			
			// do we cancel block break for people as well? 
			// or only allow block break of certian items /
			// blocks players have placed?
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void playerJoin(PlayerJoinEvent e) {			
		if(Infected.isGameRunning()) {
			Player p = e.getPlayer();
			
			// if game is running, tp to jail
			Infected.removeAlive(p);
			Infected.removeInfected(p);		
			Infected.tpToJail(p);
		}		
	}
	
	

	// respawn after death - not sure if this will be used or not hmm
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onRespawn(PlayerRespawnEvent e) {
		// if game is running: TP to infected spawn
		
		if(Infected.isGameRunning()) {
			Player p = e.getPlayer();
			
			if(Infected.getTransitionQueue().contains(p)){
				e.setRespawnLocation(Infected.getSpawn());				
				Infected.setAsInfected(p);	
				Infected.removeFromTransitionQueue(p);
			} 
			else {
				e.setRespawnLocation(Infected.getJail());	
			}							
		}

	}
	
	
	// DEATH - If they die as a Infected, its game over for them.
	// add fancy announcements here like zones
	public static HashMap<Player, Integer> KillCountStats = new HashMap<Player, Integer>();
	@EventHandler
	public void playerDeath(PlayerDeathEvent e) {		
		if(!Infected.isGameRunning()) {
			return;
		}
		
		Player p = e.getEntity();						
		
		if(Infected.isGameFrozen()) {
			Util.coloredBroadcast("&c[&4-&c] &n"+p.getName()+"&c died while game was frozen. &f"+(Infected.getAlive().size()-1)+" remain.");
			InfectedUTIL.removeFromFrozenList(p);
			return;
		}
		
		e.setDeathMessage("");
		e.getDrops().clear();				
		
		if(Infected.getAlive().contains(p)) {														
			Util.coloredBroadcast("&c[&4-&c] &n"+p.getName()+"&c died and is &nnow&c an infected. &f"+(Infected.getAlive().size()-1)+" remain!");			
			Infected.setAsInfected(p);	
			Infected.addToTransitionQueue(p);
			InfectedUTIL.checkIfWehaveAWinner(p);
			
			// add points to kill tracker
			Player killer = e.getEntity().getKiller();
			if(killer.isOnline()) {
				int count = KillCountStats.containsKey(killer) ? KillCountStats.get(killer) : 0;
				KillCountStats.put(killer, count + 1);
				
			}			
			return;
		} 
		
		if(Infected.getInfected().contains(p)) {			
			Util.coloredBroadcast("&c[&4-&c] &n"+p.getName()+"&c DIED AS AN INFECTED!");
			Util.coloredMessage(p, "&c[&7!&c] You died as a Infected & have been sent to jail!");
			Infected.removeInfected(p);
			InfectedUTIL.resetPlayer(p);
			Infected.tpToJail(p);
			return;
		}
		
		
		
	}
		
	
	// player leave/kick events
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {	
		Infected.removeAlive(e.getPlayer());
		Infected.removeInfected(e.getPlayer());
	}
	
	@EventHandler
	public void playerKick(PlayerKickEvent e) {
		Infected.removeAlive(e.getPlayer());
		Infected.removeInfected(e.getPlayer());
	}
	
	
	// hurt in queue
	@EventHandler(ignoreCancelled = true)
	public void playerAttacksPlayer(EntityDamageByEntityEvent e) {
		
		if(!(e.getEntity() instanceof Player)) {
			return;
		}				
			
		if(Infected.isQueueOpen()) {
			if(Infected.getAlive().contains((Player) e.getEntity())) {
				Util.coloredMessage((Player) e.getDamager(), "&cYou can not damage in queue!");
				e.setCancelled(true);
			}
		}
		
		if(Infected.isGameRunning()) {
			
			// if al;ive attacks alive
			if(Infected.getAlive().contains((Player) e.getDamager())) {
				if(Infected.getAlive().contains((Player) e.getEntity())) {
					Util.coloredMessage((Player) e.getDamager(), "&c[&f!&c] You can not hurt other alive members!");
					e.setCancelled(true);
					return;
				}
			}
			
			
			// if attacker is a infected, do not let them hurt other infected
			if(Infected.getInfected().contains((Player) e.getDamager())) {
				if(Infected.getInfected().contains((Player) e.getEntity())) {
					Util.coloredMessage((Player) e.getDamager(), "&c[&f!&c] You can not hurt other infected!");
					e.setCancelled(true);
					return;
				}
				
				
				
				
				// if Infected attacked a player who is not alive, tell staff that person might not be in the game
				if(Infected.isGameRunning() && !Infected.getAlive().contains((Player) e.getEntity())) {
					Bukkit.broadcast(Util.color("&c[STAFF] "+e.getEntity().getName()+
							" was attacked from an infected "+e.getDamager().getName()+" but does not seem to be alive"), Infected.permission);
					return;
				}
				
				
			}
			
			// if game is frozen, disable all attacks from players
			if(Infected.isGameFrozen()) {				
				Player p = (Player) e.getDamager();
				if(Infected.getAlive().contains(p) || Infected.getInfected().contains(p)) {
					Util.coloredMessage(p, "&cYou can not attack, game is frozen.");
					e.setCancelled(true);
					return;
				}					
			} 


		}	
		
	}
	
	
	@EventHandler
	public void playerHurtWhileFrozen(EntityDamageEvent e) {
		if(Infected.isGameFrozen()) {
			if(e.getEntity() instanceof Player) {
				if(Infected.getAlive().contains((Player) e.getEntity())) {
					e.setCancelled(true);
				} else {
					if(Infected.getInfected().contains((Player) e.getEntity())) {
						e.setCancelled(true);
					}
				}				
			}
		}
	}
	
	@EventHandler //  if player is in infected, dont allow health regen. 
	public void disableRegenOfHealth(EntityRegainHealthEvent e) {			
		if(Infected.isGameRunning()) {
			if(e.getEntity() instanceof Player) {
				
				// infected do not get health regen
				if(Infected.getInfected().contains((Player) e.getEntity())) {
					e.setCancelled(true);
				}
			}
		}
		
	}
	
	// QOL 
	@EventHandler // disabled hunger loss? or do we allow idk
	public void disableFoodLoss(FoodLevelChangeEvent e) {			
		if(Infected.isQueueOpen() || Infected.isGameRunning()) {			
			if(e.getEntity() instanceof Player) {
				e.setCancelled(true);
			}			
		}
	}
	
	
	@EventHandler//(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
		// https://www.spigotmc.org/threads/preventing-players-from-removing-armor.479351/
		if(Infected.isGameRunning()) {
			if(Infected.getInfected().contains(e.getWhoClicked())) {
				
				if(e.getClickedInventory() == null || e.getCurrentItem() == null){
					return;
				}
				
				if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
					if (e.getSlotType() == InventoryType.SlotType.ARMOR) {
						e.setCancelled(true);
					}
				}
			}
		}
		
    }
	
	@EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        // https://www.spigotmc.org/threads/stop-player-from-dropping-an-item-in-a-specific-slot.433909/
		if(Infected.isGameRunning()) {
			if(Infected.getInfected().contains(e.getPlayer())) {
				Util.coloredMessage(e.getPlayer(), "&4[!] &cYou can not drop any items as a infected!");
				e.setCancelled(true);
			}
		}
    }
	
	
}
