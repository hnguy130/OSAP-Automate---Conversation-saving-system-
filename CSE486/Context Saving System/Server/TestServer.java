 import com.sun.net.httpserver.HttpServer;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.util.concurrent.Executors;

 public class TestServer {
    public static void main(String[] args) throws Exception {
        
        // create server, not activated yet
        HttpServer server = HttpServer.create(new InetSocketAddress(8080),0);
        // server creates threads to handle concurrent requests
        server.setExecutor(Executors.newFixedThreadPool(50));

        // create pathway(s) for server
        server.createContext("/hello", exchange -> {

            String response = "hello";
            exchange.sendResponseHeaders(200, response.length());

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        });

        //start server
        server.start();
        System.out.println("Server running on http://localhost:8080");
        System.out.println("Try: http://localhost:8080/hello");
    }
 }
