package cis5550.tools;

import cis5550.flame.FlameContext;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Vector;

public class Loader {
   public static void invokeRunMethod(File var0, String var1, FlameContext var2, Vector<String> var3) throws IllegalAccessException, InvocationTargetException, MalformedURLException, ClassNotFoundException, NoSuchMethodException {
      URLClassLoader var4 = new URLClassLoader(new URL[]{var0.toURI().toURL()}, ClassLoader.getSystemClassLoader());
      Class var5 = Class.forName(var1, true, var4);
      Method var6 = var5.getMethod("run", FlameContext.class, String[].class);
      var6.invoke((Object)null, var2, var3.toArray(new String[0]));
   }
}
