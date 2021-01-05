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

/**
 * Class for representing a conventional time unit.
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
                return TimeUnit.MINUTES;
            case 'h':
            case 'H':
                return TimeUnit.HOURS;
            case 'd':
            case 'D':
                return TimeUnit.DAYS;
            default:
                return TimeUnit.SECONDS;
        }
    }
}
