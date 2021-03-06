package net.glasslauncher.guis.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.chat.Component;

public interface ProgressListener {
   void progressStartNoAbort(Component component);

   @Environment(EnvType.CLIENT)
   void progressStart(Component component);

   void progressStage(Component component);

   void progressStagePercentage(int i);

   @Environment(EnvType.CLIENT)
   void stop();
}
