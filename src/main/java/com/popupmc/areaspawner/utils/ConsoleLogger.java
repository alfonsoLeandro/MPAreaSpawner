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
