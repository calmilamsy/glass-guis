package net.glasslauncher.guis.chat;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.util.FormattedCharSequence;
import net.minecraft.locale.Language;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public abstract class BaseComponent implements MutableComponent {
   protected final List<Component> siblings = Lists.newArrayList();
   private FormattedCharSequence visualOrderText;
   @Nullable
   @Environment(EnvType.CLIENT)
   private Language decomposedWith;
   private Style style;

   public BaseComponent() {
      this.visualOrderText = FormattedCharSequence.EMPTY;
      this.style = Style.EMPTY;
   }

   public MutableComponent append(Component component) {
      this.siblings.add(component);
      return this;
   }

   public String getContents() {
      return "";
   }

   public List<Component> getSiblings() {
      return this.siblings;
   }

   public MutableComponent setStyle(Style style) {
      this.style = style;
      return this;
   }

   public Style getStyle() {
      return this.style;
   }

   public abstract BaseComponent plainCopy();

   public final MutableComponent copy() {
      BaseComponent baseComponent = this.plainCopy();
      baseComponent.siblings.addAll(this.siblings);
      baseComponent.setStyle(this.style);
      return baseComponent;
   }

   @Environment(EnvType.CLIENT)
   public FormattedCharSequence getVisualOrderText() {
      Language language = Language.getInstance();
      if (this.decomposedWith != language) {
         this.visualOrderText = language.getVisualOrder((FormattedText)this);
         this.decomposedWith = language;
      }

      return this.visualOrderText;
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof BaseComponent)) {
         return false;
      } else {
         BaseComponent baseComponent = (BaseComponent)object;
         return this.siblings.equals(baseComponent.siblings) && Objects.equals(this.getStyle(), baseComponent.getStyle());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.getStyle(), this.siblings});
   }

   public String toString() {
      return "BaseComponent{style=" + this.style + ", siblings=" + this.siblings + '}';
   }
}
