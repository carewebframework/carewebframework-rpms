/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.ui.terminology.sct.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.rpms.api.terminology.TermMatch;
import org.carewebframework.rpms.api.terminology.TermMatch.Term;
import org.carewebframework.rpms.api.terminology.TermMatch.TermType;
import org.carewebframework.rpms.api.terminology.TermSubset;
import org.carewebframework.rpms.api.terminology.TermUtil;
import org.carewebframework.rpms.ui.terminology.sct.view.TermMatchRenderer;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.mbroker.FMDate;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.DefaultTreeModel;
import org.zkoss.zul.DefaultTreeNode;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Tree;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Window;

public class LookupSCTController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = ZKUtil.getResourcePath(LookupSCTController.class, 1) + "lookupSCT.zul";
    
    private static final TermMatchRenderer termMatchRenderer = new TermMatchRenderer();
    
    private static final TermType[] RENDERED_TERM_TYPES = { TermType.PREFERRED, TermType.SYNONYM };
    
    public static class SelectedTerm {
        
        public final Term term;
        
        public final TermMatch termMatch;
        
        private SelectedTerm(Term term, TermMatch termMatch) {
            this.term = term;
            this.termMatch = termMatch;
        }
    };
    
    // Start of auto wire section
    
    private Textbox txtSearch;
    
    private Datebox datSearch;
    
    private Tree treeSearch;
    
    private Listbox lbSubset;
    
    private HtmlBasedComponent pnlSubset;
    
    private Radiogroup rgrpLookupMode;
    
    private Radiogroup rgrpMaxResults;
    
    private Button btnSelect;
    
    // End of auto wire section
    
    private SelectedTerm selectedTerm;
    
    public static SelectedTerm execute(String... subsets) {
        Map<Object, Object> args = new HashMap<>();
        args.put("subsets", subsets);
        Component root = PopupDialog.popup(DIALOG, args, false, false, true);
        LookupSCTController controller = (LookupSCTController) FrameworkController.getController(root);
        return controller.selectedTerm;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        String[] subsets = (String[]) arg.get("subsets");
        TermSubset termSubsets = TermUtil.getSubset(null);
        
        for (String subset : subsets) {
            Listitem item = new Listitem(termSubsets.getMemberName(subset));
            item.setValue(subset);
            lbSubset.appendChild(item);
        }
        
        if (subsets.length == 1) {
            pnlSubset.setVisible(false);
            lbSubset.setSelectedIndex(0);
            ((Window) root).getCaption().setLabel(lbSubset.getSelectedItem().getLabel());
        }
        
        treeSearch.setItemRenderer(termMatchRenderer);
    }
    
    private void close(boolean cancelled) {
        if (cancelled) {
            selectedTerm = null;
        } else {
            Treeitem item = treeSearch.getSelectedItem();
            TreeNode<?> treeNode = item == null ? null : (TreeNode<?>) item.getValue();
            Object data = treeNode == null ? null : treeNode.getData();
            
            Term term;
            TermMatch termMatch;
            
            if (data instanceof TermMatch) {
                termMatch = (TermMatch) data;
                term = termMatch.getTerm(TermType.PROBLEM);
            } else if (data instanceof Term) {
                term = (Term) data;
                termMatch = (TermMatch) treeNode.getParent().getData();
            } else {
                return;
            }
            
            selectedTerm = new SelectedTerm(term, termMatch);
        }
        
        root.detach();
    }
    
    public void onClick$btnSearch() {
        String text = txtSearch.getValue().trim();
        boolean synonym = rgrpLookupMode.getSelectedIndex() == 1;
        FMDate date = new FMDate(datSearch.getValue());
        Long max = (Long) rgrpMaxResults.getSelectedItem().getValue();
        String[] filters = getSelectedSubsets();
        List<TermMatch> matches = TermUtil.lookupSCT(text, synonym, date, max, filters);
        TreeNode<Object> rootNode = new DefaultTreeNode<>(null, null, false);
        buildModel(rootNode, matches);
        DefaultTreeModel<Object> model = new DefaultTreeModel<>(rootNode);
        treeSearch.setModel(model);
    }
    
    private void buildModel(TreeNode<Object> rootNode, List<TermMatch> matches) {
        if (matches.isEmpty()) {
            rootNode.add(new DefaultTreeNode<Object>(null));
        } else {
            for (TermMatch match : matches) {
                boolean synonyms = rgrpLookupMode.getSelectedIndex() == 1;
                TreeNode<Object> parentNode = synonyms ? new DefaultTreeNode<Object>(match) : new DefaultTreeNode<Object>(
                        match, null, false);
                rootNode.add(parentNode);
                
                if (!synonyms) {
                    for (TermType type : RENDERED_TERM_TYPES) {
                        for (Term term : match.getTerms(type)) {
                            parentNode.add(new DefaultTreeNode<Object>(term));
                        }
                    }
                }
            }
        }
    }
    
    public void onClick$btnSelect() {
        close(false);
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    public void onSelect$treeSearch() {
        updateControls();
    }
    
    private void updateControls() {
        btnSelect.setDisabled(treeSearch.getSelectedCount() == 0);
    }
    
    /**
     * Returns an array of selected subsets.
     * 
     * @return Array of selected subsets.
     */
    private String[] getSelectedSubsets() {
        String[] selectedSubsets = new String[lbSubset.getSelectedCount()];
        int i = 0;
        
        for (Listitem item : lbSubset.getSelectedItems()) {
            selectedSubsets[i] = item.getValue();
        }
        
        return selectedSubsets;
    }
    
}
