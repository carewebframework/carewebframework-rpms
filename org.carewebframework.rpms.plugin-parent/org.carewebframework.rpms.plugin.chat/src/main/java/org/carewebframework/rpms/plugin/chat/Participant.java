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
package org.carewebframework.rpms.plugin.chat;

import java.io.Serializable;
import java.util.Date;

import org.carewebframework.vista.mbroker.HODate;

import org.carewebframework.common.StrUtil;

/**
 * Represents a single participant in a chat session.
 */
public class Participant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final long ien;
    
    private final String name;
    
    private final String session;
    
    private final String aid;
    
    private final String station;
    
    private final Date loginTime;
    
    private final String process;
    
    /**
     * Creates a participant from raw data.
     * 
     * @param data Format is: <code>UID^WID^AID^DUZ^USER^LDT^JOB</code>
     */
    protected Participant(String data) {
        String[] pcs = StrUtil.split(data, StrUtil.U, 7);
        session = pcs[0];
        station = pcs[1];
        aid = pcs[2];
        ien = Long.parseLong(pcs[3]);
        name = pcs[4];
        loginTime = HODate.fromString((pcs[5]));
        process = pcs[6];
    }
    
    /**
     * Returns the participant's internal entry number.
     * 
     * @return Internal entry number.
     */
    public long getIen() {
        return ien;
    }
    
    /**
     * Returns the participant's name.
     * 
     * @return The name of the participant.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the id of the user's broker session.
     * 
     * @return Session id.
     */
    public String getSession() {
        return session;
    }
    
    /**
     * The application id for the user's broker session.
     * 
     * @return Application id.
     */
    public String getApplicationId() {
        return aid;
    }
    
    /**
     * The name of the client workstation.
     * 
     * @return Client workstation name.
     */
    public String getStation() {
        return station;
    }
    
    /**
     * Returns participant's the login time.
     * 
     * @return Login time.
     */
    public Date getLoginTime() {
        return loginTime;
    }
    
    /**
     * Returns the process id for the broker session.
     * 
     * @return Broker process id.
     */
    public String getProcess() {
        return process;
    }
    
    /**
     * Two participants are considered equal if their session identifiers are the same.
     */
    @Override
    public boolean equals(Object object) {
        return object instanceof Participant && ((Participant) object).session.equals(session);
    }
    
    /**
     * Returns the participant's name.
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Use the session identifier's hash code.
     */
    @Override
    public int hashCode() {
        return session.hashCode();
    }
    
}
