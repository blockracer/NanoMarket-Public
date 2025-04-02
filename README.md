Requirements:

Gradle 7.2
java 11+

# NanoMarket-Public
Nano Market


## Configuration Instructions

### Updating `Main.java`
To correctly set up file paths, edit `Main.java` and replace the following:

1. **Set the directory for `item.json` files:**
   ```java
   public static final String itemsPath = "/path/to/item/json/files/";
   ```
   Replace `"/path/to/item/json/files/"` with the actual directory where `item.json` files are stored.

2. **Set the directory for web pages:**
   ```java
   public static final String marketPages = "/path/to/web/pages/";
   ```
   Replace `"/path/to/web/pages/"` with the correct directory where the web pages are stored.

3. **Update the static file location:**
   ```java
   staticFiles.externalLocation(marketPages);
   ```
   Ensure this matches the same directory set for `marketPages`.

4. **Set the server port:**
   ```java
   port(9999);
   ```
   Change `9999` to your desired port number.

5. **Configure the RPC node:**
   ```java
   rpc = new RpcQueryNode("[::1]", 7076);
   ```
   Replace `"[::1]"` and `7076` with your own RPC node configuration.

### Updating `Ws.java`
To configure WebSockets, update the following:

1. **Set the WebSocket URI:**
   ```java
   URI uri = new URI("ws://127.0.0.1:7894");
   ```
   Change `"ws://127.0.0.1:7894"` to your own WebSocket server address.

Make sure these settings are correctly configured before running the application.
You also need to edit the HttpRequests.java with your own configuration for your email credentials and the http request for sending NANO (it should be the send method but needs to be configured/modified to fit your own route or method of sending the nano)
