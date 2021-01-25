package com.popupmc.areaspawner.events;

import com.popupmc.areaspawner.AreaSpawner;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener for letting OPs know there is a new version available whenever there is one.
 */
public class PlayerJoinUpdateCheck implements Listener {

    final private AreaSpawner plugin;

    public PlayerJoinUpdateCheck(AreaSpawner plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if(event.getPlayer().isOp()){
            Player player = event.getPlayer();
            String exclamation = "&e&l(&4&l!&e&l)";
            String prefix = plugin.getConfig().getString("config.prefix");
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix+" "+exclamation+" &4New version available &7(&e"+plugin.getLatestVersion()+"&7)"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix+" "+exclamation+" &ehttp://bit.ly/areaSpawnerUpdate") );
        }
    }


}
