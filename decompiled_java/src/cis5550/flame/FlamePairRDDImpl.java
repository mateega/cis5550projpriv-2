package cis5550.flame;

import cis5550.kvs.Row;
import cis5550.tools.Serializer;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FlamePairRDDImpl implements FlamePairRDD {
   FlameContextImpl context;
   String tableName;
   int seq;

   FlamePairRDDImpl(FlameContextImpl var1, int var2) {
      this.context = var1;
      this.seq = var2;
      this.tableName = this.context.tableName(var2);
   }

   public List<FlamePair> collect() throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         LinkedList var1 = new LinkedList();
         Iterator var2 = this.context.getKVS().scan(this.tableName, (String)null, (String)null);

         while(var2.hasNext()) {
            Row var3 = (Row)var2.next();
            Iterator var4 = var3.columns().iterator();

            while(var4.hasNext()) {
               String var5 = (String)var4.next();
               var1.add(new FlamePair(var3.key(), var3.get(var5)));
            }
         }

         return var1;
      }
   }

   public FlamePairRDD foldByKey(String var1, FlamePairRDD.TwoStringsToString var2) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlamePairRDDImpl(this.context, this.context.invokeOperation("foldByKey?inputTable=" + this.tableName + "&zero=" + URLEncoder.encode(var1, "UTF-8"), Serializer.objectToByteArray(var2)));
      }
   }

   public void saveAsTable(String var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         this.context.getKVS().rename(this.tableName, var1);
         this.tableName = var1;
      }
   }

   public void destroy() throws Exception {
      this.context.getKVS().delete(this.tableName);
      this.tableName = null;
   }

   public FlameRDD flatMap(FlamePairRDD.PairToStringIterable var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlameRDDImpl(this.context, this.context.invokeOperation("pairFlatMap?inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public FlamePairRDD flatMapToPair(FlamePairRDD.PairToPairIterable var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlamePairRDDImpl(this.context, this.context.invokeOperation("pairFlatMapToPair?inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public FlamePairRDD join(FlamePairRDD var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlamePairRDDImpl(this.context, this.context.invokeOperation("join?inputTable=" + this.tableName + "&inputTable2=" + ((FlamePairRDDImpl)var1).tableName, (byte[])null));
      }
   }

   public FlamePairRDD cogroup(FlamePairRDD var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlamePairRDDImpl(this.context, this.context.invokeOperation("cogroup?inputTable=" + this.tableName + "&inputTable2=" + ((FlamePairRDDImpl)var1).tableName, (byte[])null));
      }
   }
}
