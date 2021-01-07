package com.popupmc.areaspawner.events;

import com.popupmc.areaspawner.utils.Settings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FirstJoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Settings settings = Settings.getInstance();
        if(!event.getPlayer().hasPlayedBefore() && settings.isSpawnOnFirstJoin()){

            //Teleport.
            if(settings.isEssentialsSetHomeOnFirstJoin()){
                //TODO: Essentials set home.
            }
        }
    }
}
