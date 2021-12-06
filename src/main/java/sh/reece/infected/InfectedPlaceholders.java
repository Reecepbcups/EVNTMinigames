package sh.reece.infected;

import org.bukkit.entity.Player;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class InfectedPlaceholders extends PlaceholderExpansion {

//	private Main plugin;
//	public InfectedPlaceholders(Main instance) {
//		plugin = instance;
//	}
	

	public String getIdentifier() {
		return "infected";
	}

	public String getAuthor() {
		return "Reecepbcups";
	}

	public String getVersion() {
		return "1.0";
	}
	
	
	// Where the identifier would be epoch time ( %age_https://www.epochconverter.com/% )
	public String onPlaceholderRequest(Player player, String identifier) {
		
		if (identifier == null) {
			return null; 
		}			
		
		if(identifier.equalsIgnoreCase("alive")){ // %infected_alive%			
          return Infected.getAlive().size()+"";
		}

		if(identifier.equalsIgnoreCase("infected")){ // %infected_infected%					
			return Infected.getInfected().size()+"";
		}
		
		return null;
	}
}

