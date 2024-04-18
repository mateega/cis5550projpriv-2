package cis5550.jobs;

import cis5550.flame.FlameContext;
import cis5550.flame.FlamePair;
import cis5550.flame.FlamePairRDD;
import cis5550.flame.FlameRDD;
import cis5550.jobs.PageRank.1;
import cis5550.jobs.PageRank.2;
import cis5550.jobs.PageRank.3;
import cis5550.jobs.PageRank.4;
import cis5550.jobs.PageRank.5;
import cis5550.kvs.KVSClient;
import cis5550.kvs.Row;
import cis5550.tools.Hasher;
import java.lang.invoke.SerializedLambda;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageRank {
   static final double DECAY_FACTOR = 0.85D;

   public static void run(FlameContext var0, String[] var1) {
      double var2 = Double.parseDouble(var1[0]);

      try {
         FlameRDD var4 = var0.fromTable("pt-crawl", (var0x) -> {
            String var1 = var0x.get("url").toString();
            String var2 = var0x.get("page").toString();
            System.out.println("url: " + var1);
            String var3 = extractAndNormalizeLinks(var2, var1);
            String var4 = var1 + ",1.0,1.0," + var3;
            return var4;
         });
         FlamePairRDD var5 = var4.mapToPair((var0x) -> {
            String[] var1 = var0x.split(",", 2);
            return new FlamePair(Hasher.hash(var1[0]), var1[1]);
         });
         var5.saveAsTable("pt-stateTable");

         double var6;
         FlamePairRDD var8;
         FlamePairRDD var11;
         do {
            var8 = var5.flatMapToPair(new 1());
            FlamePairRDD var9 = var8.foldByKey("0.0", (var0x, var1x) -> {
               double var2 = Double.parseDouble(var0x) + Double.parseDouble(var1x);
               return Double.toString(var2);
            });
            FlamePairRDD var10 = var5.join(var9);
            var11 = var10.flatMapToPair(new 2());
            FlamePairRDD var12 = var11.flatMapToPair(new 3());
            var6 = var12.foldByKey("0.0", new 4()).collect().stream().mapToDouble((var0x) -> {
               return Double.parseDouble(var0x._2());
            }).max().orElse(Double.MAX_VALUE);
            var5 = var11;
         } while(var6 > var2);

         var8 = var11.foldByKey("", new 5());
         KVSClient var20 = var0.getKVS();
         List var17 = var8.collect();
         Iterator var18 = var17.iterator();

         while(var18.hasNext()) {
            FlamePair var19 = (FlamePair)var18.next();
            String var13 = var19._1();
            String var14 = var19._2();
            Row var15 = new Row(var13);
            var15.put("rank", var14);
            var20.putRow("pt-pageranks", var15);
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

   }

   public static String extractAndNormalizeLinks(String var0, String var1) {
      ArrayList var2 = new ArrayList();
      Pattern var3 = Pattern.compile("<a\\s+(?:[^>]*?\\s+)?href=\"([^\"]*)\"", 2);
      Matcher var4 = var3.matcher(var0);

      while(var4.find()) {
         String var5 = var4.group(1);
         System.out.println("link: " + var5);
         String var6 = Crawler.normalizeUrl(var5, var1);
         if (var6 != null) {
            var2.add(var6);
         }
      }

      return String.join(";", var2);
   }

   // $FF: synthetic method
   private static Object $deserializeLambda$(SerializedLambda var0) {
      String var1 = var0.getImplMethodName();
      byte var2 = -1;
      switch(var1.hashCode()) {
      case -1075860514:
         if (var1.equals("lambda$run$7eef3cea$1")) {
            var2 = 0;
         }
         break;
      case 353859658:
         if (var1.equals("lambda$run$40a86e16$1")) {
            var2 = 2;
         }
         break;
      case 1885011276:
         if (var1.equals("lambda$run$ee491346$1")) {
            var2 = 1;
         }
      }

      switch(var2) {
      case 0:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/FlameRDD$StringToPair") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Ljava/lang/String;)Lcis5550/flame/FlamePair;") && var0.getImplClass().equals("cis5550/jobs/PageRank") && var0.getImplMethodSignature().equals("(Ljava/lang/String;)Lcis5550/flame/FlamePair;")) {
            return (var0x) -> {
               String[] var1 = var0x.split(",", 2);
               return new FlamePair(Hasher.hash(var1[0]), var1[1]);
            };
         }
         break;
      case 1:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/FlameContext$RowToString") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;)Ljava/lang/String;") && var0.getImplClass().equals("cis5550/jobs/PageRank") && var0.getImplMethodSignature().equals("(Lcis5550/kvs/Row;)Ljava/lang/String;")) {
            return (var0x) -> {
               String var1 = var0x.get("url").toString();
               String var2 = var0x.get("page").toString();
               System.out.println("url: " + var1);
               String var3 = extractAndNormalizeLinks(var2, var1);
               String var4 = var1 + ",1.0,1.0," + var3;
               return var4;
            };
         }
         break;
      case 2:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/FlamePairRDD$TwoStringsToString") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;") && var0.getImplClass().equals("cis5550/jobs/PageRank") && var0.getImplMethodSignature().equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")) {
            return (var0x, var1x) -> {
               double var2 = Double.parseDouble(var0x) + Double.parseDouble(var1x);
               return Double.toString(var2);
            };
         }
      }

      throw new IllegalArgumentException("Invalid lambda deserialization");
   }
}
