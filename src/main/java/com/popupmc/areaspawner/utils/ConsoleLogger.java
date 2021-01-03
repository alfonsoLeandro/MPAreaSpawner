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
package com.popupmc.areaspawner.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

/**
 * Utility class with the only purpose of communicating with the server console.
 */
public class ConsoleLogger {

    /**
     * Sends a message to the console.
     * @param msg The message to be sent.
     */
    public static void send(String msg){
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', Settings.getInstance().getPrefix()+" "+msg));
    }

    /**
     * Sends a debug message to the console only if the debug mode is enabled.
     * @param msg The message to send.
     */
    public static void debug(String msg){
        if(Settings.getInstance().isDebug()) send(msg);
    }

}
