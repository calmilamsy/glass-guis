package net.glasslauncher.guis.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class CubicSampler {
   private static final double[] GAUSSIAN_SAMPLE_KERNEL = new double[]{0.0D, 1.0D, 4.0D, 6.0D, 4.0D, 1.0D, 0.0D};

   @NotNull
   @Environment(EnvType.CLIENT)
   public static Vec3 gaussianSampleVec3(Vec3 vec3, Vec3Fetcher vec3Fetcher) {
      int i = Mth.floor(vec3.x());
      int j = Mth.floor(vec3.y());
      int k = Mth.floor(vec3.z());
      double d = vec3.x() - (double)i;
      double e = vec3.y() - (double)j;
      double f = vec3.z() - (double)k;
      double g = 0.0D;
      Vec3 vec32 = Vec3.ZERO;

      for(int l = 0; l < 6; ++l) {
         double h = Mth.lerp(d, GAUSSIAN_SAMPLE_KERNEL[l + 1], GAUSSIAN_SAMPLE_KERNEL[l]);
         int m = i - 2 + l;

         for(int n = 0; n < 6; ++n) {
            double o = Mth.lerp(e, GAUSSIAN_SAMPLE_KERNEL[n + 1], GAUSSIAN_SAMPLE_KERNEL[n]);
            int p = j - 2 + n;

            for(int q = 0; q < 6; ++q) {
               double r = Mth.lerp(f, GAUSSIAN_SAMPLE_KERNEL[q + 1], GAUSSIAN_SAMPLE_KERNEL[q]);
               int s = k - 2 + q;
               double t = h * o * r;
               g += t;
               vec32 = vec32.add(vec3Fetcher.fetch(m, p, s).scale(t));
            }
         }
      }

      vec32 = vec32.scale(1.0D / g);
      return vec32;
   }

   public interface Vec3Fetcher {
      Vec3 fetch(int i, int j, int k);
   }
}
