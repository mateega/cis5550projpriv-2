package cis5550.flame;

import cis5550.kvs.Row;
import cis5550.tools.Serializer;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class FlameRDDImpl implements FlameRDD {
   FlameContextImpl context;
   String tableName;
   int seq;

   FlameRDDImpl(FlameContextImpl var1, int var2) {
      this.context = var1;
      this.seq = var2;
      this.tableName = this.context.tableName(var2);
   }

   public FlameRDD flatMap(FlameRDD.StringToIterable var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlameRDDImpl(this.context, this.context.invokeOperation("flatMap?inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public FlamePairRDD mapToPair(FlameRDD.StringToPair var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlamePairRDDImpl(this.context, this.context.invokeOperation("mapToPair?inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public List<String> collect() throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         LinkedList var1 = new LinkedList();
         Iterator var2 = this.context.getKVS().scan(this.tableName, (String)null, (String)null);

         while(var2.hasNext()) {
            var1.add(((Row)var2.next()).get("value"));
         }

         return var1;
      }
   }

   public int count() throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return this.context.getKVS().count(this.tableName);
      }
   }

   public void destroy() throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         this.context.getKVS().delete(this.tableName);
         this.tableName = null;
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

   public Vector<String> take(int var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         Iterator var2 = this.context.getKVS().scan(this.tableName, (String)null, (String)null);
         Vector var3 = new Vector();

         while(var2.hasNext() && var3.size() < var1) {
            var3.add(((Row)var2.next()).get("value"));
         }

         return var3;
      }
   }

   public FlameRDD distinct() throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlameRDDImpl(this.context, this.context.invokeOperation("distinct?inputTable=" + this.tableName, (byte[])null));
      }
   }

   public String fold(String var1, FlamePairRDD.TwoStringsToString var2) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         int var3 = this.context.invokeOperation("fold?inputTable=" + this.tableName + "&zero=" + URLEncoder.encode(var1, "UTF-8"), Serializer.objectToByteArray(var2));
         Iterator var4 = this.context.getKVS().scan(this.context.tableName(var3), (String)null, (String)null);

         String var5;
         for(var5 = var1; var4.hasNext(); var5 = var2.op(var5, ((Row)var4.next()).get("value"))) {
         }

         return var5;
      }
   }

   public FlamePairRDD flatMapToPair(FlameRDD.StringToPairIterable var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlamePairRDDImpl(this.context, this.context.invokeOperation("flatMapToPair?inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public FlameRDD intersection(FlameRDD var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlameRDDImpl(this.context, this.context.invokeOperation("intersection?tempTable=temp-" + System.currentTimeMillis() + "&inputTable1=" + this.tableName + "&inputTable2=" + ((FlameRDDImpl)var1).tableName, (byte[])null));
      }
   }

   public FlameRDD sample(double var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlameRDDImpl(this.context, this.context.invokeOperation("sample?inputTable=" + this.tableName + "&fraction=" + var1, (byte[])null));
      }
   }

   public FlamePairRDD groupBy(FlameRDD.StringToString var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlamePairRDDImpl(this.context, this.context.invokeOperation("groupBy?tempTable=temp-" + System.currentTimeMillis() + "&inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public FlameRDD filter(FlameRDD.StringToBoolean var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlameRDDImpl(this.context, this.context.invokeOperation("filter?inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public FlameRDD mapPartitions(FlameRDD.IteratorToIterator var1) throws Exception {
      if (this.tableName == null) {
         throw new Exception("This RDD was destroyed and can no longer be used");
      } else {
         return new FlameRDDImpl(this.context, this.context.invokeOperation("mapPartitions?inputTable=" + this.tableName, Serializer.objectToByteArray(var1)));
      }
   }

   public String toString() {
      return "FlameRDD<" + this.seq + "/" + this.tableName + ">";
   }

   public interface IteratorToIterable extends Serializable {
      Iterable<String> op(Iterator<String> var1) throws Exception;
   }
}
