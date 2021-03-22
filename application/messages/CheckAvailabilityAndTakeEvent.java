package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;

public class CheckAvailabilityAndTakeEvent implements Event<Boolean> {
    private String booktitle;
    private String customerName;

    public CheckAvailabilityAndTakeEvent(String booktitle, String customerName) {

        this.booktitle = booktitle;
        this.customerName = customerName;
    }

    public String getBooktitle() {
        return booktitle;
    }
    public String getCustomerName(){return customerName;}
}
