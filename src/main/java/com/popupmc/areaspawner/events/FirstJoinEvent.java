package com.popupmc.areaspawner.events;

import com.earth2me.essentials.Essentials;
import com.popupmc.areaspawner.AreaSpawner;
import com.popupmc.areaspawner.spawn.RandomSpawnCache;
import com.popupmc.areaspawner.utils.Logger;
import com.popupmc.areaspawner.utils.Settings;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

/**
 * Listener class for listening to {@link PlayerSpawnLocationEvent} to look for players that join for the first time.
 *
 * @author lelesape
 */
public class FirstJoinEvent implements Listener {

    /**
     * AreaSpawner's main class instance.
     */
    final private AreaSpawner plugin;

    public FirstJoinEvent(AreaSpawner plugin){
        this.plugin = plugin;
    }


    @EventHandler (priority = EventPriority.HIGHEST)
    public void onJoin(PlayerSpawnLocationEvent event){
        Settings settings = Settings.getInstance();
        Player player = event.getPlayer();

        if(!player.hasPlayedBefore() && settings.isSpawnOnFirstJoin()){

            if(settings.isNotUseAutomaticPermission() || player.hasPermission("areaSpawner.automatic")){
                //Teleport.
                Location location = RandomSpawnCache.getInstance().getSafeSpawn();
                new BukkitRunnable(){
                    public void run(){
                        player.teleport(location);
                    }
                }.runTaskLater(plugin, 5);

                Logger.send(player, plugin.getMessagesYaml().getAccess().getString("messages.you have been teleported"));
                Logger.debug("&e"+player.getName()+" has joined for the first time and has been teleported to a new random location.");

                if(settings.isEssentialsSetHomeOnFirstJoin()){
                    JavaPlugin.getPlugin(Essentials.class).getUser(player).setHome(settings.getFirstJoinHomeName(), location);
                    Logger.debug("&eEssentials home set for "+player.getName()+".");
                    Logger.send(player, plugin.getMessagesYaml().getAccess().getString("messages.essentials home set"));
                }
            }

        }
    }
}
