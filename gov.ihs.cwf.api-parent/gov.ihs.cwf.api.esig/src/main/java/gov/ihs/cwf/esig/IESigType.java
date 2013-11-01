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
 * Interface for interacting with an esignature type.
 * 
 * 
 */
public interface IESigType {
    
    String getESigTypeId(); // Return unique id for esig type
    
    String getESigTypeGroupHeader(); // Return header for grouping esig items
    
    void loadESigItems(List<ESigItem> items); // Load items pending signature
    
    void validateESigItems(List<ESigItem> items); // Called prior to signing
    
    void signESigItems(List<ESigItem> items, String esig); // Called to apply signature
    
    boolean requiresReview(); // Return true if items require review
    
    @Override
    boolean equals(Object object); // Implements equality check for esig types
}
