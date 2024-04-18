package cis5550.flame;

import cis5550.kvs.KVS;
import cis5550.kvs.KVSClient;
import cis5550.kvs.Row;
import cis5550.tools.Hasher;
import cis5550.tools.Serializer;
import cis5550.webserver.Request;
import cis5550.webserver.Server;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.util.Iterator;
import java.util.Random;

class Worker extends cis5550.generic.Worker {
   static void log(String var0) {
      System.out.println(var0);
   }

   static String performWithIterator(String var0, Request var1, Worker.IteratorTwoStringsAndKVSToString var2) throws Exception {
      String var3 = var1.queryParams("inputTable");
      String var4 = var1.queryParams("outputTable");
      String var5 = var1.queryParams("inputTable2");
      String var6 = var1.queryParams("kvsCoordinator");
      String var7 = var1.queryParams("fromKey");
      String var8 = var1.queryParams("toKeyExclusive");
      Coordinator.kvs = new KVSClient(var6);
      String var9 = var2.op(Coordinator.kvs.scan(var3, var7, var8), var4, var5, Coordinator.kvs);
      return var9;
   }

   static String perform(String var0, Request var1, Worker.RowTwoStringsAndKVSToInt var2) throws Exception {
      return performWithIterator(var0, var1, (var1x, var2x, var3, var4) -> {
         int var5 = 0;

         int var6;
         Row var7;
         for(var6 = 0; var1x.hasNext(); var6 += var2.op(var7, var2x, var3, var4)) {
            var7 = (Row)var1x.next();
            ++var5;
         }

         return "OK - " + var5 + " keys in, " + var6 + " keys out";
      });
   }

   public static void main(String[] var0) {
      if (var0.length != 2) {
         System.err.println("Syntax: Worker <port> <coordinatorIP:port>");
         System.exit(1);
      }

      int var1 = Integer.parseInt(var0[0]);
      String var2 = var0[1];
      startPingThread(var2, var1.makeConcatWithConstants<invokedynamic>(var1), var1);
      File var3 = new File("__worker" + var1 + "-current.jar");
      Server.port(var1);
      Server.post("/useJAR", (var1x, var2x) -> {
         FileOutputStream var3x = new FileOutputStream(var3);
         var3x.write(var1x.bodyAsBytes());
         var3x.close();
         return "OK";
      });
      Server.post("/flatMap", (var1x, var2x) -> {
         FlameRDD.StringToIterable var3x = (FlameRDD.StringToIterable)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("flatMap", var1x, (var1xx, var2xx, var3xx, var4) -> {
            Iterator var5 = var3x.op(var1xx.get("value")).iterator();
            int var6 = 0;

            while(var5.hasNext()) {
               String var7 = (String)var5.next();
               String var10002 = var1xx.key();
               var4.put(var2xx, Hasher.hash(var10002 + var6++), "value", var7.getBytes());
            }

            return var6;
         });
      });
      Server.post("/mapToPair", (var1x, var2x) -> {
         FlameRDD.StringToPair var3x = (FlameRDD.StringToPair)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("mapToPair", var1x, (var1xx, var2xx, var3xx, var4) -> {
            FlamePair var5 = var3x.op(var1xx.get("value"));
            if (var5 != null) {
               var4.put(var2xx, var5._1(), var1xx.key(), var5._2().getBytes());
            }

            return 1;
         });
      });
      Server.post("/foldByKey", (var1x, var2x) -> {
         FlamePairRDD.TwoStringsToString var3x = (FlamePairRDD.TwoStringsToString)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("foldByKey", var1x, (var2xx, var3xx, var4, var5) -> {
            String var6 = var1x.queryParams("zero");
            int var7 = 0;

            for(Iterator var8 = var2xx.columns().iterator(); var8.hasNext(); ++var7) {
               String var9 = (String)var8.next();
               var6 = var3x.op(var6, var2xx.get(var9));
            }

            var5.put(var3xx, var2xx.key(), "acc", var6.getBytes());
            return var7;
         });
      });
      Server.post("/filter", (var1x, var2x) -> {
         FlameRDD.StringToBoolean var3x = (FlameRDD.StringToBoolean)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("filter", var1x, (var1xx, var2xx, var3xx, var4) -> {
            boolean var5 = var3x.op(var1xx.get("value"));
            if (var5) {
               var4.put(var2xx, var1xx.key(), "value", var1xx.get("value").getBytes());
            }

            return var5 ? 1 : 0;
         });
      });
      Server.post("/mapPartitions", (var1x, var2x) -> {
         FlameRDD.IteratorToIterator var3x = (FlameRDD.IteratorToIterator)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return performWithIterator("mapPartitions", var1x, (var2xx, var3xx, var4, var5) -> {
            String var6 = var1x.queryParams("part");
            Iterator var7 = var3x.op(new Iterator<String>() {
               public String next() {
                  return ((Row)var2.next()).get("value");
               }

               public boolean hasNext() {
                  return var2.hasNext();
               }
            });

            int var8;
            for(var8 = 0; var7.hasNext(); ++var8) {
               String var9 = (String)var7.next();
               var5.put(var3xx, Hasher.hash(var6), "value", var9.getBytes());
            }

            return "OK - " + var8 + " keys written";
         });
      });
      Server.post("/cogroup", (var0x, var1x) -> {
         String var2 = var0x.queryParams("inputTable");
         String var3 = var0x.queryParams("outputTable");
         String var4 = var0x.queryParams("inputTable2");
         String var5 = var0x.queryParams("kvsCoordinator");
         String var6 = var0x.queryParams("fromKey");
         String var7 = var0x.queryParams("toKeyExclusive");
         KVSClient var8 = new KVSClient(var5);
         Iterator var9 = var8.scan(var2, var6, var7);

         int var10;
         for(var10 = 0; var9.hasNext(); ++var10) {
            Row var11 = (Row)var9.next();
            String var12 = "[";

            String var14;
            for(Iterator var13 = var11.columns().iterator(); var13.hasNext(); var12 = var12 + (var12.equals("[") ? "" : ",") + var11.get(var14)) {
               var14 = (String)var13.next();
            }

            var12 = var12 + "]";
            Row var19 = var8.getRow(var4, var11.key());
            var14 = "[";
            String var16;
            if (var19 != null) {
               for(Iterator var15 = var19.columns().iterator(); var15.hasNext(); var14 = var14 + (var14.equals("[") ? "" : ",") + var19.get(var16)) {
                  var16 = (String)var15.next();
               }
            }

            var14 = var14 + "]";
            var8.put(var3, var11.key(), "value", (var12 + "," + var14).getBytes());
         }

         for(Iterator var17 = var8.scan(var4, var6, var7); var17.hasNext(); ++var10) {
            Row var18 = (Row)var17.next();
            String var20 = "[";

            String var22;
            for(Iterator var21 = var18.columns().iterator(); var21.hasNext(); var20 = var20 + (var20.equals("[") ? "" : ",") + var18.get(var22)) {
               var22 = (String)var21.next();
            }

            var20 = var20 + "]";
            Row var23 = var8.getRow(var2, var18.key());
            if (var23 == null) {
               var8.put(var3, var18.key(), "value", ("[]," + var20).getBytes());
            }
         }

         return "OK - " + var10 + " keys written";
      });
      Server.post("/intersection", (var0x, var1x) -> {
         String var2 = var0x.queryParams("inputTable1");
         String var3 = var0x.queryParams("outputTable");
         String var4 = var0x.queryParams("inputTable2");
         String var5 = var0x.queryParams("kvsCoordinator");
         String var6 = var0x.queryParams("fromKey");
         String var7 = var0x.queryParams("toKeyExclusive");
         String var8 = var0x.queryParams("tempTable");
         KVSClient var9 = new KVSClient(var5);
         Iterator var10 = var9.scan(var2, var6, var7);

         while(var10.hasNext()) {
            var9.put(var8, ((Row)var10.next()).get("value"), "in1", "1");
         }

         Iterator var11 = var9.scan(var4, var6, var7);

         while(var11.hasNext()) {
            var9.put(var8, ((Row)var11.next()).get("value"), "in2", "1");
         }

         Iterator var12 = var9.scan(var8, var6, var7);

         while(var12.hasNext()) {
            Row var13 = (Row)var12.next();
            if (var13.columns().size() > 1) {
               var9.put(var3, var13.key(), "value", var13.key());
            }
         }

         return "OK";
      });
      Server.post("/sample", (var0x, var1x) -> {
         String var2 = var0x.queryParams("inputTable");
         String var3 = var0x.queryParams("outputTable");
         String var4 = var0x.queryParams("kvsCoordinator");
         String var5 = var0x.queryParams("fromKey");
         String var6 = var0x.queryParams("toKeyExclusive");
         double var7 = Double.valueOf(var0x.queryParams("fraction"));
         KVSClient var9 = new KVSClient(var4);
         Random var10 = new Random();
         Iterator var11 = var9.scan(var2, var5, var6);

         while(var11.hasNext()) {
            Row var12 = (Row)var11.next();
            if (var10.nextDouble() < var7) {
               var9.put(var3, var12.key(), "value", var12.get("value"));
            }
         }

         return "OK";
      });
      Server.post("/groupBy", (var1x, var2x) -> {
         String var3x = var1x.queryParams("inputTable");
         String var4 = var1x.queryParams("outputTable");
         String var5 = var1x.queryParams("kvsCoordinator");
         String var6 = var1x.queryParams("fromKey");
         String var7 = var1x.queryParams("toKeyExclusive");
         String var8 = var1x.queryParams("tempTable");
         FlameRDD.StringToString var9 = (FlameRDD.StringToString)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         KVSClient var10 = new KVSClient(var5);
         Iterator var11 = var10.scan(var3x, var6, var7);

         while(var11.hasNext()) {
            Row var12 = (Row)var11.next();
            String var13 = var9.op(var12.get("value"));
            var10.put(var8, var13, var12.get("value"), "1");
         }

         Iterator var17 = var10.scan(var8, var6, var7);

         while(var17.hasNext()) {
            Row var18 = (Row)var17.next();
            String var14 = "";

            String var16;
            for(Iterator var15 = var18.columns().iterator(); var15.hasNext(); var14 = var14 + (var14.equals("") ? "" : ",") + var16) {
               var16 = (String)var15.next();
            }

            var10.put(var4, var18.key(), "value", var14);
         }

         return "OK";
      });
      Server.post("/fromTable", (var1x, var2x) -> {
         FlameContext.RowToString var3x = (FlameContext.RowToString)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("fromTable", var1x, (var1xx, var2xx, var3xx, var4) -> {
            String var5 = var3x.op(var1xx);
            if (var5 != null) {
               var4.put(var2xx, var1xx.key(), "value", var5.getBytes());
            }

            return 1;
         });
      });
      Server.post("/flatMapToPair", (var1x, var2x) -> {
         FlameRDD.StringToPairIterable var3x = (FlameRDD.StringToPairIterable)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("flatMapToPair", var1x, (var1xx, var2xx, var3xx, var4) -> {
            Iterator var5 = var3x.op(var1xx.get("value")).iterator();
            int var6 = 0;

            while(var5.hasNext()) {
               FlamePair var7 = (FlamePair)var5.next();
               String var10002 = var7._1();
               String var10003 = var1xx.key();
               var4.put(var2xx, var10002, var10003 + "-" + var6++, var7._2().getBytes());
            }

            return var6;
         });
      });
      Server.post("/pairFlatMapToPair", (var1x, var2x) -> {
         FlamePairRDD.PairToPairIterable var3x = (FlamePairRDD.PairToPairIterable)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("pairFlatMapToPair", var1x, (var1xx, var2xx, var3xx, var4) -> {
            int var5 = 0;
            Iterator var6 = var1xx.columns().iterator();

            while(var6.hasNext()) {
               String var7 = (String)var6.next();
               Iterator var8 = var3x.op(new FlamePair(var1xx.key(), var1xx.get(var7))).iterator();

               while(var8.hasNext()) {
                  FlamePair var9 = (FlamePair)var8.next();
                  String var10002 = var9._1();
                  String var10003 = var1xx.key();
                  var4.put(var2xx, var10002, var10003 + "-" + var5++, var9._2().getBytes());
               }
            }

            return var5;
         });
      });
      Server.post("/pairFlatMap", (var1x, var2x) -> {
         FlamePairRDD.PairToStringIterable var3x = (FlamePairRDD.PairToStringIterable)Serializer.byteArrayToObject(var1x.bodyAsBytes(), var3);
         return perform("pairFlatMap", var1x, (var1xx, var2xx, var3xx, var4) -> {
            int var5 = 0;
            Iterator var6 = var1xx.columns().iterator();

            while(true) {
               String var7;
               Iterable var8;
               do {
                  if (!var6.hasNext()) {
                     return var5;
                  }

                  var7 = (String)var6.next();
                  var8 = var3x.op(new FlamePair(var1xx.key(), var1xx.get(var7)));
               } while(var8 == null);

               Iterator var9 = var8.iterator();

               while(var9.hasNext()) {
                  String var10 = (String)var9.next();
                  String var10002 = var1xx.key();
                  var4.put(var2xx, Hasher.hash(var10002 + "||" + var7 + "||" + var5++), "value", var10.getBytes());
               }
            }
         });
      });
      Server.post("/join", (var0x, var1x) -> {
         return perform("join", var0x, (var0xx, var1xx, var2x, var3) -> {
            int var4 = 0;
            Iterator var5 = var0xx.columns().iterator();

            while(true) {
               String var6;
               Row var7;
               do {
                  if (!var5.hasNext()) {
                     return var4;
                  }

                  var6 = (String)var5.next();
                  var7 = var3.getRow(var2x, var0xx.key());
               } while(var7 == null);

               for(Iterator var8 = var7.columns().iterator(); var8.hasNext(); ++var4) {
                  String var9 = (String)var8.next();
                  String var10002 = var0xx.key();
                  String var10003 = var6 + "||" + var9;
                  String var10004 = var0xx.get(var6);
                  var3.put(var1xx, var10002, var10003, (var10004 + "," + var7.get(var9)).getBytes());
               }
            }
         });
      });
      Server.post("/distinct", (var0x, var1x) -> {
         return perform("distinct", var0x, (var0xx, var1xx, var2x, var3) -> {
            var3.put(var1xx, var0xx.get("value"), "value", var0xx.getBytes("value"));
            return 1;
         });
      });
      Server.post("/fold", (var2x, var3x) -> {
         FlamePairRDD.TwoStringsToString var4 = (FlamePairRDD.TwoStringsToString)Serializer.byteArrayToObject(var2x.bodyAsBytes(), var3);
         String var5 = performWithIterator("fold", var2x, (var2xx, var3xx, var4x, var5x) -> {
            String var6;
            for(var6 = var2x.queryParams("zero"); var2xx.hasNext(); var6 = var4.op(var6, ((Row)var2xx.next()).get("value"))) {
            }

            return var6;
         });
         (new KVSClient(var2x.queryParams("kvsCoordinator"))).put(var2x.queryParams("outputTable"), var1 + "-" + System.nanoTime(), "value", var5.getBytes());
         return "OK";
      });
      Server.get("/version", (var0x, var1x) -> {
         return "v1.3a Nov 10 2022";
      });
   }

   // $FF: synthetic method
   private static Object $deserializeLambda$(SerializedLambda var0) {
      String var1 = var0.getImplMethodName();
      byte var2 = -1;
      switch(var1.hashCode()) {
      case -1895154873:
         if (var1.equals("lambda$perform$bd90f3e9$1")) {
            var2 = 4;
         }
         break;
      case -1777289064:
         if (var1.equals("lambda$main$8751840$1")) {
            var2 = 2;
         }
         break;
      case -1718846032:
         if (var1.equals("lambda$main$c0c50d57$1")) {
            var2 = 12;
         }
         break;
      case -954193677:
         if (var1.equals("lambda$main$2e24c4b4$1")) {
            var2 = 6;
         }
         break;
      case -580306644:
         if (var1.equals("lambda$main$db715bf2$1")) {
            var2 = 1;
         }
         break;
      case -210039728:
         if (var1.equals("lambda$main$da0d0ec2$1")) {
            var2 = 3;
         }
         break;
      case 281992889:
         if (var1.equals("lambda$main$ce5750cb$1")) {
            var2 = 8;
         }
         break;
      case 630460502:
         if (var1.equals("lambda$main$b96c8670$1")) {
            var2 = 0;
         }
         break;
      case 693964368:
         if (var1.equals("lambda$main$4ffe0e4c$1")) {
            var2 = 5;
         }
         break;
      case 940105617:
         if (var1.equals("lambda$main$b3e3298$1")) {
            var2 = 10;
         }
         break;
      case 940105618:
         if (var1.equals("lambda$main$b3e3298$2")) {
            var2 = 11;
         }
         break;
      case 1616810439:
         if (var1.equals("lambda$main$7674f303$1")) {
            var2 = 9;
         }
         break;
      case 1902009037:
         if (var1.equals("lambda$main$1f66bdfa$1")) {
            var2 = 7;
         }
      }

      switch(var2) {
      case 0:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/webserver/Request;Lcis5550/flame/FlamePairRDD$TwoStringsToString;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var2xx, var3xx, var4, var5) -> {
               String var6 = var1x.queryParams("zero");
               int var7 = 0;

               for(Iterator var8 = var2xx.columns().iterator(); var8.hasNext(); ++var7) {
                  String var9 = (String)var8.next();
                  var6 = var3x.op(var6, var2xx.get(var9));
               }

               var5.put(var3xx, var2xx.key(), "acc", var6.getBytes());
               return var7;
            };
         }
         break;
      case 1:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/FlameRDD$StringToBoolean;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var1xx, var2xx, var3xx, var4) -> {
               boolean var5 = var3x.op(var1xx.get("value"));
               if (var5) {
                  var4.put(var2xx, var1xx.key(), "value", var1xx.get("value").getBytes());
               }

               return var5 ? 1 : 0;
            };
         }
         break;
      case 2:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/FlamePairRDD$PairToStringIterable;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var1xx, var2xx, var3xx, var4) -> {
               int var5 = 0;
               Iterator var6 = var1xx.columns().iterator();

               while(true) {
                  String var7;
                  Iterable var8;
                  do {
                     if (!var6.hasNext()) {
                        return var5;
                     }

                     var7 = (String)var6.next();
                     var8 = var3x.op(new FlamePair(var1xx.key(), var1xx.get(var7)));
                  } while(var8 == null);

                  Iterator var9 = var8.iterator();

                  while(var9.hasNext()) {
                     String var10 = (String)var9.next();
                     String var10002 = var1xx.key();
                     var4.put(var2xx, Hasher.hash(var10002 + "||" + var7 + "||" + var5++), "value", var10.getBytes());
                  }
               }
            };
         }
         break;
      case 3:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$IteratorTwoStringsAndKVSToString") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Ljava/util/Iterator;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)Ljava/lang/String;") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/webserver/Request;Lcis5550/flame/FlamePairRDD$TwoStringsToString;Ljava/util/Iterator;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)Ljava/lang/String;")) {
            return (var2xx, var3xx, var4x, var5x) -> {
               String var6;
               for(var6 = var2x.queryParams("zero"); var2xx.hasNext(); var6 = var4.op(var6, ((Row)var2xx.next()).get("value"))) {
               }

               return var6;
            };
         }
         break;
      case 4:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$IteratorTwoStringsAndKVSToString") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Ljava/util/Iterator;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)Ljava/lang/String;") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/Worker$RowTwoStringsAndKVSToInt;Ljava/util/Iterator;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)Ljava/lang/String;")) {
            return (var1x, var2x, var3, var4) -> {
               int var5 = 0;

               int var6;
               Row var7;
               for(var6 = 0; var1x.hasNext(); var6 += var2.op(var7, var2x, var3, var4)) {
                  var7 = (Row)var1x.next();
                  ++var5;
               }

               return "OK - " + var5 + " keys in, " + var6 + " keys out";
            };
         }
         break;
      case 5:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/FlameRDD$StringToPair;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var1xx, var2xx, var3xx, var4) -> {
               FlamePair var5 = var3x.op(var1xx.get("value"));
               if (var5 != null) {
                  var4.put(var2xx, var5._1(), var1xx.key(), var5._2().getBytes());
               }

               return 1;
            };
         }
         break;
      case 6:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/FlameRDD$StringToIterable;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var1xx, var2xx, var3xx, var4) -> {
               Iterator var5 = var3x.op(var1xx.get("value")).iterator();
               int var6 = 0;

               while(var5.hasNext()) {
                  String var7 = (String)var5.next();
                  String var10002 = var1xx.key();
                  var4.put(var2xx, Hasher.hash(var10002 + var6++), "value", var7.getBytes());
               }

               return var6;
            };
         }
         break;
      case 7:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/FlameRDD$StringToPairIterable;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var1xx, var2xx, var3xx, var4) -> {
               Iterator var5 = var3x.op(var1xx.get("value")).iterator();
               int var6 = 0;

               while(var5.hasNext()) {
                  FlamePair var7 = (FlamePair)var5.next();
                  String var10002 = var7._1();
                  String var10003 = var1xx.key();
                  var4.put(var2xx, var10002, var10003 + "-" + var6++, var7._2().getBytes());
               }

               return var6;
            };
         }
         break;
      case 8:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/FlameContext$RowToString;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var1xx, var2xx, var3xx, var4) -> {
               String var5 = var3x.op(var1xx);
               if (var5 != null) {
                  var4.put(var2xx, var1xx.key(), "value", var5.getBytes());
               }

               return 1;
            };
         }
         break;
      case 9:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$IteratorTwoStringsAndKVSToString") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Ljava/util/Iterator;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)Ljava/lang/String;") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/webserver/Request;Lcis5550/flame/FlameRDD$IteratorToIterator;Ljava/util/Iterator;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)Ljava/lang/String;")) {
            return (var2xx, var3xx, var4, var5) -> {
               String var6 = var1x.queryParams("part");
               Iterator var7 = var3x.op(new Iterator<String>() {
                  public String next() {
                     return ((Row)var2.next()).get("value");
                  }

                  public boolean hasNext() {
                     return var2.hasNext();
                  }
               });

               int var8;
               for(var8 = 0; var7.hasNext(); ++var8) {
                  String var9 = (String)var7.next();
                  var5.put(var3xx, Hasher.hash(var6), "value", var9.getBytes());
               }

               return "OK - " + var8 + " keys written";
            };
         }
         break;
      case 10:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var0xx, var1xx, var2x, var3) -> {
               int var4 = 0;
               Iterator var5 = var0xx.columns().iterator();

               while(true) {
                  String var6;
                  Row var7;
                  do {
                     if (!var5.hasNext()) {
                        return var4;
                     }

                     var6 = (String)var5.next();
                     var7 = var3.getRow(var2x, var0xx.key());
                  } while(var7 == null);

                  for(Iterator var8 = var7.columns().iterator(); var8.hasNext(); ++var4) {
                     String var9 = (String)var8.next();
                     String var10002 = var0xx.key();
                     String var10003 = var6 + "||" + var9;
                     String var10004 = var0xx.get(var6);
                     var3.put(var1xx, var10002, var10003, (var10004 + "," + var7.get(var9)).getBytes());
                  }
               }
            };
         }
         break;
      case 11:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var0xx, var1xx, var2x, var3) -> {
               var3.put(var1xx, var0xx.get("value"), "value", var0xx.getBytes("value"));
               return 1;
            };
         }
         break;
      case 12:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/Worker$RowTwoStringsAndKVSToInt") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I") && var0.getImplClass().equals("cis5550/flame/Worker") && var0.getImplMethodSignature().equals("(Lcis5550/flame/FlamePairRDD$PairToPairIterable;Lcis5550/kvs/Row;Ljava/lang/String;Ljava/lang/String;Lcis5550/kvs/KVS;)I")) {
            return (var1xx, var2xx, var3xx, var4) -> {
               int var5 = 0;
               Iterator var6 = var1xx.columns().iterator();

               while(var6.hasNext()) {
                  String var7 = (String)var6.next();
                  Iterator var8 = var3x.op(new FlamePair(var1xx.key(), var1xx.get(var7))).iterator();

                  while(var8.hasNext()) {
                     FlamePair var9 = (FlamePair)var8.next();
                     String var10002 = var9._1();
                     String var10003 = var1xx.key();
                     var4.put(var2xx, var10002, var10003 + "-" + var5++, var9._2().getBytes());
                  }
               }

               return var5;
            };
         }
      }

      throw new IllegalArgumentException("Invalid lambda deserialization");
   }

   public interface IteratorTwoStringsAndKVSToString extends Serializable {
      String op(Iterator<Row> var1, String var2, String var3, KVS var4) throws Exception;
   }

   public interface RowTwoStringsAndKVSToInt extends Serializable {
      int op(Row var1, String var2, String var3, KVS var4) throws Exception;
   }
}
