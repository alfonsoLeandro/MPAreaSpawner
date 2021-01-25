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
package com.popupmc.areaspawner.spawn;


import com.popupmc.areaspawner.utils.Settings;
import com.popupmc.areaspawner.utils.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;

import java.util.Random;

/**
 * 3D region (x, y, z) for checking location of points inside/outside given areas.
 *
 * @author lelesape
 */
public class Region {

    /**
     * The first point's x coordinate.
     */
    private final int minX;
    /**
     * The seconds point's (opposite to the first point) x coordinate.
     */
    private final int maxX;
    /**
     * The first point's y coordinate.
     */
    private final int minY;
    /**
     * The seconds point's (opposite to the first point) y coordinate.
     */
    private final int maxY;
    /**
     * The first point's z coordinate.
     */
    private final int minZ;
    /**
     * The seconds point's (opposite to the first point) z coordinate.
     */
    private final int maxZ;


    /**
     * Makes a new region using ranges instead of concrete points.
     * i.e: xCenter 0 and xRange 500 is a new region which has a first point with an x value of -500 and
     * a second point with an x value of 500.
     * @param xCenter The center point's x value.
     * @param yCenter The center point's y value.
     * @param zCenter The center point's z value.
     * @param xRange The x coordinate range.
     * @param yRange The y coordinate range.
     * @param zRange The z coordinate range.
     * @return A new region with the given coordinates and ranges.
     */
    public static Region newRegionByRanges(int xCenter, int yCenter, int zCenter, int xRange, int yRange, int zRange){
        return new Region(xCenter-xRange,
                xCenter+xRange,
                yCenter-yRange,
                yCenter+yRange,
                zCenter-zRange,
                zCenter+zRange);
    }


    /**
     * Creates a new 3D (x, y, z) region with the given coordinates.
     * @param minX The first point's x coordinate.
     * @param maxX The second point's x coordinate.
     * @param minY The first point's y coordinate.
     * @param maxY The second point's y coordinate.
     * @param minZ The first point's x coordinate.
     * @param maxZ The second point's z coordinate.
     */
    public Region(int minX, int maxX, int minY, int maxY, int minZ, int maxZ){
        Logger.debug("New region created with the following coordinates");
        Logger.debug("X: "+minX+" - "+maxX);
        Logger.debug("Y: "+minY+" - "+maxY);
        Logger.debug("Z: "+minZ+" - "+maxZ);
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }



    /**
     * Checks if a point (x, y, z) is within this region.
     * @param pointX The point's x value.
     * @param pointY The point's y value.
     * @param pointZ The point's z value.
     * @return true if the point is within the given region.
     */
    public boolean contains(int pointX, int pointY, int pointZ){
        return (pointX <= maxX && pointX >= minX) &&
                (pointY <= maxY && pointY >= minY)&&
                (pointZ <= maxZ && pointZ >= minZ);
    }

    /**
     * Checks if a point (x, z) is within this region.
     * @param pointX The point's x value.
     * @param pointZ The point's z value.
     * @return true if the point is within the given region in the 2D plane.
     */
    public boolean contains2D(int pointX, int pointZ){
        return (pointX <= maxX && pointX >= minX) &&
                (pointZ <= maxZ && pointZ >= minZ);
    }

    /**
     * Checks if another region is entirely contained in this region.
     * @param region The region to check if is inside this region.
     * @return true if the region is entirely contained inside this region.
     */
    public boolean contains(Region region){
        return contains(region.getMaxX(), region.getMaxY(), region.getMaxZ()) && contains(region.getMinX(), region.getMinY(), region.getMinZ());
    }

    /**
     * Generates a new random location within this region, taking into account the forbidden region,
     * if none is defined, an empty region can be passed in its place (new Region(0,0,0,0,0,0)).
     * @param forbidden The region to avoid putting spawnpoints in.
     * @return A guaranteed safe location (according to config settings defined by the server admin).
     */
    public Location generateNewLocation(Region forbidden){
        Random r = new Random();
        Settings settings = Settings.getInstance();

        Location making = new Location(settings.getWorld(), 0, -10, 0);
        int attempts = Settings.getInstance().getFindSafeLocationAttempts();

        //Make x amount of attempts before giving up and calculating the next one
        for (int i = 1; i <= attempts ; i++) {
            Logger.debug("&eAttempt number " + i + " to generate location.");

            making.setX(r.nextInt(this.getMaxX() - this.getMinX()) + this.getMinX());
            making.setZ(r.nextInt(this.getMaxZ() - this.getMinZ()) + this.getMinZ());
            setYValue(making);

            if(isValidLocation(making, forbidden, this)) {
                Logger.debug("&aSafe valid location achieved!");
                return making;
            }


            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        return null;
    }


    /**
     * Finds a suitable Y value for the given X,Z coordinates, or sets it to a ridiculous number if none found.
     * In the process it makes sure there is at least 1 block of air gap, and the location's block is safe.
     * @param loc The location to part from.
     */
    private void setYValue(Location loc){
        Settings settings = Settings.getInstance();

        if(settings.isTopToBottom()) {
            //Top to bottom
            for (int i = this.maxY; i > this.minY; i--) {
                loc.setY(i);
                if(loc.getBlock().getType().equals(Material.VOID_AIR)) continue;
                if(!loc.getBlock().getType().equals(Material.AIR)) {
                    if(isSafeBlock(loc.getBlock().getType().toString())) {
                        loc.setY(loc.getBlockY()+1);
                        return;
                    }else {
                        if(settings.isNotCheckPastSurface()) break;
                    }

                }
            }

        }else {
            //Bottom to top
            for (int i = this.minY; i < this.maxY; i++) {
                loc.setY(i);
                if(loc.getBlock().getType().equals(Material.VOID_AIR)) continue;
                //If the block is not air and the block above is air
                if(!loc.getBlock().getType().equals(Material.AIR) && loc.getBlock().getRelative(BlockFace.UP).getType().equals(Material.AIR)) {
                    if(isSafeBlock(loc.getBlock().getType().toString())){
                        loc.setY(loc.getBlockY()+1);
                        return;
                    }else {
                        if(settings.isNotCheckPastSurface()) break;
                    }
                }
            }

        }

        loc.setY(-9999);
    }

    /**
     * Checks if the block is safe according to config criteria.
     * @param block The block to check.
     * @return true if the list is a whitelist and the block is contained in the list,
     * false if the list is a blacklist and the block is contained in the list or the list is
     * a whitelist and the block is not contained in the list.
     */
    private static boolean isSafeBlock(String block) {
        Settings settings = Settings.getInstance();

        if(settings.isListIsWhitelist()) return settings.getBlockList().contains(block);

        return !settings.getBlockList().contains(block);

    }


    public Region chooseRandomQuadrant(){
        int quadrant = new Random().nextInt(4);

        int xCenter = (this.minX+this.maxX)/2;
        int zCenter = (this.minZ+this.maxZ)/2;

        if(quadrant == 0){
            Logger.debug("The first quadrant is the chosen quadrant");
            return new Region(xCenter, this.maxX,
                    this.minY,this.maxY,
                    this.minZ , zCenter);

        }else if(quadrant == 1){
            Logger.debug("The second quadrant is the chosen quadrant");
            return new Region(xCenter, this.maxX,
                    this.minY, this.maxY,
                    zCenter, this.maxZ);

        }else if(quadrant == 2){
            Logger.debug("The third quadrant is the chosen quadrant");
            return new Region( this.minX, xCenter,
                    this.minY, this.maxY,
                    this.minZ, zCenter);
        }
        Logger.debug("The fourth quadrant is the chosen quadrant");
        return new Region(this.minX, xCenter,
                this.minY, this.maxY,
                zCenter, this.maxZ);

    }


    /**
     * Checks if a location follows a number of steps for considering it "safe" enough for a player to spawn in.
     * @param loc The location to analyze.
     * @param forbidden The "forbidden" spawn region for this location's world.
     * @return true if the location passed every check.
     */
    public static boolean isValidLocation(Location loc, Region forbidden, Region allowed){
        //Steps for getting a safe location:
        // 1. y is greater than 0 and lesser than 255.
        // 2. x and z are within the spawn region and outside no-spawn region.
        // 3. there is a block air gap above location.
        // 4. block is not in blacklist.
        // 5. block is in whitelist or blocks not in whitelist are safe.
        String blockType = loc.getBlock().getType().toString();

        if(loc.getY() < 1 || loc.getY() > 255){
            Logger.debug("&cNo non-air, non-void block found.");
            return false;
        }
        if(forbidden.contains2D(loc.getBlockX(), loc.getBlockZ())){
            Logger.debug("&cLocation is in no-spawn region.");
            return false;
        }
        if(!allowed.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())){
            Logger.debug("&cLocation is not in allowed region.");
            return false;
        }
        if(!hasAirGap(loc)){
            Logger.debug("&cThe air gap was not tall enough, or there were none at all.");
            return false;
        }
        if(!isSafeBlock(blockType)){
            Logger.debug("&cBlock "+blockType+" is not considered safe.");
            return false;
        }


        return true;
    }


    /**
     * Checks if the given location has the required number of air blocks above.
     * @param loc The location to check for air gap.
     * @return True if the location has the necessary amount of air gap above.
     */
    private static boolean hasAirGap(Location loc){
        int airGap = Settings.getInstance().getAirGapAbove();

        for (int i = 1; i <= airGap ; i++) {
            if(!loc.getBlock().getRelative(BlockFace.UP, i).getType().equals(Material.AIR)){
                return false;
            }
        }

        return true;
    }



    /**
     * Gets this region's first point x coordinate.
     * @return The value for the x coordinate of the first point for this region.
     */
    public int getMinX() {
        return minX;
    }

    /**
     * Gets this region's second point x coordinate.
     * @return The value for the x coordinate of the second point for this region.
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Gets this region's first point z coordinate.
     * @return The value for the z coordinate of the first point for this region.
     */
    public int getMinZ() {
        return minZ;
    }

    /**
     * Gets this region's second point z coordinate.
     * @return The value for the z coordinate of the second point for this region.
     */
    public int getMaxZ() {
        return maxZ;
    }

    /**
     * Gets this region's first point y coordinate.
     * @return The value for the y coordinate of the first point for this region.
     */
    public int getMinY() {
        return minY;
    }

    /**
     * Gets this region's second point y coordinate.
     * @return The value for the y coordinate of the second point for this region.
     */
    public int getMaxY() {
        return maxY;
    }



}
