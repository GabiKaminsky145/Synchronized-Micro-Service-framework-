package bgu.spl.mics.application.passiveObjects;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passive object representing the store finance management. 
 * It should hold a list of receipts issued by the store.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add ONLY private fields and methods to this class as you see fit.
 */
public class MoneyRegister implements Serializable {

	private List<OrderReceipt> receipts;
	private int totalEarnings;
	private AtomicBoolean isSerialized;

	private static class MoneyRegisterHolder{
		private static MoneyRegister instance = new MoneyRegister();
	}

	private MoneyRegister(){
		receipts = new LinkedList<>();
		totalEarnings = 0;
		isSerialized = new AtomicBoolean(false);
	}
	
	/**
     * Retrieves the single instance of this class.
     */
	public static MoneyRegister getInstance() {
		return MoneyRegisterHolder.instance;
	}
	
	/**
     * Saves an order receipt in the money register.
     * <p>   
     * @param r		The receipt to save in the money register.
     */
	public synchronized void file (OrderReceipt r) {
		boolean flag = false;
		for(int i=0;!flag && i<receipts.size();i++) {
			if (receipts.get(i).getOrderId() == r.getOrderId())
				flag = true;
		}
		if(!flag){
			receipts.add(r);
			totalEarnings+=r.getPrice();
		}
	}
	
	/**
     * Retrieves the current total earnings of the store.  
     */
	public int getTotalEarnings() {
		return totalEarnings;
	}
	
	/**
     * Charges the credit card of the customer a certain amount of money.
     * <p>
     * @param amount 	amount to charge
     */
	public void chargeCreditCard(Customer c, int amount) {
		c.chargeCustomer(amount);
	}
	
	/**
     * Prints to a file named @filename a serialized object List<OrderReceipt> which holds all the order receipts 
     * currently in the MoneyRegister
     * This method is called by the main method in order to generate the output.. 
     */
	public void printOrderReceipts(String filename) {
		if(isSerialized.compareAndSet(false, true)) {
			Serializer s = new Serializer(filename, receipts);
			s.serialize();
		}
	}
}