package cis5550.generic;

import cis5550.tools.HTTP;
import java.io.IOException;

public class Worker {
   public static void startPingThread(final String var0, final String var1, final int var2) {
      (new Thread("Ping thread") {
         public void run() {
            while(true) {
               try {
                  HTTP.Response var1x = HTTP.doRequest("GET", "http://" + var0 + "/ping?id=" + var1 + "&port=" + var2, (byte[])null);
               } catch (IOException var3) {
                  System.err.println("Unable to ping coordinator at " + var0);
               }

               try {
                  sleep(10000L);
               } catch (InterruptedException var2x) {
               }
            }
         }
      }).start();
   }
}
