package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.CheckAvailabilityAndTakeEvent;
import bgu.spl.mics.application.messages.DeliveryEvent;
import bgu.spl.mics.application.messages.InterruptBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;
import java.util.concurrent.CountDownLatch;

/**
 * Selling service in charge of taking orders from customers.
 * Holds a reference to the {@link MoneyRegister} singleton of the store.
 * Handles {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class SellingService extends MicroService{

	private CountDownLatch countDownLatch;
	private MoneyRegister moneyRegister;
	private int currentTick;

	public SellingService(CountDownLatch countDownLatch, String name) {
		super(name);
		this.countDownLatch = countDownLatch;
		moneyRegister = MoneyRegister.getInstance();
		currentTick = 0;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class,(TickBroadcast broadcast)->{
			currentTick = broadcast.getCurrentTick();
		});

		subscribeEvent(BookOrderEvent.class, (BookOrderEvent event)-> {
			boolean available = false;
			int proccessTick = 0;
			synchronized (event.getCustomer()){
				proccessTick = currentTick;
				if(event.getCustomer().getAvailableCreditAmount()<event.getPrice()) {
					complete(event, null);
				}
				else{
					Future<Boolean> future = sendEvent(new CheckAvailabilityAndTakeEvent(event.getBookTitle(), event.getCustomer().getName()));
					available = future.get();
					if(available){
						moneyRegister.chargeCreditCard(event.getCustomer(),event.getPrice());
					}
					else {
						complete(event, null);
					}
				}
			}
			if(available){
				OrderReceipt receipt = new OrderReceipt(event.getOrderId(),getName(),event.getCustomer().getId(),event.getBookTitle(),event.getPrice(),currentTick,event.getOrderTick(),proccessTick);
				moneyRegister.file(receipt);
				Future<Boolean> future = sendEvent(new DeliveryEvent(event.getCustomer().getAddress(), event.getCustomer().getDistance(), event.getBookTitle(), event.getCustomer().getName()));
				future.get();
				complete(event, receipt);
			}
		});

		subscribeBroadcast(InterruptBroadcast.class, (InterruptBroadcast broadcast)->{
			terminate();
		});

		countDownLatch.countDown();
	}
}