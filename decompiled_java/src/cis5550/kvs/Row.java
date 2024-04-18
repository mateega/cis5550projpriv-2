package cis5550.kvs;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Row implements Serializable {
   protected String key;
   protected HashMap<String, byte[]> values;

   public Row(String var1) {
      this.key = var1;
      this.values = new HashMap();
   }

   public synchronized String key() {
      return this.key;
   }

   public synchronized Row clone() {
      Row var1 = new Row(this.key);
      Iterator var2 = this.values.keySet().iterator();

      while(var2.hasNext()) {
         String var3 = (String)var2.next();
         var1.values.put(var3, (byte[])this.values.get(var3));
      }

      return var1;
   }

   public synchronized Set<String> columns() {
      return this.values.keySet();
   }

   public synchronized void put(String var1, String var2) {
      this.values.put(var1, var2.getBytes());
   }

   public synchronized void put(String var1, byte[] var2) {
      this.values.put(var1, var2);
   }

   public synchronized String get(String var1) {
      return this.values.get(var1) == null ? null : new String((byte[])this.values.get(var1));
   }

   public synchronized byte[] getBytes(String var1) {
      return (byte[])this.values.get(var1);
   }

   static String readStringSpace(InputStream var0) throws Exception {
      byte[] var1 = new byte[16384];
      int var2 = 0;

      int var3;
      do {
         if (var2 == var1.length) {
            throw new Exception("Format error: Expecting string+space");
         }

         var3 = var0.read();
         if (var3 < 0 || var3 == 10) {
            return null;
         }

         var1[var2++] = (byte)var3;
      } while(var3 != 32);

      return new String(var1, 0, var2 - 1);
   }

   static String readStringSpace(RandomAccessFile var0) throws Exception {
      byte[] var1 = new byte[16384];
      int var2 = 0;

      int var3;
      do {
         if (var2 == var1.length) {
            throw new Exception("Format error: Expecting string+space");
         }

         var3 = var0.read();
         if (var3 < 0 || var3 == 10) {
            return null;
         }

         var1[var2++] = (byte)var3;
      } while(var3 != 32);

      return new String(var1, 0, var2 - 1);
   }

   public static Row readFrom(InputStream var0) throws Exception {
      String var1 = readStringSpace(var0);
      if (var1 == null) {
         return null;
      } else {
         Row var2 = new Row(var1);

         while(true) {
            String var3 = readStringSpace(var0);
            if (var3 == null) {
               return var2;
            }

            int var4 = Integer.parseInt(readStringSpace(var0));
            byte[] var5 = new byte[var4];

            int var7;
            for(int var6 = 0; var6 < var4; var6 += var7) {
               var7 = var0.read(var5, var6, var4 - var6);
               if (var7 < 0) {
                  throw new Exception("Premature end of stream while reading value for key '" + var3 + "' (read " + var6 + " bytes, expecting " + var4 + ")");
               }
            }

            byte var8 = (byte)var0.read();
            if (var8 != 32) {
               throw new Exception("Expecting a space separator after value for key '" + var3 + "', but got " + var8);
            }

            var2.put(var3, var5);
         }
      }
   }

   public static Row readFrom(RandomAccessFile var0) throws Exception {
      String var1 = readStringSpace(var0);
      if (var1 == null) {
         return null;
      } else {
         Row var2 = new Row(var1);

         while(true) {
            String var3 = readStringSpace(var0);
            if (var3 == null) {
               return var2;
            }

            int var4 = Integer.parseInt(readStringSpace(var0));
            byte[] var5 = new byte[var4];

            int var7;
            for(int var6 = 0; var6 < var4; var6 += var7) {
               var7 = var0.read(var5, var6, var4 - var6);
               if (var7 < 0) {
                  throw new Exception("Premature end of stream while reading value for key '" + var3 + "' (read " + var6 + " bytes, expecting " + var4 + ")");
               }
            }

            byte var8 = (byte)var0.read();
            if (var8 != 32) {
               throw new Exception("Expecting a space separator after value for key '" + var3 + "'");
            }

            var2.put(var3, var5);
         }
      }
   }

   public synchronized String toString() {
      String var1 = this.key + " {";
      boolean var2 = true;

      for(Iterator var3 = this.values.keySet().iterator(); var3.hasNext(); var2 = false) {
         String var4 = (String)var3.next();
         var1 = var1 + (var2 ? " " : ", ") + var4 + ": " + new String((byte[])this.values.get(var4));
      }

      return var1 + " }";
   }

   public synchronized byte[] toByteArray() {
      ByteArrayOutputStream var1 = new ByteArrayOutputStream();

      try {
         var1.write(this.key.getBytes());
         var1.write(32);
         Iterator var2 = this.values.keySet().iterator();

         while(var2.hasNext()) {
            String var3 = (String)var2.next();
            var1.write(var3.getBytes());
            var1.write(32);
            Object var10001 = this.values.get(var3);
            var1.write(((byte[])var10001).length.makeConcatWithConstants<invokedynamic>(((byte[])var10001).length).getBytes());
            var1.write(32);
            var1.write((byte[])this.values.get(var3));
            var1.write(32);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
         throw new RuntimeException("This should not happen!");
      }

      return var1.toByteArray();
   }
}
