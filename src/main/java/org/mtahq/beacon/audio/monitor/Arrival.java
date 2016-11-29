/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mtahq.beacon.audio.monitor;

import java.util.Date;

/**
 *
 * @author mnilsen
 */
class Arrival {
    private String line;
    private String direction;
    private String destinaltion;
    private Date time;
    private long msec;


    public Arrival(String line, String direction, String destinaltion, Date time, long msec) {
        this.line = line;
        this.direction = direction;
        this.destinaltion = destinaltion;
        this.time = time;
        this.msec = msec;
    }

    public String getLine() {
        return line;
    }

    public String getDirection() {
        return direction;
    }

    public String getDestinaltion() {
        return destinaltion;
    }

    public Date getTime() {
        return time;
    }

    public long getMsec() {
        return msec;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Arrival)) {
            return false;
        }
        final Arrival other = (Arrival) object;
        if (!(line == null ? other.line == null : line.equals(other.line))) {
            return false;
        }
        if (!(direction == null ? other.direction == null : direction.equals(other.direction))) {
            return false;
        }
        if (!(destinaltion == null ? other.destinaltion == null : destinaltion.equals(other.destinaltion))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int PRIME = 37;
        int result = 1;
        result = PRIME * result + ((line == null) ? 0 : line.hashCode());
        result = PRIME * result + ((direction == null) ? 0 : direction.hashCode());
        result = PRIME * result + ((destinaltion == null) ? 0 : destinaltion.hashCode());
        return result;
    }
    
    public Long getAgeInMillis() {
        return System.currentTimeMillis() - this.time.getTime();
    }
}
