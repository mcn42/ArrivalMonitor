/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mtahq.beacon.audio.monitor;

/**
 *
 * @author mnilsen
 */
public enum AppProperty {
    DATA_URL("https://ec2-52-4-216-190.compute-1.amazonaws.com/clock/CIS?direction=all&countdown="),
    STATION_ID("R17"),
    ARRIVAL_POLLING_PERIOD("5000L"),
    MESSAGE_PERIOD("30000L");
    
    private final String defaultValue;
    
    private AppProperty(String defaultVal)
    {
        this.defaultValue = defaultVal;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
    
}
