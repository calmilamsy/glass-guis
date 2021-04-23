package net.glasslauncher.guis.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.chat.Style;

@FunctionalInterface
public interface FormattedCharSink {
   @Environment(EnvType.CLIENT)
   boolean accept(int i, Style style, int j);
}
