package net.glasslauncher.guis;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.MoreExecutors;
import it.unimi.dsi.fastutil.Hash.Strategy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.util.Mth;
import net.modificationstation.stationapi.api.common.registry.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Util {
   private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
   private static final ExecutorService BOOTSTRAP_EXECUTOR = makeExecutor("Bootstrap");
   private static final ExecutorService BACKGROUND_EXECUTOR = makeExecutor("Main");
   private static final ExecutorService IO_POOL = makeIoExecutor();
   public static LongSupplier timeSource = System::nanoTime;
   public static final UUID NIL_UUID = new UUID(0L, 0L);
   private static final Logger LOGGER = LogManager.getLogger();

   public static <K, V> Collector<Entry<? extends K, ? extends V>, ?, Map<K, V>> toMap() {
      return Collectors.toMap(Entry::getKey, Entry::getValue);
   }

   public static <T extends Comparable<T>> String getPropertyName(Property<T> property, Object object) {
      return property.getName((Comparable)object);
   }

   public static String makeDescriptionId(String string, @Nullable Identifier resourceLocation) {
      return resourceLocation == null ? string + ".unregistered_sadface" : string + '.' + resourceLocation.modID + '.' + resourceLocation.id;
   }

   public static long getMillis() {
      return getNanos() / 1000000L;
   }

   public static long getNanos() {
      return timeSource.getAsLong();
   }

   public static long getEpochMillis() {
      return Instant.now().toEpochMilli();
   }

   private static ExecutorService makeExecutor(String string) {
      int i = Mth.clamp(Runtime.getRuntime().availableProcessors() - 1, 1, 7);
      Object executorService2;
      if (i <= 0) {
         executorService2 = MoreExecutors.newDirectExecutorService();
      } else {
         executorService2 = new ForkJoinPool(i, (forkJoinPool) -> {
            ForkJoinWorkerThread forkJoinWorkerThread = new ForkJoinWorkerThread(forkJoinPool) {
               protected void onTermination(Throwable throwable) {
                  if (throwable != null) {
                     Util.LOGGER.warn("{} died", this.getName(), throwable);
                  } else {
                     Util.LOGGER.debug("{} shutdown", this.getName());
                  }

                  super.onTermination(throwable);
               }
            };
            forkJoinWorkerThread.setName("Worker-" + string + "-" + WORKER_COUNT.getAndIncrement());
            return forkJoinWorkerThread;
         }, Util::onThreadException, true);
      }

      return (ExecutorService)executorService2;
   }

   public static Executor bootstrapExecutor() {
      return BOOTSTRAP_EXECUTOR;
   }

   public static Executor backgroundExecutor() {
      return BACKGROUND_EXECUTOR;
   }

   public static Executor ioPool() {
      return IO_POOL;
   }

   public static void shutdownExecutors() {
      shutdownExecutor(BACKGROUND_EXECUTOR);
      shutdownExecutor(IO_POOL);
   }

   private static void shutdownExecutor(ExecutorService executorService) {
      executorService.shutdown();

      boolean bl2;
      try {
         bl2 = executorService.awaitTermination(3L, TimeUnit.SECONDS);
      } catch (InterruptedException var3) {
         bl2 = false;
      }

      if (!bl2) {
         executorService.shutdownNow();
      }

   }

   private static ExecutorService makeIoExecutor() {
      return Executors.newCachedThreadPool((runnable) -> {
         Thread thread = new Thread(runnable);
         thread.setName("IO-Worker-" + WORKER_COUNT.getAndIncrement());
         thread.setUncaughtExceptionHandler(Util::onThreadException);
         return thread;
      });
   }

   @Environment(EnvType.CLIENT)
   public static <T> CompletableFuture<T> failedFuture(Throwable throwable) {
      CompletableFuture<T> completableFuture = new CompletableFuture();
      completableFuture.completeExceptionally(throwable);
      return completableFuture;
   }

   @Environment(EnvType.CLIENT)
   public static void throwAsRuntime(Throwable throwable) {
      throw throwable instanceof RuntimeException ? (RuntimeException)throwable : new RuntimeException(throwable);
   }

   private static void onThreadException(Thread thread, Throwable throwable) {
      pauseInIde(throwable);
      if (throwable instanceof CompletionException) {
         throwable = throwable.getCause();
      }

      if (throwable instanceof ReportedException) {
         Bootstrap.realStdoutPrintln(((ReportedException)throwable).getReport().getFriendlyReport());
         System.exit(-1);
      }

      LOGGER.error(String.format("Caught exception in thread %s", thread), throwable);
   }

   @Nullable
   public static Type<?> fetchChoiceType(TypeReference typeReference, String string) {
      return !SharedConstants.CHECK_DATA_FIXER_SCHEMA ? null : doFetchChoiceType(typeReference, string);
   }

   @Nullable
   private static Type<?> doFetchChoiceType(TypeReference typeReference, String string) {
      Type type = null;

      try {
         type = DataFixers.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getCurrentVersion().getWorldVersion())).getChoiceType(typeReference, string);
      } catch (IllegalArgumentException var4) {
         LOGGER.error("No data fixer registered for {}", string);
         if (SharedConstants.IS_RUNNING_IN_IDE) {
            throw var4;
         }
      }

      return type;
   }

   public static OS getPlatform() {
      String string = System.getProperty("os.name").toLowerCase(Locale.ROOT);
      if (string.contains("win")) {
         return OS.WINDOWS;
      } else if (string.contains("mac")) {
         return OS.OSX;
      } else if (string.contains("solaris")) {
         return OS.SOLARIS;
      } else if (string.contains("sunos")) {
         return OS.SOLARIS;
      } else if (string.contains("linux")) {
         return OS.LINUX;
      } else {
         return string.contains("unix") ? OS.LINUX : OS.UNKNOWN;
      }
   }

   public static Stream<String> getVmArguments() {
      RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
      return runtimeMXBean.getInputArguments().stream().filter((string) -> {
         return string.startsWith("-X");
      });
   }

   public static <T> T lastOf(List<T> list) {
      return list.get(list.size() - 1);
   }

   public static <T> T findNextInIterable(Iterable<T> iterable, @Nullable T object) {
      Iterator<T> iterator = iterable.iterator();
      T object2 = iterator.next();
      if (object != null) {
         Object object3 = object2;

         while(object3 != object) {
            if (iterator.hasNext()) {
               object3 = iterator.next();
            }
         }

         if (iterator.hasNext()) {
            return iterator.next();
         }
      }

      return object2;
   }

   public static <T> T findPreviousInIterable(Iterable<T> iterable, @Nullable T object) {
      Iterator<T> iterator = iterable.iterator();

      Object object2;
      Object object3;
      for(object2 = null; iterator.hasNext(); object2 = object3) {
         object3 = iterator.next();
         if (object3 == object) {
            if (object2 == null) {
               object2 = iterator.hasNext() ? Iterators.getLast(iterator) : object;
            }
            break;
         }
      }

      return object2;
   }

   public static <T> T make(Supplier<T> supplier) {
      return supplier.get();
   }

   public static <T> T make(T object, Consumer<T> consumer) {
      consumer.accept(object);
      return object;
   }

   public static <K> Strategy<K> identityStrategy() {
      return IdentityStrategy.INSTANCE;
   }

   public static <V> CompletableFuture<List<V>> sequence(List<? extends CompletableFuture<? extends V>> list) {
      List<V> list2 = Lists.newArrayListWithCapacity(list.size());
      CompletableFuture<?>[] completableFutures = new CompletableFuture[list.size()];
      CompletableFuture<Void> completableFuture = new CompletableFuture();
      list.forEach((completableFuture2) -> {
         int i = list2.size();
         list2.add(null);
         completableFutures[i] = completableFuture2.whenComplete((object, throwable) -> {
            if (throwable != null) {
               completableFuture.completeExceptionally(throwable);
            } else {
               list2.set(i, object);
            }

         });
      });
      return CompletableFuture.allOf(completableFutures).applyToEither(completableFuture, (void_) -> {
         return list2;
      });
   }

   public static <T> Stream<T> toStream(Optional<? extends T> optional) {
      return (Stream)DataFixUtils.orElseGet(optional.map(Stream::of), Stream::empty);
   }

   public static <T> Optional<T> ifElse(Optional<T> optional, Consumer<T> consumer, Runnable runnable) {
      if (optional.isPresent()) {
         consumer.accept(optional.get());
      } else {
         runnable.run();
      }

      return optional;
   }

   public static Runnable name(Runnable runnable, Supplier<String> supplier) {
      return runnable;
   }

   public static <T extends Throwable> T pauseInIde(T throwable) {
      if (SharedConstants.IS_RUNNING_IN_IDE) {
         LOGGER.error("Trying to throw a fatal exception, pausing in IDE", throwable);

         while(true) {
            try {
               Thread.sleep(1000L);
               LOGGER.error("paused");
            } catch (InterruptedException var2) {
               return throwable;
            }
         }
      } else {
         return throwable;
      }
   }

   public static String describeError(Throwable throwable) {
      if (throwable.getCause() != null) {
         return describeError(throwable.getCause());
      } else {
         return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
      }
   }

   public static <T> T getRandom(T[] objects, Random random) {
      return objects[random.nextInt(objects.length)];
   }

   public static int getRandom(int[] is, Random random) {
      return is[random.nextInt(is.length)];
   }

   private static BooleanSupplier createRenamer(final Path path, final Path path2) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.move(path, path2);
               return true;
            } catch (IOException var2) {
               Util.LOGGER.error("Failed to rename", var2);
               return false;
            }
         }

         public String toString() {
            return "rename " + path + " to " + path2;
         }
      };
   }

   private static BooleanSupplier createDeleter(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            try {
               Files.deleteIfExists(path);
               return true;
            } catch (IOException var2) {
               Util.LOGGER.warn("Failed to delete", var2);
               return false;
            }
         }

         public String toString() {
            return "delete old " + path;
         }
      };
   }

   private static BooleanSupplier createFileDeletedCheck(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return !Files.exists(path, new LinkOption[0]);
         }

         public String toString() {
            return "verify that " + path + " is deleted";
         }
      };
   }

   private static BooleanSupplier createFileCreatedCheck(final Path path) {
      return new BooleanSupplier() {
         public boolean getAsBoolean() {
            return Files.isRegularFile(path, new LinkOption[0]);
         }

         public String toString() {
            return "verify that " + path + " is present";
         }
      };
   }

   private static boolean executeInSequence(BooleanSupplier... booleanSuppliers) {
      BooleanSupplier[] var1 = booleanSuppliers;
      int var2 = booleanSuppliers.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         BooleanSupplier booleanSupplier = var1[var3];
         if (!booleanSupplier.getAsBoolean()) {
            LOGGER.warn("Failed to execute {}", booleanSupplier);
            return false;
         }
      }

      return true;
   }

   private static boolean runWithRetries(int i, String string, BooleanSupplier... booleanSuppliers) {
      for(int j = 0; j < i; ++j) {
         if (executeInSequence(booleanSuppliers)) {
            return true;
         }

         LOGGER.error("Failed to {}, retrying {}/{}", string, j, i);
      }

      LOGGER.error("Failed to {}, aborting, progress might be lost", string);
      return false;
   }

   public static void safeReplaceFile(File file, File file2, File file3) {
      safeReplaceFile(file.toPath(), file2.toPath(), file3.toPath());
   }

   public static void safeReplaceFile(Path path, Path path2, Path path3) {
      int i = true;
      if (!Files.exists(path, new LinkOption[0]) || runWithRetries(10, "create backup " + path3, createDeleter(path3), createRenamer(path, path3), createFileCreatedCheck(path3))) {
         if (runWithRetries(10, "remove old " + path, createDeleter(path), createFileDeletedCheck(path))) {
            if (!runWithRetries(10, "replace " + path + " with " + path2, createRenamer(path2, path), createFileCreatedCheck(path))) {
               runWithRetries(10, "restore " + path + " from " + path3, createRenamer(path3, path), createFileCreatedCheck(path));
            }

         }
      }
   }

   @Environment(EnvType.CLIENT)
   public static int offsetByCodepoints(String string, int i, int j) {
      int k = string.length();
      int l;
      if (j >= 0) {
         for(l = 0; i < k && l < j; ++l) {
            if (Character.isHighSurrogate(string.charAt(i++)) && i < k && Character.isLowSurrogate(string.charAt(i))) {
               ++i;
            }
         }
      } else {
         for(l = j; i > 0 && l < 0; ++l) {
            --i;
            if (Character.isLowSurrogate(string.charAt(i)) && i > 0 && Character.isHighSurrogate(string.charAt(i - 1))) {
               --i;
            }
         }
      }

      return i;
   }

   public static Consumer<String> prefix(String string, Consumer<String> consumer) {
      return (string2) -> {
         consumer.accept(string + string2);
      };
   }

   public static DataResult<int[]> fixedSize(IntStream intStream, int i) {
      int[] is = intStream.limit(i + 1).toArray();
      if (is.length != i) {
         String string = "Input is not a list of " + i + " ints";
         return is.length >= i ? DataResult.error(string, (Object)Arrays.copyOf(is, i)) : DataResult.error(string);
      } else {
         return DataResult.success(is);
      }
   }

   public static void startTimerHackThread() {
      Thread thread = new Thread("Timer hack thread") {
         public void run() {
            while(true) {
               try {
                  Thread.sleep(2147483647L);
               } catch (InterruptedException var2) {
                  Util.LOGGER.warn("Timer hack thread interrupted, that really should not happen");
                  return;
               }
            }
         }
      };
      thread.setDaemon(true);
      thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
      thread.start();
   }

   @Environment(EnvType.CLIENT)
   public static void copyBetweenDirs(Path path, Path path2, Path path3) throws IOException {
      Path path4 = path.relativize(path3);
      Path path5 = path2.resolve(path4);
      Files.copy(path3, path5);
   }

   @Environment(EnvType.CLIENT)
   public static String sanitizeName(String string, CharPredicate charPredicate) {
      return string.toLowerCase(Locale.ROOT).chars().mapToObj((i) -> {
         return charPredicate.test((char)i) ? Character.toString((char)i) : "_";
      }).collect(Collectors.joining());
   }

   static enum IdentityStrategy implements Strategy<Object> {
      INSTANCE;

      public int hashCode(Object object) {
         return System.identityHashCode(object);
      }

      public boolean equals(Object object, Object object2) {
         return object == object2;
      }
   }

   public static enum OS {
      LINUX,
      SOLARIS,
      WINDOWS {
         @Environment(EnvType.CLIENT)
         protected String[] getOpenUrlArguments(URL uRL) {
            return new String[]{"rundll32", "url.dll,FileProtocolHandler", uRL.toString()};
         }
      },
      OSX {
         @Environment(EnvType.CLIENT)
         protected String[] getOpenUrlArguments(URL uRL) {
            return new String[]{"open", uRL.toString()};
         }
      },
      UNKNOWN;

      private OS() {
      }

      @Environment(EnvType.CLIENT)
      public void openUrl(URL uRL) {
         try {
            Process process = AccessController.doPrivileged(() -> {
               return Runtime.getRuntime().exec(this.getOpenUrlArguments(uRL));
            });
            Iterator var3 = IOUtils.readLines(process.getErrorStream()).iterator();

            while(var3.hasNext()) {
               String string = (String)var3.next();
               Util.LOGGER.error(string);
            }

            process.getInputStream().close();
            process.getErrorStream().close();
            process.getOutputStream().close();
         } catch (IOException | PrivilegedActionException var5) {
            Util.LOGGER.error("Couldn't open url '{}'", uRL, var5);
         }

      }

      @Environment(EnvType.CLIENT)
      public void openUri(URI uRI) {
         try {
            this.openUrl(uRI.toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error("Couldn't open uri '{}'", uRI, var3);
         }

      }

      @Environment(EnvType.CLIENT)
      public void openFile(File file) {
         try {
            this.openUrl(file.toURI().toURL());
         } catch (MalformedURLException var3) {
            Util.LOGGER.error("Couldn't open file '{}'", file, var3);
         }

      }

      @Environment(EnvType.CLIENT)
      protected String[] getOpenUrlArguments(URL uRL) {
         String string = uRL.toString();
         if ("file".equals(uRL.getProtocol())) {
            string = string.replace("file:", "file://");
         }

         return new String[]{"xdg-open", string};
      }

      @Environment(EnvType.CLIENT)
      public void openUri(String string) {
         try {
            this.openUrl((new URI(string)).toURL());
         } catch (MalformedURLException | IllegalArgumentException | URISyntaxException var3) {
            Util.LOGGER.error("Couldn't open uri '{}'", string, var3);
         }

      }
   }
}
