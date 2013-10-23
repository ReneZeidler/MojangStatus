package de.rene_zeidler.mojangstatus;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import de.rene_zeidler.mojangstatus.MojangStatus.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

public class StatusChecker implements Runnable {
	String json;
	protected Gson gson;
	protected ArrayList<HashMap<String, String>> jsonObject;
	
	MojangStatus ms;
	MainConfig config;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run() {
		//References the instance of the plugin and config for easier accessing
		ms = MojangStatus.getInstance();
		config = ms.getConfig();
		
		try {
			//Retrieve status info
			URL url = new URL("http://status.mojang.com/check");
			
			Scanner s = new Scanner(url.openStream());
			this.json = s.next();
			s.close();
			
			//Parse JSON data
			this.gson = new Gson();
			
			Type listType = new StatusChecker.ListType(this).getType();
			this.jsonObject = ((ArrayList) this.gson.fromJson(this.json, listType));
			if(config.debug) ms.getLogger().log(Level.FINE, "[Mojang Status] Successfully downloaded JSON");
		} catch (IOException ex) {
			ms.getLogger().log(Level.FINE, "Error when trying to retrieve Mojang server status data.");
			if(config.debug) ex.printStackTrace();
		}
		try {
			//Expected format is:
			//	[{"minecraft.net":"green"},{"login.minecraft.net":"green"},{"session.minecraft.net":"green"},{"account.mojang.com":"green"},{"auth.mojang.com":"green"},{"skins.minecraft.net":"green"},{"authserver.mojang.com":"green"}]
			//Sets the service status to offline when the corresponding jsonObject has the value "red"
			ms.setStatus(Service.MINECRAFTNET,     !((String) ((HashMap) this.jsonObject.get(0)).get("minecraft.net")).        equalsIgnoreCase("red"));
			ms.setStatus(Service.LOGINMINECRAFT,   !((String) ((HashMap) this.jsonObject.get(1)).get("login.minecraft.net")).  equalsIgnoreCase("red"));
			ms.setStatus(Service.SESSIONMINECRAFT, !((String) ((HashMap) this.jsonObject.get(2)).get("session.minecraft.net")).equalsIgnoreCase("red"));
			ms.setStatus(Service.ACCOUNTMOJANG,    !((String) ((HashMap) this.jsonObject.get(3)).get("account.mojang.com")).   equalsIgnoreCase("red"));
			ms.setStatus(Service.AUTHMOJANG,       !((String) ((HashMap) this.jsonObject.get(4)).get("auth.mojang.com")).      equalsIgnoreCase("red"));
			ms.setStatus(Service.SKINSMINECRAFT,   !((String) ((HashMap) this.jsonObject.get(5)).get("skins.minecraft.net")).  equalsIgnoreCase("red"));
			ms.setStatus(Service.AUTHSERVERMOJANG, !((String) ((HashMap) this.jsonObject.get(6)).get("authserver.mojang.com")).equalsIgnoreCase("red"));
		} catch (NullPointerException ex) {
			//JSON data have an unexpected format, just ignore it
			ms.getLogger().log(Level.FINE, "The downloaded JSON data are invalid or empty");
			if(config.debug) ex.printStackTrace();
		}
	}
	
	class ListType extends TypeToken<ArrayList<HashMap<String, String>>> {
		ListType(StatusChecker paramStatusChecker) {}
	}
}