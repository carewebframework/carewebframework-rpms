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

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DateTest {
    
    @Test
    public void testDateConversion() throws ParseException {
        testDates("2890525.124506", "05/25/1989 12:45:06", "MM/dd/yyyy HH:mm:ss");
        testDates("3090228.02030099", "02/28/2009 02:03:00:99", "MM/dd/yyyy HH:mm:ss:SS");
        testDates("2290920", "09/20/1929", "MM/dd/yyyy");
        testDates("2890525.2403", "05/26/1989 00:03", "MM/dd/yyyy HH:mm");
    }
    
    private void testDates(String FMDateStr, String refDate, String format) throws ParseException {
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        FMDate date = new FMDate(FMDateStr);
        Date date2 = fmt.parse(refDate);
        assertEquals(date, date2);
        String FMDateStr2 = date.getFMDate();
        assertEquals(FMDateStr, FMDateStr2);
        print(date);
        print(date.toStringDateOnly());
        print(date.toStringFull());
        print("---------------");
    }
    
    private void print(Object object) {
        System.out.println(object);
    }
    
}
