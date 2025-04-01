package market;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.net.URISyntaxException;
import java.net.URI;
import uk.oczadly.karl.jnano.websocket.NanoWebSocketClient;
import java.lang.InterruptedException;
import uk.oczadly.karl.jnano.websocket.topic.TopicConfirmation;
import uk.oczadly.karl.jnano.websocket.topic.TopicUnconfirmedBlocks;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.NanoAmount;
import java.math.BigInteger;
import java.math.BigDecimal;
import uk.oczadly.karl.jnano.rpc.exception.RpcException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import static spark.Spark.*;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystems;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.time.Instant;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.lang.InterruptedException;
import java.util.Timer;
import java.util.TimerTask;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;

import uk.oczadly.karl.jnano.util.workgen.OpenCLWorkGenerator;
import uk.oczadly.karl.jnano.util.workgen.WorkGenerator;
import uk.oczadly.karl.jnano.util.workgen.FutureWork;
import uk.oczadly.karl.jnano.util.workgen.GeneratedWork;
import uk.oczadly.karl.jnano.model.HexData;
import uk.oczadly.karl.jnano.model.work.WorkDifficulty;
import uk.oczadly.karl.jnano.rpc.request.wallet.RequestSend;
import uk.oczadly.karl.jnano.rpc.RpcQueryNode;
import uk.oczadly.karl.jnano.rpc.request.node.RequestFrontiers;
import uk.oczadly.karl.jnano.rpc.response.ResponseMultiAccountFrontiers;
import uk.oczadly.karl.jnano.model.NanoAccount;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockHash;
import uk.oczadly.karl.jnano.model.work.WorkSolution;
import uk.oczadly.karl.jnano.util.workgen.OpenCLWorkGenerator.OpenCLInitializerException;
import uk.oczadly.karl.jnano.rpc.exception.RpcInvalidArgumentException;

import com.talanlabs.avatargenerator.eightbit.EightBitAvatar;
import com.talanlabs.avatargenerator.Avatar;
import java.util.Base64;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CountDownLatch;





public class Tools {

    public static String getCancelID(String id) throws FileNotFoundException, IOException {
        String jsonFile = Main.itemsPath + id+ ".json"; // Replace with your file path
        BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();
        String cancelID = jsonObject.get("cancelID").getAsString();

        return cancelID;

    }


	public static boolean orderPaid(String id, String seller) {

		try {
            		// Read the JSON file into a String
            		String jsonFile = Main.itemsPath + id+ ".json"; // Replace with your file path
            		//FileReader fileReader = new FileReader(jsonFile);
                    BufferedReader reader = new BufferedReader(new FileReader(jsonFile));

            		// Parse the JSON string to JsonArray
            		JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                    HttpRequests.sendOrderMail(jsonObject);

		            // Close the file reader
            		//fileReader.close();
                    reader.close();

            		// Find object with id equal to 2
            		jsonObject = modifyObject(jsonObject, id, seller);

			return true;


        	} catch (IOException e) {
            		e.printStackTrace();
        	}
		return false;
	}

	public static boolean buyerPaid(String id, String buyerAccount, String email, String quantity, String paymentID, String lockedFunds) throws FileNotFoundException, IOException {
        System.out.println("LOCK FUNDS IN BUYER PAID: " + lockedFunds);

        String publicOrderID = UUID.randomUUID().toString();

        //long lockTimestamp = Instant.now().getEpochSecond() + 2592000;

        long lockTimestamp = Instant.now().getEpochSecond() + 3888000;

       // long lockTimestamp = Instant.now().getEpochSecond() + 120;
        
        String timeStr = String.valueOf(lockTimestamp);


        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(lockTimestamp), ZoneId.systemDefault());
        String lockDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

			// Read the JSON file into a String
            		String jsonFile = Main.itemsPath + id+ ".json"; // Replace with your file path
            		//FileReader fileReader = new FileReader(jsonFile);
                    BufferedReader reader = new BufferedReader(new FileReader(jsonFile));

            		// Parse the JSON string to JsonArray
            		JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
			//Gson gson = new Gson();
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
            		Item item = gson.fromJson(jsonObject, Item.class);
                    reader.close();


			ArrayList<Item.Buyer> buyerList = item.getBuyerList();

			Item.Buyer buyer = new Item.Buyer();
                buyer.setLockTimestamp(timeStr);
                buyer.setPublicOrderID(publicOrderID);
                buyer.setLockDate(lockDate);
			    buyer.setInvoiceID(paymentID);
			    buyer.setLockedFunds(lockedFunds);
                System.out.println("Buyer Locked Funds: " + buyer.getLockedFunds()); // Check if it's assigned properly
        		buyer.setApproved("false");
        		buyer.setBuyerAddress(buyerAccount);
        		buyer.setQuantity(quantity);
        		buyer.setEmail(email);

			int newSellerQuan = Integer.parseInt(item.getQuantity()) - Integer.parseInt(quantity);
			String newSellerQuanStr = String.valueOf(newSellerQuan);
			item.setQuantity(newSellerQuanStr);
            HttpRequests.purchaseMail(item, buyer, item.getTitle());
            HttpRequests.soldMail(item, buyer);
			buyerList.add(buyer);
			item.setBuyerList(buyerList);


            //update the total locked field
            BigInteger total = new BigInteger(item.getLocked());
            BigInteger buyerLocked = new BigInteger(buyer.getLockedFunds());
            BigInteger newTotalLocked = total.add(buyerLocked);

            item.setLocked(newTotalLocked.toString());



			//write it back to the file
			String itemJson = item.toJson();
            //System.out.println(itemJson);
			JsonObject itemJsonObject = JsonParser.parseString(itemJson).getAsJsonObject();
			FileWriter fileWriter = new FileWriter(jsonFile);
		//	Gson g = new Gson();
            Gson g = new GsonBuilder().setPrettyPrinting().create();
        		g.toJson(itemJsonObject, fileWriter);
        		fileWriter.close();

			//item.setBuyerList(buyer);
            
            //Add to paidList json file
            String paidList = "/home/server-admin/javaProjects/paidList.json"; // Replace with your file path
            reader = new BufferedReader(new FileReader(paidList));
            JsonArray paymentIdsArray = JsonParser.parseReader(reader).getAsJsonArray();
            //JsonArray paymentIdsArray = jsonObject.getAsJsonArray("paymentIDs");
            paymentIdsArray.add(paymentID);
			fileWriter = new FileWriter(paidList);
            g = new GsonBuilder().setPrettyPrinting().create();
        		g.toJson(paymentIdsArray, fileWriter);
        		fileWriter.close();
            reader.close();

            Main.homeListings = getListings();
            Main.allListings = getAllListings();

			return true;
	}


	private static JsonObject modifyObject(JsonObject jsonObject, String id, String seller) throws IOException {

        //UUID uuid = UUID.randomUUID();
        //String cancelID = uuid.toString();
        //long lockTimestamp = Instant.now().getEpochSecond() + 2592000;

        //long lockTimestamp = Instant.now().getEpochSecond() + 2592000;

        
        jsonObject.addProperty("paid", "true");
        jsonObject.addProperty("seller", seller);
		//jsonObject.addProperty("lockTimeStamp", lockTimestamp);
		jsonObject.addProperty("seller", seller);
		//jsonObject.addProperty("cancelID", cancelID);
		FileWriter fileWriter = new FileWriter(Main.itemsPath + id + ".json");
		//Gson gson = new Gson();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        	gson.toJson(jsonObject, fileWriter);
        	fileWriter.close();

                return jsonObject; // Assuming there is only one object with the given id
        }
    /*
	public static JsonObject mergeJsonObjects(JsonObject jsonObject1, JsonObject jsonObject2) {
        	for (String key : jsonObject2.keySet()) {
            		JsonElement value = jsonObject2.get(key);
                    if (value != null && !value.isJsonNull() && !value.getAsString().isEmpty()) {
            		    jsonObject1.add(key, value);
                    }
        	}
		return jsonObject1;
	}
    */
    public static JsonObject mergeJsonObjects(JsonObject jsonObject1, JsonObject jsonObject2) {
    for (String key : jsonObject2.keySet()) {
        JsonElement value = jsonObject2.get(key);

        // Allow empty values only for tag1, tag2, and tag3
        if (value != null && !value.isJsonNull() &&
           (key.equals("tag1") || key.equals("tag2") || key.equals("tag3") || !value.getAsString().isEmpty())) {
            jsonObject1.add(key, value);
        }
    }
    return jsonObject1;
}

	public static boolean checkLength(JsonObject jsonObject) {
        String tag1 = "";
        String tag2 = "";
        String tag3 = "";
        String email = "";
        String image = "";
        String description = "";
        String title = "";

        try {
		title = jsonObject.get("title").getAsString();
        }
        catch(NullPointerException e) {
            title ="";

        }
        System.out.println("title length: " + title.length());
		if(title.length() > 50) {
			return false;
		}
		
    try {
		description = jsonObject.get("description").getAsString();
    }
    catch(NullPointerException e) {
        description = "";
    }

		if(description.length() > 15000) {
			return false;
		}
        try {
		 tag1 = jsonObject.get("tag1").getAsString();
        }
        catch(NullPointerException e){
            tag1="";
        }
        try{
		 tag2 = jsonObject.get("tag2").getAsString();
        }
        catch(NullPointerException e) {
            tag2 = "";
        }
        try {
		 tag3 = jsonObject.get("tag3").getAsString();
        }
        catch(NullPointerException e)  {
           tag3=""; 
        }
		if (tag1.length() > 15 || tag2.length() > 15 || tag3.length() > 15) {
			return false;
		}
        try {
		 image = jsonObject.get("image").getAsString();
        }
        catch(NullPointerException e) {
            image = "";
        }
		byte[] bytes = image.getBytes(); // Default encoding is UTF-8
		double sizeInMB = (double) bytes.length / (1024 * 1024);
		if(sizeInMB > 1.0) {
			return false;
		}
        try {
		    email = jsonObject.get("email").getAsString();
        }
        catch(NullPointerException e){
            email = "";

        }

		if(email.length() > 50) {
			return false;
		}

		return true;

	}

    public static void lockSend() throws IOException {

        ArrayList<Item> items = (ArrayList<Item>) Main.allListings.get("items");

		ArrayList<Item.Buyer> buyerList = new ArrayList<Item.Buyer>();
        long lockTimestamp;

        long currentTimestamp = Instant.now().getEpochSecond();

        String myAddress = "nano_179398g5wptitsz1t8ennoo3krpwuhquzkncas6nkcmrc91ji4a7mg3ucoew";
        String itemAddress;
        String id;

            //write it back to the file
			//String itemJson = item.toJson();
			//JsonObject itemJsonObject = JsonParser.parseString(itemJson).getAsJsonObject();
			//FileWriter fileWriter = new FileWriter(jsonFile);
		//	Gson g = new Gson();
           // Gson g = new GsonBuilder().setPrettyPrinting().create();
        	//	g.toJson(itemJsonObject, fileWriter);
        	//	fileWriter.close();


            for (Item item  : items) {
                id = item.getId();
                itemAddress = item.getAddress();
			    buyerList = item.getBuyerList();
                for (Item.Buyer buyer  : buyerList) {
                    if(buyer.isApproved().equals("false")) {
                        lockTimestamp = Long.parseLong(buyer.getLockTimestamp());
                      //  System.out.println("locked time stamp: " + lockTimestamp);
                       // System.out.println("current timestamp: " + currentTimestamp);
                        if(lockTimestamp < currentTimestamp) {
                            System.out.println("Should send the locked funds to myself");
                            //send funds to myself 
                            String amount = buyer.getLockedFunds();
                            HttpRequests.send(myAddress, itemAddress, amount);

                            buyer.setApproved("locked");
                            buyer.setLockedFunds("");
                            String itemJson = item.toJson();
                            JsonObject itemJsonObject = JsonParser.parseString(itemJson).getAsJsonObject();
			                FileWriter fileWriter = new FileWriter(Main.itemsPath + id + ".json");
                            Gson g = new GsonBuilder().setPrettyPrinting().create();
        	                g.toJson(itemJsonObject, fileWriter);
        	                fileWriter.close();


                                                    }
                        //System.out.println("lock fail");
                    }
                }

            }


        
    }
    public static String approve(String id, String invoiceID) throws FileNotFoundException, IOException{
        try {
		    String jsonFile = Main.itemsPath + id+ ".json"; // Replace with your file path
            //FileReader fileReader = new FileReader(jsonFile);

            BufferedReader fileReader = new BufferedReader(new FileReader(jsonFile));

            // Parse the JSON string to JsonArray
            JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
			//Gson gson = new Gson();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Item item = gson.fromJson(jsonObject, Item.class);
            
            long currentTimestamp = Instant.now().getEpochSecond();
        
            //String timeStr = String.valueOf(lockTimestamp);
            //long lockTimestamp = Long.parseLong(item.Buyer.getLockTimestamp());


			ArrayList<Item.Buyer> buyerList = item.getBuyerList();
            fileReader.close();
            
            for (Item.Buyer buyer  : buyerList) {
                if(buyer.getInvoiceID().equals(invoiceID)) {
                    long lockTimestamp = Long.parseLong(buyer.getLockTimestamp());
                    if(buyer.isApproved().equals("false")){
                        //Now send payment to seller
                        
                        //destination
                        String destination = item.getSeller();
                        //get item address
                        String itemAddress = item.getAddress();
                        //get amount to send
                        String amount = buyer.getLockedFunds();


                        BigInteger split = new BigInteger(amount);

                        BigInteger totalItemLock = new BigInteger(item.getLocked());

                        BigInteger result = split.divide(BigInteger.TWO); // Divides by 2

                        BigInteger minusFromTotal = split.add(result); 
                    

                        //System.out.println("RESULT OF SPLIT: " + result);
                        BigInteger newItemLock = totalItemLock.subtract(minusFromTotal);
                        System.out.println("NEW ITEM LOCK: " + newItemLock);
                        item.setLocked(newItemLock.toString());
                        System.out.println("NEW LOCKED " + item.getLocked());


                        BigInteger myFee = calculatePercentage(result, 5);
                        BigInteger sellersFee = calculatePercentage(result, 95);
                        String myAddress = "nano_179398g5wptitsz1t8ennoo3krpwuhquzkncas6nkcmrc91ji4a7mg3ucoew";
                        BigInteger totalForSeller = result.add(sellersFee);
                        //This is the amount given to the seller for the sold item
                        HttpRequests.send(destination, itemAddress, totalForSeller.toString());
                        //HttpRequests.send(destination, itemAddress, sellersFee.toString());
                        //HttpRequests.send(destination, itemAddress, result.toString());
                        //This is the collaterol given back to the buyer
                        HttpRequests.send(buyer.getBuyerAddress(), itemAddress, result.toString());
                        //This is my fee
                        HttpRequests.send(myAddress, itemAddress, myFee.toString());

                        buyer.setApproved("true");
                        String itemJson = item.toJson();
			            JsonObject itemJsonObject = JsonParser.parseString(itemJson).getAsJsonObject();
			            FileWriter fileWriter = new FileWriter(jsonFile);
			            //Gson g = new Gson();
                        Gson g = new GsonBuilder().setPrettyPrinting().create();
        		        g.toJson(itemJsonObject, fileWriter);
                        
        		        fileWriter.close();
                        // Update listings
                        Main.homeListings = Tools.getListings();
                        Main.allListings = Tools.getAllListings();


                        return "true";
                    }
                    else if(buyer.isApproved().equals("locked")) {
                        return "lock";

                    }
                    else if(buyer.isApproved().equals("true")){
                        return "alreadyTrue";
                    }

                    if(lockTimestamp < currentTimestamp) {
                        System.out.println("lock " + lockTimestamp);
                        System.out.println("current " + currentTimestamp);
                        System.out.println("STILL UNLOCKED");
                    }

                }
                
                
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return "false";
            
    }
public static boolean removeListing(String id, String cancelID) throws IOException {
    String jsonFile = Main.itemsPath + id + ".json";
    Path filePath = Paths.get(jsonFile);
    JsonObject jsonObject = null;

    if (Files.exists(filePath)) {
    try (BufferedReader fileReader = new BufferedReader(new FileReader(jsonFile))) {
         jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
        // Process jsonObject as needed
    } catch (IOException e) {
        System.err.println("Error reading JSON file: " + e.getMessage());
        return false;
    }
    } else {
        System.out.println("File does not exist: " + jsonFile);
        return false;
    }


/*
    BufferedReader fileReader = new BufferedReader(new FileReader(jsonFile));
    JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
    fileReader.close();
    */

    File file = new File(jsonFile);


    Gson gson = new GsonBuilder().setPrettyPrinting().create();
    Item item = gson.fromJson(jsonObject, Item.class);

    if (!item.getCancelID().equals(cancelID)) {
        return false;
    }

    if(item.getQuantity().equals("0")) {
        return false;
    }

    ArrayList<Item.Buyer> buyerList = new ArrayList<>(item.getBuyerList());
    BigInteger totalBuyerLocked = BigInteger.ZERO;
    BigInteger toRefundBig = BigInteger.ZERO;  // Initialize outside loop
    String toRefund = null;
    String actualRefund = null;

    if (file.exists()) {
            if(buyerList.isEmpty() || buyerList == null) {
                boolean deleted = file.delete();
                HttpRequests.send(item.getSeller(), item.getAddress(), item.getLocked());
                if (!deleted) {
                    System.err.println("Error: Unable to delete file " + jsonFile);
                }
                else {
                    // Update listings
                    Main.homeListings = Tools.getListings();
                    Main.allListings = Tools.getAllListings();


                    return true;
                }
            }
            boolean allApproved = true;
            for (Item.Buyer buyer : buyerList) {
                if(buyer.isApproved().equals("false")) {
                    allApproved = false;
                }
            }
            if(allApproved) {
                jsonObject.addProperty("quantity", "0");
                jsonObject.addProperty("active", "false");
                jsonObject.addProperty("totalLocked", BigInteger.ZERO.toString());
                jsonObject.addProperty("cancel", "true");

                try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                    gson.toJson(jsonObject, fileWriter);
                }

                HttpRequests.send(item.getSeller(), item.getAddress(), item.getLocked());
                // Update listings
                Main.homeListings = Tools.getListings();
                Main.allListings = Tools.getAllListings();

                return true;
            }

        for (Item.Buyer buyer : buyerList) {
            if(buyer.isApproved().equals("false")) {
                BigInteger buyerLocked = new BigInteger(buyer.getLockedFunds());
                BigInteger divide = buyerLocked.divide(BigInteger.TWO);
                totalBuyerLocked = totalBuyerLocked.add(buyerLocked);
                totalBuyerLocked = totalBuyerLocked.add(divide);
            }
        }
        //totalBuyerLocked = totalBuyerLocked.divide(BigInteger.TWO);
        System.out.println("TOTAL BUYER LOCKED " + totalBuyerLocked);

        if (totalBuyerLocked.compareTo(BigInteger.ZERO) > 0) {
            BigInteger totalLocked = new BigInteger(item.getLocked());
            toRefundBig = totalLocked.subtract(totalBuyerLocked);
            toRefund = toRefundBig.toString();
        
            //actualRefund = totalLocked.subtract(toRefundBig).toString();

            // Mark as inactive instead of deleting
            jsonObject.addProperty("totalLocked", toRefund);
            jsonObject.addProperty("active", "false");
            jsonObject.addProperty("quantity", "0");
            jsonObject.addProperty("cancel", "true");

            // Write updated JSON to file
            try (FileWriter fileWriter = new FileWriter(jsonFile)) {
                gson.toJson(jsonObject, fileWriter);
            }
        }

        // If there are funds to refund, process the refund
        if (toRefundBig.compareTo(BigInteger.ZERO) > 0) {
            HttpRequests.send(item.getSeller(), item.getAddress(), toRefund);
        }

        // If no buyer funds are locked, delete the file
        /*
        if (totalBuyerLocked.compareTo(BigInteger.ZERO) == 0) {
            System.out.println("FILE DELETED");
            boolean deleted = file.delete();
            if (!deleted) {
                System.err.println("Error: Unable to delete file " + jsonFile);
            }
        }
        */

        // Update listings
        Main.homeListings = Tools.getListings();
        Main.allListings = Tools.getAllListings();

        return true;
    }

    return false;
}
/* public static boolean removeListing(String id, String cancelID) throws FileNotFoundException, IOException { String jsonFile = Main.itemsPath + id+ ".json"; // Replace with your file path //FileReader fileReader = new FileReader(jsonFile); BufferedReader fileReader = new BufferedReader(new FileReader(jsonFile)); // Parse the JSON string to JsonArray JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
			//Gson gson = new Gson();
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Item item = gson.fromJson(jsonObject, Item.class);
            fileReader.close();

           if (!item.getCancelID().equals(cancelID)) {
                return false;
            }

            File file = new File(jsonFile);

            // Check if the file exists before trying to delete it
            //
		    ArrayList<Item.Buyer> buyerList = new ArrayList<Item.Buyer>();
            buyerList = item.getBuyerList();
            BigInteger totalBuyerLocked = BigInteger.ZERO;
            String toRefund = null;
            if (file.exists()) {
                for (Item.Buyer buyer  : buyerList) {
                    BigInteger buyerLocked =  new BigInteger(buyer.getLockedFunds());
                    totalBuyerLocked = totalBuyerLocked.add(buyerLocked);
                }
                if (totalBuyerLocked.compareTo(BigInteger.ZERO) > 0) {
                    //minus is from the total locked funds
                    BigInteger totalLocked = new BigInteger(item.getLocked());
                    BigInteger toRefundBig = totalLocked.subtract(totalBuyerLocked);
                    toRefund = toRefundBig.toString();

                    //don't delete the file but make it as inactive
                    jsonObject.addProperty("active", "false");

                    //write out to file
                    FileWriter fileWriter = new FileWriter(Main.itemsPath + id + ".json");
		        //Gson gson = new Gson();
                    Gson gsonWrite = new GsonBuilder().setPrettyPrinting().create();
        	        gsonWrite.toJson(jsonObject, fileWriter);
        	        fileWriter.close();

                }
                //check if there is buyer locked funds

                if (toRefundBig.compareTo(BigInteger.ZERO) > 0) {

                    HttpRequests.send(item.getSeller(), item.getAddress(), toRefund);
                {

                if (totalBuyerLocked.compareTo(BigInteger.ZERO) == 0) {

                    boolean deleted = file.delete();
                 

                }
                Main.homeListings = Tools.getListings();
                Main.allListings = Tools.getAllListings();

                // Attempt to delete the file

                return true;
            }
            else {
                return false;
            }
    }
*/

    public static void addListing(Item item) {
        ArrayList<Item> items = (ArrayList<Item>) Main.allListings.get("items");
        items.add(item);
        Main.allListings.put("items", items);

    }

    public static HashMap<String, Object> getAllListings() {
        final String ITEMS_DIRECTORY = Main.itemsPath;

        // Initialize Gson and a list to hold items
        Gson gson = new Gson();
        List<Item> itemsList = new ArrayList<>();

        // Get all JSON files in the directory
        File dir = new File(ITEMS_DIRECTORY);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    // Deserialize JSON file to Item object
                    Item item = gson.fromJson(reader, Item.class);
                    int quantity = Integer.parseInt(item.getQuantity());


                    // Add item to list if it is active
                    if (item.isActive() && quantity > 0) {
                        itemsList.add(item);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
         // Sort items by timestamp in descending order
          List<Item> sortedItems = itemsList.stream()
                  .sorted(Comparator.comparingLong(Item::getTimestamp).reversed())
                  .collect(Collectors.toList());
        
        HashMap<String, Object> itemsMap = new HashMap<>();
        itemsMap.put("items", sortedItems); 

        return itemsMap;
    }


    public static HashMap<String, Object> getListings() {
        final String ITEMS_DIRECTORY = Main.itemsPath;

        // Initialize Gson and a list to hold items
        Gson gson = new Gson();
        List<Item> itemsList = new ArrayList<>();

        // Get all JSON files in the directory
        File dir = new File(ITEMS_DIRECTORY);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));

        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    // Deserialize JSON file to Item object
                    Item item = gson.fromJson(reader, Item.class);
                    int quantity = Integer.parseInt(item.getQuantity());


                    // Add item to list if it is active
                    if (item.isActive() && quantity > 0) {
                        itemsList.add(item);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Sort items by timestamp in descending order
        List<Item> sortedItems = itemsList.stream()
                .sorted(Comparator.comparingLong(Item::getTimestamp).reversed())
                .collect(Collectors.toList());

        // Get the last 10 items
        List<Item> latestItems = sortedItems.stream()
                .limit(30)
                .collect(Collectors.toList());

       
        // Convert the list of latest items to a HashMap with item ID as the key
        /*
        HashMap<String, Object> activeItemsMap = new HashMap<>();
        for (Item item : latestItems) {
            activeItemsMap.put(item.getId(), item);
        }
        */

        HashMap<String, Object> activeItemsMap = new HashMap<>();

        activeItemsMap.put("items", latestItems);

        return activeItemsMap;
             
    }
     public static String stringToBase64 (String input) {
        // Convert the input string to bytes
        byte[] encodedBytes = Base64.getEncoder().encode(input.getBytes());
        // Convert bytes to a string and return it
        return new String(encodedBytes);
    }
public static HashMap<String, Object> getListings(String searchTag) {
    HashMap<String, Object> activeItemsMap = new HashMap<>();

    if (searchTag == null || searchTag.stripTrailing().isEmpty()) {
        return activeItemsMap; // Return empty map if searchTag is empty
    }

    searchTag = searchTag.stripTrailing();
    ArrayList<Item> allItems = (ArrayList<Item>) Main.allListings.get("items");
    List<Item> filteredItems = new ArrayList<>();

    if (allItems != null) {
        for (Item item : allItems) {
            int quantity = Integer.parseInt(item.getQuantity());

            if (item.isActive() && quantity > 0) {
                if (searchTag.equals(item.getTag1().stripTrailing()) ||
                    searchTag.equals(item.getTag2().stripTrailing()) ||
                    searchTag.equals(item.getTag3().stripTrailing())) {
                    filteredItems.add(item);
                }
            }
        }
    }

    activeItemsMap.put("itemsList", filteredItems);
    return activeItemsMap;
}
public static HashMap<String, Object> getListingsByCountry(String country, String searchTag) {
    HashMap<String, Object> activeItemsMap = new HashMap<>();

    if (searchTag == null || searchTag.stripTrailing().isEmpty()) {
        return activeItemsMap; // Return empty map if searchTag is empty
    }

    searchTag = searchTag.stripTrailing();
    ArrayList<Item> allItems = (ArrayList<Item>) Main.allListings.get("items");
    List<Item> filteredItems = new ArrayList<>();

    if (allItems != null) {
        for (Item item : allItems) {
            String countryClean = item.getCountry().replaceAll("\\s+", "").toLowerCase();
            int quantity = Integer.parseInt(item.getQuantity());
            if(searchTag.equals("all")) {
                if (item.isActive() && quantity > 0) {
                  if (country.equals(item.getCountry().replaceAll("\\s+", "").toLowerCase())) {
                      filteredItems.add(item);
                  }
              }
                
            }
            else {

            if (item.isActive() && quantity > 0 &&
             (
             (searchTag.equals(item.getTag1().stripTrailing()) && countryClean.equals(country)) ||
             (searchTag.equals(item.getTag2().stripTrailing()) && countryClean.equals(country)) ||
             (searchTag.equals(item.getTag3().stripTrailing()) && countryClean.equals(country))
             )
            ) {
                filteredItems.add(item);
            }
            }
        }
    }

    activeItemsMap.put("itemsList", filteredItems);
    return activeItemsMap;
}

/*
    public static HashMap<String, Object> getListings(String searchTag) {

        final String ITEMS_DIRECTORY = "/home/server-admin/javaProjects/items/";
        searchTag = searchTag.stripTrailing();

        // Initialize Gson and a list to hold items
        Gson gson = new Gson();
        List<Item> itemsList = new ArrayList<>();

        // Get all JSON files in the directory
        File dir = new File(ITEMS_DIRECTORY);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        
        HashMap<String, Object> activeItemsMap = new HashMap<>();

        if(searchTag.trim().isEmpty()) {
           // returns empty map.
            return activeItemsMap;
        }
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    // Deserialize JSON file to Item object
                    Item item = gson.fromJson(reader, Item.class);
                    int quantity = Integer.parseInt(item.getQuantity());
                    // Check if item is active and quantity is greater than 0
                        if (item.isActive() && quantity > 0) {
                        // Check if any tag matches the searchTag
                            if (searchTag != null && (searchTag.equals(item.getTag1().stripTrailing()) || searchTag.equals(item.getTag2().stripTrailing()) || searchTag.equals(item.getTag3().stripTrailing()))) {
                            //   System.out.println("FOUND ITEM");
                                itemsList.add(item);
                            }
                         }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //HashMap<String, Object> activeItemsMap = new HashMap<>();
        activeItemsMap.put("itemsList", itemsList);
        /*
        //HashMap<String, Object> activeItemsMap = new HashMap<>();
        for (Item item : itemsList) {
            activeItemsMap.put(item.getId(), item);
        }

        return activeItemsMap;

    }
*/

     public static BigInteger calculatePercentage(BigInteger value, int percent) {
        // Create a BigInteger for 100 (the denominator for percentage calculation)
        BigInteger hundred = BigInteger.valueOf(100);

        // Calculate percent as BigInteger
        BigInteger percentage = BigInteger.valueOf(percent);

        // Compute percentage
        BigInteger result = value.multiply(percentage).divide(hundred);

        return result;
    }

     public static boolean checkAlreadyPaid (String targetPaymentID) throws FileNotFoundException {
            String paidList = "/home/server-admin/javaProjects/paidList.json"; // Replace with your file path
                                                                               //
            boolean found = false;
            try {
            BufferedReader reader = new BufferedReader(new FileReader(paidList));
            JsonArray paymentIdsArray = JsonParser.parseReader(reader).getAsJsonArray();
            //JsonArray paymentIdsArray = jsonObject.getAsJsonArray("paymentIDs");
            for (JsonElement element : paymentIdsArray) {
                if (element.getAsString().equals(targetPaymentID)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                System.out.println("Payment ID is present in the paidList json file.");
                return found;
            } else {
                System.out.println("Payment ID is not found in the paidList json file.");
                return found;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return found;
    }

    public static void sendToItemAddress(String uniqueAccount, String itemAccount, BigInteger amount) {

                        //HttpRequests.send(destination, itemAddress, result.toString());
    }
    public static boolean checkFileExists(String id) throws IOException  {

        String jsonFile = Main.itemsPath + id+ ".json"; // Replace with your file path
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(jsonFile));
        }
        catch(FileNotFoundException e) {
            return false;
        }
        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
        reader.close();

        return true;

    }
   public static boolean checkUniqueEmail(String targetEmail) {
       System.out.println("CHECKING EMAILS");
    if (targetEmail == null) {
        System.out.println("Target email is null!");
        return false;
    }
    if (Main.sessionObjects == null) {
        System.out.println("sessionObjects is null!");
        return false;
    }

    for (var obj : Main.sessionObjects) {
        System.out.println("Comparing: " + obj.getEmail() + " with " + targetEmail);
        if (Objects.equals(obj.getEmail(), targetEmail)) {
            System.out.println(targetEmail + " - Email match found!");
            return true;
        }
    }

    System.out.println("Email not found.");
    return false;
}

}
