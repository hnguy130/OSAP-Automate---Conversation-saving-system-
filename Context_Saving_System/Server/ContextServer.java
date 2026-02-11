import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.concurrent.Executors;

public class ContextServer {

    static String DB_URL = "jdbc:postgresql://localhost:5432/osap_contexts";
    static String DB_USER = "khoanguyen";  // fixed: added semicolon
    static String DB_PASS = "";

    public static void main(String[] args) throws Exception {

        // create server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        // server creates thread handlers
        server.setExecutor(Executors.newFixedThreadPool(50));
        // create context pathways for server
        server.createContext("/save", exchange -> saveContext(exchange));
        server.createContext("/retrieve", exchange -> retrieveContext(exchange));
        server.createContext("/health", exchange -> test(exchange));
        // start server
        server.start();
        System.out.println("Server running on http://localhost:8080");
    }

    // save context, return save code 
    public static void saveContext(HttpExchange exchange) throws IOException {
        try {
            // read incoming request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            // extract context from json
            String context = extractJson(body, "context");
            // connect to database
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            // save context into database and get save code id
            PreparedStatement query = conn.prepareStatement(
                "INSERT INTO contexts (context) VALUES (?) RETURNING id"  // fixed: table name
            );
            query.setString(1, context);  // fixed: was sqlCommand, should be query
            ResultSet result = query.executeQuery();
            result.next();
            String save_code = result.getString("id");
            // close connection to database
            conn.close();
            // send back the save code
            sendResponse(exchange, 200, "{\"code\": \"" + save_code + "\", \"status\": \"saved\"}");  // fixed: was id, should be save_code

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // retrieve context using save code
    public static void retrieveContext(HttpExchange exchange) throws IOException {
        try {
            // read incoming request body
            String body = new String(exchange.getRequestBody().readAllBytes());
            // extract save code from json
            String save_code = extractJson(body, "code");
            // connect to database
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            // query database for context using save code
            PreparedStatement query = conn.prepareStatement(
                "SELECT context FROM contexts WHERE id = ?::uuid"
            );
            query.setString(1, save_code);
            ResultSet result = query.executeQuery();
            // check if context was found
            if (result.next()) {
                String context = result.getString("context");
                context = context.replace("\"", "\\\"");  // escape quotes for json
                sendResponse(exchange, 200, "{\"found\": true, \"context\": \"" + context + "\"}");
            } else {
                sendResponse(exchange, 200, "{\"found\": false, \"context\": null}");
            }
            // close connection to database
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // health check test
    public static void test(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 200, "{\"status\": \"ok\"}");
    }

    // send response back to requester
    static void sendResponse(HttpExchange exchange, int code, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, body.length());
        OutputStream os = exchange.getResponseBody();
        os.write(body.getBytes());
        os.close();
    }

    // extract value from json string
    static String extractJson(String json, String key) {
        String search = "\"" + key + "\"";
        int start = json.indexOf(search) + search.length();  // fixed: indexOF to indexOf
        start = json.indexOf("\"", start) + 1;
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}