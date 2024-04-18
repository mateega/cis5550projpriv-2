package cis5550.kvs;

import cis5550.tools.KeyEncoder;
import cis5550.tools.Logger;
import cis5550.webserver.Response;
import cis5550.webserver.Server;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

class Worker extends cis5550.generic.Worker {
   private static final Logger logger = Logger.getLogger(Worker.class);
   private static final String version = "v1.10a Oct 26 2023";
   protected static Map<String, Map<String, Row>> data;
   static final boolean doSubdirs = false;
   static String storageDir;
   static final boolean doReplication = false;
   static final int replicationFactor = 3;
   static String[] replicas = new String[2];
   static String endOfLocalRange;

   static synchronized void createTableIfNecessary(String var0) throws FileNotFoundException {
      if (data.get(var0) == null) {
         data.put(var0, new HashMap());
         if (var0.startsWith("pt-")) {
            String var10002 = storageDir;
            (new File(var10002 + File.separator + KeyEncoder.encode(var0))).mkdirs();
         }
      }

   }

   static synchronized Row getRow(String var0, String var1, boolean var2) throws Exception {
      if (var0.startsWith("pt-")) {
         String var10002 = storageDir;
         File var6 = new File(var10002 + File.separator + KeyEncoder.encode(var0) + File.separator + KeyEncoder.encode(var1));
         if (!var6.exists()) {
            return null;
         } else {
            FileInputStream var7 = new FileInputStream(var6);
            Row var5 = Row.readFrom((InputStream)var7);
            return var5;
         }
      } else {
         Map var3 = (Map)data.get(var0);
         if (var3 == null) {
            throw new Worker.KVSException("Table '" + var0 + "' not found", 404);
         } else {
            Row var4 = (Row)var3.get(var1);
            if (var4 == null) {
               if (var2) {
                  throw new Worker.KVSException("Row '" + var1 + "' not found in table '" + var0 + "'", 404);
               } else {
                  return null;
               }
            } else {
               return var4;
            }
         }
      }
   }

   static synchronized void putRow(String var0, Row var1) throws IOException {
      if (var0.startsWith("pt-")) {
         String var10002 = storageDir;
         FileOutputStream var2 = new FileOutputStream(var10002 + File.separator + KeyEncoder.encode(var0) + File.separator + KeyEncoder.encode(var1.key()));
         var2.write(var1.toByteArray());
         var2.close();
      } else {
         Map var3 = (Map)data.get(var0);
         var3.put(var1.key(), var1);
      }

   }

   static void deleteRecursively(File var0) {
      if (var0.isDirectory()) {
         File[] var1 = var0.listFiles();
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            File var4 = var1[var3];
            deleteRecursively(var4);
         }
      }

      var0.delete();
   }

   static void returnRecursively(String var0, File var1, String var2, String var3, Response var4) throws Exception {
      File[] var5 = var1.listFiles();
      int var6 = var5.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         File var8 = var5[var7];
         if (var8.isDirectory()) {
            returnRecursively(var0, var8, var2, var3, var4);
         } else if ((var2 == null || var2.compareTo(var8.getName()) <= 0) && (var3 == null || var3.compareTo(var8.getName()) > 0)) {
            var4.write(getRow(var0, var8.getName(), true).toByteArray());
            var4.write(new byte[]{10});
         }
      }

   }

   static int countRecursively(File var0) throws Exception {
      int var1 = 0;
      File[] var2 = var0.listFiles();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         File var5 = var2[var4];
         var1 += var5.isDirectory() ? countRecursively(var5) : 1;
      }

      return var1;
   }

   static void findAllKeys(File var0, TreeSet<String> var1) throws Exception {
      File[] var2 = var0.listFiles();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         File var5 = var2[var4];
         if (var5.isDirectory()) {
            findAllKeys(var5, var1);
         } else {
            var1.add(KeyEncoder.decode(var5.getName()));
         }
      }

   }

   static int tableSize(String var0) throws Exception {
      if (!var0.startsWith("pt-")) {
         Map var2 = (Map)data.get(var0);
         return var2 == null ? 0 : var2.size();
      } else {
         String var10002 = storageDir;
         File var1 = new File(var10002 + File.separator + KeyEncoder.encode(var0));
         return var1.exists() ? countRecursively(var1) : 0;
      }
   }

   public static void main(String[] var0) {
      if (var0.length != 3) {
         System.err.println("Syntax: Worker <port> <storageDir> <coordinatorIP:port>");
         System.exit(1);
      }

      int var1 = Integer.parseInt(var0[0]);
      storageDir = var0[1];
      String var2 = var0[2];
      logger.info("KVS Worker (v1.10a Oct 26 2023) starting on port " + var1);
      data = new ConcurrentHashMap();
      String var3 = null;

      try {
         if (!(new File(storageDir)).exists()) {
            (new File(storageDir)).mkdir();
         }

         File var4 = new File(storageDir + File.separator + "id");
         if (var4.exists()) {
            var3 = (new Scanner(var4)).nextLine();
            logger.debug("Using existing ID: " + var3);
         }

         if (var3 == null) {
            var3 = ((StringBuilder)(new Random()).ints(97, 123).limit(5L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)).toString();
            logger.debug("Choosing new ID: " + var3);
            BufferedWriter var5 = new BufferedWriter(new FileWriter(var4));
            var5.write(var3);
            var5.close();
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      startPingThread(var2, var3, var1);
      Server.port(var1);
      Server.put("/data/:table/:row/:column", (var1x, var2x) -> {
         String var3x = var1x.params("table");
         String var4 = var1x.params("row");
         String var5 = var1x.params("column");
         if (var3x != null && var4 != null && var5 != null) {
            logger.info("PUT('" + var3x + "','" + var4 + "','" + var5 + "',[" + var1x.bodyAsBytes().length + " bytes])");
            createTableIfNecessary(var3x);
            synchronized(data) {
               Row var7 = getRow(var3x, var4, false);
               if (var7 == null) {
                  var7 = new Row(var4);
               }

               var7.put(var5, var1x.bodyAsBytes());
               putRow(var3x, var7);
               return "OK";
            }
         } else {
            var2x.status(401, "Bad request");
            return "Row and column keys must both be present";
         }
      });
      Server.put("/data/:table/", (var0x, var1x) -> {
         String var2 = var0x.params("table");
         logger.info("Streaming PUT('" + var2 + "')");
         createTableIfNecessary(var2);
         Map var3 = (Map)data.get(var2);
         ByteArrayInputStream var4 = new ByteArrayInputStream(var0x.bodyAsBytes());

         while(true) {
            Row var5 = Row.readFrom((InputStream)var4);
            if (var5 == null) {
               return "OK";
            }

            putRow(var2, var5);
         }
      });
      Server.get("/", (var2x, var3x) -> {
         String var4 = "<html><head><title>KVS worker</title></head><body>\n";
         var4 = var4 + "<h3>KVS worker '" + var3 + "' (port " + var1 + ")</h3>\n<table border=\"1\">\n";
         TreeSet var5 = new TreeSet();
         var5.addAll(data.keySet());
         File[] var6 = (new File(storageDir)).listFiles();
         int var7 = var6.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            File var9 = var6[var8];
            if (var9.isDirectory() && var9.getName().startsWith("pt-")) {
               var5.add(var9.getName());
            }
         }

         String var11;
         for(Iterator var10 = var5.iterator(); var10.hasNext(); var4 = var4 + "<tr><td><a href=\"/view/" + var11 + "\">" + var11 + "</a></td><td>" + tableSize(var11) + " key(s)</td></tr>\n") {
            var11 = (String)var10.next();
         }

         var4 = var4 + "</table>\n";
         return var4;
      });
      Server.put("/delete/:table/", (var0x, var1x) -> {
         String var2 = var0x.params("table");
         logger.info("DELETE('" + var2 + "')");
         if (data.get(var2) == null) {
            var1x.status(404, "Not found");
            return "Table '" + var2 + "' not found";
         } else {
            data.remove(var2);
            String var10002 = storageDir;
            deleteRecursively(new File(var10002 + File.separator + KeyEncoder.encode(var2)));
            return "OK";
         }
      });
      Server.put("/rename/:table/", (var0x, var1x) -> {
         String var2 = var0x.params("table");
         logger.info("RENAME('" + var2 + "' -> '" + var0x.body() + "')");
         if (var2.startsWith("pt-") && !var0x.body().startsWith("pt-")) {
            var1x.status(400, "Cannot convert a table from persistent to in-memory");
            return "Cannot convert a table from persistent to in-memory";
         } else {
            if (!var2.startsWith("pt-")) {
               if (data.get(var2) == null) {
                  var1x.status(404, "Not found");
                  return "Table '" + var2 + "' not found";
               }

               if (data.get(var0x.body()) != null) {
                  var1x.status(409, "Conflict");
                  return "Table '" + var0x.body() + "' already exists!";
               }

               Map var3 = (Map)data.get(var2);
               data.put(var0x.body(), var3);
               data.remove(var2);
               if (var0x.body().startsWith("pt-")) {
                  String var10002 = storageDir;
                  (new File(var10002 + File.separator + KeyEncoder.encode(var0x.body()))).mkdirs();
                  Iterator var4 = var3.keySet().iterator();

                  while(var4.hasNext()) {
                     String var5 = (String)var4.next();
                     putRow(var0x.body(), (Row)var3.get(var5));
                  }
               }
            } else {
               Files.move(FileSystems.getDefault().getPath(storageDir, var2), FileSystems.getDefault().getPath(storageDir, var0x.body()));
            }

            return "OK";
         }
      });
      Server.get("/tables", (var0x, var1x) -> {
         String var2 = "";

         String var4;
         for(Iterator var3 = data.keySet().iterator(); var3.hasNext(); var2 = var2 + var4 + "\n") {
            var4 = (String)var3.next();
         }

         return var2;
      });
      Server.get("/view/:table/", (var0x, var1x) -> {
         String var2 = var0x.params("table");
         String var3 = var0x.queryParams("startRow");
         String var4 = "<html><head><title>Table view</title></head><body>\n";
         var4 = var4 + "<h3>Table " + var2 + "</h3>\n<table border=\"1\">\n";
         Map var5 = (Map)data.get(var2);
         TreeSet var6 = new TreeSet();
         if (var2.startsWith("pt-")) {
            String var10002 = storageDir;
            findAllKeys(new File(var10002 + File.separator + KeyEncoder.encode(var2)), var6);
         } else {
            var6.addAll(var5.keySet());
         }

         if (var3 != null) {
            var6 = (TreeSet)var6.tailSet(var3);
         }

         TreeSet var7 = new TreeSet();
         Iterator var8 = var6.iterator();

         int var9;
         for(var9 = 0; var9 < 10 && var8.hasNext(); ++var9) {
            var7.addAll(getRow(var2, (String)var8.next(), true).columns());
         }

         var4 = var4 + "<tr><td><b>Key</b>";

         String var10;
         for(Iterator var14 = var7.iterator(); var14.hasNext(); var4 = var4 + "<td><b>" + var10 + "</b></td>") {
            var10 = (String)var14.next();
         }

         var4 = var4 + "</tr>\n";
         var8 = var6.iterator();

         for(var9 = 0; var9 < 10 && var8.hasNext(); ++var9) {
            var10 = (String)var8.next();
            Row var11 = getRow(var2, var10, true);
            var4 = var4 + "<tr><td>" + var10 + "</td>";

            String var13;
            for(Iterator var12 = var7.iterator(); var12.hasNext(); var4 = var4 + "<td>" + var11.get(var13) + "</td>") {
               var13 = (String)var12.next();
            }

            var4 = var4 + "</tr>\n";
         }

         var4 = var4 + "</table><p>";
         if (var8.hasNext()) {
            var4 = var4 + "<a href=\"/view/" + var2 + "?startRow=" + URLEncoder.encode((String)var8.next(), "UTF-8") + "\">Next</a>\n";
         }

         var4 = var4 + "</body></html>\n";
         return var4;
      });
      Server.get("/count/:table/", (var0x, var1x) -> {
         int var2 = tableSize(var0x.params("table"));
         if (var2 == 0) {
            var1x.status(404, "Not found");
            return "Table '" + var0x.params("table") + "' not found";
         } else {
            return var2.makeConcatWithConstants<invokedynamic>(var2);
         }
      });
      Server.get("/data/:table/", (var0x, var1x) -> {
         String var2 = var0x.params("table");
         String var3 = var0x.queryParams("startRow");
         String var4 = var0x.queryParams("endRowExclusive");
         Map var5 = (Map)data.get(var2);
         byte[] var6 = new byte[]{10};
         if (!var2.startsWith("pt-")) {
            if (var5 == null) {
               var1x.status(404, "Not found");
               return "Table '" + var2 + "' not found";
            }

            Iterator var7 = var5.keySet().iterator();

            label39:
            while(true) {
               String var8;
               do {
                  do {
                     if (!var7.hasNext()) {
                        break label39;
                     }

                     var8 = (String)var7.next();
                  } while(var3 != null && var3.compareTo(var8) > 0);
               } while(var4 != null && var4.compareTo(var8) <= 0);

               var1x.write(getRow(var2, var8, true).toByteArray());
               var1x.write(var6);
            }
         } else {
            String var10002 = storageDir;
            File var9 = new File(var10002 + File.separator + KeyEncoder.encode(var2));
            if (!var9.exists()) {
               var1x.status(404, "Not found");
               return "Table '" + var2 + "' not found";
            }

            returnRecursively(var2, var9, var3, var4, var1x);
         }

         var1x.write(var6);
         return null;
      });
      Server.get("/data/:table/:row", (var0x, var1x) -> {
         String var2 = var0x.params("table");
         String var3 = var0x.params("row");
         logger.info("GETROW('" + var2 + "','" + var3 + "')");

         try {
            Row var4 = getRow(var2, var3, true);
            if (var4 != null) {
               var1x.bodyAsBytes(var4.toByteArray());
            } else {
               var1x.status(404, "Not found");
            }

            return null;
         } catch (Worker.KVSException var5) {
            logger.debug(var5.error);
            var1x.status(var5.statusCode, var5.error);
            return var5.error;
         }
      });
      Server.get("/version", (var0x, var1x) -> {
         return "v1.10a Oct 26 2023";
      });
      Server.get("/data/:table/:row/:column", (var0x, var1x) -> {
         String var2 = var0x.params("table");
         String var3 = var0x.params("row");
         String var4 = var0x.params("column");
         if (var2 != null && var3 != null && var4 != null) {
            logger.info("GET('" + var2 + "','" + var3 + "','" + var4 + "')");

            try {
               Row var5 = getRow(var2, var3, true);
               byte[] var6 = var5 != null ? var5.getBytes(var4) : null;
               if (var6 == null) {
                  throw new Worker.KVSException("Column " + var4 + " not found in row '" + var3 + "' of table '" + var2 + "'", 404);
               } else {
                  logger.debug("Returning a " + var6.length + "-byte value");
                  var1x.bodyAsBytes(var6);
                  return null;
               }
            } catch (Worker.KVSException var7) {
               logger.debug(var7.error);
               var1x.status(var7.statusCode, var7.error);
               return var7.error;
            }
         } else {
            var1x.status(401, "Bad request");
            return "Row and column keys must both be present and be alphanumeric";
         }
      });
   }

   static final class KVSException extends Exception {
      final String error;
      final int statusCode;

      public KVSException(String var1, int var2) {
         super(var1);
         this.error = var1;
         this.statusCode = var2;
      }
   }
}
