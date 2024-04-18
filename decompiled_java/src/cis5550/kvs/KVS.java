package cis5550.kvs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

public interface KVS {
   void put(String var1, String var2, String var3, byte[] var4) throws FileNotFoundException, IOException;

   void putRow(String var1, Row var2) throws FileNotFoundException, IOException;

   Row getRow(String var1, String var2) throws FileNotFoundException, IOException;

   boolean existsRow(String var1, String var2) throws FileNotFoundException, IOException;

   byte[] get(String var1, String var2, String var3) throws FileNotFoundException, IOException;

   Iterator<Row> scan(String var1, String var2, String var3) throws FileNotFoundException, IOException;

   int count(String var1) throws FileNotFoundException, IOException;

   boolean rename(String var1, String var2) throws IOException;

   void delete(String var1) throws IOException;
}
