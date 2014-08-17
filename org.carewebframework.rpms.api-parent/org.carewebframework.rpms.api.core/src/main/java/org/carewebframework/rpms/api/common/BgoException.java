/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.rpms.api.common;

public class BgoException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final long errorCode;
    
    public BgoException(long errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public long getErrorCode() {
        return errorCode;
    }
    
}
