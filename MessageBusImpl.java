package bgu.spl.mics;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private Map<MicroService, BlockingQueue<Message>> queuesMap;
	private Map<Class<? extends Event>, BlockingQueue<MicroService>> eventSubscriptionsMap;
	private Map<Class<? extends Broadcast>, BlockingQueue<MicroService>> bcastSubscriptionsMap;
	private Map<Event, Future> eventResults;
	private Object lock;

	private static class MessageBusImplHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}
	private MessageBusImpl() {
		queuesMap = new ConcurrentHashMap<>();
		eventSubscriptionsMap = new ConcurrentHashMap<>();
		bcastSubscriptionsMap = new ConcurrentHashMap<>();
		eventResults = new ConcurrentHashMap<>();
		lock = new Object();
	}
	public static MessageBusImpl getInstance() {
		return MessageBusImplHolder.instance;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		if(!eventSubscriptionsMap.containsKey(type)) {
			BlockingQueue<MicroService> bq = new LinkedBlockingQueue<>();
			bq.add(m);
			eventSubscriptionsMap.put(type, bq);
		}
		else {
			try {
				eventSubscriptionsMap.get(type).put(m);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		if(!bcastSubscriptionsMap.containsKey(type)) {
			BlockingQueue<MicroService> bq = new LinkedBlockingQueue<>();
			bq.add(m);
			bcastSubscriptionsMap.put(type, bq);
		}
		else {
			try {
				bcastSubscriptionsMap.get(type).put(m);
			} catch (InterruptedException e) {}
		}
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		eventResults.get(e).resolve(result);
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		for(Map.Entry<MicroService, BlockingQueue<Message>> ms: queuesMap.entrySet()){
			if(bcastSubscriptionsMap.get(b.getClass()).contains(ms.getKey()))
				queuesMap.get(ms.getKey()).add(b);
		}
	}

	
	@Override
	public synchronized  <T> Future<T> sendEvent(Event<T> e) {
		MicroService ms = null;
		try {
			ms = eventSubscriptionsMap.get(e.getClass()).take();
		} catch (InterruptedException exc){}
		try {
			eventSubscriptionsMap.get(e.getClass()).put(ms);
		} catch (InterruptedException e1) {}
		eventResults.put(e, new Future());
		queuesMap.get(ms).add(e);
		return eventResults.get(e);
	}

	@Override
	public void register(MicroService m) {
		queuesMap.put(m, new LinkedBlockingQueue<>());
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (lock) {
			for(Map.Entry<Class<? extends Event>, BlockingQueue<MicroService>> event: eventSubscriptionsMap.entrySet()){
				eventSubscriptionsMap.get(event.getKey()).remove(m);
			}
			for(Map.Entry<Class<? extends Broadcast>, BlockingQueue<MicroService>> broadcast: bcastSubscriptionsMap.entrySet()){
				bcastSubscriptionsMap.get(broadcast.getKey()).remove(m);
			}
			queuesMap.remove(m);
		}


	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		Message message = null;
		if(queuesMap.get(m) == null)
			throw new IllegalStateException();
		try {
			message = queuesMap.get(m).take();
		} catch (InterruptedException e) {}
		return message;
	}
}
