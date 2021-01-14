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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;


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
//            TODO: updateChecker
//            if(!plugin.getVersion().equals(plugin.getLatestVersion())){
//                Logger.send("&fVersion: &e"+plugin.getVersion()+"&f. &cUpdate available!");
//                Logger.send("&fDownload here: http://bit.ly/2Pl4Rg7");
//                return true;
//            }
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
                Logger.send(sender, "&aThe cache process has been successfully stopped.");
                Logger.send(sender, "&eThere are currently "+locations+" locations saved in cache.");
            }else{
                Logger.send(sender, "&cThe cache process was not running.");
            }

            //unknown command
        }else {
            Logger.send(sender, unknown.replace("%command%", label));
        }



        return true;
    }
}
