/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.property;

import java.util.List;

public interface IPropertyDAO {
    
    void fetchValue(Property property, List<String> values);
    
    void saveValue(Property property, String entity);
    
    List<String> getInstances(String name, String entity);
    
    List<String> getMatching(String prefix);
    
    PropertyDefinition getDefinition(long id);
    
    PropertyDefinition getDefinition(String name);
}
