package com.popupmc.areaspawner.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab completer for AreaSpawner's travel command {@link TravelCommand}.
 */
public class TravelCommandTabAutoCompleter implements TabCompleter {

    /**
     * Checks if a string is partially or completely equal to another string
     * @param input The given string.
     * @param string The base string.
     * @return true if the first string is partially or completely equal to the second/base string.
     */
    public boolean equalsToStringUnCompleted(String input, String string){
        for(int i = 0; i < string.length(); i++){
            if(input.equalsIgnoreCase(string.substring(0,i))){
                return true;
            }
        }
        return false;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> possibilities = new ArrayList<>();

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("")) {
                possibilities.add("help");
                possibilities.add("travel");
                possibilities.add("forceTravel");

            }else if(equalsToStringUnCompleted(args[0], "help")) {
                possibilities.add("help");

            } else if(equalsToStringUnCompleted(args[0], "travel")) {
                possibilities.add("travel");

            }else if(equalsToStringUnCompleted(args[0], "forceTravel")) {
                possibilities.add("forceTravel");
            }

        }else if(args.length > 1 && args[0].equalsIgnoreCase("forceTravel")){
            //Null = online player names
            return null;
        }

        return possibilities;
    }

}

