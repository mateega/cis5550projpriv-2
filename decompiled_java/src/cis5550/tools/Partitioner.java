package cis5550.tools;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class Partitioner {
   Vector<String> flameWorkers = new Vector();
   Vector<Partitioner.Partition> partitions = new Vector();
   boolean alreadyAssigned = false;
   int keyRangesPerWorker = 1;

   boolean sameIP(String var1, String var2) {
      String[] var3 = var1.split(":");
      String[] var4 = var2.split(":");
      return var3[1].equals(var4[1]);
   }

   public void setKeyRangesPerWorker(int var1) {
      this.keyRangesPerWorker = var1;
   }

   public void addKVSWorker(String var1, String var2, String var3) {
      this.partitions.add(new Partitioner.Partition(var1, var2, var3));
   }

   public void addFlameWorker(String var1) {
      this.flameWorkers.add(var1);
   }

   public Vector<Partitioner.Partition> assignPartitions() {
      if (!this.alreadyAssigned && this.flameWorkers.size() >= 1) {
         Random var1;
         Partitioner.Partition var2;
         String var3;
         for(var1 = new Random(); this.partitions.size() < this.flameWorkers.size(); var2.toKeyExclusive = var3) {
            var2 = (Partitioner.Partition)this.partitions.elementAt(var1.nextInt(this.partitions.size()));

            do {
               do {
                  var3 = ((StringBuilder)var1.ints(97, 123).limit(5L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)).toString();
               } while(var2.fromKey != null && var3.compareTo(var2.fromKey) <= 0);
            } while(var2.toKeyExclusive != null && var3.compareTo(var2.toKeyExclusive) >= 0);

            this.partitions.add(new Partitioner.Partition(var2.kvsWorker, var3, var2.toKeyExclusive));
         }

         int[] var7 = new int[this.flameWorkers.size()];

         int var8;
         for(var8 = 0; var8 < var7.length; ++var8) {
            var7[var8] = 0;
         }

         int var10002;
         int var4;
         for(var8 = 0; var8 < this.partitions.size(); ++var8) {
            var4 = 0;
            int var5 = 9999;

            for(int var6 = 0; var6 < var7.length; ++var6) {
               if (var7[var6] < var5 || var7[var6] == var5 && this.sameIP((String)this.flameWorkers.elementAt(var6), ((Partitioner.Partition)this.partitions.elementAt(var8)).kvsWorker)) {
                  var4 = var6;
                  var5 = var7[var6];
               }
            }

            var10002 = var7[var4]++;
            ((Partitioner.Partition)this.partitions.elementAt(var8)).assignedFlameWorker = (String)this.flameWorkers.elementAt(var4);
         }

         while(true) {
            var8 = -1;

            for(var4 = 0; var4 < var7.length; ++var4) {
               if (var7[var4] < this.keyRangesPerWorker) {
                  var8 = var4;
               }
            }

            if (var8 < 0) {
               this.alreadyAssigned = true;
               return this.partitions;
            }

            Partitioner.Partition var9 = null;

            do {
               var9 = (Partitioner.Partition)this.partitions.elementAt(var1.nextInt(this.partitions.size()));
            } while(!var9.assignedFlameWorker.equals(this.flameWorkers.elementAt(var8)));

            String var10;
            do {
               do {
                  var10 = ((StringBuilder)var1.ints(97, 123).limit(5L).collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)).toString();
               } while(var9.fromKey != null && var10.compareTo(var9.fromKey) <= 0);
            } while(var9.toKeyExclusive != null && var10.compareTo(var9.toKeyExclusive) >= 0);

            this.partitions.add(new Partitioner.Partition(var9.kvsWorker, var10, var9.toKeyExclusive, var9.assignedFlameWorker));
            var9.toKeyExclusive = var10;
            var10002 = var7[var8]++;
         }
      } else {
         return null;
      }
   }

   public static void main(String[] var0) {
      Partitioner var1 = new Partitioner();
      var1.setKeyRangesPerWorker(3);
      var1.addKVSWorker("10.0.0.1:1001", (String)null, "def");
      var1.addKVSWorker("10.0.0.2:1002", "def", "mno");
      var1.addKVSWorker("10.0.0.3:1003", "mno", (String)null);
      var1.addFlameWorker("10.0.0.1:2001");
      var1.addFlameWorker("10.0.0.2:2002");
      var1.addFlameWorker("10.0.0.3:2003");
      Vector var2 = var1.assignPartitions();
      Iterator var3 = var2.iterator();

      while(var3.hasNext()) {
         Partitioner.Partition var4 = (Partitioner.Partition)var3.next();
         System.out.println(var4);
      }

   }

   public class Partition {
      public String kvsWorker;
      public String fromKey;
      public String toKeyExclusive;
      public String assignedFlameWorker;

      Partition(String param2, String param3, String param4, String param5) {
         this.kvsWorker = var2;
         this.fromKey = var3;
         this.toKeyExclusive = var4;
         this.assignedFlameWorker = var5;
      }

      Partition(String param2, String param3, String param4) {
         this.kvsWorker = var2;
         this.fromKey = var3;
         this.toKeyExclusive = var4;
         this.assignedFlameWorker = null;
      }

      public String toString() {
         String var10000 = this.kvsWorker;
         return "[kvs:" + var10000 + ", keys: " + (this.fromKey == null ? "" : this.fromKey) + "-" + (this.toKeyExclusive == null ? "" : this.toKeyExclusive) + ", flame: " + this.assignedFlameWorker + "]";
      }
   }
}
