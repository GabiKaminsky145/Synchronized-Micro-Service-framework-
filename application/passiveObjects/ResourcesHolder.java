package bgu.spl.mics.application.passiveObjects;

import bgu.spl.mics.Future;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing the resource manager.
 * You must not alter any of the given public methods of this class.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private methods and fields to this class.
 */
public class ResourcesHolder {


	private BlockingQueue<DeliveryVehicle> vehiclesQueue;
	private AtomicBoolean isLoaded;
	private Semaphore semaphore;

	private static class ResourcesHolderHolder{
		private static ResourcesHolder instance = new ResourcesHolder();
	}

	private ResourcesHolder(){
		vehiclesQueue = new LinkedBlockingQueue<>();
		isLoaded = new AtomicBoolean(false);
		semaphore = new Semaphore(1, true);
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static ResourcesHolder getInstance() {
		return ResourcesHolderHolder.instance;
	}
	
	/**
     * Tries to acquire a vehicle and gives a future object which will
     * resolve to a vehicle.
     * <p>
     * @return 	{@link Future<DeliveryVehicle>} object which will resolve to a 
     * 			{@link DeliveryVehicle} when completed.   
     */
	public Future<DeliveryVehicle> acquireVehicle() {
		Future<DeliveryVehicle> future = new Future<>();
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {}
		DeliveryVehicle vehicle = null;
		try {
			vehicle = vehiclesQueue.take();
		} catch (InterruptedException e) {}
		semaphore.release();
		future.resolve(vehicle);
		return future;
	}
	/**
     * Releases a specified vehicle, opening it again for the possibility of
     * acquisition.
     * <p>
     * @param vehicle	{@link DeliveryVehicle} to be released.
     */
	public void releaseVehicle(DeliveryVehicle vehicle) {
		try {
			vehiclesQueue.put(vehicle);
		} catch (InterruptedException e) {}
	}
	
	/**
     * Receives a collection of vehicles and stores them.
     * <p>
     * @param vehicles	Array of {@link DeliveryVehicle} instances to store.
     */
	public void load(DeliveryVehicle[] vehicles) {
		if(isLoaded.compareAndSet(false, true)){
			for(int i=0;i<vehicles.length;i++){
				vehiclesQueue.add(vehicles[i]);
			}
		}
	}
}
