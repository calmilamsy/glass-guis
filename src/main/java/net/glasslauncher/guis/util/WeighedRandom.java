package net.glasslauncher.guis.util;

import net.glasslauncher.guis.Util;

import java.util.List;
import java.util.Random;

public class WeighedRandom {
   public static int getTotalWeight(List<? extends WeighedRandomItem> list) {
      int i = 0;
      int j = 0;

      for(int k = list.size(); j < k; ++j) {
         WeighedRandomItem weighedRandomItem = (WeighedRandomItem)list.get(j);
         i += weighedRandomItem.weight;
      }

      return i;
   }

   public static <T extends WeighedRandomItem> T getRandomItem(Random random, List<T> list, int i) {
      if (i <= 0) {
         throw (IllegalArgumentException) Util.pauseInIde(new IllegalArgumentException());
      } else {
         int j = random.nextInt(i);
         return getWeightedItem(list, j);
      }
   }

   public static <T extends WeighedRandomItem> T getWeightedItem(List<T> list, int i) {
      int j = 0;

      for(int k = list.size(); j < k; ++j) {
         T weighedRandomItem = list.get(j);
         i -= weighedRandomItem.weight;
         if (i < 0) {
            return weighedRandomItem;
         }
      }

      return null;
   }

   public static <T extends WeighedRandomItem> T getRandomItem(Random random, List<T> list) {
      return getRandomItem(random, list, getTotalWeight(list));
   }

   public static class WeighedRandomItem {
      protected final int weight;

      public WeighedRandomItem(int i) {
         this.weight = i;
      }
   }
}
