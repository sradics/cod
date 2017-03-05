package net.ontheagilepath.util;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Date;

/**
 * Created by sebastianradics on 04.03.17.
 */
public class SimpleWebServer {
    public static void main(String... args){
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress(8000),0);

            HttpContext context = server.createContext("/java");
            context.setHandler((he) -> {
                InputStream is = SimpleWebServer.class.getResourceAsStream("/test.html");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] data = new byte[10000];
                while(true){
                    int bytesRead = is.read(data);
                    if (bytesRead==-1)
                        break;
                    bos.write(data,0,bytesRead);
                }
                data = bos.toByteArray();
                he.sendResponseHeaders(200, data.length);
                final OutputStream output = he.getResponseBody();
                output.write(data);
                output.flush();
                he.close();
                System.out.println("call "+new Date().toString());
            });
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
