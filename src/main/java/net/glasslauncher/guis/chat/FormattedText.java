package net.glasslauncher.guis.chat;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.util.Unit;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public interface FormattedText {
   Optional<Unit> STOP_ITERATION = Optional.of(Unit.INSTANCE);
   FormattedText EMPTY = new FormattedText() {
      public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
         return Optional.empty();
      }

      @Environment(EnvType.CLIENT)
      public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style) {
         return Optional.empty();
      }
   };

   <T> Optional<T> visit(ContentConsumer<T> contentConsumer);

   @Environment(EnvType.CLIENT)
   <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style);

   static FormattedText of(final String string) {
      return new FormattedText() {
         public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
            return contentConsumer.accept(string);
         }

         @Environment(EnvType.CLIENT)
         public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style) {
            return styledContentConsumer.accept(style, string);
         }
      };
   }

   @Environment(EnvType.CLIENT)
   static FormattedText of(final String string, final Style style) {
      return new FormattedText() {
         public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
            return contentConsumer.accept(string);
         }

         public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style stylex) {
            return styledContentConsumer.accept(style.applyTo(stylex), string);
         }
      };
   }

   @Environment(EnvType.CLIENT)
   static FormattedText composite(FormattedText... formattedTexts) {
      return composite((List) ImmutableList.copyOf((Object[])formattedTexts));
   }

   @Environment(EnvType.CLIENT)
   static FormattedText composite(final List<FormattedText> list) {
      return new FormattedText() {
         public <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
            Iterator var2 = list.iterator();

            Optional optional;
            do {
               if (!var2.hasNext()) {
                  return Optional.empty();
               }

               FormattedText formattedText = (FormattedText)var2.next();
               optional = formattedText.visit(contentConsumer);
            } while(!optional.isPresent());

            return optional;
         }

         public <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style) {
            Iterator var3 = list.iterator();

            Optional optional;
            do {
               if (!var3.hasNext()) {
                  return Optional.empty();
               }

               FormattedText formattedText = (FormattedText)var3.next();
               optional = formattedText.visit(styledContentConsumer, style);
            } while(!optional.isPresent());

            return optional;
         }
      };
   }

   default String getString() {
      StringBuilder stringBuilder = new StringBuilder();
      this.visit((string) -> {
         stringBuilder.append(string);
         return Optional.empty();
      });
      return stringBuilder.toString();
   }

   public interface ContentConsumer<T> {
      Optional<T> accept(String string);
   }

   @Environment(EnvType.CLIENT)
   public interface StyledContentConsumer<T> {
      Optional<T> accept(Style style, String string);
   }
}
