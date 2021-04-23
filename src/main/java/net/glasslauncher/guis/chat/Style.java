package net.glasslauncher.guis.chat;

import com.google.gson.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.util.GsonHelper;
import net.modificationstation.stationapi.api.common.registry.Identifier;
import net.modificationstation.stationapi.api.common.registry.ModID;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Objects;

public class Style {
   public static final Style EMPTY = new Style((TextColor)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (Boolean)null, (ClickEvent)null, (HoverEvent)null, (String)null, (Identifier)null);
   public static final Identifier DEFAULT_FONT = Identifier.of(ModID.of("minecraft"), "default");
   @Nullable
   private final TextColor color;
   @Nullable
   private final Boolean bold;
   @Nullable
   private final Boolean italic;
   @Nullable
   private final Boolean underlined;
   @Nullable
   private final Boolean strikethrough;
   @Nullable
   private final Boolean obfuscated;
   @Nullable
   private final ClickEvent clickEvent;
   @Nullable
   private final HoverEvent hoverEvent;
   @Nullable
   private final String insertion;
   @Nullable
   private final Identifier font;

   private Style(@Nullable TextColor textColor, @Nullable Boolean boolean_, @Nullable Boolean boolean2, @Nullable Boolean boolean3, @Nullable Boolean boolean4, @Nullable Boolean boolean5, @Nullable ClickEvent clickEvent, @Nullable HoverEvent hoverEvent, @Nullable String string, @Nullable Identifier resourceLocation) {
      this.color = textColor;
      this.bold = boolean_;
      this.italic = boolean2;
      this.underlined = boolean3;
      this.strikethrough = boolean4;
      this.obfuscated = boolean5;
      this.clickEvent = clickEvent;
      this.hoverEvent = hoverEvent;
      this.insertion = string;
      this.font = resourceLocation;
   }

   @Nullable
   public TextColor getColor() {
      return this.color;
   }

   public boolean isBold() {
      return this.bold == Boolean.TRUE;
   }

   public boolean isItalic() {
      return this.italic == Boolean.TRUE;
   }

   public boolean isStrikethrough() {
      return this.strikethrough == Boolean.TRUE;
   }

   public boolean isUnderlined() {
      return this.underlined == Boolean.TRUE;
   }

   public boolean isObfuscated() {
      return this.obfuscated == Boolean.TRUE;
   }

   public boolean isEmpty() {
      return this == EMPTY;
   }

   @Nullable
   public ClickEvent getClickEvent() {
      return this.clickEvent;
   }

   @Nullable
   public HoverEvent getHoverEvent() {
      return this.hoverEvent;
   }

   @Nullable
   public String getInsertion() {
      return this.insertion;
   }

   public Identifier getFont() {
      return this.font != null ? this.font : DEFAULT_FONT;
   }

   public Style withColor(@Nullable TextColor textColor) {
      return new Style(textColor, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withColor(@Nullable ChatFormatting chatFormatting) {
      return this.withColor(chatFormatting != null ? TextColor.fromLegacyFormat(chatFormatting) : null);
   }

   public Style withBold(@Nullable Boolean boolean_) {
      return new Style(this.color, boolean_, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withItalic(@Nullable Boolean boolean_) {
      return new Style(this.color, this.bold, boolean_, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   @Environment(EnvType.CLIENT)
   public Style withUnderlined(@Nullable Boolean boolean_) {
      return new Style(this.color, this.bold, this.italic, boolean_, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withClickEvent(@Nullable ClickEvent clickEvent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style withHoverEvent(@Nullable HoverEvent hoverEvent) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, hoverEvent, this.insertion, this.font);
   }

   public Style withInsertion(@Nullable String string) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, string, this.font);
   }

   @Environment(EnvType.CLIENT)
   public Style withFont(@Nullable Identifier resourceLocation) {
      return new Style(this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion, resourceLocation);
   }

   public Style applyFormat(ChatFormatting chatFormatting) {
      TextColor textColor = this.color;
      Boolean boolean_ = this.bold;
      Boolean boolean2 = this.italic;
      Boolean boolean3 = this.strikethrough;
      Boolean boolean4 = this.underlined;
      Boolean boolean5 = this.obfuscated;
      switch(chatFormatting) {
      case OBFUSCATED:
         boolean5 = true;
         break;
      case BOLD:
         boolean_ = true;
         break;
      case STRIKETHROUGH:
         boolean3 = true;
         break;
      case UNDERLINE:
         boolean4 = true;
         break;
      case ITALIC:
         boolean2 = true;
         break;
      case RESET:
         return EMPTY;
      default:
         textColor = TextColor.fromLegacyFormat(chatFormatting);
      }

      return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   @Environment(EnvType.CLIENT)
   public Style applyLegacyFormat(ChatFormatting chatFormatting) {
      TextColor textColor = this.color;
      Boolean boolean_ = this.bold;
      Boolean boolean2 = this.italic;
      Boolean boolean3 = this.strikethrough;
      Boolean boolean4 = this.underlined;
      Boolean boolean5 = this.obfuscated;
      switch(chatFormatting) {
      case OBFUSCATED:
         boolean5 = true;
         break;
      case BOLD:
         boolean_ = true;
         break;
      case STRIKETHROUGH:
         boolean3 = true;
         break;
      case UNDERLINE:
         boolean4 = true;
         break;
      case ITALIC:
         boolean2 = true;
         break;
      case RESET:
         return EMPTY;
      default:
         boolean5 = false;
         boolean_ = false;
         boolean3 = false;
         boolean4 = false;
         boolean2 = false;
         textColor = TextColor.fromLegacyFormat(chatFormatting);
      }

      return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyFormats(ChatFormatting... chatFormattings) {
      TextColor textColor = this.color;
      Boolean boolean_ = this.bold;
      Boolean boolean2 = this.italic;
      Boolean boolean3 = this.strikethrough;
      Boolean boolean4 = this.underlined;
      Boolean boolean5 = this.obfuscated;
      ChatFormatting[] var8 = chatFormattings;
      int var9 = chatFormattings.length;

      for(int var10 = 0; var10 < var9; ++var10) {
         ChatFormatting chatFormatting = var8[var10];
         switch(chatFormatting) {
         case OBFUSCATED:
            boolean5 = true;
            break;
         case BOLD:
            boolean_ = true;
            break;
         case STRIKETHROUGH:
            boolean3 = true;
            break;
         case UNDERLINE:
            boolean4 = true;
            break;
         case ITALIC:
            boolean2 = true;
            break;
         case RESET:
            return EMPTY;
         default:
            textColor = TextColor.fromLegacyFormat(chatFormatting);
         }
      }

      return new Style(textColor, boolean_, boolean2, boolean4, boolean3, boolean5, this.clickEvent, this.hoverEvent, this.insertion, this.font);
   }

   public Style applyTo(Style style) {
      if (this == EMPTY) {
         return style;
      } else {
         return style == EMPTY ? this : new Style(this.color != null ? this.color : style.color, this.bold != null ? this.bold : style.bold, this.italic != null ? this.italic : style.italic, this.underlined != null ? this.underlined : style.underlined, this.strikethrough != null ? this.strikethrough : style.strikethrough, this.obfuscated != null ? this.obfuscated : style.obfuscated, this.clickEvent != null ? this.clickEvent : style.clickEvent, this.hoverEvent != null ? this.hoverEvent : style.hoverEvent, this.insertion != null ? this.insertion : style.insertion, this.font != null ? this.font : style.font);
      }
   }

   public String toString() {
      return "Style{ color=" + this.color + ", bold=" + this.bold + ", italic=" + this.italic + ", underlined=" + this.underlined + ", strikethrough=" + this.strikethrough + ", obfuscated=" + this.obfuscated + ", clickEvent=" + this.getClickEvent() + ", hoverEvent=" + this.getHoverEvent() + ", insertion=" + this.getInsertion() + ", font=" + this.getFont() + '}';
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof Style)) {
         return false;
      } else {
         Style style = (Style)object;
         return this.isBold() == style.isBold() && Objects.equals(this.getColor(), style.getColor()) && this.isItalic() == style.isItalic() && this.isObfuscated() == style.isObfuscated() && this.isStrikethrough() == style.isStrikethrough() && this.isUnderlined() == style.isUnderlined() && Objects.equals(this.getClickEvent(), style.getClickEvent()) && Objects.equals(this.getHoverEvent(), style.getHoverEvent()) && Objects.equals(this.getInsertion(), style.getInsertion()) && Objects.equals(this.getFont(), style.getFont());
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.color, this.bold, this.italic, this.underlined, this.strikethrough, this.obfuscated, this.clickEvent, this.hoverEvent, this.insertion});
   }

   public static class Serializer implements JsonDeserializer<Style>, JsonSerializer<Style> {
      @Nullable
      public Style deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if (jsonObject == null) {
               return null;
            } else {
               Boolean boolean_ = getOptionalFlag(jsonObject, "bold");
               Boolean boolean2 = getOptionalFlag(jsonObject, "italic");
               Boolean boolean3 = getOptionalFlag(jsonObject, "underlined");
               Boolean boolean4 = getOptionalFlag(jsonObject, "strikethrough");
               Boolean boolean5 = getOptionalFlag(jsonObject, "obfuscated");
               TextColor textColor = getTextColor(jsonObject);
               String string = getInsertion(jsonObject);
               ClickEvent clickEvent = getClickEvent(jsonObject);
               HoverEvent hoverEvent = getHoverEvent(jsonObject);
               Identifier resourceLocation = getFont(jsonObject);
               return new Style(textColor, boolean_, boolean2, boolean3, boolean4, boolean5, clickEvent, hoverEvent, string, resourceLocation);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static Identifier getFont(JsonObject jsonObject) {
         if (jsonObject.has("font")) {
            String string = GsonHelper.getAsString(jsonObject, "font");

            try {
               return Identifier.of(string);
            } catch (ResourceLocationException var3) {
               throw new JsonSyntaxException("Invalid font name: " + string);
            }
         } else {
            return null;
         }
      }

      @Nullable
      private static HoverEvent getHoverEvent(JsonObject jsonObject) {
         if (jsonObject.has("hoverEvent")) {
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "hoverEvent");
            HoverEvent hoverEvent = HoverEvent.deserialize(jsonObject2);
            if (hoverEvent != null && hoverEvent.getAction().isAllowedFromServer()) {
               return hoverEvent;
            }
         }

         return null;
      }

      @Nullable
      private static ClickEvent getClickEvent(JsonObject jsonObject) {
         if (jsonObject.has("clickEvent")) {
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "clickEvent");
            String string = GsonHelper.getAsString(jsonObject2, "action", (String)null);
            ClickEvent.Action action = string == null ? null : ClickEvent.Action.getByName(string);
            String string2 = GsonHelper.getAsString(jsonObject2, "value", (String)null);
            if (action != null && string2 != null && action.isAllowedFromServer()) {
               return new ClickEvent(action, string2);
            }
         }

         return null;
      }

      @Nullable
      private static String getInsertion(JsonObject jsonObject) {
         return GsonHelper.getAsString(jsonObject, "insertion", (String)null);
      }

      @Nullable
      private static TextColor getTextColor(JsonObject jsonObject) {
         if (jsonObject.has("color")) {
            String string = GsonHelper.getAsString(jsonObject, "color");
            return TextColor.parseColor(string);
         } else {
            return null;
         }
      }

      @Nullable
      private static Boolean getOptionalFlag(JsonObject jsonObject, String string) {
         return jsonObject.has(string) ? jsonObject.get(string).getAsBoolean() : null;
      }

      @Nullable
      public JsonElement serialize(Style style, Type type, JsonSerializationContext jsonSerializationContext) {
         if (style.isEmpty()) {
            return null;
         } else {
            JsonObject jsonObject = new JsonObject();
            if (style.bold != null) {
               jsonObject.addProperty("bold", style.bold);
            }

            if (style.italic != null) {
               jsonObject.addProperty("italic", style.italic);
            }

            if (style.underlined != null) {
               jsonObject.addProperty("underlined", style.underlined);
            }

            if (style.strikethrough != null) {
               jsonObject.addProperty("strikethrough", style.strikethrough);
            }

            if (style.obfuscated != null) {
               jsonObject.addProperty("obfuscated", style.obfuscated);
            }

            if (style.color != null) {
               jsonObject.addProperty("color", style.color.serialize());
            }

            if (style.insertion != null) {
               jsonObject.add("insertion", jsonSerializationContext.serialize(style.insertion));
            }

            if (style.clickEvent != null) {
               JsonObject jsonObject2 = new JsonObject();
               jsonObject2.addProperty("action", style.clickEvent.getAction().getName());
               jsonObject2.addProperty("value", style.clickEvent.getValue());
               jsonObject.add("clickEvent", jsonObject2);
            }

            if (style.hoverEvent != null) {
               jsonObject.add("hoverEvent", style.hoverEvent.serialize());
            }

            if (style.font != null) {
               jsonObject.addProperty("font", style.font.toString());
            }

            return jsonObject;
         }
      }
   }
}
