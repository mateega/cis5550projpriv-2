package cis5550.webserver;

public interface Response {
   void body(String var1);

   void bodyAsBytes(byte[] var1);

   void header(String var1, String var2);

   void type(String var1);

   void status(int var1, String var2);

   void write(byte[] var1) throws Exception;

   void redirect(String var1, int var2);

   void halt(int var1, String var2);
}
