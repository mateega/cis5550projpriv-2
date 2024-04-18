package cis5550.jobs;

import cis5550.flame.FlameContext;
import cis5550.flame.FlamePair;
import cis5550.flame.FlamePairRDD;
import cis5550.flame.FlameRDD;
import java.lang.invoke.SerializedLambda;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Indexer {
   public static void run(FlameContext var0, String[] var1) {
      try {
         FlameRDD var2 = var0.fromTable("pt-crawl", (var0x) -> {
            String var1 = var0x.get("url").toString();
            String var2 = var0x.get("page").toString();
            return var1 + "," + var2;
         });
         FlamePairRDD var3 = var2.mapToPair((var0x) -> {
            String[] var1 = var0x.split(",", 2);
            System.out.println("row: " + var0x + "; value: " + var1[0] + "," + var1[1]);
            return new FlamePair(var1[0], var1[1]);
         });
         Pattern var4 = Pattern.compile("<[^>]+>");
         Pattern var5 = Pattern.compile("[.,:;!?’\"()-]");
         FlamePairRDD var6 = var3.flatMapToPair((var2x) -> {
            String var3 = var2x._1();
            String var4x = var2x._2();
            var4x = var4.matcher(var4x).replaceAll(" ");
            var4x = var5.matcher(var4x).replaceAll(" ");
            String[] var5x = var4x.toLowerCase().split("\\s+");
            return (Iterable)Arrays.stream(var5x).filter((var0) -> {
               return !var0.isEmpty();
            }).map((var1) -> {
               return new FlamePair(var1, var3);
            }).collect(Collectors.toList());
         });
         FlamePairRDD var7 = var6.foldByKey("", (var0x, var1x) -> {
            if (var0x.isEmpty()) {
               return var1x;
            } else {
               HashSet var2 = new HashSet(Arrays.asList(var0x.split(",")));
               var2.add(var1x);
               return String.join(",", var2);
            }
         });
         var7.saveAsTable("pt-index");
      } catch (Exception var8) {
         var8.printStackTrace();
      }

   }

   // $FF: synthetic method
   private static Object $deserializeLambda$(SerializedLambda var0) {
      String var1 = var0.getImplMethodName();
      byte var2 = -1;
      switch(var1.hashCode()) {
      case -1266211389:
         if (var1.equals("lambda$run$69f4cab3$1")) {
            var2 = 0;
         }
         break;
      case 719096722:
         if (var1.equals("lambda$run$7224210c$1")) {
            var2 = 3;
         }
         break;
      case 1007342471:
         if (var1.equals("lambda$run$bd794455$1")) {
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
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/FlamePairRDD$PairToPairIterable") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/flame/FlamePair;)Ljava/lang/Iterable;") && var0.getImplClass().equals("cis5550/jobs/Indexer") && var0.getImplMethodSignature().equals("(Ljava/util/regex/Pattern;Ljava/util/regex/Pattern;Lcis5550/flame/FlamePair;)Ljava/lang/Iterable;")) {
            return (var2x) -> {
               String var3 = var2x._1();
               String var4x = var2x._2();
               var4x = var4.matcher(var4x).replaceAll(" ");
               var4x = var5.matcher(var4x).replaceAll(" ");
               String[] var5x = var4x.toLowerCase().split("\\s+");
               return (Iterable)Arrays.stream(var5x).filter((var0) -> {
                  return !var0.isEmpty();
               }).map((var1) -> {
                  return new FlamePair(var1, var3);
               }).collect(Collectors.toList());
            };
         }
         break;
      case 1:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/FlameContext$RowToString") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Lcis5550/kvs/Row;)Ljava/lang/String;") && var0.getImplClass().equals("cis5550/jobs/Indexer") && var0.getImplMethodSignature().equals("(Lcis5550/kvs/Row;)Ljava/lang/String;")) {
            return (var0x) -> {
               String var1 = var0x.get("url").toString();
               String var2 = var0x.get("page").toString();
               return var1 + "," + var2;
            };
         }
         break;
      case 2:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/FlamePairRDD$TwoStringsToString") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;") && var0.getImplClass().equals("cis5550/jobs/Indexer") && var0.getImplMethodSignature().equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")) {
            return (var0x, var1x) -> {
               if (var0x.isEmpty()) {
                  return var1x;
               } else {
                  HashSet var2 = new HashSet(Arrays.asList(var0x.split(",")));
                  var2.add(var1x);
                  return String.join(",", var2);
               }
            };
         }
         break;
      case 3:
         if (var0.getImplMethodKind() == 6 && var0.getFunctionalInterfaceClass().equals("cis5550/flame/FlameRDD$StringToPair") && var0.getFunctionalInterfaceMethodName().equals("op") && var0.getFunctionalInterfaceMethodSignature().equals("(Ljava/lang/String;)Lcis5550/flame/FlamePair;") && var0.getImplClass().equals("cis5550/jobs/Indexer") && var0.getImplMethodSignature().equals("(Ljava/lang/String;)Lcis5550/flame/FlamePair;")) {
            return (var0x) -> {
               String[] var1 = var0x.split(",", 2);
               System.out.println("row: " + var0x + "; value: " + var1[0] + "," + var1[1]);
               return new FlamePair(var1[0], var1[1]);
            };
         }
      }

      throw new IllegalArgumentException("Invalid lambda deserialization");
   }
}
