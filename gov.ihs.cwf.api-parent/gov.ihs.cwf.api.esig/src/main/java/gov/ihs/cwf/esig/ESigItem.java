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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carewebframework.common.NumUtil;

/**
 * This represents a single item to be submitted for electronic signature.
 * 
 * @author dmartin
 */
public class ESigItem implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Represents a default signature state for a signature item.
     * 
     * @author dmartin
     */
    public static enum SignState {
        YES, // Item is marked for signature
        FORCED_YES, // Item is marked for signature and cannot be unmarked.
        NO, // Item is not marked for signature.
        FORCED_NO; // Item is not marked for signature and cannot be marked.
        
        /**
         * Returns true if the enum value equates to a yes state.
         * 
         * @return True if the enum value equates to a yes state.
         */
        public boolean isYes() {
            return this == YES || this == FORCED_YES;
        }
        
        /**
         * Returns true if the enum value equates to a forced state.
         * 
         * @return True if the enum value equates to a forced state.
         */
        public boolean isForced() {
            return this == FORCED_YES || this == FORCED_NO;
        }
    }
    
    /**
     * Represents the severity level for an issue.
     * 
     * @author dmartin
     */
    public static enum ESigItemIssueSeverity {
        MINOR, // Minor - User need not explicitly acknowledge or justify override.
        MODERATE, // Moderate - Significant issue; user must explicitly acknowledge.
        MAJOR, // Major - Significant issue; user must explicitly acknowledge and justify override.
        SEVERE
        // Severe - Serious issue; prohibits signature of item altogether.
    }
    
    /**
     * Represents a single issue and associated severity. Issues are used to convey to the user
     * potential consequences of signing an item.
     * 
     * @author dmartin
     */
    public static class ESigItemIssue implements Comparable<ESigItemIssue> {
        
        private final ESigItemIssueSeverity severity;
        
        private final String description;
        
        private String override;
        
        /**
         * Creates an issue of the specified severity.
         * 
         * @param severity Severity of the issue.
         * @param description Text to convey issue to signer.
         */
        private ESigItemIssue(ESigItemIssueSeverity severity, String description) {
            super();
            this.severity = severity;
            this.description = description;
        }
        
        /**
         * Permits sorting issues by severity.
         */
        @Override
        public int compareTo(ESigItemIssue o) {
            int result = NumUtil.compare(o.severity.ordinal(), severity.ordinal());
            return result != 0 ? result : description.compareToIgnoreCase(o.description);
        }
        
        /**
         * Returns severity of the issue.
         * 
         * @return Severity.
         */
        public ESigItemIssueSeverity getSeverity() {
            return severity;
        }
        
        /**
         * Returns the detailed description of the issue. This should be suitable for presentation
         * to the user.
         * 
         * @return Description.
         */
        public String getDescription() {
            return description;
        }
        
        /**
         * Sets the override reason. Override reasons must be provided for issues of a severity
         * level of major before signature of the item is allowed.
         * 
         * @param override
         */
        public void setOverride(String override) {
            this.override = override;
        }
        
        /**
         * Returns the override reason.
         * 
         * @return Override reason.
         */
        public String getOverride() {
            return override;
        }
    }
    
    private final IESigType eSigType;
    
    private String id;
    
    private Object data;
    
    private String text;
    
    private String subGroupName;
    
    private String session;
    
    private SignState signState;
    
    private boolean selected;
    
    private List<ESigItemIssue> issues = null;
    
    private boolean sortIssues = false;
    
    /**
     * Creates an esig item with required fields.
     * 
     * @param eSigType The esig type.
     * @param id The item id, which must be unique within a given esig type.
     */
    public ESigItem(IESigType eSigType, String id) {
        this.eSigType = eSigType;
        this.id = id;
    }
    
    /**
     * Returns the associated esig type.
     * 
     * @return Associated esig type.
     */
    public IESigType getESigType() {
        return eSigType;
    }
    
    /**
     * Returns the item id, which must be unique within its esig type.
     * 
     * @return Item id.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Sets the item id.
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Returns a data object associated with the item. This object should be serializable.
     * 
     * @return Associated data object.
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Sets a data object associated with the item. This object should be serializable.
     * 
     * @param data
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    /**
     * Returns the text that describes the sig item that is displayable to the signer.
     * 
     * @return Displayable text.
     */
    public String getText() {
        return text;
    }
    
    /**
     * Sets the text that describes the sig item. This should be sufficiently descriptive that the
     * potential signer understands what is being signed.
     * 
     * @param text
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Returns the sub group name. For presentation, items will be organized by this sub group name
     * within the item type's group name.
     * 
     * @return Subgroup name.
     */
    public String getSubGroupName() {
        return subGroupName;
    }
    
    /**
     * Sets the sub group name. For presentation, items will be organized by this sub group name
     * within the item type's group name.
     * 
     * @param subGroupName
     */
    public void setSubGroupName(String subGroupName) {
        this.subGroupName = subGroupName;
    }
    
    /**
     * Returns the identifier of the session to which this item belongs. Sessions are used to group
     * signature items together.
     * 
     * @return Session id.
     */
    public String getSession() {
        return session;
    }
    
    /**
     * Sets the session identifier. Sessions are used to group signature items together.
     * 
     * @param session
     */
    public void setSession(String session) {
        this.session = session;
    }
    
    /**
     * Returns the default signature state. This determines how the item is selected when presented
     * for signature.
     * 
     * @return Default signature state.
     */
    public SignState getSignState() {
        return signState;
    }
    
    /**
     * Returns the default signature state. This determines how the item is selected when presented
     * for signature.
     * 
     * @param signState
     */
    public void setSignState(SignState signState) {
        this.signState = signState;
    }
    
    /**
     * Returns true if the sig item is selected. This is used to allow certain operations to select
     * or deselect signature items for further processing.
     * 
     * @return True if selected.
     */
    public boolean isSelected() {
        return selected;
    }
    
    /**
     * Sets the selection state of the item. This is used to allow certain operations to select or
     * unselect signature items for further processing.
     * 
     * @param selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * Adds an issue to the signature item.
     * 
     * @param severity Severity of the issue.
     * @param description Description of the issue.
     * @return The added issue.
     */
    public ESigItemIssue addIssue(ESigItemIssueSeverity severity, String description) {
        ESigItemIssue issue = new ESigItemIssue(severity, description);
        
        if (issues == null) {
            issues = new ArrayList<ESigItemIssue>();
            sortIssues = false;
        } else {
            int i = issues.indexOf(issue);
            
            if (i >= 0) {
                return issues.get(i);
            }
            
            sortIssues = true;
        }
        
        issues.add(issue);
        return issue;
    }
    
    /**
     * Returns an iterable of issues logged for this item.
     * 
     * @return Iterable of all issues.
     */
    public Iterable<ESigItemIssue> getIssues() {
        if (sortIssues) {
            sortIssues = false;
            Collections.sort(issues);
        }
        
        return issues;
    }
    
    /**
     * Removes all issues associated with this item.
     */
    public void clearIssues() {
        sortIssues = false;
        issues = null;
    }
    
    /**
     * Two esig items are considered equal if they have the same type and id.
     * 
     * @param object Object to compare.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof ESigItem) {
            ESigItem item = (ESigItem) object;
            return item.eSigType.equals(eSigType) && item.id.equals(id);
        }
        
        return false;
    }
}
