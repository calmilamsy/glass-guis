package net.glasslauncher.guis.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.glasslauncher.guis.Util;
import net.minecraft.util.Vec3i;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Random;
import java.util.UUID;
import java.util.function.IntPredicate;

public class Mth {
   public static final float SQRT_OF_TWO = sqrt(2.0F);
   private static final float[] SIN = (float[]) Util.make(new float[65536], (fs) -> {
      for(int i = 0; i < fs.length; ++i) {
         fs[i] = (float)Math.sin((double)i * 3.141592653589793D * 2.0D / 65536.0D);
      }

   });
   private static final Random RANDOM = new Random();
   private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = new int[]{0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};
   private static final double FRAC_BIAS = Double.longBitsToDouble(4805340802404319232L);
   private static final double[] ASIN_TAB = new double[257];
   private static final double[] COS_TAB = new double[257];

   public static float sin(float f) {
      return SIN[(int)(f * 10430.378F) & '\uffff'];
   }

   public static float cos(float f) {
      return SIN[(int)(f * 10430.378F + 16384.0F) & '\uffff'];
   }

   public static float sqrt(float f) {
      return (float)Math.sqrt((double)f);
   }

   public static float sqrt(double d) {
      return (float)Math.sqrt(d);
   }

   public static int floor(float f) {
      int i = (int)f;
      return f < (float)i ? i - 1 : i;
   }

   @Environment(EnvType.CLIENT)
   public static int fastFloor(double d) {
      return (int)(d + 1024.0D) - 1024;
   }

   public static int floor(double d) {
      int i = (int)d;
      return d < (double)i ? i - 1 : i;
   }

   public static long lfloor(double d) {
      long l = (long)d;
      return d < (double)l ? l - 1L : l;
   }

   public static float abs(float f) {
      return Math.abs(f);
   }

   public static int abs(int i) {
      return Math.abs(i);
   }

   public static int ceil(float f) {
      int i = (int)f;
      return f > (float)i ? i + 1 : i;
   }

   public static int ceil(double d) {
      int i = (int)d;
      return d > (double)i ? i + 1 : i;
   }

   public static int clamp(int i, int j, int k) {
      if (i < j) {
         return j;
      } else {
         return i > k ? k : i;
      }
   }

   @Environment(EnvType.CLIENT)
   public static long clamp(long l, long m, long n) {
      if (l < m) {
         return m;
      } else {
         return l > n ? n : l;
      }
   }

   public static float clamp(float f, float g, float h) {
      if (f < g) {
         return g;
      } else {
         return f > h ? h : f;
      }
   }

   public static double clamp(double d, double e, double f) {
      if (d < e) {
         return e;
      } else {
         return d > f ? f : d;
      }
   }

   public static double clampedLerp(double d, double e, double f) {
      if (f < 0.0D) {
         return d;
      } else {
         return f > 1.0D ? e : lerp(f, d, e);
      }
   }

   public static double absMax(double d, double e) {
      if (d < 0.0D) {
         d = -d;
      }

      if (e < 0.0D) {
         e = -e;
      }

      return d > e ? d : e;
   }

   public static int intFloorDiv(int i, int j) {
      return Math.floorDiv(i, j);
   }

   public static int nextInt(Random random, int i, int j) {
      return i >= j ? i : random.nextInt(j - i + 1) + i;
   }

   public static float nextFloat(Random random, float f, float g) {
      return f >= g ? f : random.nextFloat() * (g - f) + f;
   }

   public static double nextDouble(Random random, double d, double e) {
      return d >= e ? d : random.nextDouble() * (e - d) + d;
   }

   public static double average(long[] ls) {
      long l = 0L;
      long[] var3 = ls;
      int var4 = ls.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         long m = var3[var5];
         l += m;
      }

      return (double)l / (double)ls.length;
   }

   @Environment(EnvType.CLIENT)
   public static boolean equal(float f, float g) {
      return Math.abs(g - f) < 1.0E-5F;
   }

   public static boolean equal(double d, double e) {
      return Math.abs(e - d) < 9.999999747378752E-6D;
   }

   public static int positiveModulo(int i, int j) {
      return Math.floorMod(i, j);
   }

   @Environment(EnvType.CLIENT)
   public static float positiveModulo(float f, float g) {
      return (f % g + g) % g;
   }

   @Environment(EnvType.CLIENT)
   public static double positiveModulo(double d, double e) {
      return (d % e + e) % e;
   }

   @Environment(EnvType.CLIENT)
   public static int wrapDegrees(int i) {
      int j = i % 360;
      if (j >= 180) {
         j -= 360;
      }

      if (j < -180) {
         j += 360;
      }

      return j;
   }

   public static float wrapDegrees(float f) {
      float g = f % 360.0F;
      if (g >= 180.0F) {
         g -= 360.0F;
      }

      if (g < -180.0F) {
         g += 360.0F;
      }

      return g;
   }

   public static double wrapDegrees(double d) {
      double e = d % 360.0D;
      if (e >= 180.0D) {
         e -= 360.0D;
      }

      if (e < -180.0D) {
         e += 360.0D;
      }

      return e;
   }

   public static float degreesDifference(float f, float g) {
      return wrapDegrees(g - f);
   }

   public static float degreesDifferenceAbs(float f, float g) {
      return abs(degreesDifference(f, g));
   }

   public static float rotateIfNecessary(float f, float g, float h) {
      float i = degreesDifference(f, g);
      float j = clamp(i, -h, h);
      return g - j;
   }

   public static float approach(float f, float g, float h) {
      h = abs(h);
      return f < g ? clamp(f + h, f, g) : clamp(f - h, g, f);
   }

   public static float approachDegrees(float f, float g, float h) {
      float i = degreesDifference(f, g);
      return approach(f, f + i, h);
   }

   @Environment(EnvType.CLIENT)
   public static int getInt(String string, int i) {
      return NumberUtils.toInt(string, i);
   }

   public static int smallestEncompassingPowerOfTwo(int i) {
      int j = i - 1;
      j |= j >> 1;
      j |= j >> 2;
      j |= j >> 4;
      j |= j >> 8;
      j |= j >> 16;
      return j + 1;
   }

   public static boolean isPowerOfTwo(int i) {
      return i != 0 && (i & i - 1) == 0;
   }

   public static int ceillog2(int i) {
      i = isPowerOfTwo(i) ? i : smallestEncompassingPowerOfTwo(i);
      return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int)((long)i * 125613361L >> 27) & 31];
   }

   public static int log2(int i) {
      return ceillog2(i) - (isPowerOfTwo(i) ? 0 : 1);
   }

   public static int roundUp(int i, int j) {
      if (j == 0) {
         return 0;
      } else if (i == 0) {
         return j;
      } else {
         if (i < 0) {
            j *= -1;
         }

         int k = i % j;
         return k == 0 ? i : i + j - k;
      }
   }

   @Environment(EnvType.CLIENT)
   public static int color(float f, float g, float h) {
      return color(floor(f * 255.0F), floor(g * 255.0F), floor(h * 255.0F));
   }

   @Environment(EnvType.CLIENT)
   public static int color(int i, int j, int k) {
      int l = (i << 8) + j;
      l = (l << 8) + k;
      return l;
   }

   public static float frac(float f) {
      return f - (float)floor(f);
   }

   public static double frac(double d) {
      return d - (double)lfloor(d);
   }

   public static long getSeed(Vec3i vec3i) {
      return getSeed(vec3i.x, vec3i.y, vec3i.z);
   }

   public static long getSeed(int i, int j, int k) {
      long l = (long)(i * 3129871) ^ (long)k * 116129781L ^ (long)j;
      l = l * l * 42317861L + l * 11L;
      return l >> 16;
   }

   public static UUID createInsecureUUID(Random random) {
      long l = random.nextLong() & -61441L | 16384L;
      long m = random.nextLong() & 4611686018427387903L | Long.MIN_VALUE;
      return new UUID(l, m);
   }

   public static UUID createInsecureUUID() {
      return createInsecureUUID(RANDOM);
   }

   public static double inverseLerp(double d, double e, double f) {
      return (d - e) / (f - e);
   }

   public static double atan2(double d, double e) {
      double f = e * e + d * d;
      if (Double.isNaN(f)) {
         return Double.NaN;
      } else {
         boolean bl = d < 0.0D;
         if (bl) {
            d = -d;
         }

         boolean bl2 = e < 0.0D;
         if (bl2) {
            e = -e;
         }

         boolean bl3 = d > e;
         double h;
         if (bl3) {
            h = e;
            e = d;
            d = h;
         }

         h = fastInvSqrt(f);
         e *= h;
         d *= h;
         double i = FRAC_BIAS + d;
         int j = (int)Double.doubleToRawLongBits(i);
         double k = ASIN_TAB[j];
         double l = COS_TAB[j];
         double m = i - FRAC_BIAS;
         double n = d * l - e * m;
         double o = (6.0D + n * n) * n * 0.16666666666666666D;
         double p = k + o;
         if (bl3) {
            p = 1.5707963267948966D - p;
         }

         if (bl2) {
            p = 3.141592653589793D - p;
         }

         if (bl) {
            p = -p;
         }

         return p;
      }
   }

   @Environment(EnvType.CLIENT)
   public static float fastInvSqrt(float f) {
      float g = 0.5F * f;
      int i = Float.floatToIntBits(f);
      i = 1597463007 - (i >> 1);
      f = Float.intBitsToFloat(i);
      f *= 1.5F - g * f * f;
      return f;
   }

   public static double fastInvSqrt(double d) {
      double e = 0.5D * d;
      long l = Double.doubleToRawLongBits(d);
      l = 6910469410427058090L - (l >> 1);
      d = Double.longBitsToDouble(l);
      d *= 1.5D - e * d * d;
      return d;
   }

   @Environment(EnvType.CLIENT)
   public static float fastInvCubeRoot(float f) {
      int i = Float.floatToIntBits(f);
      i = 1419967116 - i / 3;
      float g = Float.intBitsToFloat(i);
      g = 0.6666667F * g + 1.0F / (3.0F * g * g * f);
      g = 0.6666667F * g + 1.0F / (3.0F * g * g * f);
      return g;
   }

   public static int hsvToRgb(float f, float g, float h) {
      int i = (int)(f * 6.0F) % 6;
      float j = f * 6.0F - (float)i;
      float k = h * (1.0F - g);
      float l = h * (1.0F - j * g);
      float m = h * (1.0F - (1.0F - j) * g);
      float af;
      float ag;
      float ah;
      switch(i) {
      case 0:
         af = h;
         ag = m;
         ah = k;
         break;
      case 1:
         af = l;
         ag = h;
         ah = k;
         break;
      case 2:
         af = k;
         ag = h;
         ah = m;
         break;
      case 3:
         af = k;
         ag = l;
         ah = h;
         break;
      case 4:
         af = m;
         ag = k;
         ah = h;
         break;
      case 5:
         af = h;
         ag = k;
         ah = l;
         break;
      default:
         throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + f + ", " + g + ", " + h);
      }

      int ai = clamp((int)(af * 255.0F), 0, 255);
      int aj = clamp((int)(ag * 255.0F), 0, 255);
      int ak = clamp((int)(ah * 255.0F), 0, 255);
      return ai << 16 | aj << 8 | ak;
   }

   public static int murmurHash3Mixer(int i) {
      i ^= i >>> 16;
      i *= -2048144789;
      i ^= i >>> 13;
      i *= -1028477387;
      i ^= i >>> 16;
      return i;
   }

   public static int binarySearch(int i, int j, IntPredicate intPredicate) {
      int k = j - i;

      while(k > 0) {
         int l = k / 2;
         int m = i + l;
         if (intPredicate.test(m)) {
            k = l;
         } else {
            i = m + 1;
            k -= l + 1;
         }
      }

      return i;
   }

   public static float lerp(float f, float g, float h) {
      return g + f * (h - g);
   }

   public static double lerp(double d, double e, double f) {
      return e + d * (f - e);
   }

   public static double lerp2(double d, double e, double f, double g, double h, double i) {
      return lerp(e, lerp(d, f, g), lerp(d, h, i));
   }

   public static double lerp3(double d, double e, double f, double g, double h, double i, double j, double k, double l, double m, double n) {
      return lerp(f, lerp2(d, e, g, h, i, j), lerp2(d, e, k, l, m, n));
   }

   public static double smoothstep(double d) {
      return d * d * d * (d * (d * 6.0D - 15.0D) + 10.0D);
   }

   public static int sign(double d) {
      if (d == 0.0D) {
         return 0;
      } else {
         return d > 0.0D ? 1 : -1;
      }
   }

   @Environment(EnvType.CLIENT)
   public static float rotLerp(float f, float g, float h) {
      return g + f * wrapDegrees(h - g);
   }

   @Deprecated
   public static float rotlerp(float f, float g, float h) {
      float i;
      for(i = g - f; i < -180.0F; i += 360.0F) {
      }

      while(i >= 180.0F) {
         i -= 360.0F;
      }

      return f + h * i;
   }

   @Deprecated
   @Environment(EnvType.CLIENT)
   public static float rotWrap(double d) {
      while(d >= 180.0D) {
         d -= 360.0D;
      }

      while(d < -180.0D) {
         d += 360.0D;
      }

      return (float)d;
   }

   @Environment(EnvType.CLIENT)
   public static float triangleWave(float f, float g) {
      return (Math.abs(f % g - g * 0.5F) - g * 0.25F) / (g * 0.25F);
   }

   public static float square(float f) {
      return f * f;
   }

   static {
      for(int i = 0; i < 257; ++i) {
         double d = (double)i / 256.0D;
         double e = Math.asin(d);
         COS_TAB[i] = Math.cos(e);
         ASIN_TAB[i] = e;
      }

   }
}
