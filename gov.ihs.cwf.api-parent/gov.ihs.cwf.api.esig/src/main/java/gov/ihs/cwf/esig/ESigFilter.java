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

import java.util.List;

/**
 * Filter used to subselect items returned from an esig list.
 * 
 * @author dmartin
 */
public class ESigFilter {
    
    private final List<String> ids;
    
    private final IESigType eSigType;
    
    private final String session;
    
    private final Boolean selected;
    
    /**
     * Creates a filter. Pass null for those criteria to be ignored.
     * 
     * @param eSigType ESig type to match.
     * @param session Session id to match.
     * @param ids Unique item id's to match (should always be used with eSigType specified).
     * @param selected Selection state to match.
     */
    public ESigFilter(IESigType eSigType, String session, List<String> ids, Boolean selected) {
        this.eSigType = eSigType;
        this.session = session;
        this.ids = ids;
        this.selected = selected;
    }
    
    /**
     * Returns true if the item matches the selection filter.
     * 
     * @param item
     * @return True if the item matches the selection filter.
     */
    public boolean matches(ESigItem item) {
        if (selected != null && item.isSelected() != selected) {
            return false;
        }
        
        if (eSigType != null && !item.getESigType().equals(eSigType)) {
            return false;
        }
        
        if (session != null && !item.getSession().equals(session)) {
            return false;
        }
        
        if (ids != null && !ids.contains(item.getId())) {
            return false;
        }
        
        return true;
    }
    
}
