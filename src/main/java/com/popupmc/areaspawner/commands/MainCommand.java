/*
Copyright 2020 Leandro Alfonso

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.popupmc.areaspawner.commands;

import com.popupmc.areaspawner.AreaSpawner;
import com.popupmc.areaspawner.spawn.RandomSpawnCache;
import com.popupmc.areaspawner.utils.Logger;
import com.popupmc.areaspawner.utils.Settings;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;


/**
 * AreaSpawner's main command, contains admin commands, not intended for normal user interaction.
 *
 * @author lelesape
 */
public final class MainCommand implements CommandExecutor {

    /**
     * AreaSpawner's main class instance.
     */
    final private AreaSpawner plugin;
    private String configFieldsHash;
    //Translatable messages
    private String noPerm;
    private String unknown;
    private String reloaded;
    private String regenerating;
    private String locationsStored;
    private String commandList;


    /**
     * MainCommand class constructor.
     * @param plugin AreaSpawner's main class instance.
     */
    public MainCommand(AreaSpawner plugin){
        this.plugin = plugin;
        loadMessages();
        configFieldsHash = getMDHash();
    }

    /**
     * Loads/reloads every message used here from the messages file.
     */
    private void loadMessages(){
        FileConfiguration messages = plugin.getMessagesYaml().getAccess();

        commandList = messages.getString("messages.list of commands");
        noPerm = messages.getString("messages.no permission");
        unknown = messages.getString("messages.unknown command");
        reloaded = messages.getString("messages.reloaded");
        regenerating = messages.getString("messages.regenerating");
        locationsStored = messages.getString("messages.number of locations");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            Logger.send(sender, commandList);
            Logger.send(sender, "&f/randomSpawn");
            Logger.send(sender, "&f/"+label+" help");
            Logger.send(sender,"&f/"+label+" version");
            Logger.send(sender,"&f/"+label+" reload");
            Logger.send(sender,"&f/"+label+" regenerate");
            Logger.send(sender,"&f/"+label+" locations");
            Logger.send(sender,"&f/"+label+" stopCache");


        }else if(args[0].equalsIgnoreCase("version")) {
            if(!sender.hasPermission("areaSpawner.version")) {
                Logger.send(sender, noPerm);
                return true;
            }
            if(!plugin.getVersion().equals(plugin.getLatestVersion())){
                Logger.send("&fVersion: &e"+plugin.getVersion()+"&f. &cUpdate available!");
                Logger.send("&fDownload here: http://bit.ly/areaSpawnerUpdate");
                return true;
            }
            Logger.send(sender,"&fVersion: &e" + plugin.getVersion() + "&f. &aUp to date!");


        //Reloading will only verify locations and remove any of them if necessary, it will not create new locations.
        }else if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission("areaSpawner.reload")) {
                Logger.send(sender, noPerm);
                return true;
            }
            plugin.reload();
            loadMessages();
            Logger.send(sender, reloaded);
            if(Settings.getInstance().isCacheEnabled() && !getMDHash().equals(this.configFieldsHash)){
                Logger.send(sender, "&cChanges in location/cache detected. Regenerating cached locations.");
                RandomSpawnCache.getInstance().createSafeSpawns(true);
                this.configFieldsHash = getMDHash();
            }



        }else if(args[0].equalsIgnoreCase("regenerate")){
            if(!sender.hasPermission("areaSpawner.regenerate")) {
                Logger.send(sender, noPerm);
                return true;
            }
            Logger.send(sender, regenerating);
            RandomSpawnCache.getInstance().createSafeSpawns(true);



        }else if(args[0].equalsIgnoreCase("locations")) {
            if(!sender.hasPermission("areaSpawner.locations")) {
                Logger.send(sender, noPerm);
                return true;
            }
            Logger.send(sender, locationsStored
                    .replace("%locations%", String.valueOf(RandomSpawnCache.getInstance().getLocationsInCache())));



        }else if(args[0].equalsIgnoreCase("stopCache")) {
            if(!sender.hasPermission("areaSpawner.stopCache")) {
                Logger.send(sender, noPerm);
                return true;
            }
            RandomSpawnCache rsp = RandomSpawnCache.getInstance();
            int locations = rsp.getLocationsInCache();

            if(rsp.stopCache()){
                Logger.send(sender, "&aThe cache process will be stopped when it is finished with the current location");
                Logger.send(sender, "&eThere are currently "+locations+" locations saved in cache.");
            }else{
                Logger.send(sender, "&cThe cache process was not running or it is already stopping.");
            }

            //unknown command
        }else {
            Logger.send(sender, unknown.replace("%command%", label));
        }



        return true;
    }


    /**
     * Gets a string that corresponds to the MD5 hash of a group of config fields.
     * @return The MD5 hash string value that represents the given group of config fields.
     */
    private String getMDHash(){
        StringBuilder result = new StringBuilder();
        String[] keys = {"spawn world", "spawn zone.clamp to limits", "spawn zone.default to multiverse", "spawn zone.x center", "spawn zone.y center", "spawn zone.z center", "spawn zone.x range", "spawn zone.y range", "spawn zone.z range", "no spawn zone.enabled", "no spawn zone.x range", "no spawn zone.z range", "amount of cached spawns"};

        for (String key : keys){
            result.append(plugin.getConfig().get(key));
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(result.toString().getBytes());
            byte[] bytes = md.digest();
            StringBuilder toString = new StringBuilder();
            for(byte b : bytes){
                toString.append(Integer.toHexString(b & 0xff));
            }
            return toString.toString();

        }catch (NoSuchAlgorithmException e){
            return "";
        }
    }
}
