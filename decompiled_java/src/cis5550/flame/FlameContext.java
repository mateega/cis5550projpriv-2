package cis5550.flame;

import cis5550.flame.FlameRDD;
import cis5550.kvs.KVSClient;
import cis5550.kvs.Row;
import java.io.Serializable;
import java.util.List;

public interface FlameContext {
   KVSClient getKVS();

   void output(String var1);

   FlameRDD parallelize(List<String> var1) throws Exception;

   FlameRDD fromTable(String var1, FlameContext.RowToString var2) throws Exception;

   void setConcurrencyLevel(int var1);

   public interface RowToString extends Serializable {
      String op(Row var1);
   }
}
