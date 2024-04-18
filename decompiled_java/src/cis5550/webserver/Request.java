package cis5550.webserver;

import java.util.Map;
import java.util.Set;

public interface Request {
   String ip();

   int port();

   String requestMethod();

   String url();

   String protocol();

   Set<String> headers();

   String headers(String var1);

   String contentType();

   String body();

   byte[] bodyAsBytes();

   int contentLength();

   Set<String> queryParams();

   String queryParams(String var1);

   Map<String, String> params();

   String params(String var1);

   Session session();
}
