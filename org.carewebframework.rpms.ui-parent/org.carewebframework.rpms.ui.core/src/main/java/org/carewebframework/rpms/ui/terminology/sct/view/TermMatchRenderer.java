/*
 * #%L
 * carewebframework
 * %%
 * Copyright (C) 2008 - 2017 Regenstrief Institute, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related
 * Additional Disclaimer of Warranty and Limitation of Liability available at
 *
 *      http://www.carewebframework.org/licensing/disclaimer.
 *
 * #L%
 */
package org.carewebframework.rpms.ui.terminology.sct.view;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.text.WordUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.terminology.TermMatch;
import org.carewebframework.rpms.api.terminology.TermMatch.ParentTerm;
import org.carewebframework.rpms.api.terminology.TermMatch.Term;
import org.carewebframework.rpms.api.terminology.TermMatch.TermType;
import org.carewebframework.ui.zk.AbstractTreeitemRenderer;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.TreeNode;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

/**
 * Renders term matches in a hierarchical (preferred term search) or flag (synonym search) format.
 */
public class TermMatchRenderer extends AbstractTreeitemRenderer<TreeNode<Object>> {
    
    @Override
    protected void renderItem(Treeitem item, TreeNode<Object> treeNode) {
        Object data = treeNode.getData();
        Treerow treeRow = new Treerow();
        item.appendChild(treeRow);
        item.setOpen(false);
        item.addForward(Events.ON_DOUBLE_CLICK, item.getTree(), Events.ON_OK);
        
        if (data == null) {
            createCell(treeRow, "No matches found.").setSpan(3);
            item.setDisabled(true);
        } else if (data instanceof TermMatch) {
            TermMatch match = (TermMatch) data;
            createCell(treeRow, match.getTerm(TermType.PROBLEM).getTermText());
            createCell(treeRow, formatParentTerms(match.getParentTerms()));
            createCell(treeRow, StrUtil.fromList(Arrays.asList(match.getMappedICDs()), "\n"));
        } else if (data instanceof Term) {
            Term term = (Term) data;
            createCell(treeRow, term.getTermText()).setSpan(2);
            createCell(treeRow, WordUtils.capitalizeFully(term.getTermType().name()));
        }
    }
    
    private String formatParentTerms(List<ParentTerm> parentTerms) {
        StringBuilder sb = new StringBuilder();
        
        for (ParentTerm parentTerm : parentTerms) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            
            sb.append("is-a ").append(parentTerm.getTermText());
        }
        
        return sb.toString();
    }
    
}
