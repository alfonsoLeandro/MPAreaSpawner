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
package com.popupmc.areaspawner;

import com.popupmc.areaspawner.commands.MainCommand;
import com.popupmc.areaspawner.spawn.RandomSpawnCache;
import com.popupmc.areaspawner.utils.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AreaSpawner main class. In charge of registering any plugin-server interaction.
 *
 * @author lelesape
 */
public final class AreaSpawner extends JavaPlugin {

    final private PluginDescriptionFile pdfFile = getDescription();
    final private String version = pdfFile.getVersion();
//    private String latestVersion; TODO
    final private char color = 'e';
    final private String name = "&f[&" + color + pdfFile.getName() + "&f]";
    private YamlFile configYaml;
    private YamlFile messagesYaml;
    private YamlFile cacheYaml;

    /**
     * Sends a message to the console, with colors and prefix added.
     * @param msg The message to be sent.
     */
    private void send(String msg) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', name + " " + msg));
    }


    /**
     * Plugin enable logic.
     */
    @Override
    public void onEnable() {
        send("&aEnabled&f. Version: &e" + version);
        send("&fThank you for using my plugin! &" + color + pdfFile.getName() + "&f By " + pdfFile.getAuthors().get(0));
        send("&fJoin my discord server at &chttps://discordapp.com/invite/ZznhQud");
        send("Please consider subscribing to my yt channel: &c" + pdfFile.getWebsite());
        reloadFiles();
        RandomSpawnCache.createInstance(this);
        registerEvents();
        registerCommands();
        //updateChecker();
    }

    /**
     * Plugin disable logic.
     */
    @Override
    public void onDisable() {
        send("&cDisabled&f. Version: &e" + version);
        send("&fThank you for using my plugin! &" + color + pdfFile.getName() + "&f By " + pdfFile.getAuthors().get(0));
        send("&fJoin my discord server at &chttps://discordapp.com/invite/ZznhQud");
        send("Please consider subscribing to my yt channel: &c" + pdfFile.getWebsite());
        RandomSpawnCache.getInstance().saveToFile();
    }

//TODO: Update checker
//
//    private void updateChecker(){
//        try {
//            HttpURLConnection con = (HttpURLConnection) new URL(
//                    "https://api.spigotmc.org/legacy/update.php?resource=71422").openConnection();
//            final int timed_out = 1250;
//            con.setConnectTimeout(timed_out);
//            con.setReadTimeout(timed_out);
//            latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
//            if (latestVersion.length() <= 7) {
//                if(!version.equals(latestVersion)){
//                    String exclamation = "&e&l(&4&l!&e&l)";
//                    send(exclamation +" &cThere is a new version available. &e(&7"+latestVersion+"&e)");
//                    send(exclamation +" &cDownload it here: &fhttp://bit.ly/2Pl4Rg7");
//                }
//            }
//        } catch (Exception ex) {
//            send("&cThere was an error while checking for updates");
//        }
//    }


    /**
     * Gets the plugins current version.
     * @return The version string.
     */
    public String getVersion() {
        return this.version;
    }

//    /**
//     * Gets the latest version available from spigot.
//     * @return The latest version or null.
//     */
//    public String getLatestVersion() {
//        return this.latestVersion;
//    }



    /**
     * Registers and reloads plugin files.
     */
    public void reloadFiles() {
        configYaml = new YamlFile(this, "config.yml");
        messagesYaml = new YamlFile(this, "messages.yml");
        cacheYaml = new YamlFile(this, "cache.yml");
    }


    /**
     * Registers the event listeners.
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        //pm.registerEvents(new Event(this), this);
    }


    /**
     * Registers commands and command classes.
     */
    private void registerCommands() {
        PluginCommand mainCommand = getCommand("areaSpawner");

        if(mainCommand == null){
            send("&cERROR while registering plugin commands. Please check your plugin.yml file is intact.");
            send("&cDisabling the plugin.");
            setEnabled(false);
            return;
        }

        mainCommand.setExecutor(new MainCommand(this));

    }

    /**
     * Gets the config file from the YamlFile object.
     * @return The config FileConfiguration inside the config YamlFile object.
     */
    @Override
    public FileConfiguration getConfig(){
        return getConfigYaml().getAccess();
    }

    /**
     * Get the config YamlFile.
     * @return The YamlFile containing the config fileConfiguration.
     */
    public YamlFile getConfigYaml(){
        return this.configYaml;
    }

    /**
     * Get the messages YamlFile.
     * @return The YamlFile containing the messages fileConfiguration.
     */
    public YamlFile getMessagesYaml(){
        return this.messagesYaml;
    }

    /**
     * Get the cache YamlFile.
     * @return The YamlFile containing the cache fileConfiguration.
     */
    public YamlFile getCacheYaml(){
        return this.cacheYaml;
    }



}
