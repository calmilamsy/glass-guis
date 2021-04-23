package net.glasslauncher.guis.chat;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.Util;
import net.glasslauncher.guis.util.FormattedCharSequence;
import net.glasslauncher.guis.util.GsonHelper;
import net.glasslauncher.guis.util.LowerCaseEnumTypeAdapterFactory;
import net.modificationstation.stationapi.api.common.registry.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

public interface Component extends Message, FormattedText {
   Style getStyle();

   String getContents();

   default String getString() {
      return FormattedText.super.getString();
   }

   default String getString(int i) {
      StringBuilder stringBuilder = new StringBuilder();
      this.visit((string) -> {
         int j = i - stringBuilder.length();
         if (j <= 0) {
            return STOP_ITERATION;
         } else {
            stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
            return Optional.empty();
         }
      });
      return stringBuilder.toString();
   }

   List<Component> getSiblings();

   MutableComponent plainCopy();

   MutableComponent copy();

   @Environment(EnvType.CLIENT)
   FormattedCharSequence getVisualOrderText();

   @Environment(EnvType.CLIENT)
   default <T> Optional<T> visit(StyledContentConsumer<T> styledContentConsumer, Style style) {
      Style style2 = this.getStyle().applyTo(style);
      Optional<T> optional = this.visitSelf(styledContentConsumer, style2);
      if (optional.isPresent()) {
         return optional;
      } else {
         Iterator var5 = this.getSiblings().iterator();

         Optional optional2;
         do {
            if (!var5.hasNext()) {
               return Optional.empty();
            }

            Component component = (Component)var5.next();
            optional2 = component.visit(styledContentConsumer, style2);
         } while(!optional2.isPresent());

         return optional2;
      }
   }

   default <T> Optional<T> visit(ContentConsumer<T> contentConsumer) {
      Optional<T> optional = this.visitSelf(contentConsumer);
      if (optional.isPresent()) {
         return optional;
      } else {
         Iterator var3 = this.getSiblings().iterator();

         Optional optional2;
         do {
            if (!var3.hasNext()) {
               return Optional.empty();
            }

            Component component = (Component)var3.next();
            optional2 = component.visit(contentConsumer);
         } while(!optional2.isPresent());

         return optional2;
      }
   }

   @Environment(EnvType.CLIENT)
   default <T> Optional<T> visitSelf(StyledContentConsumer<T> styledContentConsumer, Style style) {
      return styledContentConsumer.accept(style, this.getContents());
   }

   default <T> Optional<T> visitSelf(ContentConsumer<T> contentConsumer) {
      return contentConsumer.accept(this.getContents());
   }

   @Environment(EnvType.CLIENT)
   static Component nullToEmpty(@Nullable String string) {
      return string != null ? new TextComponent(string) : TextComponent.EMPTY;
   }

   public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
      private static final Gson GSON = Util.make(() -> {
         GsonBuilder gsonBuilder = new GsonBuilder();
         gsonBuilder.disableHtmlEscaping();
         gsonBuilder.registerTypeHierarchyAdapter(Component.class, new Serializer());
         gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
         gsonBuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
         return gsonBuilder.create();
      });
      private static final Field JSON_READER_POS = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("pos");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
         }
      });
      private static final Field JSON_READER_LINESTART = Util.make(() -> {
         try {
            new JsonReader(new StringReader(""));
            Field field = JsonReader.class.getDeclaredField("lineStart");
            field.setAccessible(true);
            return field;
         } catch (NoSuchFieldException var1) {
            throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
         }
      });

      public MutableComponent deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
         if (jsonElement.isJsonPrimitive()) {
            return new TextComponent(jsonElement.getAsString());
         } else if (!jsonElement.isJsonObject()) {
            if (jsonElement.isJsonArray()) {
               JsonArray jsonArray3 = jsonElement.getAsJsonArray();
               MutableComponent mutableComponent13 = null;
               Iterator var14 = jsonArray3.iterator();

               while(var14.hasNext()) {
                  JsonElement jsonElement2 = (JsonElement)var14.next();
                  MutableComponent mutableComponent14 = this.deserialize(jsonElement2, jsonElement2.getClass(), jsonDeserializationContext);
                  if (mutableComponent13 == null) {
                     mutableComponent13 = mutableComponent14;
                  } else {
                     mutableComponent13.append(mutableComponent14);
                  }
               }

               return mutableComponent13;
            } else {
               throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
            }
         } else {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Object mutableComponent12;
            if (jsonObject.has("text")) {
               mutableComponent12 = new TextComponent(GsonHelper.getAsString(jsonObject, "text"));
            } else {
               String string2;
               if (jsonObject.has("translate")) {
                  string2 = GsonHelper.getAsString(jsonObject, "translate");
                  if (jsonObject.has("with")) {
                     JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "with");
                     Object[] objects = new Object[jsonArray.size()];

                     for(int i = 0; i < objects.length; ++i) {
                        objects[i] = this.deserialize(jsonArray.get(i), type, jsonDeserializationContext);
                        if (objects[i] instanceof TextComponent) {
                           TextComponent textComponent = (TextComponent)objects[i];
                           if (textComponent.getStyle().isEmpty() && textComponent.getSiblings().isEmpty()) {
                              objects[i] = textComponent.getText();
                           }
                        }
                     }

                     mutableComponent12 = new TranslatableComponent(string2, objects);
                  } else {
                     mutableComponent12 = new TranslatableComponent(string2);
                  }
               } else if (jsonObject.has("score")) {
                  JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
                  if (!jsonObject2.has("name") || !jsonObject2.has("objective")) {
                     throw new JsonParseException("A score component needs a least a name and an objective");
                  }

                  mutableComponent12 = new ScoreComponent(GsonHelper.getAsString(jsonObject2, "name"), GsonHelper.getAsString(jsonObject2, "objective"));
               } else if (jsonObject.has("selector")) {
                  mutableComponent12 = new SelectorComponent(GsonHelper.getAsString(jsonObject, "selector"));
               } else if (jsonObject.has("keybind")) {
                  mutableComponent12 = new KeybindComponent(GsonHelper.getAsString(jsonObject, "keybind"));
               } else {
                  if (!jsonObject.has("nbt")) {
                     throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                  }

                  string2 = GsonHelper.getAsString(jsonObject, "nbt");
                  boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
                  if (jsonObject.has("block")) {
                     mutableComponent12 = new NbtComponent.BlockNbtComponent(string2, bl, GsonHelper.getAsString(jsonObject, "block"));
                  } else if (jsonObject.has("entity")) {
                     mutableComponent12 = new NbtComponent.EntityNbtComponent(string2, bl, GsonHelper.getAsString(jsonObject, "entity"));
                  } else {
                     if (!jsonObject.has("storage")) {
                        throw new JsonParseException("Don't know how to turn " + jsonElement + " into a Component");
                     }

                     mutableComponent12 = new NbtComponent.StorageNbtComponent(string2, bl, Identifier.of(GsonHelper.getAsString(jsonObject, "storage")));
                  }
               }
            }

            if (jsonObject.has("extra")) {
               JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
               if (jsonArray2.size() <= 0) {
                  throw new JsonParseException("Unexpected empty array of components");
               }

               for(int j = 0; j < jsonArray2.size(); ++j) {
                  ((MutableComponent)mutableComponent12).append(this.deserialize(jsonArray2.get(j), type, jsonDeserializationContext));
               }
            }

            ((MutableComponent)mutableComponent12).setStyle(jsonDeserializationContext.deserialize(jsonElement, Style.class));
            return (MutableComponent)mutableComponent12;
         }
      }

      private void serializeStyle(Style style, JsonObject jsonObject, JsonSerializationContext jsonSerializationContext) {
         JsonElement jsonElement = jsonSerializationContext.serialize(style);
         if (jsonElement.isJsonObject()) {
            JsonObject jsonObject2 = (JsonObject)jsonElement;
            Iterator var6 = jsonObject2.entrySet().iterator();

            while(var6.hasNext()) {
               Entry<String, JsonElement> entry = (Entry)var6.next();
               jsonObject.add(entry.getKey(), entry.getValue());
            }
         }

      }

      public JsonElement serialize(Component component, Type type, JsonSerializationContext jsonSerializationContext) {
         JsonObject jsonObject = new JsonObject();
         if (!component.getStyle().isEmpty()) {
            this.serializeStyle(component.getStyle(), jsonObject, jsonSerializationContext);
         }

         if (!component.getSiblings().isEmpty()) {
            JsonArray jsonArray = new JsonArray();
            Iterator var6 = component.getSiblings().iterator();

            while(var6.hasNext()) {
               Component component2 = (Component)var6.next();
               jsonArray.add(this.serialize(component2, component2.getClass(), jsonSerializationContext));
            }

            jsonObject.add("extra", jsonArray);
         }

         if (component instanceof TextComponent) {
            jsonObject.addProperty("text", ((TextComponent)component).getText());
         } else if (component instanceof TranslatableComponent) {
            TranslatableComponent translatableComponent = (TranslatableComponent)component;
            jsonObject.addProperty("translate", translatableComponent.getKey());
            if (translatableComponent.getArgs() != null && translatableComponent.getArgs().length > 0) {
               JsonArray jsonArray2 = new JsonArray();
               Object[] var19 = translatableComponent.getArgs();
               int var8 = var19.length;

               for(int var9 = 0; var9 < var8; ++var9) {
                  Object object = var19[var9];
                  if (object instanceof Component) {
                     jsonArray2.add(this.serialize(object, object.getClass(), jsonSerializationContext));
                  } else {
                     jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
                  }
               }

               jsonObject.add("with", jsonArray2);
            }
         } else if (component instanceof ScoreComponent) {
            ScoreComponent scoreComponent = (ScoreComponent)component;
            JsonObject jsonObject2 = new JsonObject();
            jsonObject2.addProperty("name", scoreComponent.getName());
            jsonObject2.addProperty("objective", scoreComponent.getObjective());
            jsonObject.add("score", jsonObject2);
         } else if (component instanceof SelectorComponent) {
            SelectorComponent selectorComponent = (SelectorComponent)component;
            jsonObject.addProperty("selector", selectorComponent.getPattern());
         } else if (component instanceof KeybindComponent) {
            KeybindComponent keybindComponent = (KeybindComponent)component;
            jsonObject.addProperty("keybind", keybindComponent.getName());
         } else {
            if (!(component instanceof NbtComponent)) {
               throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
            }

            NbtComponent nbtComponent = (NbtComponent)component;
            jsonObject.addProperty("nbt", nbtComponent.getNbtPath());
            jsonObject.addProperty("interpret", nbtComponent.isInterpreting());
            if (component instanceof NbtComponent.BlockNbtComponent) {
               NbtComponent.BlockNbtComponent blockNbtComponent = (NbtComponent.BlockNbtComponent)component;
               jsonObject.addProperty("block", blockNbtComponent.getPos());
            } else if (component instanceof NbtComponent.EntityNbtComponent) {
               NbtComponent.EntityNbtComponent entityNbtComponent = (NbtComponent.EntityNbtComponent)component;
               jsonObject.addProperty("entity", entityNbtComponent.getSelector());
            } else {
               if (!(component instanceof NbtComponent.StorageNbtComponent)) {
                  throw new IllegalArgumentException("Don't know how to serialize " + component + " as a Component");
               }

               NbtComponent.StorageNbtComponent storageNbtComponent = (NbtComponent.StorageNbtComponent)component;
               jsonObject.addProperty("storage", storageNbtComponent.getId().toString());
            }
         }

         return jsonObject;
      }

      public static String toJson(Component component) {
         return GSON.toJson(component);
      }

      public static JsonElement toJsonTree(Component component) {
         return GSON.toJsonTree(component);
      }

      @Nullable
      public static MutableComponent fromJson(String string) {
         return GsonHelper.fromJson(GSON, string, MutableComponent.class, false);
      }

      @Nullable
      public static MutableComponent fromJson(JsonElement jsonElement) {
         return GSON.fromJson(jsonElement, MutableComponent.class);
      }

      @Nullable
      public static MutableComponent fromJsonLenient(String string) {
         return GsonHelper.fromJson(GSON, string, MutableComponent.class, true);
      }

      public static MutableComponent fromJson(com.mojang.brigadier.StringReader stringReader) {
         try {
            JsonReader jsonReader = new JsonReader(new StringReader(stringReader.getRemaining()));
            jsonReader.setLenient(false);
            MutableComponent mutableComponent = GSON.getAdapter(MutableComponent.class).read(jsonReader);
            stringReader.setCursor(stringReader.getCursor() + getPos(jsonReader));
            return mutableComponent;
         } catch (StackOverflowError | IOException var3) {
            throw new JsonParseException(var3);
         }
      }

      private static int getPos(JsonReader jsonReader) {
         try {
            return JSON_READER_POS.getInt(jsonReader) - JSON_READER_LINESTART.getInt(jsonReader) + 1;
         } catch (IllegalAccessException var2) {
            throw new IllegalStateException("Couldn't read position of JsonReader", var2);
         }
      }
   }
}
