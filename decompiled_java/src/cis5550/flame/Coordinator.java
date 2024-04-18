package cis5550.flame;

import cis5550.flame.FlameContextImpl;
import cis5550.kvs.KVSClient;
import cis5550.tools.HTTP;
import cis5550.tools.Loader;
import cis5550.tools.Logger;
import cis5550.webserver.Server;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Vector;

class Coordinator extends cis5550.generic.Coordinator {
   private static final Logger logger = Logger.getLogger(Coordinator.class);
   private static final String version = "v1.5 Jan 1 2023";
   static int nextJobID = 1;
   static HashMap<String, String> outputs;
   public static KVSClient kvs;

   public static void main(String[] var0) {
      if (var0.length != 2) {
         System.err.println("Syntax: Coordinator <port> <kvsCoordinator>");
         System.exit(1);
      }

      int var1 = Integer.valueOf(var0[0]);
      kvs = new KVSClient(var0[1]);
      outputs = new HashMap();
      logger.info("Flame coordinator (v1.5 Jan 1 2023) starting on port " + var1);
      Server.port(var1);
      registerRoutes();
      Server.get("/", (var0x, var1x) -> {
         var1x.type("text/html");
         return "<html><head><title>Flame coordinator</title></head><body><h3>Flame Coordinator</h3>\n" + clientTable() + "</body></html>";
      });
      Server.post("/submit", (var0x, var1x) -> {
         String var2 = var0x.queryParams("class");
         logger.info("New job submitted; main class is " + var2);
         if (var2 == null) {
            var1x.status(400, "Bad request");
            return "Missing class name (parameter 'class')";
         } else {
            Vector var3 = new Vector();

            for(int var4 = 1; var0x.queryParams("arg" + var4) != null; ++var4) {
               var3.add(URLDecoder.decode(var0x.queryParams("arg" + var4), "UTF-8"));
            }

            Thread[] var16 = new Thread[getWorkers().size()];
            final String[] var5 = new String[getWorkers().size()];

            final int var6;
            final String var7;
            for(var6 = 0; var6 < getWorkers().size(); ++var6) {
               String var10000 = (String)getWorkers().elementAt(var6);
               var7 = "http://" + var10000 + "/useJAR";
               var16[var6] = new Thread("JAR upload #" + (var6 + 1)) {
                  public void run() {
                     try {
                        var5[var6] = new String(HTTP.doRequest("POST", var7, var0.bodyAsBytes()).body());
                     } catch (Exception var2) {
                        var5[var6] = "Exception: " + String.valueOf(var2);
                        var2.printStackTrace();
                     }

                  }
               };
               var16[var6].start();
            }

            for(var6 = 0; var6 < var16.length; ++var6) {
               try {
                  var16[var6].join();
                  logger.debug("JAR upload #" + (var6 + 1) + ": " + var5[var6]);
               } catch (InterruptedException var15) {
               }
            }

            var6 = nextJobID++;
            var7 = "job-" + var6 + ".jar";
            File var8 = new File(var7);
            FileOutputStream var9 = new FileOutputStream(var8);
            var9.write(var0x.bodyAsBytes());
            var9.close();
            outputs.remove(var7);

            try {
               Loader.invokeRunMethod(var8, var2, new FlameContextImpl(var7), var3);
            } catch (IllegalAccessException var12) {
               var1x.status(400, "Bad request");
               return "Double-check that the class " + var2 + " contains a public static run(FlameContext, String[]) method, and that the class itself is public!";
            } catch (NoSuchMethodException var13) {
               var1x.status(400, "Bad request");
               return "Double-check that the class " + var2 + " contains a public static run(FlameContext, String[]) method";
            } catch (InvocationTargetException var14) {
               logger.error("The job threw an exception, which was:", var14.getCause());
               StringWriter var11 = new StringWriter();
               var14.getCause().printStackTrace(new PrintWriter(var11));
               var1x.status(500, "Job threw an exception");
               return var11.toString();
            }

            return outputs.containsKey(var7) ? outputs.get(var7) : "Job finished, but produced no output";
         }
      });
      Server.get("/version", (var0x, var1x) -> {
         return "v1.2 Oct 28 2022";
      });
   }
}
