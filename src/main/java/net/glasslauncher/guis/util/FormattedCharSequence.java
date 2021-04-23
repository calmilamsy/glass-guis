package net.glasslauncher.guis.util;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.chat.Style;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@FunctionalInterface
public interface FormattedCharSequence {
   FormattedCharSequence EMPTY = (formattedCharSink) -> {
      return true;
   };

   @Environment(EnvType.CLIENT)
   boolean accept(FormattedCharSink formattedCharSink);

   @Environment(EnvType.CLIENT)
   static FormattedCharSequence codepoint(int i, Style style) {
      return (formattedCharSink) -> {
         return formattedCharSink.accept(0, style, i);
      };
   }

   @Environment(EnvType.CLIENT)
   static FormattedCharSequence forward(String string, Style style) {
      return string.isEmpty() ? EMPTY : (formattedCharSink) -> {
         return StringDecomposer.iterate(string, style, formattedCharSink);
      };
   }

   @Environment(EnvType.CLIENT)
   static FormattedCharSequence backward(String string, Style style, Int2IntFunction int2IntFunction) {
      return string.isEmpty() ? EMPTY : (formattedCharSink) -> {
         return StringDecomposer.iterateBackwards(string, style, decorateOutput(formattedCharSink, int2IntFunction));
      };
   }

   @Environment(EnvType.CLIENT)
   static FormattedCharSink decorateOutput(FormattedCharSink formattedCharSink, Int2IntFunction int2IntFunction) {
      return (i, style, j) -> {
         return formattedCharSink.accept(i, style, (Integer)int2IntFunction.apply(j));
      };
   }

   @Environment(EnvType.CLIENT)
   static FormattedCharSequence composite(FormattedCharSequence formattedCharSequence, FormattedCharSequence formattedCharSequence2) {
      return fromPair(formattedCharSequence, formattedCharSequence2);
   }

   @Environment(EnvType.CLIENT)
   static FormattedCharSequence composite(List<FormattedCharSequence> list) {
      int i = list.size();
      switch(i) {
      case 0:
         return EMPTY;
      case 1:
         return (FormattedCharSequence)list.get(0);
      case 2:
         return fromPair((FormattedCharSequence)list.get(0), (FormattedCharSequence)list.get(1));
      default:
         return fromList(ImmutableList.copyOf((Collection)list));
      }
   }

   @Environment(EnvType.CLIENT)
   static FormattedCharSequence fromPair(FormattedCharSequence formattedCharSequence, FormattedCharSequence formattedCharSequence2) {
      return (formattedCharSink) -> {
         return formattedCharSequence.accept(formattedCharSink) && formattedCharSequence2.accept(formattedCharSink);
      };
   }

   @Environment(EnvType.CLIENT)
   static FormattedCharSequence fromList(List<FormattedCharSequence> list) {
      return (formattedCharSink) -> {
         Iterator var2 = list.iterator();

         FormattedCharSequence formattedCharSequence;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            formattedCharSequence = (FormattedCharSequence)var2.next();
         } while(formattedCharSequence.accept(formattedCharSink));

         return false;
      };
   }
}
