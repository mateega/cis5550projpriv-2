package cis5550.tools;

import java.security.MessageDigest;

public class Hasher {
   protected static final String[] byte2chars = new String[]{"aa", "ba", "ca", "da", "ea", "fa", "ga", "ha", "ia", "ja", "ka", "la", "ma", "na", "oa", "pa", "qa", "ra", "sa", "ta", "ua", "va", "wa", "xa", "ya", "za", "ac", "bc", "cc", "dc", "ec", "fc", "gc", "hc", "ic", "jc", "kc", "lc", "mc", "nc", "oc", "pc", "qc", "rc", "sc", "tc", "uc", "vc", "wc", "xc", "yc", "zc", "ae", "be", "ce", "de", "ee", "fe", "ge", "he", "ie", "je", "ke", "le", "me", "ne", "oe", "pe", "qe", "re", "se", "te", "ue", "ve", "we", "xe", "ye", "ze", "ag", "bg", "cg", "dg", "eg", "fg", "gg", "hg", "ig", "jg", "kg", "lg", "mg", "ng", "og", "pg", "qg", "rg", "sg", "tg", "ug", "vg", "wg", "xg", "yg", "zg", "ai", "bi", "ci", "di", "ei", "fi", "gi", "hi", "ii", "ji", "ki", "li", "mi", "ni", "oi", "pi", "qi", "ri", "si", "ti", "ui", "vi", "wi", "xi", "yi", "zi", "ak", "bk", "ck", "dk", "ek", "fk", "gk", "hk", "ik", "jk", "kk", "lk", "mk", "nk", "ok", "pk", "qk", "rk", "sk", "tk", "uk", "vk", "wk", "xk", "yk", "zk", "am", "bm", "cm", "dm", "em", "fm", "gm", "hm", "im", "jm", "km", "lm", "mm", "nm", "om", "pm", "qm", "rm", "sm", "tm", "um", "vm", "wm", "xm", "ym", "zm", "ao", "bo", "co", "do", "eo", "fo", "go", "ho", "io", "jo", "ko", "lo", "mo", "no", "oo", "po", "qo", "ro", "so", "to", "uo", "vo", "wo", "xo", "yo", "zo", "aq", "bq", "cq", "dq", "eq", "fq", "gq", "hq", "iq", "jq", "kq", "lq", "mq", "nq", "oq", "pq", "qq", "rq", "sq", "tq", "uq", "vq", "wq", "xq", "yq", "zq", "as", "bs", "cs", "ds", "es", "fs", "gs", "hs", "is", "js", "ks", "ls", "ms", "ns", "os", "ps", "qs", "rs", "ss", "ts", "us", "vs"};

   public static String hash(String var0) {
      String var1 = "";

      try {
         MessageDigest var2 = MessageDigest.getInstance("SHA-1");
         var2.reset();
         var2.update(var0.getBytes("UTF-8"));
         byte[] var3 = var2.digest();

         for(int var4 = 0; var4 < var3.length; ++var4) {
            var1 = var1 + byte2chars[var3[var4] > 0 ? var3[var4] : 255 + var3[var4]];
         }
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      return var1;
   }

   public static void main(String[] var0) {
      for(int var1 = 0; var1 < 10000; ++var1) {
         System.out.println(hash(var1.makeConcatWithConstants<invokedynamic>(var1)));
      }

   }
}
