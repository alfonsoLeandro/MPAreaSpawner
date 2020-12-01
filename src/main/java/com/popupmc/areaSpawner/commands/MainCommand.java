/*
MIT License

Copyright (c) 2020 Leandro Alfonso

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.popupmc.areaSpawner.commands;

import com.popupmc.areaSpawner.AreaSpawner;
import com.popupmc.areaSpawner.utils.RandomSpawnCache;
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

public final class MainCommand implements CommandExecutor {

    final private AreaSpawner plugin;
    private CommandSender sender;
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
    private void send(String msg){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfigYaml().getAccess().getString("config.prefix")+" "+msg));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        this.sender = sender;


        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            send("&6List of commands");
            send("&f/"+label+" help");
            send("&f/"+label+" version");
            send("&f/"+label+" reload");


        }else if(args[0].equalsIgnoreCase("version")) {
            if(!sender.hasPermission("areaSpawner.version")) {
                send(noPerm);
                return true;
            }
//            TODO: updateChecker
//            if(!plugin.getVersion().equals(plugin.getLatestVersion())){
//                send("&fVersion: &e"+plugin.getVersion()+"&f. &cUpdate available!");
//                send("&fDownload here: http://bit.ly/2Pl4Rg7");
//                return true;
//            }
            send("&fVersion: &e" + plugin.getVersion() + "&f. &aUp to date!");


        //Reloading will only verify locations and remove any of them if necessary, it will not create new locations.
        }else if(args[0].equalsIgnoreCase("reload")) {
            if(!sender.hasPermission("areaSpawner.reload")) {
                send(noPerm);
                return true;
            }
            plugin.reloadFiles();
            loadMessages();
            RandomSpawnCache.getInstance().reValidateSpawns();
            send(reloaded);


        }else if(args[0].equalsIgnoreCase("regenerate")){
            if(!sender.hasPermission("areaSpawner.regenerate")) {
                send(noPerm);
                return true;
            }
            send(regenerating);
            RandomSpawnCache.getInstance().createNewSafeSpawns();


        }else if(args[0].equalsIgnoreCase("getCache")) {
            if(!sender.hasPermission("areaSpawner.getCache")) {
                send(noPerm);
                return true;
            }
            RandomSpawnCache rsp = RandomSpawnCache.getInstance();
            HashMap<String, List<Location>> locations = rsp.getLocationsInCache();

            for(String worldName : locations.keySet()) {
                for (Location loc : locations.get(worldName)) {
                    send("location: " + loc.getWorld() + " " + loc.getX() + " " + loc.getY() + " " + loc.getZ());
                }
            }
            send("done");

        }else if(args[0].equalsIgnoreCase("teleport")) {
            if(sender instanceof ConsoleCommandSender){
                send("&cYou cannot send that command from console.");
                return true;
            }
            if(!sender.hasPermission("areaSpawner.teleport")) {
                send(noPerm);
                return true;
            }
            RandomSpawnCache rsp = RandomSpawnCache.getInstance();
            ((Player)sender).teleport(rsp.getSafeSpawn(((Player) sender).getWorld().getName()));
            send("&aTeleported!");



            send("done");

            //unknown command
        }else {
            send(unknown.replace("%command%", label));
        }



        return true;
    }
}
