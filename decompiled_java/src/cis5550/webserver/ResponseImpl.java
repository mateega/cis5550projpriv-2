package cis5550.webserver;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

class ResponseImpl implements Response {
   int statusCode = 200;
   String reasonPhrase = "OK";
   byte[] body = null;
   HashMap<String, List<String>> headers;
   OutputStream out;
   boolean committed = false;
   boolean isHeadRequest;

   ResponseImpl(OutputStream var1, boolean var2) {
      this.out = var1;
      this.headers = new HashMap();
      this.header("content-type", "text/html");
      this.isHeadRequest = var2;
   }

   public void status(int var1, String var2) {
      this.statusCode = var1;
      this.reasonPhrase = var2;
   }

   public void header(String var1, String var2) {
      Object var3 = (List)this.headers.get(var1);
      if (var3 == null) {
         var3 = new LinkedList();
         this.headers.put(var1, var3);
      }

      ((List)var3).add(var2);
   }

   public void bodyAsBytes(byte[] var1) {
      this.body = var1;
   }

   public void body(String var1) {
      this.body = var1 == null ? null : var1.getBytes();
   }

   public void type(String var1) {
      this.headers.remove("content-type");
      this.header("content-type", var1);
   }

   public void write(byte[] var1) throws Exception {
      if (!this.committed) {
         this.commit();
      }

      if (!this.isHeadRequest) {
         this.out.write(var1);
         this.out.flush();
      }

   }

   void commit() throws Exception {
      if (this.committed) {
         throw new Exception("Response already committed!");
      } else {
         PrintWriter var1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.out)));
         var1.write("HTTP/1.1 " + this.statusCode + " " + this.reasonPhrase + "\r\n");
         Iterator var2 = this.headers.keySet().iterator();

         while(var2.hasNext()) {
            String var3 = (String)var2.next();
            List var4 = (List)this.headers.get(var3);
            Iterator var5 = var4.iterator();

            while(var5.hasNext()) {
               String var6 = (String)var5.next();
               var1.write(var3 + ": " + var6 + "\r\n");
            }
         }

         var1.write("\r\n");
         var1.flush();
         this.committed = true;
      }
   }

   public void redirect(String var1, int var2) {
      this.status(var2, "Redirect");
      this.header("location", var1);

      try {
         this.commit();
      } catch (Exception var4) {
      }

   }

   public void halt(int var1, String var2) {
      this.status(var1, var2);

      try {
         this.commit();
      } catch (Exception var4) {
      }

   }
}
