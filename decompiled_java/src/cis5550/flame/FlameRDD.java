package cis5550.flame;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public interface FlameRDD {
   int count() throws Exception;

   void saveAsTable(String var1) throws Exception;

   FlameRDD distinct() throws Exception;

   void destroy() throws Exception;

   Vector<String> take(int var1) throws Exception;

   String fold(String var1, FlamePairRDD.TwoStringsToString var2) throws Exception;

   List<String> collect() throws Exception;

   FlameRDD flatMap(FlameRDD.StringToIterable var1) throws Exception;

   FlamePairRDD flatMapToPair(FlameRDD.StringToPairIterable var1) throws Exception;

   FlamePairRDD mapToPair(FlameRDD.StringToPair var1) throws Exception;

   FlameRDD intersection(FlameRDD var1) throws Exception;

   FlameRDD sample(double var1) throws Exception;

   FlamePairRDD groupBy(FlameRDD.StringToString var1) throws Exception;

   FlameRDD filter(FlameRDD.StringToBoolean var1) throws Exception;

   FlameRDD mapPartitions(FlameRDD.IteratorToIterator var1) throws Exception;

   public interface IteratorToIterator extends Serializable {
      Iterator<String> op(Iterator<String> var1) throws Exception;
   }

   public interface StringToBoolean extends Serializable {
      boolean op(String var1) throws Exception;
   }

   public interface StringToString extends Serializable {
      String op(String var1) throws Exception;
   }

   public interface StringToPairIterable extends Serializable {
      Iterable<FlamePair> op(String var1) throws Exception;
   }

   public interface StringToPair extends Serializable {
      FlamePair op(String var1) throws Exception;
   }

   public interface StringToIterable extends Serializable {
      Iterable<String> op(String var1) throws Exception;
   }
}
