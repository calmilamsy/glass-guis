package net.glasslauncher.guis.util;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.chat.TranslatableComponent;
import net.minecraft.util.ProgressListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class HttpUtil {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final ListeningExecutorService DOWNLOAD_EXECUTOR;

   @Environment(EnvType.CLIENT)
   public static CompletableFuture<?> downloadTo(File file, String string, Map<String, String> map, int i, @Nullable ProgressListener progressListener, Proxy proxy) {
      return CompletableFuture.supplyAsync(() -> {
         HttpURLConnection httpURLConnection = null;
         InputStream inputStream = null;
         OutputStream outputStream = null;
         if (progressListener != null) {
            progressListener.progressStart(new TranslatableComponent("resourcepack.downloading"));
            progressListener.progressStage(new TranslatableComponent("resourcepack.requesting"));
         }

         try {
            try {
               byte[] bs = new byte[4096];
               URL uRL = new URL(string);
               httpURLConnection = (HttpURLConnection)uRL.openConnection(proxy);
               httpURLConnection.setInstanceFollowRedirects(true);
               float f = 0.0F;
               float g = (float)map.entrySet().size();
               Iterator var13 = map.entrySet().iterator();

               while(var13.hasNext()) {
                  Entry<String, String> entry = (Entry)var13.next();
                  httpURLConnection.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
                  if (progressListener != null) {
                     progressListener.progressStagePercentage((int)(++f / g * 100.0F));
                  }
               }

               inputStream = httpURLConnection.getInputStream();
               g = (float)httpURLConnection.getContentLength();
               int j = httpURLConnection.getContentLength();
               if (progressListener != null) {
                  progressListener.progressStage(new TranslatableComponent("resourcepack.progress", new Object[]{String.format(Locale.ROOT, "%.2f", g / 1000.0F / 1000.0F)}));
               }

               if (file.exists()) {
                  long l = file.length();
                  if (l == (long)j) {
                     if (progressListener != null) {
                        progressListener.stop();
                     }

                     Object var16 = null;
                     return var16;
                  }

                  LOGGER.warn((String)"Deleting {} as it does not match what we currently have ({} vs our {}).", (Object)file, j, l);
                  FileUtils.deleteQuietly(file);
               } else if (file.getParentFile() != null) {
                  file.getParentFile().mkdirs();
               }

               outputStream = new DataOutputStream(new FileOutputStream(file));
               if (i > 0 && g > (float)i) {
                  if (progressListener != null) {
                     progressListener.stop();
                  }

                  throw new IOException("Filesize is bigger than maximum allowed (file is " + f + ", limit is " + i + ")");
               }

               int k;
               while((k = inputStream.read(bs)) >= 0) {
                  f += (float)k;
                  if (progressListener != null) {
                     progressListener.progressStagePercentage((int)(f / g * 100.0F));
                  }

                  if (i > 0 && f > (float)i) {
                     if (progressListener != null) {
                        progressListener.stop();
                     }

                     throw new IOException("Filesize was bigger than maximum allowed (got >= " + f + ", limit was " + i + ")");
                  }

                  if (Thread.interrupted()) {
                     LOGGER.error("INTERRUPTED");
                     if (progressListener != null) {
                        progressListener.stop();
                     }

                     Object var15 = null;
                     return var15;
                  }

                  outputStream.write(bs, 0, k);
               }

               if (progressListener != null) {
                  progressListener.stop();
                  return null;
               }
            } catch (Throwable var22) {
               var22.printStackTrace();
               if (httpURLConnection != null) {
                  InputStream inputStream2 = httpURLConnection.getErrorStream();

                  try {
                     LOGGER.error(IOUtils.toString(inputStream2));
                  } catch (IOException var21) {
                     var21.printStackTrace();
                  }
               }

               if (progressListener != null) {
                  progressListener.stop();
                  return null;
               }
            }

            return null;
         } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly((OutputStream)outputStream);
         }
      }, DOWNLOAD_EXECUTOR);
   }

   public static int getAvailablePort() {
      try {
         ServerSocket serverSocket = new ServerSocket(0);
         Throwable var1 = null;

         int var2;
         try {
            var2 = serverSocket.getLocalPort();
         } catch (Throwable var12) {
            var1 = var12;
            throw var12;
         } finally {
            if (serverSocket != null) {
               if (var1 != null) {
                  try {
                     serverSocket.close();
                  } catch (Throwable var11) {
                     var1.addSuppressed(var11);
                  }
               } else {
                  serverSocket.close();
               }
            }

         }

         return var2;
      } catch (IOException var14) {
         return 25564;
      }
   }

   static {
      DOWNLOAD_EXECUTOR = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool((new ThreadFactoryBuilder()).setDaemon(true).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER)).setNameFormat("Downloader %d").build()));
   }
}
