package cis5550.webserver;

public interface Session {
   String id();

   long creationTime();

   long lastAccessedTime();

   void maxActiveInterval(int var1);

   void invalidate();

   Object attribute(String var1);

   void attribute(String var1, Object var2);
}
