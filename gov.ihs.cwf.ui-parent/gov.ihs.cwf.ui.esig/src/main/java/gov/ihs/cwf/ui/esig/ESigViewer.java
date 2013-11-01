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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gov.ihs.cwf.context.PatientContext;
import gov.ihs.cwf.esig.ESigFilter;
import gov.ihs.cwf.esig.ESigItem;
import gov.ihs.cwf.esig.ESigItem.SignState;
import gov.ihs.cwf.esig.IESigService;
import gov.ihs.cwf.esig.IESigType;

import org.apache.commons.lang.StringUtils;

import org.carewebframework.api.FrameworkUtil;
import org.carewebframework.api.security.SecurityUtil;
import org.carewebframework.ui.zk.SelectionGrid;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Group;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for the signature review dialog.
 * 
 * 
 */
public class ESigViewer extends Window implements PatientContext.IPatientContextEvent {
    
    private static final long serialVersionUID = 1L;
    
    private boolean canceled = true;
    
    @SuppressWarnings("unused")
    private IESigService esigService;
    
    private final Set<IESigType> esigTypes = new HashSet<IESigType>();
    
    private SelectionGrid grid;
    
    private Textbox txtPassword;
    
    private Label lblError;
    
    private Label lblSelectionCount;
    
    private Button btnOK;
    
    /**
     * Displays the signature review dialog.
     * 
     * @param esigService Reference to the esignature service.
     * @param filter Optional filter to control which items are displayed.
     * @return True unless the dialog was canceled.
     * @throws Exception
     */
    public static boolean execute(IESigService esigService, ESigFilter filter) throws Exception {
        Iterable<ESigItem> items = esigService.getItems(filter);
        
        if (items == null || !items.iterator().hasNext()) {
            return true;
        }
        
        ESigViewer dlg = (ESigViewer) ZKUtil.loadZulPage(Constants.RESOURCE_PREFIX + "esigViewer.zul", null);
        dlg.init(esigService, items);
        dlg.doModal();
        return !dlg.canceled;
    }
    
    /**
     * Initialize the dialog, adding the signature items to the list.
     * 
     * @param esigService
     * @param items
     */
    private void init(IESigService esigService, Iterable<ESigItem> items) {
        this.esigService = esigService;
        ZKUtil.wireController(this, this);
        
        for (ESigItem item : items) {
            addItem(item);
        }
        
        updateControls();
        
        if (grid.getRowCount() > 20) {
            grid.setHeight("600px");
        }
        
        FrameworkUtil.getAppFramework().registerObject(this);
    }
    
    /**
     * Add an esignature item to the list.
     * 
     * @param item
     */
    private void addItem(ESigItem item) {
        esigTypes.add(item.getESigType());
        Group group = nextGroup(findGroup(item));
        Row row = grid.addRow(new Row(), group);
        ((HtmlBasedComponent) row.getFirstChild()).setStyle("margin-left:15px");
        row.setStyle("border:none");
        Label label = new Label(item.getText());
        label.setPre(true);
        row.appendChild(label);
        row.setValue(item);
        SignState ss = item.getSignState();
        grid.setSelected(row, !ss.isYes());
        grid.setDisabled(row, ss.isForced());
    }
    
    /**
     * Locates or creates the group for the specified esig item.
     * 
     * @param item
     * @return
     */
    private Group findGroup(ESigItem item) {
        String label = item.getESigType().getESigTypeGroupHeader();
        
        if (!StringUtils.isEmpty(item.getSubGroupName())) {
            label += " - " + item.getSubGroupName();
        }
        
        Group group = null;
        
        while ((group = nextGroup(group)) != null) {
            String lbl = (String) group.getValue();
            int i = label.compareToIgnoreCase(lbl);
            
            if (i == 0) {
                return group;
            }
            
            if (i < 0) {
                break;
            }
        }
        
        Row insertBefore = group;
        group = new Group();
        grid.addRow(group, insertBefore);
        group.setValue(label);
        ((HtmlBasedComponent) group.getFirstChild()).setStyle("float:left");
        group.appendChild(new Label(label));
        return group;
    }
    
    /**
     * Returns the group that follows the specified group, or null if there is none.
     * 
     * @param group The returned group is the first one following this group. If this value is null,
     *            returns the first group encountered.
     * @return
     */
    private Group nextGroup(Group group) {
        Component cmpt = group == null ? grid.getRows().getFirstChild() : group.getNextSibling();
        
        while (cmpt != null && !(cmpt instanceof Group)) {
            cmpt = cmpt.getNextSibling();
        }
        
        return (Group) cmpt;
    }
    
    /**
     * Update controls to reflect the current state.
     */
    private void updateControls() {
        int count = 0;
        int total = 0;
        
        for (Row row : grid.getAllRows()) {
            if (!(row instanceof Group)) {
                total++;
                boolean isSelected = grid.isSelected(row);
                
                if (isSelected) {
                    count++;
                }
            }
        }
        
        lblSelectionCount.setValue(" (" + count + "/" + total + ")");
        btnOK.setDisabled(count == 0);
        txtPassword.setDisabled(count == 0);
        
        if (!txtPassword.isDisabled()) {
            txtPassword.setFocus(true);
        }
    }
    
    /**
     * Returns a list of all selected items of the specified esignature type.
     * 
     * @param esigType
     * @return
     */
    private List<ESigItem> getSelectedItems(IESigType esigType) {
        List<ESigItem> list = new ArrayList<ESigItem>();
        
        for (Row row : grid.getAllRows()) {
            if (!(row instanceof Group) && grid.isSelected(row)) {
                ESigItem item = (ESigItem) row.getValue();
                
                if (esigType == null || esigType.equals(item.getESigType())) {
                    list.add(item);
                }
            }
        }
        
        return list;
    }
    
    /**
     * Apply esignature to all selected items.
     */
    public void onClick$btnOK() {
        if (btnOK.isDisabled()) {
            return;
        }
        
        setError(null);
        String pwd = txtPassword.getValue();
        txtPassword.setValue(null);
        
        try {
            if (SecurityUtil.getSecurityService().validatePassword(pwd)) {
                Map<IESigType, List<ESigItem>> items = new HashMap<IESigType, List<ESigItem>>();
                
                for (IESigType esigType : esigTypes) {
                    List<ESigItem> selItems = getSelectedItems(esigType);
                    
                    if (selItems.size() > 0) {
                        items.put(esigType, selItems);
                    }
                }
                
                List<ESigItem> issues = new ArrayList<ESigItem>();
                
                for (IESigType esigType : items.keySet()) {
                    doValidate(esigType, items.get(esigType), issues);
                }
                
                if (issues.size() > 0 && !IssueViewer.execute(issues)) {
                    return;
                }
                
                for (IESigType esigType : items.keySet()) {
                    List<ESigItem> selItems = items.get(esigType);
                    selItems.removeAll(issues);
                    doSign(esigType, selItems, pwd);
                }
                
                close(false);
            }
        } catch (Exception e) {}
        
        setError("Electronic signature code not valid.");
    }
    
    /**
     * Performs validation of specified esignature items.
     * 
     * @param esigType Type of the esignature items (all will be of this type).
     * @param items Esignature items of the specified type that are to be validated.
     * @param issues List of items that had reported issues.
     */
    private void doValidate(IESigType esigType, List<ESigItem> items, List<ESigItem> issues) {
        if (items.size() > 0) {
            esigType.validateESigItems(items);
            
            for (ESigItem item : items) {
                if (item.getIssues() != null) {
                    issues.add(item);
                }
            }
        }
    }
    
    /**
     * Applies electronic signature to the specified items.
     * 
     * @param esigType Type of the esignature items (all will be of this type).
     * @param items Esignature items of the specified type that are to be signed.
     * @param password Validated esignature password to be applied.
     */
    private void doSign(IESigType esigType, List<ESigItem> items, String password) {
        if (items.size() > 0) {
            esigType.signESigItems(items, password);
        }
    }
    
    /**
     * Display the error message.
     * 
     * @param message
     */
    private void setError(String message) {
        lblError.setValue(message == null ? " " : message);
    }
    
    /**
     * Force focus to the password text box.
     */
    public void onBlur$txtPassword() {
        Events.postEvent(Events.ON_FOCUS, this, null);
    }
    
    /**
     * Clear error message upon re-entering password.
     */
    public void onChanging$txtPassword() {
        setError(null);
    }
    
    /**
     * Update controls to reflect changes in item selection.
     */
    public void onSelect$grid() {
        updateControls();
    }
    
    /**
     * Dialog focus always goes to the password text box.
     */
    public void onFocus() {
        txtPassword.setFocus(true);
    }
    
    /**
     * Close the dialog with a cancel status.
     */
    public void onCancel() {
        close(true);
    }
    
    /**
     * Close the dialog with the specified cancel status.
     * 
     * @param canceled
     */
    private void close(boolean canceled) {
        this.canceled = canceled;
        FrameworkUtil.getAppFramework().unregisterObject(this);
        detach();
    }
    
    @Override
    public void canceled() {
    }
    
    @Override
    public void committed() {
    }
    
    @Override
    public String pending(boolean silent) {
        return null;
    }
    
}
