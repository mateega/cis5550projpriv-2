package cis5550.flame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;

public class FlameSubmit {
   static int responseCode;
   static String errorResponse;

   public static String submit(String var0, String var1, String var2, String[] var3) throws Exception {
      responseCode = 200;
      errorResponse = null;
      String var4 = "http://" + var0 + "/submit?class=" + var2;

      for(int var5 = 0; var5 < var3.length; ++var5) {
         var4 = var4 + "&arg" + (var5 + 1) + "=" + URLEncoder.encode(var3[var5], "UTF-8");
      }

      File var14 = new File(var1);
      byte[] var6 = new byte[(int)var14.length()];
      FileInputStream var7 = new FileInputStream(var1);
      var7.read(var6);
      var7.close();
      HttpURLConnection var8 = (HttpURLConnection)(new URI(var4)).toURL().openConnection();
      var8.setRequestMethod("POST");
      var8.setDoOutput(true);
      var8.setFixedLengthStreamingMode(var6.length);
      var8.setRequestProperty("Content-Type", "application/jar-archive");
      var8.connect();
      OutputStream var9 = var8.getOutputStream();
      var9.write(var6);

      String var12;
      try {
         BufferedReader var10 = new BufferedReader(new InputStreamReader(var8.getInputStream()));
         String var15 = "";

         while(true) {
            var12 = var10.readLine();
            if (var12 == null) {
               return var15;
            }

            var15 = var15 + (var15.equals("") ? "" : "\n") + var12;
         }
      } catch (IOException var13) {
         responseCode = var8.getResponseCode();
         BufferedReader var11 = new BufferedReader(new InputStreamReader(var8.getErrorStream()));
         errorResponse = "";

         while(true) {
            var12 = var11.readLine();
            if (var12 == null) {
               return null;
            }

            String var10000 = errorResponse;
            errorResponse = var10000 + (errorResponse.equals("") ? "" : "\n") + var12;
         }
      }
   }

   public static int getResponseCode() {
      return responseCode;
   }

   public static String getErrorResponse() {
      return errorResponse;
   }

   public static void main(String[] var0) throws Exception {
      if (var0.length < 3) {
         System.err.println("Syntax: FlameSubmit <server> <jarFile> <className> [args...]");
         System.exit(1);
      }

      String[] var1 = new String[var0.length - 3];

      for(int var2 = 3; var2 < var0.length; ++var2) {
         var1[var2 - 3] = var0[var2];
      }

      try {
         String var4 = submit(var0[0], var0[1], var0[2], var1);
         if (var4 != null) {
            System.out.println(var4);
         } else {
            System.err.println("*** JOB FAILED ***\n");
            System.err.println(getErrorResponse());
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }
}
