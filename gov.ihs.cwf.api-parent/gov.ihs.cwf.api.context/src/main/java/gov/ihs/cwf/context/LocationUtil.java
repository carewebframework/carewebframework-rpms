/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.context;

import org.carewebframework.vista.api.domain.Location;

import java.util.List;

/**
 * Location-related utility functions.
 * 
 * 
 */
public class LocationUtil {
    
    /**
     * Lookup a location by partial name. Matches an occurrence of lookup string anywhere within the
     * location name.
     * 
     * @param lookup Partial name for lookup.
     * @return List of service locations matching the lookup.
     * @throws Exception
     */
    public static List<Location> findLocations(String lookup) throws Exception {
        return null;
    }
    
    /**
     * Enforces static class.
     */
    private LocationUtil() {
    };
    
}
