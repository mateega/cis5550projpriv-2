package cis5550.webserver;

import cis5550.tools.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class Server implements Runnable {
   private static final Logger logger = Logger.getLogger(Server.class);
   private static final String version = "v1.7 Dec 31 2022";
   private static final int NUM_WORKERS = 100;
   static int port;
   static Map<String, String> staticFilePath;
   static String currentHost = "default";
   static boolean running = false;
   int securePort;
   Map<String, SessionImpl> sessions;
   Map<String, Vector<Server.RouteEntry>> routes;
   static Server instance = null;
   BlockingQueue<Socket> queue;

   static void launchIfNecessary() {
      if (instance == null) {
         instance = new Server();
      }

      if (!running) {
         running = true;
         Thread var0 = new Thread(instance);
         var0.start();
      }

   }

   public static void port(int var0) {
      if (instance == null) {
         instance = new Server();
      }

      instance.setPort(var0);
   }

   protected SessionImpl makeSession() {
      SessionImpl var1 = new SessionImpl();
      synchronized(this.sessions) {
         this.sessions.put(var1.id(), var1);
         return var1;
      }
   }

   public static void securePort(int var0) {
      if (instance == null) {
         instance = new Server();
      }

      instance.setSecurePort(var0);
   }

   void setStaticLocation(String var1) {
      logger.info("Serving static files for " + currentHost + " from " + var1);
      staticFilePath.put(currentHost, var1);
   }

   Server() {
      port = 8000;
      running = false;
      staticFilePath = new HashMap();
      this.queue = new LinkedBlockingQueue();
      this.routes = new HashMap();
      this.routes.put("default", new Vector());
      this.sessions = new HashMap();
      this.securePort = -1;
   }

   void addRoute(String var1, String var2, Route var3) {
      ((Vector)this.routes.get(currentHost)).add(new Server.RouteEntry(var1, var2, var3));
   }

   public static void get(String var0, Route var1) {
      launchIfNecessary();
      instance.addRoute("GET", var0, var1);
   }

   public static void put(String var0, Route var1) {
      launchIfNecessary();
      instance.addRoute("PUT", var0, var1);
   }

   public static void post(String var0, Route var1) {
      launchIfNecessary();
      instance.addRoute("POST", var0, var1);
   }

   public void setPort(int var1) {
      if (running) {
         throw new RuntimeException("You need to call port() before calling any other methods!");
      } else {
         port = var1;
      }
   }

   public void setSecurePort(int var1) {
      if (running) {
         throw new RuntimeException("You need to call securePort() before calling any other methods!");
      } else {
         this.securePort = var1;
      }
   }

   boolean patternMatches(String var1, String var2, HashMap<String, String> var3) {
      String[] var4 = var1.split("/");
      String[] var5 = var2.split("/");
      if (var4.length != var5.length) {
         return false;
      } else {
         for(int var6 = 0; var6 < var4.length; ++var6) {
            if (var4[var6].startsWith(":")) {
               try {
                  var3.put(var4[var6].substring(1), URLDecoder.decode(var5[var6], "UTF-8"));
               } catch (UnsupportedEncodingException var8) {
               }
            } else if (!var4[var6].equals(var5[var6])) {
               return false;
            }
         }

         return true;
      }
   }

   void parseQueryParams(String var1, HashMap<String, String> var2) {
      String[] var3 = var1.split("&");

      for(int var4 = 0; var4 < var3.length; ++var4) {
         String[] var5 = var3[var4].split("=", 2);

         try {
            var2.put(var5[0], var5.length > 1 ? URLDecoder.decode(var5[1], "UTF-8") : "");
         } catch (UnsupportedEncodingException var7) {
         }
      }

   }

   void sendError(PrintWriter var1, int var2, String var3) {
      logger.debug("Sending a " + var2 + " response");
      var1.write("HTTP/1.1 " + var2 + " " + var3 + "\r\nContent-Length: " + (4 + var3.length()) + "\r\n\r\n" + var2 + " " + var3);
   }

   void workerThread(int var1) {
      while(true) {
         Socket var2 = null;

         try {
            var2 = (Socket)this.queue.take();
            InputStream var3 = var2.getInputStream();
            byte[] var4 = new byte[100000];
            int var5 = 0;

            PrintWriter var41;
            for(boolean var6 = false; !var6; var41.flush()) {
               ByteArrayOutputStream var7 = new ByteArrayOutputStream();
               boolean var8 = true;
               String var9 = null;
               String var10 = null;
               String var11 = null;
               HashMap var12 = new HashMap();
               boolean var13 = false;
               int var14 = -1;

               String var23;
               do {
                  int var15 = var3.read(var4, var5, var4.length - var5);
                  if (var15 < 0 || var5 >= var4.length) {
                     var6 = true;
                     break;
                  }

                  var5 += var15;
                  int var16;
                  if (var8) {
                     var16 = 0;

                     label337:
                     for(int var17 = 0; var17 < var5; ++var17) {
                        if (var4[var17] == 10) {
                           ++var16;
                        } else if (var4[var17] != 13) {
                           var16 = 0;
                        }

                        if (var16 == 2) {
                           var7.write(var4, 0, var17);
                           ByteArrayInputStream var18 = new ByteArrayInputStream(var7.toByteArray());
                           BufferedReader var19 = new BufferedReader(new InputStreamReader(var18));
                           String[] var20 = var19.readLine().split(" ");
                           if (var20.length == 3) {
                              var9 = var20[0];
                              var10 = var20[1];
                              var11 = var20[2];
                           } else {
                              var13 = true;
                           }

                           while(true) {
                              String var21 = var19.readLine();
                              if (var21.equals("")) {
                                 if (var12.get("content-length") != null) {
                                    var14 = Integer.parseInt((String)var12.get("content-length"));
                                 }

                                 var7.reset();
                                 System.arraycopy(var4, var17 + 1, var4, 0, var5 - (var17 + 1));
                                 var5 -= var17 + 1;
                                 var8 = false;
                                 break label337;
                              }

                              String[] var22 = var21.split(" ", 2);
                              if (var22.length == 2) {
                                 var23 = var22[0].substring(0, var22[0].length() - 1);
                                 var12.put(var23.toLowerCase(), var22[1]);
                              } else {
                                 var13 = true;
                              }
                           }
                        }
                     }
                  }

                  if (!var8) {
                     var16 = var14 >= 0 && var5 > var14 ? var14 : var5;
                     var7.write(var4, 0, var16);
                     System.arraycopy(var4, var16, var4, 0, var5 - var16);
                     var5 -= var16;
                  }
               } while(var8 || var14 >= 0 && var7.size() < var14);

               if (var6) {
                  break;
               }

               var41 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(var2.getOutputStream())), true);
               boolean var42 = false;
               String var43 = "default";
               if (var12.get("host") != null) {
                  var43 = ((String)var12.get("host")).split(":")[0];
               }

               if (this.routes.get(var43) == null) {
                  var43 = "default";
               }

               logger.info("Received a " + var9 + " request for " + var10 + " (on host '" + var43 + "')");
               if (var9 == null || var10 == null || var11 == null || var43 == null) {
                  this.sendError(var41, 400, "Bad request");
                  var42 = true;
               }

               if (!var42 && !var11.equals("HTTP/1.1") && !var11.equals("HTTP/1.0")) {
                  this.sendError(var41, 505, "Version Not Supported");
                  var42 = true;
               }

               if (!var42 && !var9.equals("GET") && !var9.equals("HEAD") && !var9.equals("PUT") && !var9.equals("POST")) {
                  this.sendError(var41, 501, "Not implemented");
                  var42 = true;
               }

               if (!var42 && var10.contains("..")) {
                  this.sendError(var41, 403, "Forbidden");
                  var42 = true;
               }

               HashMap var44 = new HashMap();
               String var45 = var10;
               if (!var42) {
                  if (var10.indexOf(63) >= 0) {
                     this.parseQueryParams(var10.substring(var10.indexOf(63) + 1), var44);
                     var45 = var10.substring(0, var10.indexOf(63));
                  }

                  if (var12.get("content-type") != null && ((String)var12.get("content-type")).equals("application/x-www-form-urlencoded") && var7.size() > 0) {
                     this.parseQueryParams(new String(var7.toByteArray(), StandardCharsets.UTF_8), var44);
                  }
               }

               RequestImpl var46 = new RequestImpl(var9, var45, var11, var12, var44, (Map)null, (InetSocketAddress)var2.getRemoteSocketAddress(), var7.toByteArray(), this);
               ResponseImpl var47 = new ResponseImpl(var2.getOutputStream(), var9 != null ? var9.equals("HEAD") : false);
               var47.header("Connection", "close");

               for(int var48 = 0; !var42 && var48 < ((Vector)this.routes.get(var43)).size(); ++var48) {
                  Server.RouteEntry var50 = (Server.RouteEntry)((Vector)this.routes.get(var43)).elementAt(var48);
                  HashMap var24 = new HashMap();
                  if ((var50.method.equals(var9) || var9.equals("HEAD") && var50.method.equals("GET")) && this.patternMatches(var50.path, var45, var24)) {
                     var46.setParams(var24);
                     Object var25 = null;

                     try {
                        var25 = var50.route.handle(var46, var47);
                        if (!var47.committed) {
                           if (var25 != null) {
                              var47.body(var25.toString());
                           }

                           int var10002 = var47.body != null ? var47.body.length : 0;
                           var47.header("Content-Length", var10002.makeConcatWithConstants<invokedynamic>(var10002));
                           SessionImpl var26 = var46.session;
                           if (var26 != null) {
                              if (var26.lastAccessedTime() > 0L) {
                                 if (!var46.cookieFound) {
                                    var47.header("Set-Cookie", "SessionID=" + var26.id());
                                 }
                              } else {
                                 var47.header("Set-Cookie", "SessionID=x; Max-Age=0" + var26.id());
                              }
                           }

                           int var10001 = var47.statusCode;
                           logger.debug("Sending a " + var10001 + " response " + (var47.body == null ? "with an empty body" : "(" + var47.body.length + " bytes)"));
                           var47.commit();
                           if (var47.body != null) {
                              var2.getOutputStream().write(var47.body, 0, var47.body.length);
                           }
                        } else {
                           var6 = true;
                        }

                        var2.getOutputStream().flush();
                     } catch (Exception var37) {
                        logger.error("Handler for " + var50.method + " route " + var50.path + " threw an exception:", var37);
                        if (!var47.committed) {
                           this.sendError(var41, 500, "Internal server error");
                        } else {
                           var6 = true;
                        }
                     }

                     var42 = true;
                  }
               }

               if (!var42 && staticFilePath.get(var43) != null) {
                  File var49 = new File((String)staticFilePath.get(var43), var10);
                  if (var49.exists()) {
                     if (var49.canRead()) {
                        if (!var9.equals("GET") && !var9.equals("HEAD")) {
                           this.sendError(var41, 405, "Method not allowed");
                           var42 = true;
                        } else {
                           logger.info("Returning static file " + var49.getAbsolutePath());
                           var23 = "application/octet-stream";
                           if (!var10.endsWith(".jpg") && !var10.endsWith(".jpeg")) {
                              if (var10.endsWith(".txt")) {
                                 var23 = "text/plain";
                              } else if (var10.endsWith(".html")) {
                                 var23 = "text/html";
                              }
                           } else {
                              var23 = "image/jpeg";
                           }

                           long var51 = 0L;
                           long var52 = var49.length();
                           boolean var28 = false;
                           if (var12.get("range") != null) {
                              String[] var29 = ((String)var12.get("range")).split("=");
                              String[] var30 = var29.length > 1 ? var29[1].split("-") : null;
                              if (var29[0].equals("bytes") && var30 != null && var30.length == 2) {
                                 try {
                                    var51 = Long.valueOf(var30[0]);
                                    long var31 = Long.valueOf(var30[1]);
                                    var52 = var31 - var51 + 1L;
                                    var28 = true;
                                 } catch (Exception var36) {
                                 }
                              }
                           }

                           var41.write("HTTP/1.1 " + (var28 ? "206 Range" : "200 OK") + "\r\nContent-Length: " + var52 + "\r\nServer: AndreasServer/1.0\r\nContent-Type: " + var23 + "\r\n");
                           if (var28) {
                              var41.write("Range: bytes=" + var51 + "-" + (var51 + var52 - 1L) + "/" + var49.length() + "\r\n");
                           }

                           var41.write("\r\n");
                           var41.flush();
                           if (var9.equals("GET")) {
                              FileInputStream var53 = new FileInputStream(var49);
                              var53.skip(var51);
                              byte[] var54 = new byte[1024];

                              while(true) {
                                 int var55 = var53.read(var54);
                                 if (var55 <= 0) {
                                    var53.close();
                                    break;
                                 }

                                 int var32 = (int)((long)var55 > var52 ? var52 : (long)var55);
                                 var2.getOutputStream().write(var54, 0, var32);
                                 var52 -= (long)var32;
                              }
                           }

                           var42 = true;
                        }
                     } else {
                        logger.info("Static file " + var49.getAbsolutePath() + " exists, but cannot be read");
                        this.sendError(var41, 403, "Forbidden");
                        var42 = true;
                     }
                  } else {
                     logger.info("No suitable route found, and no static file " + var49.getAbsolutePath() + " exists");
                  }
               }

               if (!var42) {
                  this.sendError(var41, 404, "Not found");
               }
            }

            var2.close();
         } catch (SocketTimeoutException var38) {
            try {
               var2.close();
            } catch (IOException var33) {
            }

            var38.printStackTrace();
         } catch (SocketException var39) {
            try {
               var2.close();
            } catch (IOException var35) {
            }

            var39.printStackTrace();
         } catch (Exception var40) {
            try {
               var2.close();
            } catch (IOException var34) {
            }

            var40.printStackTrace();
         }
      }
   }

   void serverLoop(ServerSocket var1) {
      while(true) {
         try {
            Socket var2 = var1.accept();
            var2.setTcpNoDelay(true);
            this.queue.put(var2);
         } catch (Exception var3) {
            var3.printStackTrace();
         }
      }
   }

   public void run() {
      ServerSocket var1 = null;
      final ServerSocket var2 = null;
      String var3 = "keystore.jks";
      String var4 = "secret";
      Thread.currentThread().setName("main");
      logger.info("Webserver (v1.7 Dec 31 2022) starting on port " + port);

      try {
         var1 = new ServerSocket(port);
         if (this.securePort >= 0) {
            KeyStore var5 = KeyStore.getInstance("JKS");
            var5.load(new FileInputStream(var3), var4.toCharArray());
            KeyManagerFactory var6 = KeyManagerFactory.getInstance("SunX509");
            var6.init(var5, var4.toCharArray());
            SSLContext var7 = SSLContext.getInstance("TLS");
            var7.init(var6.getKeyManagers(), (TrustManager[])null, (SecureRandom)null);
            var2 = var7.getServerSocketFactory().createServerSocket(this.securePort);
         }
      } catch (Exception var8) {
         var8.printStackTrace();
         System.exit(1);
      }

      running = true;

      for(final int var9 = 0; var9 < 100; ++var9) {
         (new Thread("W-" + (var9 < 10 ? "0" : "") + var9) {
            public void run() {
               Server.this.workerThread(var9);
            }
         }).start();
      }

      (new Thread("S-XP") {
         public void run() {
            while(true) {
               try {
                  Thread.sleep(1000L);
               } catch (InterruptedException var6) {
               }

               synchronized(Server.this.sessions) {
                  Iterator var2 = Server.this.sessions.keySet().iterator();

                  while(var2.hasNext()) {
                     String var3 = (String)var2.next();
                     if (((SessionImpl)Server.this.sessions.get(var3)).lastAccessedTime() < System.currentTimeMillis() - (long)(1000 * ((SessionImpl)Server.this.sessions.get(var3)).maxActiveInterval())) {
                        Server.this.sessions.remove(var3);
                     }
                  }
               }
            }
         }
      }).start();
      if (this.securePort >= 0) {
         (new Thread("TTLS") {
            public void run() {
               Server.this.serverLoop(var2);
            }
         }).start();
      }

      this.serverLoop(var1);
   }

   class RouteEntry {
      String method;
      String path;
      Route route;

      RouteEntry(String param2, String param3, Route param4) {
         this.method = var2;
         this.path = var3;
         this.route = var4;
      }
   }

   public static class staticFiles {
      public static void location(String var0) {
         Server.launchIfNecessary();
         Server.instance.setStaticLocation(var0);
      }
   }
}
