package cis5550.flame;

import cis5550.generic.Coordinator;
import cis5550.kvs.KVSClient;
import cis5550.tools.HTTP;
import cis5550.tools.Hasher;
import cis5550.tools.Logger;
import cis5550.tools.Partitioner;
import cis5550.tools.Serializer;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class FlameContextImpl implements FlameContext, Serializable {
   private static final Logger logger = Logger.getLogger(FlameContextImpl.class);
   int nextRddSequenceNo = 1;
   String jobID;
   String jobName;
   String outputData;
   int keyRangesPerWorker;

   int invokeOperation(String var1, final byte[] var2) throws Exception {
      int var3 = this.nextRddSequenceNo++;
      Partitioner var4 = new Partitioner();
      var4.setKeyRangesPerWorker(this.keyRangesPerWorker);
      var4.addKVSWorker(this.getKVS().getWorkerAddress(this.getKVS().numWorkers() - 1), (String)null, this.getKVS().getWorkerID(0));

      for(int var5 = 0; var5 < this.getKVS().numWorkers() - 1; ++var5) {
         var4.addKVSWorker(this.getKVS().getWorkerAddress(var5), this.getKVS().getWorkerID(var5), this.getKVS().getWorkerID(var5 + 1));
      }

      var4.addKVSWorker(this.getKVS().getWorkerAddress(this.getKVS().numWorkers() - 1), this.getKVS().getWorkerID(this.getKVS().numWorkers() - 1), (String)null);
      Iterator var13 = Coordinator.getWorkers().iterator();

      while(var13.hasNext()) {
         String var6 = (String)var13.next();
         var4.addFlameWorker(var6);
      }

      Vector var14 = var4.assignPartitions();
      Thread[] var15 = new Thread[var14.size()];
      final String[] var7 = new String[var14.size()];

      final int var8;
      for(var8 = 0; var8 < var14.size(); ++var8) {
         String var10000 = ((Partitioner.Partition)var14.elementAt(var8)).assignedFlameWorker;
         final String var10 = "http://" + var10000 + "/" + var1;
         var10 = var10 + (var10.contains("?") ? "&" : "?") + "kvsCoordinator=" + this.getKVS().getCoordinator() + "&outputTable=" + this.tableName(var3) + "&part=" + var8;
         if (((Partitioner.Partition)var14.elementAt(var8)).fromKey != null) {
            var10 = var10 + "&fromKey=" + ((Partitioner.Partition)var14.elementAt(var8)).fromKey;
         }

         if (((Partitioner.Partition)var14.elementAt(var8)).toKeyExclusive != null) {
            var10 = var10 + "&toKeyExclusive=" + ((Partitioner.Partition)var14.elementAt(var8)).toKeyExclusive;
         }

         var15[var8] = new Thread("Request #" + (var8 + 1)) {
            public void run() {
               try {
                  var7[var8] = new String(HTTP.doRequest("POST", var10, var2).body());
               } catch (Exception var2x) {
                  var2x.printStackTrace();
                  var7[var8] = "Exception: " + String.valueOf(var2x);
               }

            }
         };
         var15[var8].start();
      }

      for(var8 = 0; var8 < var14.size(); ++var8) {
         try {
            var15[var8].join();
         } catch (InterruptedException var12) {
         }
      }

      var8 = 0;

      for(int var9 = 0; var9 < var14.size(); ++var9) {
         if (!var7[var9].startsWith("OK")) {
            logger.error("Job #" + (var9 + 1) + "/" + var14.size() + " (" + var1 + ") failed with a " + var7[var9]);
            ++var8;
         }
      }

      if (var8 > 0) {
         throw new Exception(var8 + " job(s) failed");
      } else {
         logger.debug("Operation completed and returned as RDD #" + var3);
         return var3;
      }
   }

   public String tableName(int var1) {
      return this.jobID + "." + var1;
   }

   public KVSClient getKVS() {
      return Coordinator.kvs;
   }

   public FlameContextImpl(String var1) {
      this.jobName = var1;
      this.jobID = var1 + "-" + System.currentTimeMillis();
      this.outputData = "";
      this.keyRangesPerWorker = 1;
   }

   public void output(String var1) {
      this.outputData = this.outputData + var1;
      Coordinator.outputs.put(this.jobName, this.outputData);
   }

   public FlameRDD parallelize(List<String> var1) throws IOException {
      FlameRDDImpl var2 = new FlameRDDImpl(this, this.nextRddSequenceNo++);
      int var3 = 0;
      Iterator var4 = var1.iterator();

      while(var4.hasNext()) {
         String var5 = (String)var4.next();
         KVSClient var10000 = this.getKVS();
         int var10002 = var3++;
         var10000.put(var2.tableName, Hasher.hash(var10002.makeConcatWithConstants<invokedynamic>(var10002)), "value", var5.getBytes());
      }

      return var2;
   }

   public FlameRDD fromTable(String var1, FlameContext.RowToString var2) throws Exception {
      return new FlameRDDImpl(this, this.invokeOperation("fromTable?inputTable=" + var1, Serializer.objectToByteArray(var2)));
   }

   public void setConcurrencyLevel(int var1) {
      this.keyRangesPerWorker = var1;
   }
}
