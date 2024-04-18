package cis5550.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Logger {
   protected static HashMap<String, Logger> prefixToLogger = null;
   protected static PrintWriter logfile = null;
   protected static Logger defaultLogger = null;
   protected static SimpleDateFormat dateFormat = null;
   protected static final int ALL = 6;
   protected static final int DEBUG = 5;
   protected static final int INFO = 4;
   protected static final int WARN = 3;
   protected static final int ERROR = 2;
   protected static final int FATAL = 1;
   protected static final int OFF = 0;
   int upToLevel;

   protected Logger(int var1) {
      this.upToLevel = var1;
   }

   protected void write(int var1, String var2, Throwable var3) {
      if (this.upToLevel != 0 && var1 <= this.upToLevel) {
         String var4 = null;
         if (var3 != null) {
            StringWriter var5 = new StringWriter();
            PrintWriter var6 = new PrintWriter(var5);
            var3.printStackTrace(var6);
            var4 = var5.toString();
         }

         String var10000 = dateFormat.format(new Date());

         String var9;
         for(var9 = var10000 + " " + Thread.currentThread().getName(); var9.length() < 30; var9 = var9 + " ") {
         }

         if (var1 == 3) {
            var9 = var9 + "WARNING: ";
         } else if (var1 == 2) {
            var9 = var9 + "ERROR: ";
         } else if (var1 == 1) {
            var9 = var9 + "FATAL: ";
         } else if (var1 == 5) {
            var9 = var9 + "  ";
         }

         synchronized(defaultLogger) {
            if (logfile != null) {
               logfile.println(var9 + var2);
               if (var4 != null) {
                  logfile.print(var4);
                  logfile.flush();
               }
            }

            if (logfile == null || var1 <= 2) {
               System.err.println(var9 + var2);
               if (var4 != null) {
                  System.err.print(var4);
               }
            }

         }
      }
   }

   public void fatal(String var1, Throwable var2) {
      this.write(1, var1, var2);
   }

   public void fatal(String var1) {
      this.write(1, var1, (Throwable)null);
   }

   public void error(String var1, Throwable var2) {
      this.write(2, var1, var2);
   }

   public void error(String var1) {
      this.write(2, var1, (Throwable)null);
   }

   public void warn(String var1, Throwable var2) {
      this.write(3, var1, var2);
   }

   public void warn(String var1) {
      this.write(3, var1, (Throwable)null);
   }

   public void info(String var1, Throwable var2) {
      this.write(4, var1, var2);
   }

   public void info(String var1) {
      this.write(4, var1, (Throwable)null);
   }

   public void debug(String var1, Throwable var2) {
      this.write(5, var1, var2);
   }

   public void debug(String var1) {
      this.write(5, var1, (Throwable)null);
   }

   protected static String getMainClassName() {
      StackTraceElement[] var0 = Thread.currentThread().getStackTrace();
      return var0.length > 0 ? var0[var0.length - 1].getClassName() : "Unknown";
   }

   public static Logger getLogger(Class var0) {
      String var2;
      if (prefixToLogger == null) {
         defaultLogger = new Logger(2);
         logfile = null;
         prefixToLogger = new HashMap();
         dateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS");

         try {
            BufferedReader var1 = new BufferedReader(new FileReader("log.properties"));

            label88:
            while(true) {
               while(true) {
                  do {
                     do {
                        var2 = null;

                        try {
                           var2 = var1.readLine();
                        } catch (IOException var9) {
                        }

                        if (var2 == null) {
                           try {
                              var1.close();
                           } catch (IOException var7) {
                           }
                           break label88;
                        }

                        var2 = var2.trim();
                     } while(var2.startsWith("#"));
                  } while(var2.equals(""));

                  String[] var3 = var2.split("=");
                  var3[0] = var3[0].trim();
                  var3[1] = var3[1].trim();
                  if (var3[0].equals("log")) {
                     String var13 = var3[1].replaceAll("\\$MAINCLASS", getMainClassName()).replaceAll("\\$PID", ProcessHandle.current().pid().makeConcatWithConstants<invokedynamic>(ProcessHandle.current().pid()));

                     try {
                        logfile = new PrintWriter(new FileWriter(var13, true), true);
                     } catch (Exception var8) {
                        System.err.println("Cannot create log file: '" + var13 + "'");
                        System.exit(1);
                     }
                  } else {
                     String[] var4 = new String[]{"off", "fatal", "error", "warn", "info", "debug", "all"};
                     boolean var5 = false;

                     for(int var6 = 0; var6 < var4.length; ++var6) {
                        if (var3[1].equalsIgnoreCase(var4[var6])) {
                           prefixToLogger.put(var3[0], new Logger(var6));
                           var5 = true;
                        }
                     }

                     if (!var5) {
                        System.err.println("Invalid loglevel '" + var3[1] + "' for prefix '" + var3[0] + "' in '" + var3[0] + "'");
                        System.exit(1);
                     }
                  }
               }
            }
         } catch (FileNotFoundException var10) {
         }
      }

      String[] var11 = var0.getName().split("\\.");
      var2 = "";

      for(int var12 = 0; var12 < var11.length; ++var12) {
         var2 = var2 + (var12 == 0 ? "" : ".") + var11[var12];
         if (prefixToLogger.get(var2) != null) {
            return (Logger)prefixToLogger.get(var2);
         }
      }

      return defaultLogger;
   }
}
