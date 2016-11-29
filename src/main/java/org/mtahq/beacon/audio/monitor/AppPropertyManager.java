/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mtahq.beacon.audio.monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mnilsen
 */
public class AppPropertyManager {
    private Map<AppProperty,String> props = new HashMap<>();
    
    public String getPropertyValue(AppProperty propId)
    {
        String s = this.props.get(propId);
        if(s == null) s = propId.getDefaultValue();
        return s;
    }
    
    public void loadProperties()
    {
        Properties p = new Properties();
        try {
            p.load(new FileReader(new File("")));
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppPropertyManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppPropertyManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
