package cis5550.webserver;

@FunctionalInterface
public interface Route {
   Object handle(Request var1, Response var2) throws Exception;
}
