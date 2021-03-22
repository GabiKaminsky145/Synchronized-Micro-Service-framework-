package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import java.util.concurrent.CountDownLatch;

/**
 * Logistic service in charge of delivering books that have been purchased to customers.
 * Handles {@link DeliveryEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LogisticsService extends MicroService {
	private CountDownLatch countDownLatch;
	private int currentTick;

	public LogisticsService(CountDownLatch countDownLatch, String name) {
		super(name);
		this.countDownLatch = countDownLatch;
		currentTick = 0;
	}

	@Override
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class,(TickBroadcast broadcast)->{
			currentTick = broadcast.getCurrentTick();
		});

		subscribeEvent(DeliveryEvent.class,(DeliveryEvent event)->{
			Future<DeliveryVehicle> future = sendEvent(new AcquireVehicleEvent(event.getAddress(), event.getDistance(), event.getBookTitle(), event.getCustomerName()));
			future.get();
			complete(event, true);
		});

		subscribeBroadcast(InterruptBroadcast.class, (InterruptBroadcast broadcast)->{
			terminate();
		});

		countDownLatch.countDown();
	}
}