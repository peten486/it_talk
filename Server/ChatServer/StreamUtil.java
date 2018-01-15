package ChatServer;

import java.io.*; 

public class StreamUtil {
     public static void close(Closeable out) {
         if (out != null) {
             try { 
                out.close(); 
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
        } 
    } 
}