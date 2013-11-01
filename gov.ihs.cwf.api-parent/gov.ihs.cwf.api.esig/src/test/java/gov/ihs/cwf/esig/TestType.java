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

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.esig.ESigItem.ESigItemIssueSeverity;

public abstract class TestType extends ESigType {
    
    private int id = 0;
    
    private int severityIndex = -1;
    
    private final int severityMax = ESigItemIssueSeverity.values().length;
    
    protected TestType(String id, String groupHeader) {
        super(id, groupHeader);
    }
    
    private String uniqueId() {
        return getESigTypeId() + "-" + ++id;
    }
    
    public ESigItem newItem() {
        String text = getText();
        ESigItem item = new ESigItem(this, uniqueId());
        item.setSession("Test Session");
        item.setText(text);
        return item;
    }
    
    private ESigItem newItem(List<ESigItem> items) {
        ESigItem item = newItem();
        items.add(item);
        return item;
    }
    
    abstract protected String getText();
    
    @Override
    public void loadESigItems(List<ESigItem> items) {
        if (PatientContext.getCurrentPatient() != null) {
            for (int i = 0; i < 5; i++) {
                newItem(items);
            }
        }
    }
    
    @Override
    public void validateESigItems(List<ESigItem> items) {
        super.validateESigItems(items);
        
        for (ESigItem item : items) {
            if (++severityIndex >= severityMax) {
                severityIndex = -1;
            } else {
                ESigItemIssueSeverity severity = ESigItemIssueSeverity.values()[severityIndex];
                item.addIssue(severity, "This is a test issue with a severity level of " + severity.name());
            }
        }
    }
    
    @Override
    public void signESigItems(List<ESigItem> items, String esig) {
        super.signESigItems(items, esig);
    }
    
}
