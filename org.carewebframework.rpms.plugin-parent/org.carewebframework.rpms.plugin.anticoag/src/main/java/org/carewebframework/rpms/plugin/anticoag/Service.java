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
package org.carewebframework.rpms.plugin.anticoag;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;
import org.hspconsortium.cwf.api.patient.PatientContext;
import org.hspconsortium.cwf.fhir.common.IReferenceable;

/**
 * Anticoagulation management services.
 */
public class Service {

    private static final List<String> goalPresets = new ArrayList<>();

    private static final List<String> durationPresets = new ArrayList<>();

    private static transient boolean initialized;

    private final BrokerSession broker;

    public Service(BrokerSession broker) throws Exception {
        this.broker = broker;
    }

    private void errorCheck(List<String> result) {
        try {
            errorCheck(result == null || result.isEmpty() ? null : result.get(0));
        } catch (RuntimeException e) {
            result.clear();
            throw e;
        }
    }

    private void errorCheck(String msg) {
        if (msg != null && msg.startsWith("-")) {
            throw new RuntimeException(StrUtil.piece(msg, StrUtil.U, 2));
        }
    }

    private void getChoices(String file, String field, List<String> result) {
        result.clear();
        broker.callRPCList("BGOUTL3 GETSET", result, file, field, "");
        errorCheck(result);

        for (int i = 0; i < result.size(); i++) {
            result.set(i, StrUtil.piece(result.get(i), StrUtil.U, 2));
        }
    }

    public List<String> getGoalPresets() {
        if (!initialized) {
            initPresets();
        }

        return goalPresets;
    }

    public List<String> getDurationPresets() {
        if (!initialized) {
            initPresets();
        }

        return durationPresets;
    }

    private synchronized void initPresets() {
        if (!initialized) {
            initialized = true;
            getChoices("9000010.51", ".04", goalPresets);
            goalPresets.remove("N/A");
            getChoices("9000010.51", ".07", durationPresets);
        }
    }

    public void delete(AntiCoagRecord record, String reasonCode, String reasonText) throws Exception {
        errorCheck(broker.callRPC("BGOVCOAG DEL",
            record.getId().getIdPart() + "^" + reasonCode + (reasonText == null ? "" : "^" + reasonText)));
    }

    /**
     * @param record The anticoagulation record.
     * @throws Exception Unspecified exception.
     */
    public void update(AntiCoagRecord record) throws Exception {
        String result = broker.callRPC("BGOVCOAG SET", toDAO(record));
        errorCheck(result);
        record.setId(result);

    }

    /**
     * @param record The anticoagulation record.
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
        sb.append(data == null ? "" : data instanceof IReferenceable ? ((IReferenceable) data).getId().getIdPart() : data)
                .append('^');
    }

}
