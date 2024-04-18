package cis5550.flame;

import java.io.Serializable;
import java.util.List;

public interface FlamePairRDD {
   List<FlamePair> collect() throws Exception;

   FlamePairRDD foldByKey(String var1, FlamePairRDD.TwoStringsToString var2) throws Exception;

   void saveAsTable(String var1) throws Exception;

   FlameRDD flatMap(FlamePairRDD.PairToStringIterable var1) throws Exception;

   void destroy() throws Exception;

   FlamePairRDD flatMapToPair(FlamePairRDD.PairToPairIterable var1) throws Exception;

   FlamePairRDD join(FlamePairRDD var1) throws Exception;

   FlamePairRDD cogroup(FlamePairRDD var1) throws Exception;

   public interface PairToStringIterable extends Serializable {
      Iterable<String> op(FlamePair var1) throws Exception;
   }

   public interface PairToPairIterable extends Serializable {
      Iterable<FlamePair> op(FlamePair var1) throws Exception;
   }

   public interface TwoStringsToString extends Serializable {
      String op(String var1, String var2);
   }
}
