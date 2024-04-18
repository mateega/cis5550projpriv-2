package cis5550.kvs;

import cis5550.webserver.Server;

class Coordinator extends cis5550.generic.Coordinator {
   public static void main(String[] var0) {
      if (var0.length != 1) {
         System.err.println("Syntax: Coordinator <port>");
         System.exit(1);
      }

      Server.port(Integer.parseInt(var0[0]));
      registerRoutes();
      Server.get("/", (var0x, var1) -> {
         return "<html><head><title>KVS Coordinator</title></head><body><h3>KVS Coordinator</h3>\n" + clientTable() + "</body></html>";
      });
      Server.get("/version", (var0x, var1) -> {
         return "v1.3 Oct 28 2022";
      });
   }
}
