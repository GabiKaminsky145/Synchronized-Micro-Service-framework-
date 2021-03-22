package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;

public class OrderSchedule {
    private BookInventoryInfo book;
    private int tick;
    private int orderId;

    public OrderSchedule(BookInventoryInfo book, int tick, int orderId){
        this.book = book;
        this.tick = tick;
        this.orderId=orderId;
    }

    public BookInventoryInfo getBook(){
        return book;
    }
    public int getTick(){
        return tick;
    }
    public int getOrderId() {
        return orderId;
    }
}
