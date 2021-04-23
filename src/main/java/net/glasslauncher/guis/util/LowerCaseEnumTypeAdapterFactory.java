package net.glasslauncher.guis.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

public class LowerCaseEnumTypeAdapterFactory implements TypeAdapterFactory {
   @Nullable
   public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
      Class<T> class_ = (Class<T>) typeToken.getRawType();
      if (!class_.isEnum()) {
         return null;
      } else {
         final Map<String, T> map = Maps.newHashMap();
         Object[] var5 = class_.getEnumConstants();
         int var6 = var5.length;

         for (Object object : var5) {
            map.put(this.toLowercase(object), (T) object);
         }

         return new TypeAdapter<T>() {
            public void write(JsonWriter jsonWriter, T object) throws IOException {
               if (object == null) {
                  jsonWriter.nullValue();
               } else {
                  jsonWriter.value(LowerCaseEnumTypeAdapterFactory.this.toLowercase(object));
               }

            }

            @Nullable
            public T read(JsonReader jsonReader) throws IOException {
               if (jsonReader.peek() == JsonToken.NULL) {
                  jsonReader.nextNull();
                  return null;
               } else {
                  return map.get(jsonReader.nextString());
               }
            }
         };
      }
   }

   private String toLowercase(Object object) {
      return object instanceof Enum ? ((Enum)object).name().toLowerCase(Locale.ROOT) : object.toString().toLowerCase(Locale.ROOT);
   }
}
