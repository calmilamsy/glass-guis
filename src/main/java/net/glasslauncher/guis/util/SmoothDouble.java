package net.glasslauncher.guis.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SmoothDouble {
   private double targetValue;
   private double remainingValue;
   private double lastAmount;

   public double getNewDeltaValue(double d, double e) {
      this.targetValue += d;
      double f = this.targetValue - this.remainingValue;
      double g = Mth.lerp(0.5D, this.lastAmount, f);
      double h = Math.signum(f);
      if (h * f > h * this.lastAmount) {
         f = g;
      }

      this.lastAmount = g;
      this.remainingValue += f * e;
      return f * e;
   }

   public void reset() {
      this.targetValue = 0.0D;
      this.remainingValue = 0.0D;
      this.lastAmount = 0.0D;
   }
}
