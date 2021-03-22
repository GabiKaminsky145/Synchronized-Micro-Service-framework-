package bgu.spl.mics.application;

        import bgu.spl.mics.application.passiveObjects.*;
        import bgu.spl.mics.application.services.*;
        import com.google.gson.Gson;
        import com.google.gson.JsonArray;
        import com.google.gson.JsonObject;
        import java.io.*;
        import java.nio.file.Paths;
        import java.util.*;
        import java.util.concurrent.CountDownLatch;

/** This is the Main class of the application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output serialized objects.
 */
public class BookStoreRunner {
    public static void main(String[] args) throws FileNotFoundException, InterruptedException {

        /*parsing the json input file*/

        Gson gson = new Gson();
        File jsonFile = Paths.get(args[0]).toFile();
        JsonObject jsonObject = gson.fromJson(new FileReader(jsonFile), JsonObject.class);

        JsonArray initInventory = jsonObject.getAsJsonArray("initialInventory");
        JsonArray initResources = jsonObject.getAsJsonArray("initialResources");
        JsonObject vehicles = initResources.get(0).getAsJsonObject();
        JsonArray vehiclesArr = vehicles.getAsJsonArray("vehicles");
        JsonObject services = jsonObject.getAsJsonObject("services");
        JsonObject time = services.getAsJsonObject("time");
        Integer selling = services.get("selling").getAsInt();
        Integer inventoryService = services.get("inventoryService").getAsInt();
        Integer logistics = services.get("logistics").getAsInt();
        Integer resourcesService = services.get("resourcesService").getAsInt();
        JsonArray customers = services.get("customers").getAsJsonArray();


        List<SellingService> sellingServices = new ArrayList<>();
        List<InventoryService> inventoryServices = new ArrayList<>();
        List<LogisticsService> logisticsServices = new ArrayList<>();
        List<ResourceService> resourceServices = new ArrayList<>();
        List<APIService> apiServices = new ArrayList<>();


        int numOfTasks = selling + inventoryService + logistics + resourcesService + customers.size();
        CountDownLatch countDownLatch = new CountDownLatch(numOfTasks);
        CountDownLatch apiHasFinished = new CountDownLatch(customers.size());

        String customersFileName = args[1];
        String booksFileName = args[2];
        String receiptsFileName = args[3];
        String moneyRegisterFileName = args[4];
        Map<Integer, Customer> customersOutput = new HashMap<>();

        BookInventoryInfo[] books = new BookInventoryInfo[initInventory.size()];
        for (int i = 0; i < initInventory.size(); i++) {
            JsonObject book = (JsonObject) initInventory.get(i);
            String bookTitle = book.get("bookTitle").getAsString();
            int amount = book.get("amount").getAsInt();
            int price = book.get("price").getAsInt();
            books[i] = new BookInventoryInfo(bookTitle, amount, price);
        }
        for (int i = 0; i < inventoryService; i++) {
            inventoryServices.add(new InventoryService(countDownLatch, "inventory " + (i + 1), books, booksFileName));
        }

        DeliveryVehicle[] autos = new DeliveryVehicle[vehiclesArr.size()];
        for (int i = 0; i < vehiclesArr.size(); i++) {
            JsonObject auto = (JsonObject) vehiclesArr.get(i);
            int license = auto.get("license").getAsInt();
            int speed = auto.get("speed").getAsInt();
            autos[i] = new DeliveryVehicle(license, speed);
        }
        for (int i = 0; i < resourcesService; i++) {
            resourceServices.add(new ResourceService(countDownLatch, "resource " + (i + 1), autos));
        }

        for (int i = 0; i < selling; i++) {
            sellingServices.add(new SellingService(countDownLatch, "selling " + (i + 1)));
        }

        for (int i = 0; i < logistics; i++) {
            logisticsServices.add(new LogisticsService(countDownLatch, "logistic " + (i + 1)));
        }

        int speed = time.get("speed").getAsInt();
        int duration = time.get("duration").getAsInt();
        TimeService timeService = new TimeService(countDownLatch, speed, duration, apiHasFinished);

        int currentOrderId = 0;
        for (int i = 0; i < customers.size(); i++) {
            JsonObject customerObj = (JsonObject) customers.get(i);
            Integer id = customerObj.get("id").getAsInt();
            String name = customerObj.get("name").getAsString();
            String address = customerObj.get("address").getAsString();
            Integer distance = customerObj.get("distance").getAsInt();
            JsonObject creditCard = customerObj.get("creditCard").getAsJsonObject();
            int number = creditCard.get("number").getAsInt();
            int amount = creditCard.get("amount").getAsInt();
            List<OrderSchedule> orders = new ArrayList<>();
            JsonArray orderArr = customerObj.get("orderSchedule").getAsJsonArray();
            for (int j = 0; j < orderArr.size(); j++) {
                JsonObject order = orderArr.get(j).getAsJsonObject();
                String bookTitle = order.get("bookTitle").getAsString();
                BookInventoryInfo book = null;
                boolean flag = false;
                for (int k = 0; !flag && k < books.length; k++) {
                    if (books[k].getBookTitle().equals(bookTitle)) {
                        book = books[k];
                        flag = true;
                    }
                }
                if (flag) {
                    Integer tick = order.get("tick").getAsInt();
                    orders.add(new OrderSchedule(book, tick, currentOrderId));
                    currentOrderId++;
                }
            }
            Collections.sort(orders, (Comparator.comparingInt(OrderSchedule::getTick)));
            Customer c = new Customer(id, name, address, distance, number, amount);
            apiServices.add(new APIService(countDownLatch, "api " + (i + 1), c, orders, apiHasFinished, duration));
            customersOutput.put(id, c);
        }

        List<Thread> threads = new ArrayList<>();
        int counter = 0;

        for (SellingService service : sellingServices) {
            threads.add(new Thread(service));
            threads.get(counter).start();
            counter++;
        }
        for (InventoryService service : inventoryServices) {
            threads.add(new Thread(service));
            threads.get(counter).start();
            counter++;
        }
        for (LogisticsService service : logisticsServices){
            threads.add(new Thread(service));
            threads.get(counter).start();
            counter++;
        }
        for(ResourceService service: resourceServices) {
            threads.add(new Thread(service));
            threads.get(counter).start();
            counter++;
        }
        for(APIService service: apiServices) {
            threads.add(new Thread(service));
            threads.get(counter).start();
            counter++;
        }
        threads.add(new Thread(timeService));
        threads.get(counter).start();

        for(Thread t: threads)
            t.join();

        /*serializing*/

        MoneyRegister.getInstance().printOrderReceipts(receiptsFileName);
        MoneyRegisterSerializer.getInstance().serialize(moneyRegisterFileName,MoneyRegister.getInstance());
        Inventory.getInstance().printInventoryToFile(booksFileName);
        Serializer s = new Serializer(customersFileName, customersOutput);
        s.serialize();

        System.exit(0);
    }
}