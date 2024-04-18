package cis5550.tools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTP {
   static Map<String, Vector<Socket>> cachedConnections;

   static Socket openSocket(String var0, String var1, int var2) {
      try {
         if (var0.equals("https")) {
            TrustManager[] var3 = new TrustManager[]{new X509TrustManager() {
               public X509Certificate[] getAcceptedIssuers() {
                  return null;
               }

               public void checkClientTrusted(X509Certificate[] var1, String var2) {
               }

               public void checkServerTrusted(X509Certificate[] var1, String var2) {
               }
            }};

            try {
               SSLContext var4 = SSLContext.getInstance("SSL");
               var4.init((KeyManager[])null, var3, new SecureRandom());
               return var4.getSocketFactory().createSocket(var1, var2);
            } catch (NoSuchAlgorithmException var6) {
            } catch (KeyManagementException var7) {
            }
         } else if (var0.equals("http")) {
            return new Socket(var1, var2);
         }
      } catch (Exception var8) {
         var8.printStackTrace();
      }

      return null;
   }

   public static HTTP.Response doRequest(String var0, String var1, byte[] var2) throws IOException {
      return doRequestWithTimeout(var0, var1, var2, -1, false);
   }

   public static HTTP.Response doRequestWithTimeout(String var0, String var1, byte[] var2, int var3, boolean var4) throws IOException {
      String var5 = "http";
      int var6 = var1.indexOf("://");
      if (var6 >= 0) {
         var5 = var1.substring(0, var6);
         var1 = var1.substring(var6 + 3);
      }

      var6 = var1.indexOf(47);
      if (var6 < 0) {
         return null;
      } else {
         String var7 = var1.substring(0, var6);
         String var8 = var1.substring(var6);
         int var9 = var5.equals("https") ? 443 : 80;
         var6 = var7.indexOf(":");
         String var10;
         if (var6 > 0) {
            var10 = var7.substring(var6 + 1);

            try {
               var9 = Integer.valueOf(var10);
            } catch (NumberFormatException var33) {
            }

            var7 = var7.substring(0, var6);
         }

         var10 = var5 + "-" + var7 + "-" + var9;

         while(true) {
            boolean var11 = false;
            Socket var12 = null;
            if (cachedConnections != null && cachedConnections.get(var10) != null) {
               synchronized(cachedConnections) {
                  if (((Vector)cachedConnections.get(var10)).size() > 0) {
                     var12 = (Socket)((Vector)cachedConnections.get(var10)).remove(0);
                  }
               }

               if (var12 != null) {
                  var11 = true;
               }
            }

            if (var12 == null) {
               var12 = openSocket(var5, var7, var9);
            }

            if (var12 == null) {
               throw new IOException("Cannot connect to server " + var7 + ":" + var9);
            }

            var12.setTcpNoDelay(true);

            try {
               if (var3 > 0) {
                  var12.setSoTimeout(var3);
               }

               OutputStream var13 = var12.getOutputStream();
               String var14 = var0 + " " + var8 + " HTTP/1.1\r\nHost: " + var7 + "\r\n";
               if (var2 != null) {
                  var14 = var14 + "Content-Length: " + var2.length + "\r\n";
               }

               var14 = var14 + "Connection: keep-alive\r\n\r\n";
               var13.write(var14.getBytes());
               if (var2 != null) {
                  var13.write(var2);
               }

               var13.flush();
            } catch (IOException var37) {
               try {
                  var12.close();
               } catch (Exception var32) {
               }

               if (!var11) {
                  throw new IOException("Connection to " + var7 + ":" + var9 + " failed while writing the request");
               }

               System.out.println("XXX cached connection failed; continuing with normal connection");
               continue;
            }

            ByteArrayOutputStream var38 = new ByteArrayOutputStream();
            boolean var39 = true;
            int var15 = -1;
            HashMap var16 = new HashMap();
            byte[] var17 = new byte[100000];
            int var18 = 0;
            int var19 = -1;

            try {
               InputStream var20 = var12.getInputStream();

               while(true) {
                  int var21 = var20.read(var17, var18, var17.length - var18);
                  if (var21 < 0) {
                     break;
                  }

                  var18 += var21;
                  int var22;
                  if (var39) {
                     var22 = 0;

                     label162:
                     for(int var23 = 0; var23 < var18; ++var23) {
                        if (var17[var23] == 10) {
                           ++var22;
                        } else if (var17[var23] != 13) {
                           var22 = 0;
                        }

                        if (var22 == 2) {
                           var38.write(var17, 0, var23);
                           ByteArrayInputStream var24 = new ByteArrayInputStream(var38.toByteArray());
                           BufferedReader var25 = new BufferedReader(new InputStreamReader(var24));
                           String[] var26 = var25.readLine().split(" ");
                           var19 = Integer.valueOf(var26[1]);

                           while(true) {
                              String var27 = var25.readLine();
                              if (var27.equals("")) {
                                 var38.reset();
                                 System.arraycopy(var17, var23 + 1, var17, 0, var18 - (var23 + 1));
                                 var18 -= var23 + 1;
                                 var39 = false;
                                 break label162;
                              }

                              String[] var28 = var27.split(":", 2);
                              if (var28.length == 2) {
                                 String var29 = var28[0];
                                 var16.put(var29.toLowerCase(), var28[1].trim());
                                 if (var29.toLowerCase().equals("content-length")) {
                                    var15 = Integer.parseInt(var28[1].trim());
                                 }
                              }
                           }
                        }
                     }
                  }

                  if (!var39) {
                     var22 = var15 >= 0 && var18 > var15 ? var15 : var18;
                     var38.write(var17, 0, var22);
                     System.arraycopy(var17, var22, var17, 0, var18 - var22);
                     var18 -= var22;
                     if (var38.size() >= var15 || var4) {
                        break;
                     }
                  }
               }
            } catch (Exception var35) {
               try {
                  var12.close();
               } catch (Exception var31) {
               }

               var35.printStackTrace();
               throw new IOException("Connection to " + var7 + ":" + var9 + " failed while reading the response (" + String.valueOf(var35) + ")");
            }

            if (cachedConnections == null) {
               cachedConnections = new HashMap();
            }

            synchronized(cachedConnections) {
               if (cachedConnections.get(var10) == null) {
                  cachedConnections.put(var10, new Vector());
               }

               ((Vector)cachedConnections.get(var10)).add(var12);
            }

            return new HTTP.Response(var38.toByteArray(), var16, var19);
         }
      }
   }

   public static class Response {
      byte[] body;
      Map<String, String> headers;
      int statusCode;

      public Response(byte[] var1, Map<String, String> var2, int var3) {
         this.body = var1;
         this.headers = var2;
         this.statusCode = var3;
      }

      public byte[] body() {
         return this.body;
      }

      public int statusCode() {
         return this.statusCode;
      }

      public Map<String, String> headers() {
         return this.headers;
      }
   }
}
