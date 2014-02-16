/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.ui.chat;

import java.io.Serializable;
import java.util.Date;

import gov.ihs.cwf.mbroker.HODate;

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
