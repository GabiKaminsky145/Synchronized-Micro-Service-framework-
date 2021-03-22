package bgu.spl.mics.application;

import bgu.spl.mics.application.passiveObjects.BookInventoryInfo;
import bgu.spl.mics.application.passiveObjects.OrderResult;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConReaderWriterMap extends ReaderWriter<String> {

    /*A Reader-Writer that uses "doRead" for "checkAvailabilityAndGetPrice" function, and "doWrite" for "take" function of the Inventory*/

    private Map<String, BookInventoryInfo> map;
    private Map<String, Integer> booksToPrint; //We added this line to the fixed version

    public ConReaderWriterMap() {
        map = new ConcurrentHashMap<>();
	booksToPrint = new HashMap<>(); //We added this line to the fixed version
    }

    @Override
    protected int doRead(String obj) {
        if(!map.containsKey(obj))
            return -1;
        return map.get(obj).getPrice();
    }

    @Override
    protected OrderResult doWrite(String obj) {
        if(!map.containsKey(obj))
            return OrderResult.NOT_IN_STOCK;
        int amount = map.get(obj).getAmountInInventory();
        if(amount == 1)
            map.remove(obj);
        else map.get(obj).setAmount(amount-1);
        return OrderResult.SUCCESSFULLY_TAKEN;
    }

    public Map getMap(){
        return map;
    }

    public void load(BookInventoryInfo[] inventory){
        for(int i=0;i<inventory.length;i++) {
            map.put(inventory[i].getBookTitle(), inventory[i]);
	    booksToPrint.put(inventory[i].getBookTitle(), 0); //We added this line to the fixed version
        }
    }

    public Map<String,Integer> getOutputMap(){
	//We fixed this function according to our explanation in the appeal
	for(String book: booksToPrint.keySet()){
	    if(map.containsKey(book))
	        booksToPrint.replace(book, 0, map.get(book).getAmountInInventory());
	}
	return booksToPrint;
    }
}
