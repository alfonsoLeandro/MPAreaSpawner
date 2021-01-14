package com.popupmc.areaspawner.commands;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.popupmc.areaspawner.AreaSpawner;
import com.popupmc.areaspawner.spawn.RandomSpawnCache;
import com.popupmc.areaspawner.utils.Logger;
import com.popupmc.areaspawner.utils.Settings;
import com.popupmc.areaspawner.utils.TimeUnit;
import com.popupmc.areaspawner.utils.TravelCooldownManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class TravelCommand implements CommandExecutor {

    final private AreaSpawner plugin;
    //Translatable messages
    private String commandList;
    private String noPerm;
    private String unknown;
    private String travelDisabled;
    private String inCooldown;
    private String notEnoughMoney;
    private String charged;
    private String permissionRemoved;
    private String essentialsHomeSet;
    private String teleported;
    private String invalidPlayer;
    private String teleportedPlayer;


    /**
     * TravelCommand class constructor.
     * @param plugin The main class instance.
     */
    public TravelCommand(AreaSpawner plugin){
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Loads/reloads every message used here from the messages file.
     */
    public void loadMessages(){
        FileConfiguration messages = plugin.getMessagesYaml().getAccess();

        commandList = messages.getString("messages.list of commands");
        noPerm = messages.getString("messages.no permission");
        unknown = messages.getString("messages.unknown command");
        travelDisabled = messages.getString("messages.travel disabled");
        inCooldown = messages.getString("messages.still in cooldown");
        notEnoughMoney = messages.getString("messages.not enough money");
        charged = messages.getString("messages.charged");
        permissionRemoved = messages.getString("messages.travel permission removed");
        essentialsHomeSet = messages.getString("messages.essentials home set");
        teleported = messages.getString("messages.you have been teleported");
        invalidPlayer = messages.getString("messages.invalid player");
        teleportedPlayer = messages.getString("messages.player has been teleported");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(args.length == 0 || args[0].equalsIgnoreCase("help")) {
            Logger.send(sender, commandList);
            Logger.send(sender, "&f/" + label + " help");
            Logger.send(sender, "&f/" + label + " travel");
            Logger.send(sender, "&f/" + label + " forceTravel (player)");


        }else if(args[0].equalsIgnoreCase("travel")) {
            Settings settings = Settings.getInstance();
            if(sender instanceof ConsoleCommandSender) {
                Logger.send("&cThat command should only be executed by a player.");
                return true;
            }
            FileConfiguration config = plugin.getConfig();
            if(!settings.isTravelEnabled()) {
                Logger.send(sender, travelDisabled);
                return true;
            }
            if(!sender.hasPermission("areaSpawner.travel")) {
                Logger.send(sender, noPerm);
                return true;
            }

            //Cooldown
            TravelCooldownManager tcm = TravelCooldownManager.getInstance();
            long timeLeft = tcm.getTimeLeft(sender.getName());

            if(timeLeft > 0 && !sender.hasPermission("areaSpawner.cooldown.bypass")) {
                Logger.send(sender, inCooldown.replace("%time%", TimeUnit.getTimeString(timeLeft / 50)));
                return true;
            }

            Player player = (Player) sender;

            int travelPrice = config.getInt("travel cost");

            //Charge
            if(plugin.setupEconomy() && travelPrice > 0) {
                Economy econ = plugin.getEconomy();
                double balance = econ.getBalance(player);
                if(travelPrice > balance) {
                    Logger.send(sender, notEnoughMoney.replace("%amount%", String.valueOf(travelPrice)));
                    return true;
                }
                econ.withdrawPlayer(player, travelPrice);
                Logger.send(sender, charged.replace("%price%", String.valueOf(travelPrice)));
            }

            //Teleport
            Location location = RandomSpawnCache.getInstance().getSafeSpawn();
            player.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
            Logger.send(player, teleported);



            //Add to cooldown
            tcm.addToCooldown(sender.getName());

            //Remove permission
            if(!player.isOp() && settings.isRemovePermissionOnTravel() && plugin.getPerms() != null) {
                plugin.getPerms().playerRemove(player, "areaSpawner.travel");
                Logger.send(sender, permissionRemoved);
            }

            //Essentials set home
            if(settings.isEssentialsSetHomeOnTravel()) {
                User user = JavaPlugin.getPlugin(Essentials.class).getUser(player);
                if(user.getHomes().isEmpty()) {
                    user.setHome("home", location);
                    Logger.debug("&eEssentials home set for " + player.getName() + ".");
                    Logger.send(sender, essentialsHomeSet);
                } else {
                    Logger.debug("&eAn essentials home has not been set for " + player.getName() + " on travel because the player already has at least one home set.");
                }
            }


        }else if(args[0].equalsIgnoreCase("forceTravel")){
            if(!sender.hasPermission("areaSpawner.forceTravel")) {
                Logger.send(sender, noPerm);
                return true;
            }
            if(args.length < 2){
                Logger.send(sender, "&cUse: "+label+" forceTravel (player)");
                return true;
            }
            Player toTeleport = Bukkit.getPlayer(args[1]);
            if(toTeleport == null){
                Logger.send(sender, invalidPlayer);
                return true;
            }

            //Teleport
            RandomSpawnCache.getInstance().teleport(toTeleport);
            Logger.send(sender, teleportedPlayer.replace("%player%", args[1]));





            //unknown command
        }else {
            Logger.send(sender, unknown.replace("%command%", label));
        }


        return true;
    }
}
