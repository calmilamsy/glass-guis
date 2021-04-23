package net.glasslauncher.guis.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;

public class UniformInt {
   public static final Codec<UniformInt> CODEC;
   private final int baseValue;
   private final int spread;

   public static Codec<UniformInt> codec(int i, int j, int k) {
      Function<UniformInt, DataResult<UniformInt>> function = (uniformInt) -> {
         if (uniformInt.baseValue >= i && uniformInt.baseValue <= j) {
            return uniformInt.spread <= k ? DataResult.success(uniformInt) : DataResult.error("Spread too big: " + uniformInt.spread + " > " + k);
         } else {
            return DataResult.error("Base value out of range: " + uniformInt.baseValue + " [" + i + "-" + j + "]");
         }
      };
      return CODEC.flatXmap(function, function);
   }

   private UniformInt(int i, int j) {
      this.baseValue = i;
      this.spread = j;
   }

   public static UniformInt fixed(int i) {
      return new UniformInt(i, 0);
   }

   public static UniformInt of(int i, int j) {
      return new UniformInt(i, j);
   }

   public int sample(Random random) {
      return this.spread == 0 ? this.baseValue : this.baseValue + random.nextInt(this.spread + 1);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (object != null && this.getClass() == object.getClass()) {
         UniformInt uniformInt = (UniformInt)object;
         return this.baseValue == uniformInt.baseValue && this.spread == uniformInt.spread;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.baseValue, this.spread});
   }

   public String toString() {
      return "[" + this.baseValue + '-' + (this.baseValue + this.spread) + ']';
   }

   static {
      CODEC = Codec.either(Codec.INT, RecordCodecBuilder.create((instance) -> {
         return instance.group(Codec.INT.fieldOf("base").forGetter((uniformInt) -> {
            return uniformInt.baseValue;
         }), Codec.INT.fieldOf("spread").forGetter((uniformInt) -> {
            return uniformInt.spread;
         })).apply(instance, (BiFunction)(UniformInt::new));
      }).comapFlatMap((uniformInt) -> {
         return uniformInt.spread < 0 ? DataResult.error("Spread must be non-negative, got: " + uniformInt.spread) : DataResult.success(uniformInt);
      }, Function.identity())).xmap((either) -> {
         return (UniformInt)either.map(UniformInt::fixed, (uniformInt) -> {
            return uniformInt;
         });
      }, (uniformInt) -> {
         return uniformInt.spread == 0 ? Either.left(uniformInt.baseValue) : Either.right(uniformInt);
      });
   }
}
