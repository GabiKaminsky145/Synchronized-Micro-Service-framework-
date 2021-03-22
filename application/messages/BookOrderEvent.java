package bgu.spl.mics.application.messages;

import bgu.spl.mics.application.passiveObjects.Customer;
import bgu.spl.mics.application.passiveObjects.OrderReceipt;
import bgu.spl.mics.Event;

public class BookOrderEvent implements Event<OrderReceipt> {

    Customer customer;
    private String bookTitle;
    private int price;
    private int orderId;
    private int orderTick;


    public BookOrderEvent(Customer customer, String bookTitle, int price, int orderId, int orderTick) {
        this.customer = customer;
        this.bookTitle = bookTitle;
        this.price = price;
        this.orderId = orderId;
        this.orderTick = orderTick;
    }

    public String toString(){
        return "Book Order Event:\nBook Title: " + bookTitle + ", Order ID: " + orderId;
    }

    public Customer getCustomer() {
        return customer;
    }


    public String getBookTitle() {
        return bookTitle;
    }

    public int getPrice() {
        return price;
    }


    public int getOrderId() {
        return orderId;
    }

    public int getOrderTick() {
        return orderTick;
    }
}