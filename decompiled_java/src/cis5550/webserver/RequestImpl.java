package cis5550.webserver;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

class RequestImpl implements Request {
   String method;
   String url;
   String protocol;
   InetSocketAddress remoteAddr;
   Map<String, String> headers;
   Map<String, String> queryParams;
   Map<String, String> params;
   SessionImpl session;
   boolean cookieFound;
   byte[] bodyRaw;
   Server server;

   RequestImpl(String var1, String var2, String var3, Map<String, String> var4, Map<String, String> var5, Map<String, String> var6, InetSocketAddress var7, byte[] var8, Server var9) {
      this.method = var1;
      this.url = var2;
      this.remoteAddr = var7;
      this.protocol = var3;
      this.headers = var4;
      this.queryParams = var5;
      this.params = var6;
      this.bodyRaw = var8;
      this.server = var9;
      this.session = null;
      this.cookieFound = false;
      if (this.headers.get("cookie") != null) {
         String[] var10 = ((String)this.headers.get("cookie")).split(";");

         for(int var11 = 0; var11 < var10.length; ++var11) {
            String[] var12 = var10[var11].trim().split("=");
            if (var12[0].equals("SessionID")) {
               synchronized(this.server.sessions) {
                  this.session = (SessionImpl)this.server.sessions.get(var12[1]);
               }

               if (this.session != null && this.session.sLastAccessedTime < System.currentTimeMillis() - (long)(this.session.sMaxActiveInterval * 1000)) {
                  this.session = null;
               }

               if (this.session != null) {
                  this.cookieFound = true;
                  this.session.updateLastAccessed();
               }
            }
         }
      }

   }

   public String requestMethod() {
      return this.method;
   }

   public void setParams(Map<String, String> var1) {
      this.params = var1;
   }

   public int port() {
      return this.remoteAddr.getPort();
   }

   public String url() {
      return this.url;
   }

   public String protocol() {
      return this.protocol;
   }

   public String contentType() {
      return (String)this.headers.get("content-type");
   }

   public String ip() {
      return this.remoteAddr.getAddress().getHostAddress();
   }

   public String body() {
      return new String(this.bodyRaw, StandardCharsets.UTF_8);
   }

   public byte[] bodyAsBytes() {
      return this.bodyRaw;
   }

   public int contentLength() {
      return this.bodyRaw.length;
   }

   public String headers(String var1) {
      return (String)this.headers.get(var1.toLowerCase());
   }

   public Set<String> headers() {
      return this.headers.keySet();
   }

   public String queryParams(String var1) {
      return (String)this.queryParams.get(var1);
   }

   public Set<String> queryParams() {
      return this.queryParams.keySet();
   }

   public String params(String var1) {
      return (String)this.params.get(var1);
   }

   public Map<String, String> params() {
      return this.params;
   }

   public Session session() {
      if (this.session == null) {
         this.session = this.server.makeSession();
      }

      return this.session;
   }
}
