/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.esig;

import java.util.ArrayList;
import java.util.List;

import gov.ihs.cwf.esig.ESigItem;
import gov.ihs.cwf.esig.ESigItem.ESigItemIssue;
import gov.ihs.cwf.esig.ESigItem.ESigItemIssueSeverity;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.ui.zk.SelectionGrid;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Group;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

/**
 * Controller for the issue viewer dialog.
 * 
 * 
 */
public class IssueViewer extends Window {
    
    private static final long serialVersionUID = 1L;
    
    private static final String ATTR_OVERRIDE = "_override";
    
    private SelectionGrid grid;
    
    private Textbox txtReason;
    
    private HtmlBasedComponent pnlReason;
    
    private Label lblError;
    
    private List<ESigItem> items;
    
    private boolean canceled = true;
    
    /**
     * Display the issue viewer dialog for the specified items.
     * 
     * @param items Esignature items that have issues for display.
     * @return True unless the dialog was canceled.
     * @throws Exception
     */
    public static boolean execute(List<ESigItem> items) throws Exception {
        IssueViewer dlg = (IssueViewer) ZKUtil.loadZulPage(Constants.RESOURCE_PREFIX + "issueViewer.zul", null);
        dlg.init(items);
        dlg.doModal();
        return !dlg.canceled;
    }
    
    private void init(List<ESigItem> items) {
        ZKUtil.wireController(this, this);
        this.items = items;
        
        for (ESigItem item : items) {
            addItem(item);
        }
        
        updateControls();
    }
    
    private void updateControls() {
        boolean requiresOverride = false;
        
        for (Row row : grid.getAllRows()) {
            if (grid.isSelected(row) && row.getAttribute(ATTR_OVERRIDE) != null) {
                requiresOverride = true;
                break;
            }
        }
        
        pnlReason.setVisible(requiresOverride);
    }
    
    private void addItem(ESigItem item) {
        Iterable<ESigItemIssue> issues = item.getIssues();
        
        if (issues == null) {
            return;
        }
        
        Row row = grid.addRow();
        row.setStyle("border:none");
        Label label = new Label(item.getText());
        label.setPre(true);
        row.appendChild(label);
        row.setValue(item);
        Detail detail = new Detail();
        detail.setContentSclass("esig-issue-detail");
        row.appendChild(detail);
        Vbox vbox = new Vbox();
        detail.appendChild(vbox);
        boolean isSelected = true;
        boolean isDisabled = false;
        
        for (ESigItemIssue issue : issues) {
            Hbox hbox = new Hbox();
            vbox.appendChild(hbox);
            label = new Label(issue.getSeverity().name() + ": ");
            label.setSclass("esig-severity-" + issue.getSeverity().name().toLowerCase());
            hbox.appendChild(label);
            label = new Label(issue.getDescription());
            label.setPre(true);
            hbox.appendChild(label);
            ESigItemIssueSeverity severity = issue.getSeverity();
            isSelected &= severity == ESigItemIssueSeverity.MINOR;
            isDisabled |= severity == ESigItemIssueSeverity.SEVERE;
            
            if (severity == ESigItemIssueSeverity.MAJOR) {
                row.setAttribute(ATTR_OVERRIDE, true);
            }
        }
        
        grid.setSelected(row, isSelected);
        grid.setDisabled(row, isDisabled);
        detail.setOpen(true);
        return;
    }
    
    private List<ESigItem> getItems(boolean selected) {
        List<ESigItem> items = new ArrayList<ESigItem>();
        
        for (Row row : grid.getAllRows()) {
            if (!(row instanceof Group) && grid.isSelected(row) == selected) {
                items.add((ESigItem) row.getValue());
            }
        }
        
        return items;
    }
    
    public void onCancel() {
        close(true);
    }
    
    public void onBlur$txtReason() {
        Events.postEvent(Events.ON_FOCUS, this, null);
    }
    
    public void onChanging$txtReason() {
        setError(null);
    }
    
    public void onSelect$grid() {
        updateControls();
        txtReason.setFocus(true);
    }
    
    public void onFocus() {
        txtReason.setFocus(true);
    }
    
    public void onClick$btnOK() {
        if (pnlReason.isVisible() && StringUtils.isEmpty(txtReason.getValue())) {
            setError("You must provide a reason for overriding these warnings.");
            txtReason.setFocus(true);
            return;
        }
        
        items.clear();
        items.addAll(getItems(false));
        close(false);
    }
    
    private void close(boolean canceled) {
        this.canceled = canceled;
        FrameworkUtil.getAppFramework().unregisterObject(this);
        detach();
    }
    
    private void setError(String message) {
        lblError.setValue(message == null ? " " : message);
    }
    
}
