package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.CheckAvailabilityAndTakeEvent;
import bgu.spl.mics.application.messages.InterruptBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.*;
import java.util.concurrent.CountDownLatch;

/**
 * InventoryService is in charge of the book inventory and stock.
 * Holds a reference to the {@link Inventory} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class InventoryService extends MicroService{
	private CountDownLatch countDownLatch;
	private Inventory it;
	private String fileName;
	private int currentTick;

	public InventoryService(CountDownLatch countDownLatch, String name, BookInventoryInfo[] books, String fileName) {
		super(name);
		this.countDownLatch = countDownLatch;
		it = Inventory.getInstance();
		it.load(books);
		this.fileName = fileName;
		currentTick = 0;
	}

	@Override
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class,(TickBroadcast broadcast)->{
			currentTick = broadcast.getCurrentTick();
		});

		subscribeEvent(CheckAvailabilityAndTakeEvent.class, (CheckAvailabilityAndTakeEvent event)->{
			if(it.take(event.getBooktitle()) == OrderResult.NOT_IN_STOCK){
				complete(event, false);
			}
			else {
				complete(event, true);
			}
		});

		subscribeBroadcast(InterruptBroadcast.class, (InterruptBroadcast broadcast)->{
			terminate();
		});

		countDownLatch.countDown();
		
	}

}