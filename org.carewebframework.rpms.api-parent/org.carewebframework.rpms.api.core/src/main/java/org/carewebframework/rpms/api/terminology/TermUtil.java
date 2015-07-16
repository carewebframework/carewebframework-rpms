/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.terminology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;

/**
 * Static convenience class for accessing terminology services.
 */
public class TermUtil {
    
    private static final String BMX_DELIMITER = "\036";
    
    private static final String BMX_EOD = "\037";
    
    private static final Map<String, TermSubset> subsets = new HashMap<>();
    
    private static final BrokerSession brokerSession = VistAUtil.getBrokerSession();
    
    public static TermSubset getSubset(String id) {
        if (id == null) {
            id = "36"; // SNOMED-CT is the default
        }
        
        if (!subsets.containsKey(id)) {
            initSubset(id);
        }
        
        return subsets.get(id);
    }
    
    private static void initSubset(String id) {
        synchronized (subsets) {
            if (!subsets.containsKey(id)) {
                TermSubset subset = new TermSubset(id, callBMXRPC("BSTS GET SUBSET LIST", id));
                subsets.put(id, subset);
            }
        }
    }
    
    /**
     * Perform lookup of SNOMED CT terms.
     * 
     * @param text Text of term to lookup.
     * @param synonym If true, lookup synonyms. Otherwise, lookup only preferred terms.
     * @param date Reference date for lookup.
     * @param max Maximum hits to return.
     * @param filters Subset filters to apply.
     * @return List of matching terms.
     */
    public static List<TermMatch> lookupSCT(String text, boolean synonym, FMDate date, Long max, String... filters) {
        text = text.replace("|^", "");
        String filter = filters == null ? "" : StrUtil.fromList(Arrays.asList(filters), "~");
        String searchType = synonym ? "S" : "F";
        String dateStr = date == null ? "" : date.getFMDate();
        List<String> results = callBMXRPC("BSTS SNOMED SEARCH", text, searchType, "", filter, dateStr, max);
        List<TermMatch> matches = new ArrayList<>(results.size());
        
        // Note, ignore first entry as it is a fixed header.
        
        for (int i = 1; i < results.size(); i++) {
            matches.add(new TermMatch(results.get(i)));
        }
        
        return matches;
    }
    
    /**
     * Call a BMX-style RPC.
     * 
     * @param rpcName Remote procedure name.
     * @param args Argument list.
     * @return Returned list data.
     */
    private static List<String> callBMXRPC(String rpcName, Object... args) {
        String arg = args == null ? null : StrUtil.toDelimitedStr("|", args);
        List<String> result = StrUtil.toList(brokerSession.callRPC(rpcName, arg), BMX_DELIMITER);
        int last = result.size() - 1;
        
        if (last >= 0 && BMX_EOD.equals(result.get(last))) {
            result.remove(last);
        }
        
        return result;
    }
    
    /**
     * Enforce static class.
     */
    private TermUtil() {
    
    }
}
