package cis5550.generic;

import cis5550.webserver.Server;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

public class Coordinator {
   static HashMap<String, Coordinator.WorkerEntry> workers;

   public static Vector<String> getWorkers() {
      Vector var0 = new Vector();
      Iterator var1 = workers.keySet().iterator();

      while(var1.hasNext()) {
         String var2 = (String)var1.next();
         String var10001 = ((Coordinator.WorkerEntry)workers.get(var2)).ip;
         var0.add(var10001 + ":" + ((Coordinator.WorkerEntry)workers.get(var2)).port);
      }

      return var0;
   }

   static void cleanWorkerList() {
      HashSet var0 = new HashSet();
      Iterator var1 = workers.keySet().iterator();

      String var2;
      while(var1.hasNext()) {
         var2 = (String)var1.next();
         long var3 = (System.currentTimeMillis() - ((Coordinator.WorkerEntry)workers.get(var2)).lastCheckin.getTime()) / 1000L;
         if (var3 > 15L) {
            var0.add(var2);
         }
      }

      var1 = var0.iterator();

      while(var1.hasNext()) {
         var2 = (String)var1.next();
         System.out.println("Removing dead worker: " + var2);
         workers.remove(var2);
      }

   }

   public static String clientTable() {
      String var0 = "";
      cleanWorkerList();
      if (workers.size() > 0) {
         var0 = var0 + "    <table border=\"1\">\n      <tr><td><b>ID</b></td><td><b>Address</b></td><td><b>Last checkin</b></td></tr>\n";
         TreeSet var1 = new TreeSet();
         var1.addAll(workers.keySet());

         String var3;
         for(Iterator var2 = var1.iterator(); var2.hasNext(); var0 = var0 + "<td>" + (int)((System.currentTimeMillis() - ((Coordinator.WorkerEntry)workers.get(var3)).lastCheckin.getTime()) / 1000L) + " seconds ago</td></tr>\n") {
            var3 = (String)var2.next();
            String var10000 = ((Coordinator.WorkerEntry)workers.get(var3)).ip;
            String var4 = var10000 + ":" + ((Coordinator.WorkerEntry)workers.get(var3)).port;
            var0 = var0 + "      <tr><td><a href=\"http://" + var4 + "/\">" + var3 + "</a></td><td>" + var4 + "</td>";
         }

         var0 = var0 + "    </table>\n";
      } else {
         var0 = var0 + "No active workers.<p>";
      }

      return var0;
   }

   public static void registerRoutes() {
      workers = new HashMap();
      Server.get("/ping", (var0, var1) -> {
         String var2 = var0.queryParams("id");
         String var3 = var0.queryParams("port");
         if (var2 != null && var3 != null) {
            if (workers.get(var2) == null) {
               workers.put(var2, new Coordinator.WorkerEntry());
            }

            Coordinator.WorkerEntry var4 = (Coordinator.WorkerEntry)workers.get(var2);
            var4.lastCheckin = new Date();
            var4.ip = var0.ip();
            var4.port = Integer.parseInt(var3);
            var1.type("text/plain");
            return "OK";
         } else {
            var1.status(400, "Bad request");
            return "One of the two required parameters (id, port) is missing";
         }
      });
      Server.get("/workers", (var0, var1) -> {
         cleanWorkerList();
         var1.type("text/plain");
         TreeSet var2 = new TreeSet();
         var2.addAll(workers.keySet());
         String var3 = var2.size() + "\n";

         String var5;
         for(Iterator var4 = var2.iterator(); var4.hasNext(); var3 = var3 + var5 + "," + ((Coordinator.WorkerEntry)workers.get(var5)).ip + ":" + ((Coordinator.WorkerEntry)workers.get(var5)).port + "\n") {
            var5 = (String)var4.next();
         }

         return var3;
      });
   }

   static class WorkerEntry {
      String ip;
      int port;
      Date lastCheckin;
   }
}
