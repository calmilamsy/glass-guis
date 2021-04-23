package net.glasslauncher.guis.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.entity.EntityBase;
import net.minecraft.locale.Language;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslatableComponent extends BaseComponent implements ContextAwareComponent {
   private static final Object[] NO_ARGS = new Object[0];
   private static final FormattedText TEXT_PERCENT = FormattedText.of("%");
   private static final FormattedText TEXT_NULL = FormattedText.of("null");
   private final String key;
   private final Object[] args;
   @Nullable
   private Language decomposedWith;
   private final List<FormattedText> decomposedParts = Lists.newArrayList();
   private static final Pattern FORMAT_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");

   public TranslatableComponent(String string) {
      this.key = string;
      this.args = NO_ARGS;
   }

   public TranslatableComponent(String string, Object... objects) {
      this.key = string;
      this.args = objects;
   }

   private void decompose() {
      Language language = Language.getInstance();
      if (language != this.decomposedWith) {
         this.decomposedWith = language;
         this.decomposedParts.clear();
         String string = language.getOrDefault(this.key);

         try {
            this.decomposeTemplate(string);
         } catch (TranslatableFormatException var4) {
            this.decomposedParts.clear();
            this.decomposedParts.add(FormattedText.of(string));
         }

      }
   }

   private void decomposeTemplate(String string) {
      Matcher matcher = FORMAT_PATTERN.matcher(string);

      try {
         int i = 0;

         int j;
         int l;
         for(j = 0; matcher.find(j); j = l) {
            int k = matcher.start();
            l = matcher.end();
            String string3;
            if (k > j) {
               string3 = string.substring(j, k);
               if (string3.indexOf(37) != -1) {
                  throw new IllegalArgumentException();
               }

               this.decomposedParts.add(FormattedText.of(string3));
            }

            string3 = matcher.group(2);
            String string4 = string.substring(k, l);
            if ("%".equals(string3) && "%%".equals(string4)) {
               this.decomposedParts.add(TEXT_PERCENT);
            } else {
               if (!"s".equals(string3)) {
                  throw new TranslatableFormatException(this, "Unsupported format: '" + string4 + "'");
               }

               String string5 = matcher.group(1);
               int m = string5 != null ? Integer.parseInt(string5) - 1 : i++;
               if (m < this.args.length) {
                  this.decomposedParts.add(this.getArgument(m));
               }
            }
         }

         if (j < string.length()) {
            String string6 = string.substring(j);
            if (string6.indexOf(37) != -1) {
               throw new IllegalArgumentException();
            }

            this.decomposedParts.add(FormattedText.of(string6));
         }

      } catch (IllegalArgumentException var11) {
         throw new TranslatableFormatException(this, var11);
      }
   }

   private FormattedText getArgument(int i) {
      if (i >= this.args.length) {
         throw new TranslatableFormatException(this, i);
      } else {
         Object object = this.args[i];
         if (object instanceof Component) {
            return (Component)object;
         } else {
            return object == null ? TEXT_NULL : FormattedText.of(object.toString());
         }
      }
   }

   public TranslatableComponent plainCopy() {
      return new TranslatableComponent(this.key, this.args);
   }

   @Environment(EnvType.CLIENT)
   public <T> Optional<T> visitSelf(StyledContentConsumer<T> styledContentConsumer, Style style) {
      this.decompose();
      Iterator var3 = this.decomposedParts.iterator();

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

   public <T> Optional<T> visitSelf(ContentConsumer<T> contentConsumer) {
      this.decompose();
      Iterator var2 = this.decomposedParts.iterator();

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

   public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable EntityBase entity, int i) throws CommandSyntaxException {
      Object[] objects = new Object[this.args.length];

      for(int j = 0; j < objects.length; ++j) {
         Object object = this.args[j];
         if (object instanceof Component) {
            objects[j] = ComponentUtils.updateForEntity(commandSourceStack, (Component)object, entity, i);
         } else {
            objects[j] = object;
         }
      }

      return new TranslatableComponent(this.key, objects);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof TranslatableComponent)) {
         return false;
      } else {
         TranslatableComponent translatableComponent = (TranslatableComponent)object;
         return Arrays.equals(this.args, translatableComponent.args) && this.key.equals(translatableComponent.key) && super.equals(object);
      }
   }

   public int hashCode() {
      int i = super.hashCode();
      i = 31 * i + this.key.hashCode();
      i = 31 * i + Arrays.hashCode(this.args);
      return i;
   }

   public String toString() {
      return "TranslatableComponent{key='" + this.key + '\'' + ", args=" + Arrays.toString(this.args) + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }

   public String getKey() {
      return this.key;
   }

   public Object[] getArgs() {
      return this.args;
   }
}
