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
import com.popupmc.areaspawner.utils.Settings;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;


/**
 * AreaSpawner's main command, contains admin commands, not intended for normal user interaction.
 *
 * @author lelesape
 */
public final class MainCommand implements CommandExecutor {

    final private AreaSpawner plugin;
    //Translatable messages
    private String noPerm;
    private String unknown;
    private String reloaded;
    private String regenerating;
    private String locationsStored;
    private String commandList;


    /**
     * MainCommand class constructor.
     * @param plugin The main class instance.
     */
    public MainCommand(AreaSpawner plugin){
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Loads every message used here from the messages file.
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

    /**
     * Sends a message to the CommandSender.
     * @param msg The message to be sent.
     */
    private void send(CommandSender sender, String msg){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigYaml().getAccess().getString("prefix")+" "+msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            send(sender, commandList);
            send(sender, "&f/"+label+" help");
            send(sender,"&f/"+label+" version");
            send(sender,"&f/"+label+" reload");
            send(sender,"&f/"+label+" regenerate");
            send(sender,"&f/"+label+" locations");
            send(sender,"&f/"+label+" stopCache");
            send(sender,"&f/"+label+" getSettings");


        }else if(args[0].equalsIgnoreCase("version")) {
            if(!sender.hasPermission("areaSpawner.version")) {
                send(sender, noPerm);
                return true;
            }
//            TODO: updateChecker
//            if(!plugin.getVersion().equals(plugin.getLatestVersion())){
//                send("&fVersion: &e"+plugin.getVersion()+"&f. &cUpdate available!");
//                send("&fDownload here: http://bit.ly/2Pl4Rg7");
//                return true;
//            }
            send(sender,"&fVersion: &e" + plugin.getVersion() + "&f. &aUp to date!");


        //Reloading will only verify locations and remove any of them if necessary, it will not create new locations.
        }else if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission("areaSpawner.reload")) {
                send(sender, noPerm);
                return true;
            }
            plugin.reloadFiles();
            loadMessages();
            Settings.getInstance().reloadFields();
            RandomSpawnCache.getInstance().reValidateSpawns();
            send(sender, reloaded);
            plugin.checkDangerousSettings();



        }else if(args[0].equalsIgnoreCase("regenerate")){
            if(!sender.hasPermission("areaSpawner.regenerate")) {
                send(sender, noPerm);
                return true;
            }
            send(sender, regenerating);
            RandomSpawnCache.getInstance().createSafeSpawns(true);



        }else if(args[0].equalsIgnoreCase("locations")) {
            if(!sender.hasPermission("areaSpawner.locations")) {
                send(sender, noPerm);
                return true;
            }
            send(sender, locationsStored
                    .replace("%locations%", String.valueOf(RandomSpawnCache.getInstance().getLocationsInCache())));



        }else if(args[0].equalsIgnoreCase("stopCache")) {
            if(!sender.hasPermission("areaSpawner.stopCache")) {
                send(sender, noPerm);
                return true;
            }
            RandomSpawnCache rsp = RandomSpawnCache.getInstance();
            int locations = rsp.getLocationsInCache();

            if(rsp.stopCache()){
                send(sender, "&aThe cache process has been successfully stopped.");
                send(sender, "&eThere are currently "+locations+" locations saved in cache.");
            }else{
                send(sender, "&cThe cache process was not running.");
            }

        }else if(args[0].equalsIgnoreCase("getSettings")) {
            if(!sender.hasPermission("areaSpawner.getSettings")) {
                send(sender, noPerm);
                return true;
            }
            Settings settings = Settings.getInstance();

            send(sender, "&cBooleans");
            send(sender, "&fDebug: "+settings.isDebug());
            send(sender, "&fReplace used locations: "+settings.isRemoveUsedLocation());
            send(sender, "&fCache enabled: "+settings.isCacheEnabled());
            send(sender, "&fCheck past surface: "+!settings.isNotCheckPastSurface());
            send(sender, "&fCheck safety on use: "+settings.isCheckSafetyOnUse());
            send(sender, "&fDelete on unsafe: "+settings.isDeleteOnUnsafe());
            send(sender, "&cIntegers");
            send(sender, "&fAttempts to find a safe location: "+settings.getFindSafeLocationAttempts());
            send(sender, "&fAmount of locations to try and save to cache: "+settings.getCachedLocationsAmount());
            send(sender, "&fTicks to wait between generating locations: "+settings.getTimeBetweenLocations());
            send(sender, "&cStrings");
            send(sender, "&fWorld name: "+settings.getWorldName());
            send(sender, "&cRegions");
            send(sender, "&fSpawn zone:");
            send(sender, "X:"+settings.getAllowedRegion().getMinX()+","+settings.getAllowedRegion().getMaxX());
            send(sender, "Y:"+settings.getAllowedRegion().getMinY()+","+settings.getAllowedRegion().getMaxY());
            send(sender, "Z:"+settings.getAllowedRegion().getMinZ()+","+settings.getAllowedRegion().getMaxZ());
            send(sender, "&fNo spawn zone:");
            send(sender, "X:"+settings.getForbiddenRegion().getMinX()+","+settings.getForbiddenRegion().getMaxX());
            send(sender, "Y:"+settings.getForbiddenRegion().getMinY()+","+settings.getForbiddenRegion().getMaxY());
            send(sender, "Z:"+settings.getForbiddenRegion().getMinZ()+","+settings.getForbiddenRegion().getMaxZ());


        //TEMP - Used for testing
        }else if(args[0].equalsIgnoreCase("teleport")) {
            if(sender instanceof ConsoleCommandSender){
                send(sender, "&cYou cannot send that command from console.");
                return true;
            }
            if(!sender.hasPermission("areaSpawner.teleport")) {
                send(sender, noPerm);
                return true;
            }
            RandomSpawnCache rsp = RandomSpawnCache.getInstance();
            ((Player)sender).teleport(rsp.getSafeSpawn());
            send(sender, "&aTeleported!");


            //unknown command
        }else {
            send(sender, unknown.replace("%command%", label));
        }



        return true;
    }
}
