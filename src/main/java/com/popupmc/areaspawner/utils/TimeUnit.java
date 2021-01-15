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

import com.popupmc.areaspawner.AreaSpawner;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for representing a conventional time unit.
 *
 * @author lelesape
 */
public enum TimeUnit {

    /**
     * Seconds, conformed by 20 ticks.
     */
    SECONDS( 20),
    /**
     * Minutes, conformed by 60 seconds, 1,200 ticks.
     */
    MINUTES( 1200),
    /**
     * Hours, conformed by 60 minutes, 1,200 seconds, 72,000 ticks.
     */
    HOURS( 72000),
    /**
     * Days, conformed by 24 hours, 3,600 minutes, 216,000 seconds, 1,728,000 ticks.
     */
    DAYS( 1_728_000);


    /**
     * The value that a value needs to be multiplied to transform to ticks.
     */
    private final int multiplier;

    /**
     * Represents a conventional time unit.
     * @param multiplier The value that a value needs to be multiplied to transform to ticks.
     */
    TimeUnit(int multiplier){
        this.multiplier = multiplier;
    }

    /**
     * Gets the value that a value needs to be multiplied to transform to ticks.
     * @return The value that a value needs to be multiplied to transform to ticks.
     */
    public int getMultiplier(){
        return this.multiplier;
    }


    /**
     * Gets a timeUnit by its alias.
     * @param alias The alias the timeunit is known by.
     * @return The timeUnit
     */
    public static TimeUnit getByAlias(char alias){
        switch (alias){
            case 'm':
            case 'M':
                return MINUTES;
            case 'h':
            case 'H':
                return HOURS;
            case 'd':
            case 'D':
                return DAYS;
            default:
                return SECONDS;
        }
    }


    /**
     * Gets the amount of ticks a given amount of time of the given unit represents.
     * @param amount The amount of time of the given time unit.
     * @param timeUnit The timeunit for the given amount.
     * @return The value in ticks of the given time amount.
     */
    public static int getTicks(int amount, TimeUnit timeUnit){
        return amount * timeUnit.getMultiplier();
    }

    /**
     * Gets the amount of ticks a given amount of time of the given unit represents.
     * @param amount The amount of time of the given time unit.
     * @param timeUnit The char representing the timeunit for the given amount.
     * @return The value in ticks of the given time amount.
     */
    public static int getTicks(int amount, char timeUnit){
        return getTicks(amount, TimeUnit.getByAlias(timeUnit));
    }

    /**
     * Translates and amount of ticks into days, hours, minutes and seconds.
     * @param ticks The amount of ticks to translate
     * @return A string with an d,h,m and s format.
     */
    public static String getTimeString(long ticks){
        FileConfiguration messages = JavaPlugin.getPlugin(AreaSpawner.class).getMessagesYaml().getAccess();
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        long days = getDays(ticks);
        long hours = getHours(ticks);
        long minutes = getMinutes(ticks);
        long seconds = getSeconds(ticks);


        if(days > 0){
            String s = days + " " +
                    (days > 1 ? messages.getString("messages.days") : messages.getString("messages.day"));

            args.add(s);
        }

        if(hours > 0){
            String s = hours + " " +
                    (hours > 1 ? messages.getString("messages.hours") : messages.getString("messages.hour"));

            args.add(s);
        }

        if(minutes > 0){
            String s = minutes + " " +
                    (minutes > 1 ? messages.getString("messages.minutes") : messages.getString("messages.minute"));
            args.add(s);
        }

        String s = (seconds >= 0 ? seconds : 0) + " " +
                (seconds == 0 || seconds > 1 ? messages.getString("messages.seconds") : messages.getString("messages.second"));
        args.add(s);


        for (int i = 0; i < args.size(); i++) {

            if(args.size() > 1 && i != 0) {
                sb.append(i == args.size()-1 ? " "+messages.getString("messages.and")+" " : ", ");
            }

            sb.append(args.get(i));
        }

        return sb.toString();
    }

    /**
     * Gets the total amount of seconds a given amount of ticks represents.
     * @param ticks The ticks to translate to seconds.
     * @return The amount of seconds the given amount of ticks represent.
     */
    public static long getTotalSeconds(long ticks){
        return ticks/20;
    }

    /**
     * Gets only the amount of seconds (between 0 and 60) an amount of ticks represent.
     * @param ticks The amount of ticks.
     * @return A number between 0 and 60 representing the seconds for the given amount of ticks.
     */
    public static long getSeconds(long ticks){
        return java.util.concurrent.TimeUnit.SECONDS.toSeconds(getTotalSeconds(ticks)) - java.util.concurrent.TimeUnit.MINUTES.toSeconds(getMinutes(ticks)) - java.util.concurrent.TimeUnit.HOURS.toSeconds(getHours(ticks)) - java.util.concurrent.TimeUnit.DAYS.toSeconds(getDays(ticks));
    }

    /**
     * Gets only the amount of minutes (between 0 and 60) an amount of ticks represent.
     * @param ticks The amount of ticks.
     * @return A number between 0 and 60 representing the minutes for the given amount of ticks.
     */
    public static long getMinutes(long ticks){
        return java.util.concurrent.TimeUnit.SECONDS.toMinutes(getTotalSeconds(ticks)) - java.util.concurrent.TimeUnit.HOURS.toMinutes(getHours(ticks)) - java.util.concurrent.TimeUnit.DAYS.toMinutes(getDays(ticks));
    }

    /**
     * Gets only the amount of hours (between 0 and 60) an amount of ticks represent.
     * @param ticks The amount of ticks.
     * @return A number between 0 and 60 representing the hours for the given amount of ticks.
     */
    public static long getHours(long ticks){
        return java.util.concurrent.TimeUnit.SECONDS.toHours(getTotalSeconds(ticks)) - java.util.concurrent.TimeUnit.DAYS.toHours(getDays(ticks));
    }

    /**
     * Gets only the amount of days (between 0 and 24) an amount of ticks represent.
     * @param ticks The amount of ticks.
     * @return A number between 0 and 24 representing the days for the given amount of ticks.
     */
    public static long getDays(long ticks){
        return java.util.concurrent.TimeUnit.SECONDS.toDays(getTotalSeconds(ticks));
    }
}
