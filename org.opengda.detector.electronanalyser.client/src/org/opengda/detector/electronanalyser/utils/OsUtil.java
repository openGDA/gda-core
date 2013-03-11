package org.opengda.detector.electronanalyser.utils;

public final class OsUtil
{
   private static String OS = null;
   public static String getOsName()
   {
      if(OS == null) { OS = System.getProperty("os.name"); }
      return OS;
   }
   public static boolean isWindows()
   {
      return getOsName().startsWith("Windows");
   }

   public static boolean isUnix() {
	      return getOsName().startsWith("Linux");
   }
}