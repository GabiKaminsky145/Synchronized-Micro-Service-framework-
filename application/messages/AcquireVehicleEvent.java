package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;

public class AcquireVehicleEvent implements Event<DeliveryVehicle> {

    private String address;
    private int distance;
    private String bookTitle;
    private String customerName;

    public AcquireVehicleEvent(String address, int distance, String bookTitle, String customerName){
        this.address = address;
        this.distance = distance;
        this.bookTitle = bookTitle;
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public int getDistance() {
        return distance;
    }

    public String getBookTitle(){
        return bookTitle;
    }

    public String getCustomerName(){
        return customerName;
    }
}
