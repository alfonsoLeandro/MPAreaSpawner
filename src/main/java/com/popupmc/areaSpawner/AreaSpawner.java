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
package com.popupmc.areaSpawner;

import com.popupmc.areaSpawner.commands.MainCommand;
import com.popupmc.areaSpawner.utils.RandomSpawnCache;
import com.popupmc.areaSpawner.utils.YamlFile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class AreaSpawner extends JavaPlugin {

    final private PluginDescriptionFile pdfFile = getDescription();
    final private String version = pdfFile.getVersion();
//    private String latestVersion; TODO
    final private char color = 'e';
    final private String name = "&f[&" + color + pdfFile.getName() + "&f]";
    private YamlFile configYaml;
    private YamlFile messagesYaml;

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
        registerEvents();
        registerCommands();
        RandomSpawnCache.createInstance(this);
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



}
