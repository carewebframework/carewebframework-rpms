/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.patientlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.IRegisterEvent;
import org.carewebframework.api.spring.SpringUtil;

/**
 * Registry for all patient lists.
 * 
 * @author dmartin
 */
public class PatientListRegistry implements IRegisterEvent, IPatientListRegistry {
    
    private static final Log log = LogFactory.getLog(PatientListRegistry.class);
    
    /**
     * Comparator for sorting patient lists by sequence number. Lists with the same sequence number
     * will sort alphabetically by name.
     */
    private static class PatientListComparator implements Comparator<IPatientList> {
        
        @Override
        public int compare(IPatientList list1, IPatientList list2) {
            if (list1 == list2) {
                return 0;
            }
            
            if (list1 == null) {
                return -1;
            }
            
            if (list2 == null) {
                return 1;
            }
            
            if (list1.getSequence() == list2.getSequence()) {
                return list1.getName().compareToIgnoreCase(list2.getName());
            }
            
            return list1.getSequence() < list2.getSequence() ? -1 : 1;
        }
        
    }
    
    private static final PatientListComparator patientListComparator = new PatientListComparator();
    
    private final List<IPatientList> patientList = new ArrayList<IPatientList>();
    
    private final Map<String, IPatientList> patientListIndex = new HashMap<String, IPatientList>();
    
    private boolean needsSorting;
    
    /**
     * Returns a reference to the patient list registry.
     * 
     * @return Patient list registry instance.
     */
    public static IPatientListRegistry getInstance() {
        return SpringUtil.getBean("patientListRegistry", IPatientListRegistry.class);
    }
    
    public PatientListRegistry() {
    }
    
    /**
     * Called when an object is registered to the framework. If this object is a patient list, add
     * it to this registry.
     */
    @Override
    public void registerObject(Object object) {
        if (object instanceof IPatientList) {
            IPatientList list = (IPatientList) object;
            String name = list.getName();
            
            if (findByName(name) != null) {
                log.warn("A patient list named '" + name + "' has already been registered.");
            } else {
                needsSorting = true;
                patientList.add(list);
                patientListIndex.put(name, list);
            }
        }
    }
    
    /**
     * Called when an object is unregistered from the framework. If this object is a patient list,
     * remove it from this registry.
     */
    @Override
    public void unregisterObject(Object object) {
        if (object instanceof IPatientList) {
            IPatientList list = (IPatientList) object;
            patientList.remove(list);
            patientListIndex.remove(list.getName());
        }
    }
    
    /**
     * Returns an iterator for iterating across all registered patient lists.
     */
    @Override
    public Iterator<IPatientList> iterator() {
        if (needsSorting) {
            sort();
        }
        
        return patientList.iterator();
    }
    
    /**
     * Sort the list, if necessary, using the supplied comparator.
     */
    private synchronized void sort() {
        if (needsSorting) {
            needsSorting = false;
            Collections.sort(patientList, patientListComparator);
        }
    }
    
    /**
     * Looks up a patient list by its name.
     * 
     * @param name Name of the list being sought.
     * @return Instance of a patient list, or null if none matching the specified name is found.
     */
    @Override
    public IPatientList findByName(String name) {
        return patientListIndex.get(name);
    }
}
