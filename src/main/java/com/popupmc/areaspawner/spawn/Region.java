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

/**
 * 2D region (x,z) for checking location of points inside/outside given areas.
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
     * The first point's z coordinate.
     */
    private final int minZ;
    /**
     * The seconds point's (opposite to the first point) z coordinate.
     */
    private final int maxZ;

    /**
     * Creates a new 2D (x,z) region with the given coordinates.
     * @param minX The first point's x coordinate
     * @param maxX The second point's x coordinate
     * @param minZ The first point's x coordinate
     * @param maxZ The second point's z coordinate
     */
    public Region( int minX, int maxX, int minZ, int maxZ){
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    /**
     * Checks if a point (x,z) is within this region.
     * @param pointX The point's x value.
     * @param pointZ The point's y value.
     * @return true if the point is within the given region.
     */
    public boolean contains(int pointX, int pointZ){
        return (pointX <= maxX && pointX >= minX) && (pointZ <= maxZ && pointZ >= minZ);
    }

    /**
     * Checks if another region is entirely contained in this region.
     * @param region The region to check if is inside this region.
     * @return true if the point is within the given region.
     */
    public boolean contains(Region region){
        return contains(region.getMaxX(), region.getMaxZ()) && contains(region.getMinX(), region.getMinZ());
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



}
