package net.glasslauncher.guis.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import net.glasslauncher.guis.IdMap;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Iterator;

public class CrudeIncrementalIntIdentityHashBiMap<K> implements IdMap<K> {
   private static final Object EMPTY_SLOT = null;
   private K[] keys;
   private int[] values;
   private K[] byId;
   private int nextId;
   private int size;

   public CrudeIncrementalIntIdentityHashBiMap(int i) {
      i = (int)((float)i / 0.8F);
      this.keys = (Object[])(new Object[i]);
      this.values = new int[i];
      this.byId = (Object[])(new Object[i]);
   }

   public int getId(@Nullable K object) {
      return this.getValue(this.indexOf(object, this.hash(object)));
   }

   @Nullable
   public K byId(int i) {
      return i >= 0 && i < this.byId.length ? this.byId[i] : null;
   }

   private int getValue(int i) {
      return i == -1 ? -1 : this.values[i];
   }

   public int add(K object) {
      int i = this.nextId();
      this.addMapping(object, i);
      return i;
   }

   private int nextId() {
      while(this.nextId < this.byId.length && this.byId[this.nextId] != null) {
         ++this.nextId;
      }

      return this.nextId;
   }

   private void grow(int i) {
      K[] objects = this.keys;
      int[] is = this.values;
      this.keys = (Object[])(new Object[i]);
      this.values = new int[i];
      this.byId = (Object[])(new Object[i]);
      this.nextId = 0;
      this.size = 0;

      for(int j = 0; j < objects.length; ++j) {
         if (objects[j] != null) {
            this.addMapping(objects[j], is[j]);
         }
      }

   }

   public void addMapping(K object, int i) {
      int j = Math.max(i, this.size + 1);
      int k;
      if ((float)j >= (float)this.keys.length * 0.8F) {
         for(k = this.keys.length << 1; k < i; k <<= 1) {
         }

         this.grow(k);
      }

      k = this.findEmpty(this.hash(object));
      this.keys[k] = object;
      this.values[k] = i;
      this.byId[i] = object;
      ++this.size;
      if (i == this.nextId) {
         ++this.nextId;
      }

   }

   private int hash(@Nullable K object) {
      return (Mth.murmurHash3Mixer(System.identityHashCode(object)) & Integer.MAX_VALUE) % this.keys.length;
   }

   private int indexOf(@Nullable K object, int i) {
      int k;
      for(k = i; k < this.keys.length; ++k) {
         if (this.keys[k] == object) {
            return k;
         }

         if (this.keys[k] == EMPTY_SLOT) {
            return -1;
         }
      }

      for(k = 0; k < i; ++k) {
         if (this.keys[k] == object) {
            return k;
         }

         if (this.keys[k] == EMPTY_SLOT) {
            return -1;
         }
      }

      return -1;
   }

   private int findEmpty(int i) {
      int k;
      for(k = i; k < this.keys.length; ++k) {
         if (this.keys[k] == EMPTY_SLOT) {
            return k;
         }
      }

      for(k = 0; k < i; ++k) {
         if (this.keys[k] == EMPTY_SLOT) {
            return k;
         }
      }

      throw new RuntimeException("Overflowed :(");
   }

   public Iterator<K> iterator() {
      return Iterators.filter(Iterators.forArray(this.byId), (Predicate) Predicates.notNull());
   }

   public void clear() {
      Arrays.fill(this.keys, (Object)null);
      Arrays.fill(this.byId, (Object)null);
      this.nextId = 0;
      this.size = 0;
   }

   public int size() {
      return this.size;
   }
}
