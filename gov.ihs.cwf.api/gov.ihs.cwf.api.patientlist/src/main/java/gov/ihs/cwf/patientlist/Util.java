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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gov.ihs.cwf.domain.Patient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import org.carewebframework.common.StrUtil;

/**
 * Utility methods for patient lists.
 * 
 * @author dmartin
 */
public class Util {
    
    private static final char[] wordDelimiters = new char[] { ' ', ',' };
    
    protected static final String DELIM = "^";
    
    protected static final String REGEX_DELIM = "\\" + DELIM;
    
    /**
     * Finds a list item associated with the specified patient.
     * 
     * @param patient Patient to find.
     * @param items List of items to search.
     * @return The patient list item associated with the specified patient, or null if not found.
     */
    public static PatientListItem findListItem(Patient patient, Iterable<PatientListItem> items) {
        if (items == null || patient == null) {
            return null;
        }
        
        for (PatientListItem item : items) {
            if (item.getPatient() != null && patient.equals(item.getPatient())) {
                return item;
            }
        }
        
        return null;
    }
    
    /**
     * Creates an immutable set from a list of elements.
     * 
     * @param elements Elements to add to the set.
     * @return An immutable set containing the specified elements.
     */
    public static <T> Set<T> createImmutableSet(T... elements) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(elements)));
    }
    
    /**
     * Appends an object to the string builder using the default delimiter.
     * 
     * @param sb String builder to receive value.
     * @param value Value to add to the string builder.
     */
    public static void append(StringBuilder sb, Object value) {
        append(sb, value, DELIM);
    }
    
    /**
     * Appends an object to the string builder using the specified delimiter.
     * 
     * @param sb String builder to receive value.
     * @param value Value to add to the string builder.
     * @param delimiter Delimiter to separate consecutive values.
     */
    public static void append(StringBuilder sb, Object value, String delimiter) {
        if (delimiter != null) {
            sb.append(delimiter);
        }
        
        if (value != null) {
            sb.append(value.toString());
        }
    }
    
    /**
     * Split the specified value using the default delimiter. The result is guaranteed to have the
     * number of elements specified by the pieces parameter. If the specified value has fewer
     * elements than requested, the result array will be expanded with null elements.
     * 
     * @param value Value to split.
     * @param pieces Number of pieces to return.
     * @return An array of string values resulting from the operation.
     */
    public static String[] split(String value, int pieces) {
        return split(value, pieces, REGEX_DELIM);
    }
    
    /**
     * Split the specified value using the specified delimiter. The result is guaranteed to have the
     * number of elements specified by the pieces parameter. If the specified value has fewer
     * elements than requested, the result array will be expanded with null elements.
     * 
     * @param value Value to split.
     * @param pieces Number of pieces to return.
     * @param delimiter The delimiter to use in the split operation.
     * @return An array of string values resulting from the operation.
     */
    public static String[] split(String value, int pieces, String delimiter) {
        String[] pcs = value == null ? new String[pieces] : value.split(delimiter, pieces);
        return pcs.length < pieces ? Arrays.copyOf(pcs, pieces) : pcs;
    }
    
    /**
     * Returns the deserialized form of a list.
     * 
     * @param serializedValue
     * @return Deserialized patient list.
     */
    public static IPatientList deserializePatientList(String serializedValue) {
        String[] pcs = split(serializedValue, 2);
        IPatientList list = PatientListRegistry.getInstance().findByName(pcs[0]);
        
        if (list == null) {
            throw new PatientListException("Unable to create patient list: " + pcs[0]);
        }
        
        return list.copy(serializedValue);
    }
    
    public static String formatName(final String name) {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        
        String pcs[] = StrUtil.split(WordUtils.capitalizeFully(name, wordDelimiters), ",");
        StringBuilder sb = new StringBuilder(name.length() + 5);
        
        for (String pc : pcs) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            
            sb.append(pc.trim());
        }
        
        return sb.toString();
    }
    
    /**
     * Enforce static class.
     */
    private Util() {
    };
}
