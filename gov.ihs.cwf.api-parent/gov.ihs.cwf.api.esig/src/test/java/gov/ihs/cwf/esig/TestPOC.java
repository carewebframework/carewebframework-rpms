/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.esig;

import gov.ihs.cwf.esig.ESigItem.SignState;

public class TestPOC extends TestType {
    
    private static final String[] text = { "Blood Pressure: 120/82", "Heart Rate: 88", "Respiratory Rate: 12",
            "Weight: 183 lbs", "Fingerstick BS: 120" };
    
    private int index = 0;
    
    public TestPOC() throws Exception {
        super("POC", "Encounter Data");
    }
    
    @Override
    protected String getText() {
        if (index >= text.length) {
            index = 0;
        }
        
        return text[index++];
    }
    
    @Override
    public ESigItem newItem() {
        ESigItem item = super.newItem();
        item.setSignState(SignState.FORCED_YES);
        return item;
    }
    
}
