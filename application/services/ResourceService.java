package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.AcquireVehicleEvent;
import bgu.spl.mics.application.messages.InterruptBroadcast;
import bgu.spl.mics.application.messages.ReleaseVehicleEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import bgu.spl.mics.application.passiveObjects.DeliveryVehicle;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import java.util.concurrent.CountDownLatch;

/**
 * ResourceService is in charge of the store resources - the delivery vehicles.
 * Holds a reference to the {@link ResourcesHolder} singleton of the store.
 * This class may not hold references for objects which it is not responsible for:
 * {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ResourceService extends MicroService{
	private CountDownLatch countDownLatch;
	private ResourcesHolder resources;
	private int currentTick;

	public ResourceService(CountDownLatch countDownLatch, String name, DeliveryVehicle[] vehicles) {
		super(name);
		this.countDownLatch = countDownLatch;
		resources = ResourcesHolder.getInstance();
		resources.load(vehicles);
		currentTick = 0;
	}

	@Override
	protected void initialize() {

		subscribeBroadcast(TickBroadcast.class,(TickBroadcast broadcast)->{
			currentTick = broadcast.getCurrentTick();
		});

		subscribeEvent(AcquireVehicleEvent.class, (AcquireVehicleEvent event)->{
			Future<DeliveryVehicle> future = resources.acquireVehicle();
			DeliveryVehicle vehicle = future.get();
			vehicle.deliver(event.getAddress(), event.getDistance());
			resources.releaseVehicle(vehicle);
			complete(event, vehicle);
		});

		subscribeBroadcast(InterruptBroadcast.class, (InterruptBroadcast broadcast)->{
			terminate();
		});

		subscribeEvent(ReleaseVehicleEvent.class, (ReleaseVehicleEvent event)->{
			resources.releaseVehicle(event.getVehicle());
		});

		countDownLatch.countDown();
	}
}
