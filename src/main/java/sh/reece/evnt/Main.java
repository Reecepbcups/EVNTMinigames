package sh.reece.evnt;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

//import org.apache.commons.io.FileUtils;
// import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

// import me.clip.placeholderapi.PlaceholderAPI;
// import sh.reece.infected.Infected;
// import sh.reece.infected.InfectedPlaceholders;
import sh.reece.voidgame.Zones;

public class Main extends JavaPlugin implements Listener {

	public final String PREFIX = "&dZones> &f";
	public String timeStamp = new SimpleDateFormat("[MM/dd HH:mm]").format(new Date());
	public final File logFile = new File(getDataFolder()+File.separator+"_ZonesData.txt");
	
	public void onEnable() {

		loadConfig();	
		
		new Zones(this);		
		//new Infected(this); - removed due to being buggy
		
//		if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
//			// %terminator_alive% & terminators
//			new InfectedPlaceholders().register();
//		}
		
		//FancyStartup();

	}

	public void onDisable() {
		this.saveDefaultConfig();
		
		Util.coloredBroadcast("&cMinigames unloaded :( Minigame Stopped");
		Zones.StopEvent();
		//Infected.STOP_GAME();		
	}

	public void logFile(String log) {
//		try {
//			FileUtils.writeStringToFile(logFile, log+"\n", true);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}

	// Configuration File Functions
	public void loadConfig() {
		createConfig("config.yml");		
		getConfig().options().copyDefaults(true);				
	}

	public FileConfiguration getConfigFile(String name) {
		return YamlConfiguration.loadConfiguration(new File(getDataFolder(), name));
	}

	public void createDirectory(String DirName) {
		File newDir = new File(getDataFolder(), DirName.replace("/", File.separator));
		if (!newDir.exists()){
			newDir.mkdirs();
		}
	}

	public void createConfig(String name) {
		File file = new File(getDataFolder(), name);

		if (!new File(getDataFolder(), name).exists()) {

			saveResource(name, false);
		}

		@SuppressWarnings("static-access")
		FileConfiguration configuration = new YamlConfiguration().loadConfiguration(file);
		if (!file.exists()) {
			try {
				configuration.save(file);
			}			
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void createFile(String name) {
		File file = new File(getDataFolder(), name);

		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}	
	public void saveConfig(FileConfiguration config, String name) {
		try {
			config.save(new File(getDataFolder(), name));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void FancyStartup() {
		String S =  "&b  __  __ _____       _                                 \r\n" + 
				"&b |  \\/  |_   _|     (_)                                \r\n" + 
				"&b | \\  / | | |  _ __  _  __ _  __ _ _ __ ___   ___  ___ \r\n" + 
				"&b | |\\/| | | | | '_ \\| |/ _` |/ _` | '_ ` _ \\ / _ \\/ __|\r\n" + 
				"&b | |  | |_| |_| | | | | (_| | (_| | | | | | |  __/\\__ \\\r\n" + 
				"&b |_|  |_|_____|_| |_|_|\\__, |\\__,_|_| |_| |_|\\___||___/\r\n" + 
				"&b                        __/ |                          \r\n" + 
				"&b                       |___/                           ";

		System.out.println(Util.color(S));

	}	



}
