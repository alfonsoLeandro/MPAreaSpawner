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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

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


    /**
     * MainCommand class constructor.
     * @param plugin The main class instance.
     */
    public MainCommand(AreaSpawner plugin){
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Loads every message from config.
     */
    private void loadMessages(){
        FileConfiguration messages = plugin.getMessagesYaml().getAccess();

        noPerm = messages.getString("messages.no permission");
        unknown = messages.getString("messages.unknown command");
        reloaded = messages.getString("messages.reloaded");
        regenerating = messages.getString("messages.regenerating");
    }

    /**
     * Sends a message to the CommandSender.
     * @param msg The message to be sent.
     */
    private void send(CommandSender sender, String msg){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigYaml().getAccess().getString("config.prefix")+" "+msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            send(sender, "&6List of commands");
            send(sender, "&f/"+label+" help");
            send(sender,"&f/"+label+" version");
            send(sender,"&f/"+label+" reload");


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
            RandomSpawnCache.getInstance().reValidateSpawns();
            send(sender, reloaded);


        }else if(args[0].equalsIgnoreCase("regenerate")){
            if(!sender.hasPermission("areaSpawner.regenerate")) {
                send(sender, noPerm);
                return true;
            }
            send(sender, regenerating);
            RandomSpawnCache.getInstance().createNewSafeSpawns();


        }else if(args[0].equalsIgnoreCase("getCache")) {
            if(!sender.hasPermission("areaSpawner.getCache")) {
                send(sender, noPerm);
                return true;
            }
            RandomSpawnCache rsp = RandomSpawnCache.getInstance();
            HashMap<String, List<Location>> locations = rsp.getLocationsInCache();

            send(sender, "");
            for(String worldName : locations.keySet()) {
                send(sender, worldName+": "+locations.get(worldName).size()+" locations");
            }

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
            ((Player)sender).teleport(rsp.getSafeSpawn(((Player) sender).getWorld().getName()));
            send(sender, "&aTeleported!");



            send(sender, "done");

            //unknown command
        }else {
            send(sender, unknown.replace("%command%", label));
        }



        return true;
    }
}
