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
package com.popupmc.areaspawner.events;

import com.popupmc.areaspawner.AreaSpawner;
import com.popupmc.areaspawner.spawn.RandomSpawnCache;
import com.popupmc.areaspawner.utils.Logger;
import com.popupmc.areaspawner.utils.Settings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerDieEvent implements Listener {

    final private AreaSpawner plugin;

    public PlayerDieEvent(AreaSpawner plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent event){
        Settings settings = Settings.getInstance();

        if(settings.isSpawnOnDeath()){
            if(!(event.isBedSpawn() && settings.isSpawnOnBed())){
                if(settings.isEssentialsHomeOnRespawn() /*&& player tiene home*/){
                    //TODO: Essentials home respawn
                }else{
                    event.setRespawnLocation(RandomSpawnCache.getInstance().getSafeSpawn());
                    Logger.send(event.getPlayer(), plugin.getMessagesYaml().getAccess().getString("you have been teleported"));
                }
            }
        }
    }
}
