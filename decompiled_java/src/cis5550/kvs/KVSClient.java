package cis5550.kvs;

import cis5550.tools.HTTP;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

public class KVSClient implements KVS {
   String coordinator;
   Vector<KVSClient.WorkerEntry> workers;
   boolean haveWorkers;

   public int numWorkers() throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      return this.workers.size();
   }

   public static String getVersion() {
      return "v1.5 Oct 20 2023";
   }

   public String getCoordinator() {
      return this.coordinator;
   }

   public String getWorkerAddress(int var1) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      return ((KVSClient.WorkerEntry)this.workers.elementAt(var1)).address;
   }

   public String getWorkerID(int var1) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      return ((KVSClient.WorkerEntry)this.workers.elementAt(var1)).id;
   }

   synchronized void downloadWorkers() throws IOException {
      String var1 = new String(HTTP.doRequest("GET", "http://" + this.coordinator + "/workers", (byte[])null).body());
      String[] var2 = var1.split("\n");
      int var3 = Integer.parseInt(var2[0]);
      if (var3 < 1) {
         throw new IOException("No active KVS workers");
      } else if (var2.length != var3 + 1) {
         throw new RuntimeException("Received truncated response when asking KVS coordinator for list of workers");
      } else {
         this.workers.clear();

         for(int var4 = 0; var4 < var3; ++var4) {
            String[] var5 = var2[1 + var4].split(",");
            this.workers.add(new KVSClient.WorkerEntry(var5[1], var5[0]));
         }

         Collections.sort(this.workers);
         this.haveWorkers = true;
      }
   }

   int workerIndexForKey(String var1) {
      int var2 = this.workers.size() - 1;
      if (var1 != null) {
         for(int var3 = 0; var3 < this.workers.size() - 1; ++var3) {
            if (var1.compareTo(((KVSClient.WorkerEntry)this.workers.elementAt(var3)).id) >= 0 && var1.compareTo(((KVSClient.WorkerEntry)this.workers.elementAt(var3 + 1)).id) < 0) {
               var2 = var3;
            }
         }
      }

      return var2;
   }

   public KVSClient(String var1) {
      this.coordinator = var1;
      this.workers = new Vector();
      this.haveWorkers = false;
   }

   public boolean rename(String var1, String var2) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      boolean var3 = true;
      Iterator var4 = this.workers.iterator();

      while(var4.hasNext()) {
         KVSClient.WorkerEntry var5 = (KVSClient.WorkerEntry)var4.next();

         try {
            byte[] var6 = HTTP.doRequest("PUT", "http://" + var5.address + "/rename/" + URLEncoder.encode(var1, "UTF-8") + "/", var2.getBytes()).body();
            String var7 = new String(var6);
            var3 &= var7.equals("OK");
         } catch (Exception var8) {
         }
      }

      return var3;
   }

   public void delete(String var1) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      Iterator var2 = this.workers.iterator();

      while(var2.hasNext()) {
         KVSClient.WorkerEntry var3 = (KVSClient.WorkerEntry)var2.next();

         try {
            byte[] var4 = HTTP.doRequest("PUT", "http://" + var3.address + "/delete/" + URLEncoder.encode(var1, "UTF-8") + "/", (byte[])null).body();
            new String(var4);
         } catch (Exception var6) {
         }
      }

   }

   public void put(String var1, String var2, String var3, byte[] var4) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      try {
         String var5 = "http://" + ((KVSClient.WorkerEntry)this.workers.elementAt(this.workerIndexForKey(var2))).address + "/data/" + var1 + "/" + URLEncoder.encode(var2, "UTF-8") + "/" + URLEncoder.encode(var3, "UTF-8");
         byte[] var6 = HTTP.doRequest("PUT", var5, var4).body();
         String var7 = new String(var6);
         if (!var7.equals("OK")) {
            throw new RuntimeException("PUT returned something other than OK: " + var7 + "(" + var5 + ")");
         }
      } catch (UnsupportedEncodingException var8) {
         throw new RuntimeException("UTF-8 encoding not supported?!?");
      }
   }

   public void put(String var1, String var2, String var3, String var4) throws IOException {
      this.put(var1, var2, var3, var4.getBytes());
   }

   public void putRow(String var1, Row var2) throws FileNotFoundException, IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      String var10001 = ((KVSClient.WorkerEntry)this.workers.elementAt(this.workerIndexForKey(var2.key()))).address;
      byte[] var3 = HTTP.doRequest("PUT", "http://" + var10001 + "/data/" + var1, var2.toByteArray()).body();
      String var4 = new String(var3);
      if (!var4.equals("OK")) {
         throw new RuntimeException("PUT returned something other than OK: " + var4);
      }
   }

   public Row getRow(String var1, String var2) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      String var10001 = ((KVSClient.WorkerEntry)this.workers.elementAt(this.workerIndexForKey(var2))).address;
      HTTP.Response var3 = HTTP.doRequest("GET", "http://" + var10001 + "/data/" + var1 + "/" + URLEncoder.encode(var2, "UTF-8"), (byte[])null);
      if (var3.statusCode() == 404) {
         return null;
      } else {
         byte[] var4 = var3.body();

         try {
            return Row.readFrom((InputStream)(new ByteArrayInputStream(var4)));
         } catch (Exception var6) {
            throw new RuntimeException("Decoding error while reading Row '" + var2 + "' in table '" + var1 + "' from getRow() URL (encoded as '" + URLEncoder.encode(var2, "UTF-8") + "')");
         }
      }
   }

   public byte[] get(String var1, String var2, String var3) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      String var10001 = ((KVSClient.WorkerEntry)this.workers.elementAt(this.workerIndexForKey(var2))).address;
      HTTP.Response var4 = HTTP.doRequest("GET", "http://" + var10001 + "/data/" + var1 + "/" + URLEncoder.encode(var2, "UTF-8") + "/" + URLEncoder.encode(var3, "UTF-8"), (byte[])null);
      return var4 != null && var4.statusCode() == 200 ? var4.body() : null;
   }

   public boolean existsRow(String var1, String var2) throws FileNotFoundException, IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      String var10001 = ((KVSClient.WorkerEntry)this.workers.elementAt(this.workerIndexForKey(var2))).address;
      HTTP.Response var3 = HTTP.doRequest("GET", "http://" + var10001 + "/data/" + var1 + "/" + URLEncoder.encode(var2, "UTF-8"), (byte[])null);
      return var3.statusCode() == 200;
   }

   public int count(String var1) throws IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      int var2 = 0;
      Iterator var3 = this.workers.iterator();

      while(var3.hasNext()) {
         KVSClient.WorkerEntry var4 = (KVSClient.WorkerEntry)var3.next();
         HTTP.Response var5 = HTTP.doRequest("GET", "http://" + var4.address + "/count/" + var1, (byte[])null);
         if (var5 != null && var5.statusCode() == 200) {
            String var6 = new String(var5.body());
            var2 += Integer.valueOf(var6);
         }
      }

      return var2;
   }

   public Iterator<Row> scan(String var1) throws FileNotFoundException, IOException {
      return this.scan(var1, (String)null, (String)null);
   }

   public Iterator<Row> scan(String var1, String var2, String var3) throws FileNotFoundException, IOException {
      if (!this.haveWorkers) {
         this.downloadWorkers();
      }

      return new KVSClient.KVSIterator(var1, var2, var3);
   }

   public static void main(String[] var0) throws Exception {
      if (var0.length < 2) {
         System.err.println("Syntax: client <coordinator> get <tableName> <row> <column>");
         System.err.println("Syntax: client <coordinator> put <tableName> <row> <column> <value>");
         System.err.println("Syntax: client <coordinator> scan <tableName>");
         System.err.println("Syntax: client <coordinator> count <tableName>");
         System.err.println("Syntax: client <coordinator> rename <oldTableName> <newTableName>");
         System.err.println("Syntax: client <coordinator> delete <tableName>");
         System.exit(1);
      }

      KVSClient var1 = new KVSClient(var0[0]);
      if (var0[1].equals("put")) {
         if (var0.length != 6) {
            System.err.println("Syntax: client <coordinator> put <tableName> <row> <column> <value>");
            System.exit(1);
         }

         var1.put(var0[2], var0[3], var0[4], var0[5].getBytes("UTF-8"));
      } else if (var0[1].equals("get")) {
         if (var0.length != 5) {
            System.err.println("Syntax: client <coordinator> get <tableName> <row> <column>");
            System.exit(1);
         }

         byte[] var2 = var1.get(var0[2], var0[3], var0[4]);
         if (var2 == null) {
            System.err.println("No value found");
         } else {
            System.out.write(var2);
         }
      } else if (var0[1].equals("scan")) {
         if (var0.length != 3) {
            System.err.println("Syntax: client <coordinator> scan <tableName>");
            System.exit(1);
         }

         Iterator var4 = var1.scan(var0[2], (String)null, (String)null);

         int var3;
         for(var3 = 0; var4.hasNext(); ++var3) {
            System.out.println(var4.next());
         }

         System.err.println(var3 + " row(s) scanned");
      } else if (var0[1].equals("count")) {
         if (var0.length != 3) {
            System.err.println("Syntax: client <coordinator> count <tableName>");
            System.exit(1);
         }

         PrintStream var10000 = System.out;
         int var10001 = var1.count(var0[2]);
         var10000.println(var10001 + " row(s) in table '" + var0[2] + "'");
      } else if (var0[1].equals("delete")) {
         if (var0.length != 3) {
            System.err.println("Syntax: client <coordinator> delete <tableName>");
            System.exit(1);
         }

         var1.delete(var0[2]);
         System.err.println("Table '" + var0[2] + "' deleted");
      } else if (var0[1].equals("rename")) {
         if (var0.length != 4) {
            System.err.println("Syntax: client <coordinator> rename <oldTableName> <newTableName>");
            System.exit(1);
         }

         if (var1.rename(var0[2], var0[3])) {
            System.out.println("Success");
         } else {
            System.out.println("Failure");
         }
      } else {
         System.err.println("Unknown command: " + var0[1]);
         System.exit(1);
      }

   }

   static class WorkerEntry implements Comparable<KVSClient.WorkerEntry> {
      String address;
      String id;

      WorkerEntry(String var1, String var2) {
         this.address = var1;
         this.id = var2;
      }

      public int compareTo(KVSClient.WorkerEntry var1) {
         return this.id.compareTo(var1.id);
      }
   }

   class KVSIterator implements Iterator<Row> {
      InputStream in = null;
      boolean atEnd = false;
      Row nextRow;
      int currentRangeIndex = 0;
      String endRowExclusive;
      String startRow;
      String tableName;
      Vector<String> ranges;

      KVSIterator(String param2, String param3, String param4) throws IOException {
         this.endRowExclusive = var4;
         this.tableName = var2;
         this.startRow = var3;
         this.ranges = new Vector();
         if (var3 == null || var3.compareTo(KVSClient.this.getWorkerID(0)) < 0) {
            String var5 = this.getURL(var2, KVSClient.this.numWorkers() - 1, var3, var4 != null && var4.compareTo(KVSClient.this.getWorkerID(0)) < 0 ? var4 : KVSClient.this.getWorkerID(0));
            this.ranges.add(var5);
         }

         for(int var9 = 0; var9 < KVSClient.this.numWorkers(); ++var9) {
            if ((var3 == null || var9 == KVSClient.this.numWorkers() - 1 || var3.compareTo(KVSClient.this.getWorkerID(var9 + 1)) < 0) && (var4 == null || var4.compareTo(KVSClient.this.getWorkerID(var9)) > 0)) {
               boolean var6 = var3 != null && var3.compareTo(KVSClient.this.getWorkerID(var9)) > 0;
               boolean var7 = var4 != null && (var9 == KVSClient.this.numWorkers() - 1 || var4.compareTo(KVSClient.this.getWorkerID(var9 + 1)) < 0);
               String var8 = this.getURL(var2, var9, var6 ? var3 : KVSClient.this.getWorkerID(var9), var7 ? var4 : (var9 < KVSClient.this.numWorkers() - 1 ? KVSClient.this.getWorkerID(var9 + 1) : null));
               this.ranges.add(var8);
            }
         }

         this.openConnectionAndFill();
      }

      protected String getURL(String var1, int var2, String var3, String var4) throws IOException {
         String var5 = "";
         if (var3 != null) {
            var5 = "startRow=" + var3;
         }

         String var10000;
         if (var4 != null) {
            var10000 = var5.equals("") ? "" : var5 + "&";
            var5 = var10000 + "endRowExclusive=" + var4;
         }

         var10000 = KVSClient.this.getWorkerAddress(var2);
         return "http://" + var10000 + "/data/" + var1 + (var5.equals("") ? "" : "?" + var5);
      }

      void openConnectionAndFill() {
         try {
            if (this.in != null) {
               this.in.close();
               this.in = null;
            }

            if (this.atEnd) {
               return;
            }

            while(true) {
               if (this.currentRangeIndex >= this.ranges.size()) {
                  this.atEnd = true;
                  return;
               }

               try {
                  URL var1 = (new URI((String)this.ranges.elementAt(this.currentRangeIndex))).toURL();
                  HttpURLConnection var2 = (HttpURLConnection)var1.openConnection();
                  var2.setRequestMethod("GET");
                  var2.connect();
                  this.in = var2.getInputStream();
                  Row var3 = this.fill();
                  if (var3 != null) {
                     this.nextRow = var3;
                     break;
                  }
               } catch (FileNotFoundException var5) {
               } catch (URISyntaxException var6) {
               }

               ++this.currentRangeIndex;
            }
         } catch (IOException var7) {
            if (this.in != null) {
               try {
                  this.in.close();
               } catch (Exception var4) {
               }

               this.in = null;
            }

            this.atEnd = true;
         }

      }

      synchronized Row fill() {
         try {
            Row var1 = Row.readFrom(this.in);
            return var1;
         } catch (Exception var2) {
            return null;
         }
      }

      public synchronized Row next() {
         if (this.atEnd) {
            return null;
         } else {
            Row var1 = this.nextRow;
            this.nextRow = this.fill();

            while(this.nextRow == null && !this.atEnd) {
               ++this.currentRangeIndex;
               this.openConnectionAndFill();
            }

            return var1;
         }
      }

      public synchronized boolean hasNext() {
         return !this.atEnd;
      }
   }
}
