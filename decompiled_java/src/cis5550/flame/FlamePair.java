package cis5550.flame;

public class FlamePair implements Comparable<FlamePair> {
   String a;
   String b;

   public String _1() {
      return this.a;
   }

   public String _2() {
      return this.b;
   }

   public FlamePair(String var1, String var2) {
      this.a = var1;
      this.b = var2;
   }

   public int compareTo(FlamePair var1) {
      return this._1().equals(var1._1()) ? this._2().compareTo(var1._2()) : this._1().compareTo(var1._1());
   }

   public String toString() {
      return "(" + this.a + "," + this.b + ")";
   }
}
