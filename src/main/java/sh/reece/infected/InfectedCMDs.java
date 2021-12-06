package sh.reece.infected;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import sh.reece.evnt.Main;
import sh.reece.evnt.Util;

public class InfectedCMDs implements CommandExecutor, TabCompleter {

	private Main plugin;
	private static List<UUID> confirmCMD = new ArrayList<UUID>();
	public InfectedCMDs(Main plugin) {
		this.plugin = plugin;
	}
	
	
	public void commands(Player p) {
		Util.coloredMessage(p, " \n&8*&7*&F* &4&lINFECTED &f*&7*&8* ");
		Util.coloredMessage(p, "&f/infected &7<join, quit, jail>");
		Util.coloredMessage(p, "&f/infected &7<alive>");

		if(Infected.isPlayerStaff(p)) {
			Util.coloredMessage(p, "&c/infected &f<start, stop, freeze, addall>");
			Util.coloredMessage(p, "&c/infected &f<forcejoin, revive, remove>");
			Util.coloredMessage(p, "&c/infected &f<controlpanel (cp), set>");
		}

	}
	
	
	private List<String> possibleArugments = new ArrayList<String>();
	private List<String> result = new ArrayList<String>();
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {		
		if(possibleArugments.isEmpty()) {
			possibleArugments.add("join");
			possibleArugments.add("quit");			
			possibleArugments.add("warpjail"); 
			possibleArugments.add("alive");	
			possibleArugments.add("start");
			possibleArugments.add("clearaliveinv");
			possibleArugments.add("freeze");
			possibleArugments.add("unfreezeplayer");
			possibleArugments.add("rules");
			possibleArugments.add("addall");
			possibleArugments.add("forcejoin");
			possibleArugments.add("revive");
			possibleArugments.add("controlpanel");
			possibleArugments.add("set");
			possibleArugments.add("stop");
		}		
		result.clear();
		if(args.length == 1) {			
			for(String a : possibleArugments) {
				if(a.toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(a);			
				}
			}
			return result;
		}	
		if(args[0].equalsIgnoreCase("set") && args.length == 3) {
			for(String a : Arrays.asList("alive", "infected")) {
				if(a.toLowerCase().startsWith(args[1].toLowerCase())) {
					result.add(a);			
				}
			}
			return result;
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {				
		Player p = (Player) sender;

		if (args.length == 0) {		
			commands(p);
			return true;
		}

		//player commands
		switch(args[0].toLowerCase()){ // void args[0]
		case "join":
			Infected.gameAction(p, "JOIN");
			return true;
		case "leave":
		case "quit":
			Infected.gameAction(p, "LEAVE");
			return true;
		case "warpjail":
		case "jail":
			Infected.tpToJail(p);
			Util.coloredMessage(p, "&a&l[!] &fTeleported to the Jail");
			return true;
		case "alive":
			String msg = Infected.getAlive().size()+ " ";
			for(Player play : Infected.getAlive()) {
				msg+="&a"+play.getDisplayName() + "&7, ";
			}			
			Util.coloredMessage(p, "   &7----- &f* Alive * &7-----");
			Util.coloredMessage(p, msg);
			
			String msg2 = Infected.getInfected().size()+ " ";
			for(Player play : Infected.getInfected()) {
				msg2+="&f"+play.getDisplayName() + "&7, ";
			}
			Util.coloredMessage(p, "&7----- &8* &4Infected &8* &7-----");
			Util.coloredMessage(p, msg2);
			
			return true;
		}

		// admin commands
		
		// if player is not staff, do not even give these options
		if(!Infected.isPlayerStaff(p)) {
			return true;
		}
		
		switch(args[0].toLowerCase()){		
		case "start":
			if(Infected.getSpawn() == null) {
				Util.coloredMessage(p, "&cSpawn is not set, /infected setspawn");
				return true;
			}					
			Infected.START_GAME(p);
			return true;
		case "stop":
			Infected.STOP_GAME();
			return true;

		case "set":
			if(args.length < 3) {
				Util.coloredMessage(p, "&c/infected set <player> <alive/infected>");
				return true;
			}		
			if(Infected.isGameRunning()) {
				Infected.revivePlayer(Bukkit.getPlayer(args[1]), args[2], false);
			} else {
				Util.coloredMessage(p, "&cYou can not revive someone when the game is off!");
			}
			
			
			return true;	
			
		case "freeze":
			InfectedUTIL.freezeGame(p);
			return true;	
			
		case "unfreezeplayer":
			if(args.length != 2) {
				Util.coloredMessage(p, "&c/infected unfreezeplayer <IGN>");
			}
			Player frozenPlayer = Bukkit.getPlayer(args[1]);
			if(frozenPlayer != null) {
				InfectedUTIL.playerLocs.remove(frozenPlayer);
			} else {
				Util.coloredMessage(p, "&cThat player does not seem to even be online!");
			}
			
		case "clearaliveinv":	
			
			if(!Infected.isGameRunning()) {
				Util.coloredMessage(p, "&cThere is no infected game running!");
				return true;
			}
			
			if(!(Infected.getAlive().size() > 0)) {
				Util.coloredMessage(p, "&cThere are no alive players on!");
				return true;
			}
			
			for(Player alive : Infected.getAlive()) {
				Util.console("minecraft:clear " + alive.getName());
			}
			Util.coloredMessage(p, "&aAll alive players have cleared their inv & Armour");
			return true;
			
		case "remove":
			if(args.length < 2) {
				Util.coloredMessage(p, "&c/infected remove <player>");
				return true;
			}		
			if(Infected.isGameRunning()) {
				Player target = Bukkit.getPlayer(args[1]);
				Infected.removeAlive(target);
				Infected.removeInfected(target);
				Infected.tpToJail(target);
				Util.coloredMessage(p, "&c&l[!] &cRemoved "+target.getName()+" from game, sent to Jail.");
				
			} else {
				Util.coloredMessage(p, "&cYou can not remove someone when the game is off!");
			}
			
			return true;
		case "controlpanel":
		case "control":
		case "cp":
			p.openInventory(InfectedGUI.control);
			return true;
			
		case "tpdead":			
			for(Player online : Bukkit.getOnlinePlayers()) {
				// if player not alive or a infected, TP them to player
				if(!Infected.getAlive().contains(online)) {
					if(!Infected.getInfected().contains(online)) {
						online.teleport(p.getLocation());
					}
				}
			}			
			return true;			

		case "rules":
			Infected.broadcastRules();						
			return true;	
			
		case "winner":	
		case "broadcastwinner":	
			// /zones winner IGN			
			if(args.length != 2) {	
				Util.coloredMessage(p, "/infected winner IGN");				
			} else {
				InfectedUTIL.announceWinner(args[1]);								
			}						
			return true;			

		case "forcejoin":
			if(args.length == 2) {					
				Infected.forceJoin(p, args[1]);
				Util.coloredMessage(p, "&a Added you to the " + args[1]);
				Infected.tpToSpawn(p);
			} else {
				Util.coloredMessage(p, "/infected forcejoin <alive/infected>");
			}				
			return true;

		case "revive":
			if(args.length < 3) {
				Util.coloredMessage(p, "&c/infected revive <player> <alive/infected>");
				return true;
			}		
			if(Infected.isGameRunning()) {
				Infected.revivePlayer(Bukkit.getPlayer(args[1]), args[2], true);
			} else {
				Util.coloredMessage(p, "&cYou can not revive someone when the game is off!");
			}
			return true;
			
			
			

//		case "gui":
//			openGUI(p);	
//			return true;



		case "reload":
			p.performCommand("plugman reload "+plugin.getDescription().getName());
			return true;

		case "setspawn":			
			InfectedUTIL.setLocToConfig(p, "spawn");
			Infected.setSpawn(InfectedUTIL.getLocationFromConfig("spawn"));
			return true;
		case "setjail":			
			InfectedUTIL.setLocToConfig(p, "jail");
			Infected.setJail(InfectedUTIL.getLocationFromConfig("jail"));
			return true;

		case "addall":	
			
			if(Infected.isQueueOpen()) {
				addAll(p);
				return true;
			} else {
				Util.coloredMessage(p, "&cThere is no infected queue running! Make sure to start the game");
			}
			
			if(Infected.isGameRunning()) {				
				if(confirmCMD.contains(p.getUniqueId())) {
					Util.coloredMessage(p, "&aAdded all players to the active game!");
					confirmCMD.remove(p.getUniqueId());
					addAll(p);
				} else {
					Util.coloredMessage(p, "&c[!] WAIT Are you sure you want to add all to a game which is already active?");
					Util.coloredMessage(p, "&&7Run this command again to confirm.");
					confirmCMD.add(p.getUniqueId());
				}
			} 
			return true;
				
		default:
			commands(p);
			return true;		
		}

	}	
	
	public static void addAll(Player p) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!Infected.isPlayerStaff(player)) {
				Infected.gameAction(player, "JOIN");
			} else {
				Util.coloredMessage(p, " \n&e&k[!]&c You're a staff member, to join the game &e/infected join\n ");
			}
		}
	}
	
}
