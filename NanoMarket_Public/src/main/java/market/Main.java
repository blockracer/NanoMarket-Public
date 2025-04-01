package market;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.nio.file.Paths;
import java.nio.file.Files;
import org.apache.velocity.app.VelocityEngine;
import java.io.BufferedReader;
import static spark.Spark.*;
import spark.staticfiles.*;
import java.io.FileReader;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.Random;
import java.io.FileWriter;

import uk.oczadly.karl.jnano.rpc.RpcQueryNode;
import uk.oczadly.karl.jnano.rpc.request.wallet.RequestAccountCreate;
import uk.oczadly.karl.jnano.rpc.response.ResponseAccount;
import uk.oczadly.karl.jnano.rpc.exception.RpcException;
import uk.oczadly.karl.jnano.model.NanoAccount;

import java.io.IOException;
import java.time.Instant;

//gson
import com.google.gson.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import org.apache.velocity.runtime.RuntimeConstants;


import java.net.URISyntaxException;
import java.net.URI;
import uk.oczadly.karl.jnano.websocket.NanoWebSocketClient;
import java.lang.InterruptedException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;

public class Main {

	public static RpcQueryNode rpc;
	public static URI uri;
    	public static NanoWebSocketClient ws;
	public static JsonArray orderArray = new JsonArray();
	public static List<SessionObject> sessionObjects = new ArrayList<>();

	public static HashMap<String, Message> responseActive = new HashMap<>();
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2); // Adjust the pool size as needed
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private static final ScheduledExecutorService  schedularAlive = Executors.newScheduledThreadPool(2);

	public static List<String> paidList = new ArrayList<>();

    public static Map<String, Object> homeListings = Tools.getListings();

    public static Map<String, Object> allListings = Tools.getAllListings();
    public static final String itemsPath = "/home/admin/items";

    public static final String marketPages = "/home/admin/pages";

	public static void main(String[] args) throws URISyntaxException, InterruptedException, IOException {
        /*
        VelocityEngine ve = new VelocityEngine();

        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER_CACHE, "false"); // Enable caching
        ve.setProperty("classpath.resource.loader.modification_check_interval", "1");

        ve.init();
                                                                                            
                                                                                            
        VelocityTemplateEngine velocityTemplateEngine = new VelocityTemplateEngine(ve);

        */

        Runnable task = () -> {
            System.out.println("Executing task at: " + java.time.LocalTime.now());
            try {
               Tools.lockSend();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        };
        Runnable alive = () -> {

            for (SessionObject obj : Main.sessionObjects) {
            Session session = obj.getSession();

        if (session == null || !session.isOpen()) {
            continue;
        }
        System.out.println("FOUND SESSION:");

        try {
                if(obj.isPaid() == true) {
                    System.out.println("SENDING PAID TO SESSION FROM RUNNABLE");
                    session.getRemote().sendString("PAID");
                }
                else {
                    System.out.println(obj.isPaid());
                    session.getRemote().sendString("PAID: " + String.valueOf(obj.isPaid()));
                }
        } catch (IOException e) {
            System.out.println("IOException while sending keep-alive: " + e.getMessage());
        }
            }
        };


         Runnable sessionClose = () -> {

                  long timestamp = Instant.now().getEpochSecond();

                  Iterator<SessionObject> iterator = Main.sessionObjects.iterator();
                  while (iterator.hasNext()) {
                      SessionObject item = iterator.next();
                      if (item.getClose() < timestamp) {
                          Main.sessionObjects.remove(item);

                          }
                      }
          };



        
        scheduler.scheduleAtFixedRate(sessionClose, 0, 10, TimeUnit.MINUTES);
        
        scheduler.scheduleAtFixedRate(task, 0, 30, TimeUnit.MINUTES);

        scheduler.scheduleAtFixedRate(alive, 0, 30, TimeUnit.SECONDS);


        port(9999);

          webSocket("/checkpayment", WebSocketHandler.class);

          webSocket("/checkpurchase", WebSocketPurchaseHandler.class);




          rpc = new RpcQueryNode("[::1]", 7076);
          staticFiles.externalLocation("/home/server-admin/javaProjects/marketPagesTest2/");


           uri = new URI("ws://127.0.0.1:7894");
              ws = new NanoWebSocketClient(uri);
              ws.setObserver(new Observer());
              if (!ws.connect()) {
                      System.err.println("Could not connect to WebSocket!");
              }
     
          get("/test", (req, res) -> {
              return "test";
          });


		 /*
        get("/search/:tag", (req, res) -> {

            Map<String, Object> model  =  new HashMap<>();
            String tag = req.params("tag").toLowerCase();

            Map<String, Object> dataMap  = Tools.getListings(tag);
            model.put("data", dataMap); 

            return new ModelAndView(model, "search.vm"); // Velocity template file


        }, new VelocityTemplateEngine());
        */
        get("/search/:tag", (req, res) -> {

            Map<String, Object> model  =  new HashMap<>();
            String tag = req.params("tag").toLowerCase();

            Map<String, Object> dataMap  = Tools.getListings(tag);
            model.put("data", dataMap.get("itemsList")); 

            return new ModelAndView(model, "home.vm"); // Velocity template file


        }, new VelocityTemplateEngine());

        get("/search/:country/:tag", (req, res) -> {

            Map<String, Object> model  =  new HashMap<>();
            try {
            String tag = req.params("tag").toLowerCase();

            String country = req.params("country").toLowerCase();

            System.out.println(country);

            Map<String, Object> dataMap  = Tools.getListingsByCountry(country, tag);

            model.put("data", dataMap.get("itemsList")); 
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return new ModelAndView(model, "home.vm"); // Velocity template file


        }, new VelocityTemplateEngine());




	
        get("/buy", (req, res) -> {
            String htmlContent = new String(Files.readAllBytes(Paths.get(marketPages + "buy.html")));
            res.type("text/html");
            System.out.println("attempting to return content");
            return htmlContent;
        }); 
        get("/", (req, res) -> {

            Map<String, Object> model  =  new HashMap<>();
            //Map<String, Object> dataMap  = Tools.getListings();
            ArrayList<Item> totalItems = (ArrayList<Item>) allListings.get("items");
            ArrayList<Item> items = getSublist(totalItems, 1);
            if(totalItems.size() > 30) {
                model.put("next", "true");
            } 
            model.put("back", "false");
            model.put("data", items); 

            return new ModelAndView(model, "home.vm"); // Velocity template file

        }, new VelocityTemplateEngine());

        get("/page/:page", (req, res) -> {
            String page = req.params("page");
    Map<String, Object> model = new HashMap<>();

    int pageInt = Integer.valueOf(page); // Auto-unboxes to int

    // Example: Assuming 'allListings' contains the full list of items
    ArrayList<Item> totalItems = (ArrayList<Item>) allListings.get("items");

    // Determine the starting index of the items based on the current page (1-based)
    int startIndex = (pageInt - 1) * 30;
    int endIndex = Math.min(startIndex + 30, totalItems.size());  // Avoid going out of bounds

    // Get the sublist of items for the current page
    ArrayList<Item> items = new ArrayList<>(totalItems.subList(startIndex, endIndex));

    // Determine if there should be a "back" button
    model.put("back", pageInt > 1 ? "true" : "false");

    // Determine if there should be a "next" button
    model.put("next", endIndex < totalItems.size() ? "true" : "false");

    // Add the items to the model for the view
    model.put("data", items);
        return new ModelAndView(model, "home.vm"); // Velocity template file

        }, new VelocityTemplateEngine());



         get("/listing/:id", (request, response) -> {
             Map<String, Object> model = null;

             try {
            String id = request.params(":id");
            System.out.println(id);
            String jsonFilePath = itemsPath + id + ".json"; // Path to your JSON file

            // Read JSON data
            BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath));
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            reader.close();

            Gson gson = new Gson();
            Map<String, Object> dataMap = gson.fromJson(jsonObject, Map.class);

            // Prepare the model for the template
            model = new HashMap<>();
            model.put("data", dataMap);

            System.out.println("model put");

            // Return a ModelAndView using VelocityTemplateEngine
            //
             }
             catch(Exception e) {
                e.printStackTrace();
            }

            return new ModelAndView(model, "listing.vm"); // Velocity template file

        }, new VelocityTemplateEngine());


        get("/approve/:itemid/:invoiceid", (req, res) -> {
            res.type("text/html");
            System.out.println("GOT IN APPROVE");

            String invoiceID = req.params("invoiceid");
            String id = req.params("itemid");
            String approve = Tools.approve(id, invoiceID);

            System.out.println("APPROVE: " + approve);

            if(approve.equals("true")){
                String htmlContent = new String(Files.readAllBytes(Paths.get(marketPages+"approve.html")));
                return htmlContent;
            }
            if(approve.equals("lock")){
                 String htmlContent = new String(Files.readAllBytes(Paths.get(marketPages + "lock.html")));
                return htmlContent;
            }
            if(approve.equals("alreadyTrue")){
                String htmlContent = new String(Files.readAllBytes(Paths.get(marketPages + "alreadyUnlocked.html")));
                return htmlContent;

            }
            else {
                  String htmlContent = new String(Files.readAllBytes(Paths.get(marketPages + "error.html")));
                return htmlContent;
            }

        });
         get("/cancel/:itemID/:cancelID", (req, res) -> {
            res.type("text/html");
            String cancelID = req.params("cancelID");
            String id = req.params("itemID");
            boolean isRemoved = Tools.removeListing(id, cancelID);

            if(isRemoved) {
            String htmlContent = new String(Files.readAllBytes(Paths.get(marketPages + "cancel.html")));
            return htmlContent;

            }
            String htmlContent = new String(Files.readAllBytes(Paths.get(marketPages + "cancelfail.html")));
            return htmlContent;





            //return isRemoved;

        });

		 post("/buypayment", (req, res) -> {
             System.out.println("Buy request");

            res.type("application/json");
			String body = req.body();
			JsonObject Orderjson = JsonParser.parseString(body).getAsJsonObject();

            String quanStr = Orderjson.get("quantity").getAsString();

			BigDecimal quantity = new BigDecimal(Orderjson.get("quantity").getAsString());
			String email = Orderjson.get("email").getAsString();
			String id = Orderjson.get("id").getAsString();

            //System.out.println("ID: " + d);

            String jsonFilePath = itemsPath + id + ".json"; // Path to your JSON file

            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                //get raw cost
                BigDecimal itemCost =  new BigDecimal(jsonObject.get("cost").getAsString());
                BigDecimal decCost = itemCost.multiply(quantity);
                decCost = decCost.multiply(BigDecimal.valueOf(2));
                String decCostStr = decCost.toPlainString();
                decCost = decCost.movePointRight(30);
				String rawValue = decCost.toPlainString();
				BigInteger rawCost = decCost.toBigInteger();

                //get item address
                String address = jsonObject.get("address").getAsString();
                System.out.println("ITEM ADDRESS: " + address);

                UUID uuid = UUID.randomUUID();
                String paymentID = uuid.toString();
                
			    long timestamp = Instant.now().getEpochSecond() + 600;
                
                String uniqueAccount = Main.getListingAddress();
               // session.getRemote().sendString(paymentID + "," + uniqueAccount);
                SessionObject session = new SessionObject();
                session.setPaymentID(paymentID);
                session.setClose(timestamp);
                session.setPaymentAddress(address);
                session.setPaid(false);
                sessionObjects.add(session);
                Ws.paymentChecker(uniqueAccount, rawCost, id, "buyer", email, quanStr, paymentID);


                JsonObject buyerInfo = new JsonObject();
                buyerInfo.addProperty("paymentID", paymentID);
                buyerInfo.addProperty("cost", decCostStr);
                buyerInfo.addProperty("paymentAddress", uniqueAccount);

                return buyerInfo; 
                //Main.sessionObjects.add(new SessionObject(session, obj.getId(), itemAddress, false, paymentID, uniqueAccount, obj.getEmail()));
            }
            catch(Exception e) {
                e.printStackTrace();
            }


            return "fail";

         });


		 post("/sellpayment", (req, res) -> {
            System.out.println("FOUND A SELL PAYMENT REQUEST");
            res.type("application/json");
			String body = req.body();
			JsonObject json = JsonParser.parseString(body).getAsJsonObject();

			String quantity = json.get("quantity").getAsString();
			String itemCost = json.get("itemcost").getAsString();
			String email = json.get("email").getAsString();

            System.out.println(email);


            System.out.println(quantity);
            System.out.println(itemCost);



            UUID uuid = UUID.randomUUID();
            String cancelID = uuid.toString();


			BigDecimal dcost = new BigDecimal("0.0");
			try {
        		dcost =  new BigDecimal(itemCost);
				System.out.println("dcost: " + dcost);
				if(isValidPrice(itemCost) ) {
					//check valid quantity
					try {
						int iquan = Integer.parseInt(quantity);
						BigDecimal bigDecimalQuan = new BigDecimal(quantity);
						BigDecimal totalDeposit = dcost.multiply(bigDecimalQuan);						
						System.out.println("total deposit: " + totalDeposit);
						totalDeposit = totalDeposit.movePointRight(30);
						String rawValue = totalDeposit.toPlainString();
						System.out.println("raw:" + rawValue);
						BigInteger bi = totalDeposit.toBigInteger();
						System.out.println("bigint: " + bi);

						//return payment Address, open a websockets connection to check for the payment. Give it a 10min time limit before closing the payment checking.
						String paymentAddress = getListingAddress();
						String id = getId();
                        System.out.println("payment id: " + id);
						//open a ws and close it in 10 min if payment has not been made.
						Ws.paymentChecker(paymentAddress, bi, id, "seller", "", "", "");
						//need to return unique id and payment address.
						JsonObject obj = new JsonObject();

						obj.addProperty("totalLocked", rawValue);
						obj.addProperty("id", id);
						obj.addProperty("address", paymentAddress);
						obj.addProperty("cost", itemCost);
						obj.addProperty("quantity", quantity);
						obj.addProperty("paid", "false");
						obj.addProperty("email", email);
						obj.addProperty("cancelID", cancelID);
						
						orderArray.add(obj);
						//Write to file
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
						try (FileWriter fileWriter = new FileWriter(itemsPath + id + ".json")) {
            						gson.toJson(obj, fileWriter);
            						System.out.println("JSON data written to json new file");
        					} catch (IOException e) {
            						e.printStackTrace();
        					}

						//Make sure on the client side to create a websocket connection to the spark server to check for payment.. 
						//
						//
						//
                        //
                        //
                        long timestamp = Instant.now().getEpochSecond() + 600;
                        SessionObject session = new SessionObject();
                        session.setClose(timestamp);
                        session.setID(id);
                        session.setPaid(false);
                        session.setPaymentAddress(paymentAddress);
                        Main.sessionObjects.add(session);
                        //session.setID
						JsonObject  obj1 = new JsonObject(); obj1.addProperty("ID", id);
						obj1.addProperty("Address", paymentAddress);
						return obj;
					}
					catch(Exception e){
						e.printStackTrace();
						return "Error: " + e.getMessage();
					}


				}
    			} 
			catch (NumberFormatException e) {
        			return "item cost must be within 0.1 increments"; // The string is not a valid double
   			}

		return "temp";	

		 });

         get("/modify/:id", (request, response) -> {
            Gson gson = new Gson();
            String id = request.params(":id");
            String jsonFilePath = itemsPath + id + ".json"; // Path to your JSON file

            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFilePath))) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                // Extract required fields
                Map<String, Object> result = new HashMap<>();
                result.put("description", jsonObject.has("description") ? jsonObject.get("description").getAsString() : "");
                result.put("tag1", jsonObject.has("tag1") ? jsonObject.get("tag1").getAsString() : "");
                result.put("tag2", jsonObject.has("tag2") ? jsonObject.get("tag2").getAsString() : "");
                result.put("tag3", jsonObject.has("tag3") ? jsonObject.get("tag3").getAsString() : "");
                result.put("email", jsonObject.has("email") ? jsonObject.get("email").getAsString() : "");
                result.put("title", jsonObject.has("title") ? jsonObject.get("title").getAsString() : "");
                result.put("country", jsonObject.has("country") ? jsonObject.get("country").getAsString() : "");

                response.type("application/json");
                return gson.toJson(result);
            } catch (Exception e) {
                response.status(500);
                return gson.toJson(Collections.singletonMap("error", "Failed to read JSON file"));
            }
        });



		 post("/submitsell/:id/:cancelid/:modify", (req, res) -> {
             System.out.println("GOT IN submitsell");

            res.type("application/json");

			String id = req.params(":id"); 

			String modify = req.params(":modify"); 

			String cancelID = req.params(":cancelid"); 



			JsonObject jsonObject = new JsonObject();
			//false object in in case of submition restriction
			JsonObject falseObj = new JsonObject();
			JsonObject trueObj = new JsonObject();
			trueObj.addProperty("isValid", true);

			
			try {
				//get json object in file
                String filePath = itemsPath + id + ".json";
                BufferedReader fileReader = new BufferedReader(new FileReader(filePath));
				//FileReader fileReader = new FileReader("items/" + id + ".json");

                        	// Parse the JSON string to JsonArray
                jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
				System.out.println("json object made from file");

                        	// Close the file reader
                        	fileReader.close();
			}
			catch(Exception e)  {
				e.printStackTrace();
				return falseObj;
			}
			//check paid
			JsonElement ele = jsonObject.get("paid");
			String checkPaid = ele.getAsString();
			System.out.println(checkPaid);
			if(checkPaid.equals("false")) {
				System.out.println("NOT PAID");
				return falseObj;
			}
            ele = jsonObject.get("cancelID");
			String cancelIDFromJsonFile = ele.getAsString();
            System.out.println(cancelID);
            System.out.println(cancelIDFromJsonFile);

            boolean first =  jsonObject.has("title");

            if(jsonObject.has("cancel") && jsonObject.get("cancel").equals("true")) {

                return falseObj;
            }
            if(jsonObject.get("quantity").getAsString().equals("0")) {
                return falseObj;
            }

            
            if(!cancelID.equals(cancelIDFromJsonFile)) {
                return falseObj;
            }


			String body = req.body();
			JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            System.out.println("read json from client");
            String title2 = json.get("title").getAsString();
            System.out.println(title2);
            try {
			    if(Tools.checkLength(json) == false) {
                    System.out.println("json to big");
				    return falseObj;
			    }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            
            String s1 = "";
            String s2 = "";
            String s3 = "";
            String description = "";
            String country = "";

            try {
           
			for (java.util.Map.Entry<String, JsonElement> entry : json.entrySet()) {
                System.out.println(entry.getKey());
            	String key = entry.getKey();
				JsonElement value = entry.getValue();
				//System.out.println("Key: " + key + ", Value: " + value);
				//System.out.println(key);

                if(key.equals("description")) {
                    description = value.getAsString();
                }
                if(key.equals("tag1")) {
                    s1 = value.getAsString();
                }
                if(key.equals("tag2")) {
                    s2 = value.getAsString();
                }
                if(key.equals("tag3")) {
                    s3 = value.getAsString();
                }
                if(key.equals("country")) {
                    country = value.getAsString();
                
                }
                System.out.println("COUNTRY: " + country);


				if (key.equals("title") || key.equals("tag1") || key.equals("tag2") || key.equals("tag3") || key.equals("image") || key.equals("email") || key.equals("description") || key.equals("country")) {
					continue;
				}
				else {
					return falseObj;
				}
            }
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            json.addProperty("tag1", json.get("tag1").getAsString().toLowerCase());
            json.addProperty("tag2", json.get("tag2").getAsString().toLowerCase());
            json.addProperty("tag3", json.get("tag3").getAsString().toLowerCase());

           /* 
            if (s1.equals(s2) && s1.length() > 0){
                System.out.println(s1);
                System.out.println(s2);
                System.out.println("fail at 1");
                return falseObj;  // At least one pair of strings is not unique
            }
            if (s1.equals(s3) && !s1.isEmpty()){
                System.out.println("fail at 2");

                return falseObj;

            }
            if (s2.equals(s3) && !s2.isEmpty()){
                System.out.println("fail at 3");
                return falseObj;

            }

            */
            System.out.println(s1);
            System.out.println(s2);
            System.out.println(s3);


            if ((!s1.isEmpty() && !s2.isEmpty() && s1.equals(s2)) ||
                (!s1.isEmpty() && !s3.isEmpty() && s1.equals(s3)) ||
                (!s2.isEmpty() && !s3.isEmpty() && s2.equals(s3))) {
                return falseObj;  // Only return false if non-empty values are not unique
            }

            //check country is valid
            boolean isValidCountry = isValidCountry(country);
            System.out.println("is valid country? " + isValidCountry);

            if(isValidCountry == false) {
                return falseObj;
            }

                                   

            System.out.println("tag pass");




			long timestamp = Instant.now().getEpochSecond();
            String timeStr = String.valueOf(timestamp);


            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
            String date = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));



			json.addProperty("active", "true");
			json.addProperty("timestamp", timeStr);
			json.addProperty("date", date);

            //String base64Description = Tools.stringToBase64(description); 
			//json.addProperty("description", base64Description);

			//save object to file
            System.out.println("merging");
			JsonObject mergedObj = Tools.mergeJsonObjects(jsonObject, json);
            System.out.println("MERGED COUNTRY: " + mergedObj.get("country").getAsString());

            // Return response immediately
            executorService.submit(() -> {
            try {
                if(modify.equals("false") && first == false)
                    HttpRequests.sendNewListing(mergedObj);
             } catch (Exception e) {
                e.printStackTrace();
                }
            }); //HttpRequests.sendNewListing(mergedObj); System.out.println("merged");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(mergedObj);
            
			//write to file
			try(PrintWriter writer = new PrintWriter(new File(itemsPath + id + ".json"))) {
                        	writer.write(prettyJson);
                	} catch (FileNotFoundException e) {
                        	e.printStackTrace();
                	}

            //System.out.println("response: " + prettyJson);
                    allListings = Tools.getAllListings();
                    homeListings = Tools.getListings();
                	return trueObj;
        	});
	}

	public static String getListingAddress() throws IOException, RpcException {
		RequestAccountCreate create = new RequestAccountCreate("712B6BD69582E38E2414B6C5D81B9ADA4A85B824A4E10E666E2F8BE35B3BE999");
		ResponseAccount res = rpc.processRequest(create);
		String account = res.getAccountAddress().toAddress();
		return account;
			
	}


	public static String createId(int length) {
        	// Characters to choose from
        	String characters = "0123456789abcdefghijklmnopqrstuvwxyz";
        	StringBuilder sb = new StringBuilder(length);
        	Random random = new Random();

        	// Build the random string
        	for (int i = 0; i < length; i++) {
            		// Generate a random index within the characters string length
            		int index = random.nextInt(characters.length());
            		// Append the character at the generated index
            		sb.append(characters.charAt(index));
        	}

        		return sb.toString();
   	}

	public static boolean checkValid(JsonObject obj) {
		String rawJson = new Gson().toJson(obj);
		if(obj.size() < 3500) {
			String price = obj.get("price").getAsString();
			double priceFormat = Double.parseDouble(price);	
			if(priceFormat >= 0.1) {
				return true;
			}	
			else {
				return false;
			}
			
		}	
		else {
			return false;
		}
		
	}

	public static boolean isValidPrice(String valuestr) {
		double value = Double.parseDouble(valuestr);
    		double threshold = 0.01; // Adjust as needed for precision
    		double remainder = Math.abs(value % 0.01);
    		return remainder < threshold || Math.abs(remainder - 0.01) < threshold;
	}

    public static String getId() {
        String uniqueString = UUID.randomUUID().toString();
	return uniqueString;
    }

     public static ArrayList<Item> getSublist(ArrayList<Item> list, int rangeStart) {
        int startIndex = (rangeStart - 1) * 30;
        int endIndex = startIndex + 30;

        // If startIndex is out of bounds, return an empty list
        if (startIndex >= list.size()) {
            return new ArrayList<>();
        }

        // Adjust endIndex to avoid IndexOutOfBoundsException
        endIndex = Math.min(endIndex, list.size());

        // Return the sublist
        return new ArrayList<>(list.subList(startIndex, endIndex));
    }

    /*
    public static boolean isValidCountry(String countryString) {
        // Loop through all available locales
        for (Locale locale : Locale.getAvailableLocales()) {
            // Compare the full country name (e.g., "United States")
            if (locale.getDisplayCountry().equalsIgnoreCase(countryString) || countryString.equals("Digital")) {
                return true; // Found a match
            }
        }
        return false; // No match found
    }
    */
    public static boolean isValidCountry(String countryString) {
    for (String countryCode : Locale.getISOCountries()) {
        Locale locale = new Locale("", countryCode);
        if (locale.getDisplayCountry().equalsIgnoreCase(countryString) || countryString.equals("Digital")) {
            return true;
        }
    }
    return false;
}


}



