package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.InterruptBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;
import bgu.spl.mics.application.passiveObjects.MoneyRegister;
import bgu.spl.mics.application.passiveObjects.ResourcesHolder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private CountDownLatch countDownLatch;
	private int currentTick;
	private int speed;
	private int duration;
	private CountDownLatch apiHasFinished;

	public TimeService(CountDownLatch countDownLatch, int speed, int duration, CountDownLatch apiHasFinished) {
		super("Time Service");
		this.countDownLatch = countDownLatch;
		currentTick = 1;
		this.speed = speed;
		this.duration = duration;
		this.apiHasFinished = apiHasFinished;
	}

	@Override
	protected void initialize() {
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {}
		timeRun();
	}


	private void timeRun() {
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				if (currentTick != duration) {
					sendBroadcast(new TickBroadcast(currentTick));
					currentTick++;
				} else {
					cancel();
					try {
						apiHasFinished.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					sendBroadcast(new InterruptBroadcast());
					terminate();
				}
			}
		};
		timer.scheduleAtFixedRate(timerTask, speed, speed);
	}

}
