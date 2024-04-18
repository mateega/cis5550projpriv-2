package cis5550.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.net.URL;
import java.net.URLClassLoader;

public class Serializer {
   public static byte[] objectToByteArray(Object var0) {
      try {
         ByteArrayOutputStream var1 = new ByteArrayOutputStream();
         ObjectOutputStream var2 = new ObjectOutputStream(var1);
         var2.writeObject(var0);
         var2.flush();
         return var1.toByteArray();
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   public static Object byteArrayToObject(byte[] var0, File var1) {
      Object var2 = null;

      try {
         ByteArrayInputStream var3 = new ByteArrayInputStream(var0);
         ClassLoader var4 = Thread.currentThread().getContextClassLoader();
         final URLClassLoader var5 = var1 != null ? new URLClassLoader(new URL[]{var1.toURI().toURL()}, var4) : null;
         ObjectInputStream var6 = new ObjectInputStream(var3) {
            protected Class<?> resolveClass(ObjectStreamClass var1) throws IOException, ClassNotFoundException {
               try {
                  Class var2 = Class.forName(var1.getName(), false, (ClassLoader)null);
                  return var2;
               } catch (ClassNotFoundException var3) {
                  return var5 != null ? var5.loadClass(var1.getName()) : null;
               }
            }
         };
         var2 = var6.readObject();
      } catch (Exception var7) {
         var7.printStackTrace();
      }

      return var2;
   }
}
