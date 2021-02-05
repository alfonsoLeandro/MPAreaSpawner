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
import com.popupmc.areaspawner.commands.MainCommandTabAutoCompleter;
import com.popupmc.areaspawner.commands.TravelCommand;
import com.popupmc.areaspawner.commands.TravelCommandTabAutoCompleter;
import com.popupmc.areaspawner.events.FirstJoinEvent;
import com.popupmc.areaspawner.events.PlayerDieEvent;
import com.popupmc.areaspawner.events.PlayerJoinUpdateCheck;
import com.popupmc.areaspawner.spawn.RandomSpawnCache;
import com.popupmc.areaspawner.utils.Logger;
import com.popupmc.areaspawner.utils.Settings;
import com.popupmc.areaspawner.utils.TravelCooldownManager;
import com.popupmc.areaspawner.utils.YamlFile;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * AreaSpawner main class. In charge of registering any plugin-server interaction.
 *
 * @author lelesape
 */
public final class AreaSpawner extends JavaPlugin {

    final private PluginDescriptionFile pdfFile = getDescription();
    final private String version = pdfFile.getVersion();
    private String latestVersion;
    final private char color = 'e';
    final private String name = "&f[&" + color + pdfFile.getName() + "&f]";
    private TravelCommand travelCommand;
    private Economy econ;
    private Permission perms;
    private YamlFile configYaml;
    private YamlFile messagesYaml;
    private YamlFile cacheYaml;
    private YamlFile cooldownYaml;

    /**
     * Sends a message to the console, with colors and prefix added.
     *
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
        checkFilesFields();
        Settings.createInstance(this);
        checkDangerousSettings();
        RandomSpawnCache.createInstance(this);
        TravelCooldownManager.createInstance(this);
        if(setupEconomy()) {
            Logger.send("&aEconomy hooked successfully.");
        } else {
            Logger.send("&cVault or a vault supported economy plugin has not been found.");
            Logger.send("&cEconomy features disabled.");
        }
        if(setupPermissions()) {
            Logger.send("&aPermissions hooked successfully.");
        } else {
            Logger.send("&cVault or a vault supported permissions plugin has not been found.");
            Logger.send("&cPermissions features disabled.");
        }
        registerEvents();
        registerCommands();
        updateChecker();
    }

    /**
     * Plugin disable logic.
     */
    @Override
    public void onDisable() {
        if(RandomSpawnCache.getInstance() != null) RandomSpawnCache.getInstance().saveToFile();
        send("&cDisabled&f. Version: &e" + version);
        send("&fThank you for using my plugin! &" + color + pdfFile.getName() + "&f By " + pdfFile.getAuthors().get(0));
        send("&fJoin my discord server at &chttps://discordapp.com/invite/ZznhQud");
        send("Please consider subscribing to my yt channel: &c" + pdfFile.getWebsite());
    }

    public boolean setupEconomy() {
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if(vault == null || !vault.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if(rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public boolean setupPermissions() {
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if(vault == null || !vault.isEnabled()) {
            return false;
        }
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if(rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return true;
    }


    private void updateChecker(){
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=88263").openConnection();
            final int timed_out = 1250;
            con.setConnectTimeout(timed_out);
            con.setReadTimeout(timed_out);
            latestVersion = new BufferedReader(new InputStreamReader(con.getInputStream())).readLine();
            if (latestVersion.length() <= 7) {
                if(!version.equals(latestVersion)){
                    String exclamation = "&e&l(&4&l!&e&l)";
                    send(exclamation +" &cThere is a new version available. &e(&7"+latestVersion+"&e)");
                    send(exclamation +" &cDownload it here: &fhttp://bit.ly/areaSpawnerUpdate");
                }
            }
        } catch (Exception ex) {
            send("&cThere was an error while checking for updates");
        }
    }


    /**
     * Gets the plugins current version.
     * @return The version string.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest version available from spigot.
     * @return The latest version or null.
     */
    public String getLatestVersion() {
        return this.latestVersion;
    }


    /**
     * Registers and reloads plugin files.
     */
    public void reloadFiles() {
        configYaml = new YamlFile(this, "config.yml");
        messagesYaml = new YamlFile(this, "messages.yml");
        cacheYaml = new YamlFile(this, "cache.yml");
        cooldownYaml = new YamlFile(this, "travel cooldown.yml");
    }

    /**
     * Re-loads the plugin.
     */
    public void reload(){
        reloadFiles();
        Settings.getInstance().reloadFields();
        RandomSpawnCache.getInstance().reValidateSpawns();
        checkDangerousSettings();
        this.travelCommand.loadMessages();
    }


    private void checkFilesFields(){
        FileConfiguration messagesEndFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
        FileConfiguration messages = getMessagesYaml().getAccess();

        if(!messagesEndFile.contains("messages.teleported to home")) {
            messages.set("messages.teleported to home", "&aYou have been teleported to your essentials home.");
            messagesYaml.save();
        }

        FileConfiguration configEndFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        FileConfiguration config = getConfigYaml().getAccess();

        if(!configEndFile.contains("time between location attempts")){
            config.set("time between location attempts", "5T");
            configYaml.save();
        }

    }

    /**
     * Registers the event listeners.
     */
    private void registerEvents() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new FirstJoinEvent(this), this);
        pm.registerEvents(new PlayerDieEvent(this), this);
        pm.registerEvents(new PlayerJoinUpdateCheck(this), this);
    }


    /**
     * Registers commands and command classes.
     */
    private void registerCommands() {
        PluginCommand mainCommand = getCommand("areaSpawner");
        PluginCommand travelCommand = getCommand("randomSpawn");

        if(mainCommand == null || travelCommand == null){
            send("&cERROR while registering plugin commands. Please check your plugin.yml file is correct.");
            send("&cDisabling the plugin.");
            setEnabled(false);
            return;
        }

        mainCommand.setExecutor(new MainCommand(this));
        mainCommand.setTabCompleter(new MainCommandTabAutoCompleter());
        this.travelCommand = new TravelCommand(this);
        travelCommand.setExecutor(this.travelCommand);
        travelCommand.setTabCompleter(new TravelCommandTabAutoCompleter());
    }

    /**
     * Checks for dangerous settings, settings that could affect player experience in a bad way or that
     * can result in slowing the server down or not letting this plugins work properly.
     */
    public void checkDangerousSettings(){
        Settings fields = Settings.getInstance();

        if(fields.getWorldName() == null || fields.getWorld() == null){
            send("&cERROR &f- World is null, please check your world name in config");
            send("&fDisabling AreaSpawner...");
            setEnabled(false);
            return;
        }
        if(fields.getForbiddenRegion().contains(fields.getAllowedRegion())){
            send("&cERROR &f- The safe region spawn is inside and smaller than the forbidden region spawn");
            send("&fDisabling AreaSpawner...");
            setEnabled(false);
            return;
        }


        if(!getConfig().getBoolean("spawn zone.clamp to limits")){
            send("&eWARNING &f- Clamp to limits is set to false in config, players may be able to spawn off limits in some cases.");
        }
        if(fields.getAirGapAbove() < 2){
            send("&eWARNING &f- Air gap above is set to a value lower than 2 (player height), players may suffocate in walls.");
        }
        if(!fields.isCacheEnabled()){
            send("&eWARNING &f- Location cache is disabled. Locations will be calculated on the spot, players may take a while to respawn depending on your other settings.");
        }
        if(!fields.isCheckSafetyOnUse()){
            send("&eWARNING &f- Locations' safety is not checked once again on use. Players might be teleported to unsafe locations that were considered safe.");
        }
    }

    /**
     * Gets the economy object.
     * @return The economy object.
     */
    public Economy getEconomy() {
        return this.econ;
    }

    /**
     * Gets the permission object.
     * @return The permission object.
     */
    public Permission getPerms() {
        return this.perms;
    }

    /**
     * Gets the config file from the YamlFile object.
     * @return The config FileConfiguration inside the config YamlFile object.
     */
    @Override
    public @NotNull FileConfiguration getConfig(){
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

    /**
     * Get the travel cooldown YamlFile.
     * @return The YamlFile containing the travel cooldown fileConfiguration.
     */
    public YamlFile getCooldownYaml(){
        return this.cooldownYaml;
    }

}
