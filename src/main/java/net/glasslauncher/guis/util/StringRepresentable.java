package net.glasslauncher.guis.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public interface StringRepresentable {
   String getSerializedName();

   static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> supplier, Function<? super String, ? extends E> function) {
      E[] enums = (Enum[])supplier.get();
      return fromStringResolver(Enum::ordinal, (i) -> {
         return enums[i];
      }, function);
   }

   static <E extends StringRepresentable> Codec<E> fromStringResolver(final ToIntFunction<E> toIntFunction, final IntFunction<E> intFunction, final Function<? super String, ? extends E> function) {
      return new Codec<E>() {
         public <T> DataResult<T> encode(E stringRepresentable, DynamicOps<T> dynamicOps, T object) {
            return dynamicOps.compressMaps() ? dynamicOps.mergeToPrimitive(object, dynamicOps.createInt(toIntFunction.applyAsInt(stringRepresentable))) : dynamicOps.mergeToPrimitive(object, dynamicOps.createString(stringRepresentable.getSerializedName()));
         }

         public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
            return dynamicOps.compressMaps() ? dynamicOps.getNumberValue(object).flatMap((number) -> {
               return (DataResult)Optional.ofNullable(intFunction.apply(number.intValue())).map(DataResult::success).orElseGet(() -> {
                  return DataResult.error("Unknown element id: " + number);
               });
            }).map((stringRepresentable) -> {
               return Pair.of(stringRepresentable, dynamicOps.empty());
            }) : dynamicOps.getStringValue(object).flatMap((string) -> {
               return (DataResult)Optional.ofNullable(function.apply(string)).map(DataResult::success).orElseGet(() -> {
                  return DataResult.error("Unknown element name: " + string);
               });
            }).map((stringRepresentable) -> {
               return Pair.of(stringRepresentable, dynamicOps.empty());
            });
         }

         public String toString() {
            return "StringRepresentable[" + toIntFunction + "]";
         }
      };
   }

   static Keyable keys(final StringRepresentable[] stringRepresentables) {
      return new Keyable() {
         public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
            if (dynamicOps.compressMaps()) {
               IntStream var2 = IntStream.range(0, stringRepresentables.length);
               dynamicOps.getClass();
               return var2.mapToObj(dynamicOps::createInt);
            } else {
               Stream var10000 = Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName);
               dynamicOps.getClass();
               return var10000.map(dynamicOps::createString);
            }
         }
      };
   }
}
