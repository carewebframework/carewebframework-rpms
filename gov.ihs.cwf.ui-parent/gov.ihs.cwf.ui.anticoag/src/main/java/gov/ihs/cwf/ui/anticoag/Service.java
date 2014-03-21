/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.anticoag;

import java.util.List;

import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;

import org.carewebframework.api.domain.IDomainObject;
import org.carewebframework.cal.api.context.PatientContext;
import org.carewebframework.common.StrUtil;

/**
 * Anticoagulation management services.
 */
public class Service {
    
    private final BrokerSession broker;
    
    public Service(BrokerSession broker) throws Exception {
        this.broker = broker;
        AntiCoagRecord.init(this);
    }
    
    private void errorCheck(List<String> result) throws Exception {
        try {
            errorCheck(result == null || result.isEmpty() ? null : result.get(0));
        } catch (RuntimeException e) {
            result.clear();
            throw e;
        }
    }
    
    private void errorCheck(String msg) throws Exception {
        if (msg != null && msg.startsWith("-")) {
            throw new Exception(StrUtil.piece(msg, StrUtil.U, 2));
        }
    }
    
    private void getChoices(String file, String field, List<String> result) throws Exception {
        result.clear();
        broker.callRPCList("BGOUTL3 GETSET", result, file, field, "");
        errorCheck(result);
        
        for (int i = 0; i < result.size(); i++) {
            result.set(i, StrUtil.piece(result.get(i), StrUtil.U, 2));
        }
    }
    
    public void getGoals(List<String> result) throws Exception {
        getChoices("9000010.51", ".04", result);
        result.remove("N/A");
    }
    
    public void getDurations(List<String> result) throws Exception {
        getChoices("9000010.51", ".07", result);
    }
    
    public void delete(AntiCoagRecord record, String reasonCode, String reasonText) throws Exception {
        errorCheck(broker.callRPC("BGOVCOAG DEL", record.getDomainId() + "^" + reasonCode
                + (reasonText == null ? "" : "^" + reasonText)));
    }
    
    /**
     * @param record
     * @throws Exception
     */
    public void update(AntiCoagRecord record) throws Exception {
        String result = broker.callRPC("BGOVCOAG SET", toDAO(record));
        errorCheck(result);
        record.setDomainId(Long.parseLong(result));
        
    }
    
    /**
     * @param record
     * @return <code>
     * V anticoag IEN (if edit) [1] ^ Indication [2] ^ Patient IEN [3] ^ Visit IEN [4] ^ 
     * Provider IEN [5] ^ Goal [6] ^ Min [7] ^ Max [8] ^ Duration [9] Start date [10] ^
     * Event Date [11] ^ Location IEN [12] ^ Other Location [13] ^ Historical Flag [14] ^
     * Comment [15] 
     *  </code>
     */
    private String toDAO(AntiCoagRecord record) {
        StringBuilder sb = new StringBuilder();
        boolean indicated = record.getIndicated();
        appendData(sb, record);
        appendData(sb, indicated ? "YES" : "NO");
        appendData(sb, PatientContext.getActivePatient());
        appendData(sb, record.getVisitIEN());
        appendData(sb, record.getProvider());
        appendData(sb, indicated ? record.getGoalRange() : null);
        appendData(sb, indicated ? record.getGoalMin() : null);
        appendData(sb, indicated ? record.getGoalMax() : null);
        appendData(sb, indicated ? record.getDuration() : null);
        appendData(sb, indicated ? new FMDate(record.getStartDate()).getFMDate() : null);
        appendData(sb, null); // Event date
        appendData(sb, null); // Location ien
        appendData(sb, null); // Other location
        appendData(sb, null); // Historical flag
        appendData(sb, indicated ? record.getComment() : null);
        return sb.toString();
    }
    
    private void appendData(StringBuilder sb, Object data) {
        sb.append(data == null ? "" : data instanceof IDomainObject ? ((IDomainObject) data).getDomainId() : data).append(
            '^');
    }
    
}
