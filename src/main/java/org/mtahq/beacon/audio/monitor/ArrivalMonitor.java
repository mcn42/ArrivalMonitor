/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mtahq.beacon.audio.monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author mnilsen
 */
public class ArrivalMonitor {

    private static final long APPROACHING_LIMIT_MILLIS = 1000 * 45L;
    private static final int STALE_APPROACHING_MESSAGE = -10000;
    private static final long MAXIMUM_MESSAGE_TIME = 50 * 60 * 1000L; // Fifty minutes

    private AppPropertyManager props = new AppPropertyManager();
    private Timer monitorTimer = new Timer();
    private Date lastUpdate;

    private boolean httpsMode = false;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss zzzz", Locale.US);

    public ArrivalMonitor() {
        super();
        Locale.setDefault(new Locale("en", "US"));
        TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
        Utils.getLogger().log(Level.INFO, "Setting Time Zone: {0}", TimeZone.getDefault().getDisplayName());
        try {

            URL url = new URL(buildDataUrl());
            this.httpsMode = (url.getProtocol().equals("https"));
            Utils.getLogger().info(String.format("HTTPS mode set to %s", this.httpsMode));

        } catch (MalformedURLException e) {
            Utils.getLogger()
                    .log(Level.SEVERE, String.format("Could not test for HTTPS: '%s'", props.getPropertyValue(AppProperty.DATA_URL)), e);
        }

        //  TODO REMOVE THIS TEMPORARY FIX WHEN SLL CERT IS FIXED
        if (this.httpsMode) {
            HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

                public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                    return hostname.endsWith("amazonaws.com");
                }
            });
        }

        start();
    }

    private String buildDataUrl() {
        String u = String.format("%s%s", props.getPropertyValue(AppProperty.DATA_URL), props.getPropertyValue(AppProperty.STATION_ID));
        return u;
    }

    public void start() {
        long period = Long.valueOf(props.getPropertyValue(AppProperty.ARRIVAL_POLLING_PERIOD));
        monitorTimer.schedule(new PollingTask(), 5 * 1000L, period);
    }

    public void stop() {
        monitorTimer.cancel();
    }

    class PollingTask extends TimerTask {

        @Override
        public void run() {
            Utils.getLogger().info("Retrieving Arrival data...");
            retrieveData();
        }
    }

    public static void main(String[] args) {
        new ArrivalMonitor();
    }

    private void retrieveData() {
        try {

            URL url = new URL(buildDataUrl());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            List<String[]> myEntries = new ArrayList<>();
            StringBuffer buff = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                buff.append(line);
                line = reader.readLine();
            }
            try {
                this.parseData(buff.toString());
            } catch (JSONException e) {
                Utils.getLogger().log(Level.SEVERE, "JSON parsing error", e);
            }
            reader.close();
            this.lastUpdate = new Date();
        } catch (MalformedURLException e) {
            Utils.getLogger().log(Level.SEVERE, String.format("Invalid data URL: '%s'", this.buildDataUrl()), e);
        } catch (IOException e) {
            Utils.getLogger()
                    .log(Level.SEVERE, String.format("Data retrieval IO Error for URL: '%s'", this.buildDataUrl()),
                            e);
        }

    }

    /**
     * 1 - Station 2 - Direction 3 - Train 4 - Time
     */
    private void parseData(String json) throws JSONException {
        boolean next = true;
        Set<Arrival> linesFound = new HashSet<>();
        StringBuffer buff = new StringBuffer();

        JSONArray jsob = new JSONArray(json);

        for (int i = 0; i < jsob.length(); i++) {
            JSONObject jobj = jsob.getJSONObject(i);
            String line = jobj.getString("line");
            String dest = jobj.getString("destination");
            String time = jobj.getString("time");
            long millis = jobj.getLong("msec");
            Date d = null;
            try {
                d = this.sdf.parse(time.replace("GMT", ""));
            } catch (ParseException e) {
                Utils.getLogger().log(Level.SEVERE, "Bad date: " + time, e);
                continue;
            }
            String direction = jobj.getString("direction");
            buff.append(String.format("Line '%s', Dest. '%s', Time '%s', Direction: '%s', millis: %s\n", line, dest, d,
                    direction, millis));

            Arrival arr = new Arrival(line, direction, dest, d, millis);
            next = !linesFound.contains(arr);
            linesFound.add(arr);

            Integer mins = new Long(TimeUnit.MILLISECONDS.toMinutes(millis)).intValue();

            if (millis > STALE_APPROACHING_MESSAGE && millis <= APPROACHING_LIMIT_MILLIS) {

                buff.append("Skipping repeated 'Approaching' alert\n");

                buff.append(String.format("generated APPROACHING message\n.", "args"));
            }

            if (millis > APPROACHING_LIMIT_MILLIS && millis < MAXIMUM_MESSAGE_TIME) {
                if (mins <= 0) {
                    mins = 1;
                }
                buff.append(String.format("generated WILL ARRIVE message for %s mins., NEXT = %s\n.", mins, next));
            }

        }

        Utils.getLogger().info(buff.toString());

    }

}
