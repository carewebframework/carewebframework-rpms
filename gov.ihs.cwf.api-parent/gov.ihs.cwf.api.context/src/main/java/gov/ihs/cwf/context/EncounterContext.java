/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.context;

import gov.ihs.cwf.domain.Encounter;
import gov.ihs.cwf.domain.Patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.ContextItems;
import org.carewebframework.api.context.ContextManager;
import org.carewebframework.api.context.IContextEvent;
import org.carewebframework.api.context.ISharedContext;
import org.carewebframework.api.context.ManagedContext;
import org.carewebframework.cal.api.context.PatientContext.IPatientContextEvent;

/**
 * Wrapper for shared encounter context.
 * 
 * @author dmartin
 */
public class EncounterContext extends ManagedContext<Encounter> implements IPatientContextEvent {
    
    private static final Log log = LogFactory.getLog(EncounterContext.class);
    
    private static final String SUBJECT_NAME = "Encounter";
    
    public interface IEncounterContextEvent extends IContextEvent {};
    
    /**
     * Returns the managed encounter context.
     * 
     * @return Encounter context.
     */
    @SuppressWarnings("unchecked")
    static public ISharedContext<Encounter> getEncounterContext() {
        return (ISharedContext<Encounter>) ContextManager.getInstance().getSharedContext(EncounterContext.class.getName());
    }
    
    /**
     * Returns the current encounter from the shared context.
     * 
     * @return Current encounter.
     */
    public static Encounter getCurrentEncounter() {
        return getEncounterContext().getContextObject(false);
    }
    
    /**
     * Requests a context change to the specified encounter.
     * 
     * @param encounter
     */
    public static void changeEncounter(Encounter encounter) {
        try {
            getEncounterContext().requestContextChange(encounter);
        } catch (Exception e) {
            log.error("Error during request context change.", e);
        }
    }
    
    /**
     * Requests a context change to the encounter specified by the encoded string.
     * 
     * @param encounter
     */
    public static void changeEncounter(String encounter) {
        changeEncounter(Encounter.decode(encounter));
    }
    
    /**
     * Creates the context wrapper and registers its context change callback interface.
     */
    public EncounterContext() {
        this(null);
    }
    
    /**
     * Creates the context wrapper and registers its context change callback interface.
     * 
     * @param encounter = Initial value for context.
     */
    public EncounterContext(Encounter encounter) {
        super(SUBJECT_NAME, IEncounterContextEvent.class, encounter);
    }
    
    /**
     * Commits or rejects the pending context change.
     * 
     * @param accept If true, the pending change is committed. If false, the pending change is
     *            canceled.
     */
    @Override
    public void commit(boolean accept) {
        super.commit(accept);
    }
    
    /**
     * Creates a CCOW context from the specified encounter object.
     */
    @Override
    protected ContextItems toCCOWContext(Encounter encounter) {
        //TODO: contextItems.setItem(...);
        return contextItems;
    }
    
    /**
     * Returns a list of patient objects based on the specified CCOW context.
     */
    @Override
    protected Encounter fromCCOWContext(ContextItems contextItems) {
        Encounter encounter = null;
        
        try {
            encounter = new Encounter();
            //TODO: Populate encounter object from context items.
            return encounter;
        } catch (Exception e) {
            log.error(e);
            return null;
        }
    }
    
    /**
     * Returns a priority value of 5.
     * 
     * @return Priority value for context manager.
     */
    @Override
    public int getPriority() {
        return 5;
    }
    
    // IPatientContextEvent
    
    @Override
    public void canceled() {
    }
    
    @Override
    public void committed() {
    }
    
    @Override
    public String pending(boolean silent) {
        Patient patient = (Patient) PatientContext.getPatientContext().getContextObject(true);
        changeEncounter(EncounterUtil.getDefaultEncounter(patient));
        return null;
    }
}
