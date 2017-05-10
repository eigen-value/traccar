package org.traccar.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TicketStampingTest {

    @Test
    public void parseTicket() throws Exception {

        String mag = "MAG=1,01/01/2000,00:16:03,205,1,0,*****,0,****,0,0,*****,0,****,84,207,S,0,00;";
        String csc = "CSC=1,01/01/2000,00:17:23,205,1,0,*****,0,****,0,0,*****,0,****,46005274,125,00;";
        String mag_ = "MAG=1 01/01/2000 00:16:03 205 1 0 ***** 0 **** 0 0 ***** 0 **** 84 207 S 0 00;";
        String csc_ = "CSC=1 01/01/2000 00:17:23 205 1 0 ***** 0 **** 0 0 ***** 0 **** 46005274 125 00;";

        TicketStamping magTicketStamping = new TicketStamping(mag);
        TicketStamping cscTicketStamping = new TicketStamping(csc);

        TicketStamping mag_TicketStamping = new TicketStamping(mag_);
        TicketStamping csc_TicketStamping = new TicketStamping(csc_);

    }
}
