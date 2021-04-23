package net.glasslauncher.guis.util;

import java.nio.channels.*;
import java.nio.*;
import java.nio.file.attribute.*;
import java.io.*;
import java.nio.file.*;
import net.fabricmc.api.*;
import com.google.common.base.*;

public class DirectoryLock implements AutoCloseable
{
   private final FileChannel lockFile;
   private final FileLock lock;
   private static final ByteBuffer DUMMY;

   public static DirectoryLock create(final Path path) throws IOException {
      final Path path2 = path.resolve("session.lock");
      if (!Files.isDirectory(path, new LinkOption[0])) {
         Files.createDirectories(path, (FileAttribute<?>[])new FileAttribute[0]);
      }
      final FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
      try {
         fileChannel.write(DirectoryLock.DUMMY.duplicate());
         fileChannel.force(true);
         final FileLock fileLock = fileChannel.tryLock();
         if (fileLock == null) {
            throw LockException.alreadyLocked(path2);
         }
         return new DirectoryLock(fileChannel, fileLock);
      }
      catch (IOException iOException) {
         try {
            fileChannel.close();
         }
         catch (IOException iOException2) {
            iOException.addSuppressed(iOException2);
         }
         throw iOException;
      }
   }

   private DirectoryLock(final FileChannel fileChannel, final FileLock fileLock) {
      this.lockFile = fileChannel;
      this.lock = fileLock;
   }

   @Override
   public void close() throws IOException {
      try {
         if (this.lock.isValid()) {
            this.lock.release();
         }
      }
      finally {
         if (this.lockFile.isOpen()) {
            this.lockFile.close();
         }
      }
   }

   public boolean isValid() {
      return this.lock.isValid();
   }

   @Environment(EnvType.CLIENT)
   public static boolean isLocked(final Path path) throws IOException {
      final Path path2 = path.resolve("session.lock");
      try (final FileChannel fileChannel = FileChannel.open(path2, StandardOpenOption.WRITE);
           final FileLock fileLock = fileChannel.tryLock()) {
         return fileLock == null;
      }
      catch (AccessDeniedException accessDeniedException) {
         return true;
      }
      catch (NoSuchFileException noSuchFileException) {
         return false;
      }
   }

   static {
      final byte[] bs = "\u2603".getBytes(Charsets.UTF_8);
      (DUMMY = ByteBuffer.allocateDirect(bs.length)).put(bs);
      DirectoryLock.DUMMY.flip();
   }

   public static class LockException extends IOException {
      private LockException(Path path, String string) {
         super(path.toAbsolutePath() + ": " + string);
      }

      public static DirectoryLock.LockException alreadyLocked(Path path) {
         return new DirectoryLock.LockException(path, "already locked (possibly by other Minecraft instance?)");
      }
   }
}
