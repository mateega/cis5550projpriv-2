package cis5550.tools;

public class KeyEncoder {
   public static String encode(String var0) {
      String var1 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-";
      String var2 = "";

      for(int var3 = 0; var3 < var0.length(); ++var3) {
         char var10001 = var0.charAt(var3);
         if (var1.contains(var10001.makeConcatWithConstants<invokedynamic>(var10001))) {
            var2 = var2 + var0.charAt(var3);
         } else {
            var2 = var2 + "_" + Integer.toHexString(var0.charAt(var3));
         }
      }

      return var2;
   }

   public static String decode(String var0) {
      String var1 = "";

      for(int var2 = 0; var2 < var0.length(); ++var2) {
         if (var0.charAt(var2) == '_') {
            String var10001 = var0.substring(var2 + 1, var2 + 3);
            var1 = var1 + (char)Integer.decode("0x" + var10001);
            var2 += 2;
         } else {
            var1 = var1 + var0.charAt(var2);
         }
      }

      return var1;
   }

   public static void main(String[] var0) {
      System.out.println(encode(var0[0]));
      System.out.println(decode(encode(var0[0])));
   }
}
