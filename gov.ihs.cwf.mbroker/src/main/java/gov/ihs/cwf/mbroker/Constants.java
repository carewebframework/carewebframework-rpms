/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.mbroker;

import java.nio.charset.Charset;

public class Constants {
    
    public static final String TC_RECONNECT = "Connection Lost";
    
    public static final String TC_RECONNECTING = "Reconnecting";
    
    public static final String TX_RECONNECT = "The connection to the host has been terminated.\r\n"
            + "Do you wish to reconnect";
    
    public static final String TX_RECONNECTING = "Attempting to re-establish a connection to the host...";
    
    public static final byte EOD = -1;
    
    public static final String BROKER_VERSION = "1.6.5";
    
    public static final String DEFAULT_APP_ID = "CIAV VUECENTRIC";
    
    public static final Charset UTF8 = Charset.forName("UTF-8");
    
    public static final String JSON_PREFIX = "@json@";
    
    public static final String LINE_SEPARATOR = "\r";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    };
}
