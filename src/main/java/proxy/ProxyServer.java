package proxy;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import sun.misc.IOUtils;


import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProxyServer {

    private static final BlackList blackList = new BlackList("blacklist.txt");
    private static final StatsCounter statsCounter = new StatsCounter("domainStats.csv");


    public void start() throws IOException{

        int port = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new ProxyHandler());
        
        statsCounter.registerDomain("localhost");
        statsCounter.registerDomain("www.gazeta.pl");
        statsCounter.registerDomain("www.fujitsu.com");
        server.start();
    }

    static class ProxyHandler implements HttpHandler{


        @Override
        public void handle(HttpExchange httpExchange) throws IOException{

            URL url = httpExchange.getRequestURI().toURL();

            statsCounter.increaseQueriesCount(url);
            blackList.refresh();

            if(!blackList.isOnList(url)){

                HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setInstanceFollowRedirects(false);
                urlConnection.setReadTimeout(100000);
                urlConnection.setConnectTimeout(100000);


                String method = httpExchange.getRequestMethod();
                urlConnection.setRequestMethod(method);
                setUrlRequestHeaders(httpExchange, urlConnection);

                urlConnection.setDoInput(true);

                if(method.equals("POST") || method.equals("PUT")){

                    urlConnection.setDoOutput(true);
                    int send = writeUrlRequestBody(httpExchange, urlConnection);
                    statsCounter.increaseSendBytes(url, send);

                }

                urlConnection.connect();
                System.out.println("connection to " + url.toString());


                int contentLength = setHttpExchangeResponseHeaders(httpExchange, urlConnection);
                int status = urlConnection.getResponseCode();
                System.out.println("status " + status);

                if(status > 400){
                    sendErrorResponse(httpExchange, urlConnection, status, contentLength);

                }else if(status > 300 || status == 204){
                    httpExchange.sendResponseHeaders(status, -1);

                }else{
                    httpExchange.sendResponseHeaders(status, contentLength);
                    int received = writeHttpExchangeResponseBody(httpExchange, urlConnection);
                    statsCounter.increaseReceivedBytes(url, received);
                }

                statsCounter.save(url);
                urlConnection.disconnect();
                System.out.println();

            }else{

                String response = "address is blocked";
                httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                sendMessage(httpExchange, response, 403);
            }


        }

        public void sendErrorResponse(HttpExchange httpExchange, HttpURLConnection urlConnection, int status, int contentLength) throws IOException{
            InputStream errorStream = urlConnection.getErrorStream();
            if(errorStream != null){
                byte[] data = IOUtils.readFully(errorStream, -1, true);
                httpExchange.sendResponseHeaders(status, contentLength);
                OutputStream os = httpExchange.getResponseBody();
                os.write(data);
                os.close();
            }else{
                String response = "response code: " + status;
                sendMessage(httpExchange, response, status);
            }

        }

        public void sendMessage(HttpExchange httpExchange, String message, int status)throws IOException{
            httpExchange.sendResponseHeaders(status, message.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(message.getBytes());
            os.close();
        }

        public int writeUrlRequestBody(HttpExchange httpExchange, HttpURLConnection urlConnection){

            InputStream inputStream = httpExchange.getRequestBody();
            int send = 0;

            try(OutputStream outputStream = urlConnection.getOutputStream()){
                byte[] data = IOUtils.readFully(inputStream, -1, true);
                outputStream.write(data);
                send = data.length;

            }catch(IOException e){
                e.printStackTrace();
            }

            return send;

        }

        public int writeHttpExchangeResponseBody(HttpExchange httpExchange, HttpURLConnection urlConnection){

            OutputStream os = httpExchange.getResponseBody();
            int received = 0;

            try(InputStream inputStream = urlConnection.getInputStream()){
                byte[] data = IOUtils.readFully(inputStream, -1, true);
                os.write(data);
                received = data.length;
                os.close();

            }catch(IOException e) {
                System.err.println(e.getMessage());
            }

            return received;
        }

        public void setUrlRequestHeaders(HttpExchange httpExchange, HttpURLConnection urlConnection){

            Headers reqHeaders = httpExchange.getRequestHeaders();

            for(Map.Entry<String, List<String>> entry: reqHeaders.entrySet()){
                for(String value: entry.getValue()){
                    urlConnection.setRequestProperty(entry.getKey(), value);
                    System.out.println("request " + entry.getKey() + ": " + value);
                }

            }

        }

        public int setHttpExchangeResponseHeaders(HttpExchange httpExchange, HttpURLConnection urlConnection){

            Headers respHeaders = httpExchange.getResponseHeaders();
            int contentLength = 0;

            for(Map.Entry<String, List<String>> entry: urlConnection.getHeaderFields().entrySet()){

                if(entry.getKey() != null){

                    if(!entry.getKey().equals("Content-Length")){
                        for(String value: entry.getValue()){
                            respHeaders.set(entry.getKey(), value);
                            System.out.println("response " + entry.getKey() + ": " + value);
                        }
                    }else{
                        contentLength = Integer.valueOf(entry.getValue().get(0));
                    }
                }
            }

            return contentLength;

        }

        public byte[] readInputStream(InputStream inputStream)throws IOException{

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int numReaded;
            while((numReaded = inputStream.read(data)) != -1){
                buffer.write(data, 0, numReaded);
            }
            return buffer.toByteArray();
        }




    }


}
