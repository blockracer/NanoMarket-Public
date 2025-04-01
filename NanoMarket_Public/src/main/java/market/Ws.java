package market; 

import java.util.UUID;
import java.util.Iterator;
import java.net.URISyntaxException;
import java.net.URI;
import java.io.BufferedReader;
import uk.oczadly.karl.jnano.websocket.NanoWebSocketClient;
import java.lang.InterruptedException;
import uk.oczadly.karl.jnano.websocket.topic.TopicConfirmation;
import uk.oczadly.karl.jnano.websocket.topic.TopicUnconfirmedBlocks;
import uk.oczadly.karl.jnano.model.block.Block;
import uk.oczadly.karl.jnano.model.NanoAmount;
import java.math.BigInteger;
import java.math.BigDecimal;
import uk.oczadly.karl.jnano.rpc.exception.RpcException;
import uk.oczadly.karl.jnano.model.NanoAccount;

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
import uk.oczadly.karl.jnano.rpc.request.node.RequestBlockInfo;
import uk.oczadly.karl.jnano.rpc.response.ResponseBlockInfo;



// Custom exception for timeout scenario
 class TimeoutException extends Exception {
	private static final long serialVersionUID = 123456789L; // Use any long value you like
	public TimeoutException(String message) {
        	super(message);
    	}
}
class PaymentCompleteException extends Exception {
	private static final long serialVersionUID = 123456788L; // Use any long value you like
    	public PaymentCompleteException(String message) {
        	super(message);
    	}
}




public class Ws {

    public static void paymentChecker(String account, BigInteger amount, String id, String paymentType, String email, String quantity, String paymentID) throws URISyntaxException, InterruptedException, IOException, FileNotFoundException {

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        System.out.println("INSIDE PAYMENT CHECKER FOR ACCOUNT: " + account);

        System.out.println("PAYMENT ID IN PAYMENT CHECKER: " +  paymentID);

        boolean paymentConfirmed = false;

        URI uri = new URI("ws://127.0.0.1:7894");
        NanoWebSocketClient ws2 = new NanoWebSocketClient(uri);

          ws2.setObserver(new Observer());
            if (!ws2.connect()) {
                System.err.println("Could not connect to WebSocket!");
              }

        System.out.println("position 333");

        // Register a topic listener (in this case, using a lambda function)
        ws2.getTopics().topicConfirmedBlocks().registerListener((message, context) -> {
            String link = "";
            Block block = message.getBlock();
            JsonObject blockJson = block.toJsonObject();
            Gson gson = new Gson();
            String jsonString = gson.toJson(blockJson);
            //System.out.println(jsonString);
            String subtype = blockJson.get("subtype").getAsString();
            int comparisonResult = -1;

            try {
                if (subtype.equals("receive")) {
                    System.out.println("FOUND RECEIVE");
                    String balance = blockJson.get("balance").getAsString();
                    BigInteger balanceBig = new BigInteger(balance);
                    comparisonResult = balanceBig.compareTo(amount);

                    if (comparisonResult >= 0) {
                        System.out.println("id: " + id + " PAID");
                        System.out.println("payment type: " + paymentType);
                        System.out.println("payment id: " + paymentID);
                        Iterator<SessionObject> iterator = Main.sessionObjects.iterator();

                        SessionObject sessionObj = null;
                        if (paymentType.equals("seller")) {
                            while (iterator.hasNext()) {
                                SessionObject itemSession = iterator.next();
                                if(itemSession.getId() != null) {
                                if (itemSession.getId().equals(id)) {
                                    Session session = itemSession.getSession();
                                    sessionObj = itemSession;
                                    try {
                                        String cancelID = Tools.getCancelID(id);
                                        if(session != null) {
                                        if (session.isOpen()) {
                                            session.getRemote().sendString(cancelID);
                                            iterator.remove();
                                            session.close();
                                        }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                }
                            }

                        }

                        link = blockJson.get("link").getAsString();
                        String sentAccount = "";

                        try {
                            sentAccount = getSeller(link);
                        } catch (IOException e) {
                            System.out.println("something failed");
                            e.printStackTrace();
                        } catch (RpcException e) {
                            System.out.println("something failed");
                            e.printStackTrace();
                        }
                        boolean refund = false;
                        if (paymentType.equals("seller")) {
                            System.out.println("SELLER PAID, SETTING PAID TO TRUE");
                            if(sessionObj != null) {
                                System.out.println("SESSION EXISTS TO SET PAID");
                                sessionObj.setPaid(true);
                            }
                            else {
                                System.out.println("SESSION  OBJECT IS NULL FAIL");
                            }
                            boolean paid = Tools.orderPaid(id, sentAccount);
                            executorService.shutdownNow();
                            ws2.close();
                            return;
                        } else if (paymentType.equals("buyer")) {
                            try {
                                System.out.println("almost at iterator");
                                //check if still in stock
                                if(!checkInStock(id, quantity) && !Tools.checkFileExists(id)) {
                                    //return funds: get destination, source and amount
                                    HttpRequests.send(sentAccount, account, amount.toString());
                                    refund = true;
                                }
                                
                                System.out.println("session objects Main size: " +  Main.sessionObjects.size());

                                for (int i = 0; i < Main.sessionObjects.size(); i++) {
                                    SessionObject item = Main.sessionObjects.get(i);
                                     System.out.println("payment id found in list: " + item.getPaymentID());
                                    System.out.println("LOOKING FOR PAYMENT ID: " + paymentID);
                                    System.out.println("item object payment id: " + item.getPaymentID());
                                    System.out.println("PAYMENT CHECKER PAYMENT ID: " + paymentID);
            if(item.getPaymentID() != null) {
            if (item.getPaymentID().equals(paymentID)) {
                System.out.println("PAYMENT ID FOUND");
                Session session = item.getSession();
                try {
                    if (!refund) {
                        System.out.println("SETTING PAID TO TRUE FOR BUYER");
                        item.setPaid(true);
                        System.out.println("AMOUNT TO ADD TO LOCKED FUNDS: " + amount.toString());
                        boolean paid = Tools.buyerPaid(id, sentAccount, email, quantity, paymentID, amount.toString());
                        System.out.println("TRYING TO SEND PAID TO CLIENT");
                        System.out.println("account: " + account);
                        //Tools.sendToItemAddress(account, item.getAddress(), balanceBig );
                        if(session != null) {
                            if(session.isOpen()) {
                                session.getRemote().sendString("PAID");
                            }


                        HttpRequests.send(item.getAddress(), account, amount.toString());
                        System.out.println("Should be sent here to item account from: " + account + "to " + item.getAddress() + " amount: " + balanceBig.toString());
                    }                     // Remove the item from the list
                    //Main.sessionObjects.remove(i);
                    i--; // Adjust index due to removal
                    if(session.isOpen()) {

                        session.close();

                    }
                        executorService.shutdownNow();
                        ws2.close(); // Gracefully close WebSocket
                        return;


                }
                else {
                        System.out.println("OUT OF STOCK");
                        if(session != null)
                            if(session.isOpen()) {
                                session.getRemote().sendString("Out Of Stock, refunded");
                            }
                            executorService.shutdownNow();
                            ws2.close(); // Gracefully close WebSocket
                            return;
                        }
                }
                 catch (IOException e) {
                    e.printStackTrace();
                }
                break; // Assuming you only want to find and process the first matching item
            }
            }
        }

                                System.out.println("creating iterator");
                                Iterator<SessionObject> iterator2 = Main.sessionObjects.iterator();
                                /*
                                while (iterator2.hasNext()) {
                                    SessionObject item = iterator2.next();
                                    System.out.println("payment id found in iterator: " + item.getPaymentID());
                                    System.out.println("LOOKING FOR PAYMENT ID: " + paymentID);
                                    if (item.getPaymentID().equals(paymentID)) {
                                        System.out.println("PAYMENT ID FOUND");
                                        Session session = item.getSession();
                                            try {
                                                if(!refund) { 
                                                    System.out.println("TRYING TO SEND PAID TO CLIENT");
                                                    session.getRemote().sendString("PAID");
                                                    boolean paid = Tools.buyerPaid(id, sentAccount, email, quantity, paymentID, amount.toString());
                                                }
                                                else {
                                                    System.out.println("OUT OF STOCK");
                                                    session.getRemote().sendString("Out Of Stock, refunded");
                                                }
                                                iterator2.remove();
                                                session.close();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                    }
                                }
                                */
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        Main.paidList.add(id);

                        try {
                           if(comparisonResult >= 0) {
                                System.out.println("payment complete");
                                ws2.close(); // Gracefully close WebSocket
                                return;
                                //throw new PaymentCompleteException("Payment Complete");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (WebSocketException e) {
                e.printStackTrace();
            }
        });

        System.out.println("position 555");

        // Subscribe to the confirmed blocks topic, and specify filters and configuration
        boolean subscribed = ws2.getTopics().topicConfirmedBlocks().subscribeBlocking(
                new TopicConfirmation.SubArgs()
                        .includeElectionInfo() // Include election info in the messages
                        .filterAccounts(account)
        );
        System.out.println("should be filtered by: " + account);


        executorService.schedule(()  -> {
            System.out.println("IN EXECUTOR");
                BufferedReader fileReader = null;
                //File file = new File(id + ".json");
               if(paymentType.equals("seller")) {
                    File file = new File(Main.itemsPath + id + ".json");
                    try {
                      fileReader = new BufferedReader(new FileReader(file));
                    }
                    catch(FileNotFoundException e) {
                        System.out.println("File not found in timeout close");
                    }
                    // Parse the JSON string to JsonArray
                    JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
                    //String title = jsonObject.get("title").getAsString();
                    if(!jsonObject.has("title")) {
                        if(file.delete()) {
                            System.out.println("File deleted");
                        }
                        else {
                            System.out.println("No file to delete");
                        }
                    }
               }
                

                Iterator<SessionObject> iterator = Main.sessionObjects.iterator();

                while (iterator.hasNext()) {
                    SessionObject item = iterator.next();
                    if (item.getId().equals(id)) {
                        Session session = item.getSession();
                        Main.sessionObjects.remove(item);

                        if (session.isOpen()) {
                            try {
                                session.getRemote().sendString("TIME OUT");
                                session.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                ws2.close();
                System.out.println("time out");
                executorService.shutdownNow();
                //throw new RuntimeException("Time out");
        }, 10, TimeUnit.MINUTES);
    }

    public static String getSeller(String blockHash) throws IOException, RpcException {
        RequestBlockInfo blockInfo = new RequestBlockInfo(blockHash);
        ResponseBlockInfo response = Main.rpc.processRequest(blockInfo);
        NanoAccount account = response.getAccount();
        System.out.println(account.toString());
        return account.toString();
    }
    public static boolean checkInStock(String id, String quantity) throws FileNotFoundException, IOException {
        String jsonFile = Main.itemsPath + id+ ".json"; // Replace with your file path
        //FileReader fileReader = new FileReader(jsonFile);
        BufferedReader fileReader = new BufferedReader(new FileReader(jsonFile));

        // Parse the JSON string to JsonArray
        JsonObject jsonObject = JsonParser.parseReader(fileReader).getAsJsonObject();
        Gson gson = new Gson();
        Item item = gson.fromJson(jsonObject, Item.class);

        //check quantity is valid
        String itemQuantity = item.getQuantity();
        //convert both to int
        int itemQuantityInt = Integer.parseInt(itemQuantity);
        int buyerQuantityInt = Integer.parseInt(quantity);
        fileReader.close();
        //check if enough stock for buyer
        if(itemQuantityInt >= buyerQuantityInt && itemQuantityInt > 0) {
            return true;
        }
        return false;
    }
}

