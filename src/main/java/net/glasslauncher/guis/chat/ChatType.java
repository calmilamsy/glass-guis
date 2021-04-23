package net.glasslauncher.guis.chat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

public enum ChatType {
   CHAT((byte)0, false),
   SYSTEM((byte)1, true),
   GAME_INFO((byte)2, true);

   private final byte index;
   private final boolean interrupt;

   private ChatType(byte b, boolean bl) {
      this.index = b;
      this.interrupt = bl;
   }

   public byte getIndex() {
      return this.index;
   }

   public static ChatType getForIndex(byte b) {
      ChatType[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         ChatType chatType = var1[var3];
         if (b == chatType.index) {
            return chatType;
         }
      }

      return CHAT;
   }

   @Environment(EnvType.CLIENT)
   public boolean shouldInterrupt() {
      return this.interrupt;
   }
}
