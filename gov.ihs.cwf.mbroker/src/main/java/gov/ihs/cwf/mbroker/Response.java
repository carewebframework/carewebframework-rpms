/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package gov.ihs.cwf.mbroker;

import java.io.DataInputStream;
import java.io.IOException;

public class Response {
    
    public static enum ResponseType {
        ACK, ERROR, ASYNC, EVENT
    };
    
    private ResponseType responseType;
    
    private final String data;
    
    private byte sequenceId = 0;
    
    public Response(DataInputStream stream) throws IOException {
        int bufsize = 100;
        byte[] temp = new byte[bufsize];
        DynamicByteBuffer buffer = new DynamicByteBuffer(bufsize, 1000);
        int bytesRead;
        boolean eod = false;
        int start = 2;
        
        while (!eod && (bytesRead = stream.read(temp)) > 0) {
            eod = temp[bytesRead - 1] == Constants.EOD;
            buffer.put(temp, start, eod ? bytesRead - 1 : bytesRead);
            
            if (start != 0) {
                start = 0;
                sequenceId = temp[0];
                responseType = ResponseType.values()[temp[1]];
            }
        }
        
        data = new String(buffer.toArray(), Constants.UTF8);
    }
    
    public Response(Throwable e) {
        responseType = ResponseType.ERROR;
        data = e.toString();
    }
    
    public ResponseType getResponseType() {
        return responseType;
    }
    
    public String getData() {
        return data;
    }
    
    public byte getSequenceId() {
        return sequenceId;
    }
    
}
