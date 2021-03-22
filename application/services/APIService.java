package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.BookOrderEvent;
import bgu.spl.mics.application.messages.InterruptBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.OrderSchedule;
import bgu.spl.mics.application.passiveObjects.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * APIService is in charge of the connection between a client and the store.
 * It informs the store about desired purchases using {@link BookOrderEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link ResourcesHolder}, {@link MoneyRegister}, {@link Inventory}.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class APIService extends MicroService{

	private CountDownLatch countDownLatch;
	private Customer c;
	private List<OrderSchedule> orders;
	private int currentOrder;
	private CountDownLatch apiHasFinished;
	private int ordersThatHasDone;
	private int duration;
	private int currentTick;

	public APIService(CountDownLatch countDownLatch, String name, Customer c, List<OrderSchedule> orders, CountDownLatch apiHasFinished, int duration) {
		super(name);
		this.countDownLatch = countDownLatch;
		this.c = c;
		this.orders = orders;
		currentOrder = 0;
		this.apiHasFinished = apiHasFinished;
		ordersThatHasDone = 0;
		this.duration = duration;
		currentTick = 0;
	}

	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, (TickBroadcast tb)->{
			currentTick = tb.getCurrentTick();
			if(!isDone()) {
				List<Future<OrderReceipt>> futures = new ArrayList<>();
				for (int i = currentOrder; i < orders.size() && orders.get(i).getTick() == currentTick; i++) {
					futures.add(sendEvent(new BookOrderEvent(c, orders.get(i).getBook().getBookTitle(), orders.get(i).getBook().getPrice(), orders.get(i).getOrderId(), orders.get(i).getTick())));
					currentOrder++;
				}
				for (int i = 0; i < futures.size(); i++) {
					OrderReceipt receipt = futures.get(i).get();
					ordersThatHasDone++;
					if (receipt != null)
						c.getCustomerReceiptList().add(receipt);
				}
				if (isDone()) {
					apiHasFinished.countDown();
				}
			}
		});

		subscribeBroadcast(InterruptBroadcast.class, (InterruptBroadcast broadcast)->{
			terminate();
		});

		countDownLatch.countDown();
	}

	public Customer getC(){
		return c;
	}

	private boolean isDone(){
		return ((orders.size() == ordersThatHasDone) || ((ordersThatHasDone < orders.size()) && (orders.get(ordersThatHasDone).getTick()>=duration)));
	}

}
