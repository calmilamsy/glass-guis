package net.glasslauncher.guis.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClassInstanceMultiMap<T> extends AbstractCollection<T> {
   private final Map<Class<?>, List<T>> byClass = Maps.newHashMap();
   private final Class<T> baseClass;
   private final List<T> allInstances = Lists.newArrayList();

   public ClassInstanceMultiMap(Class<T> class_) {
      this.baseClass = class_;
      this.byClass.put(class_, this.allInstances);
   }

   public boolean add(T object) {
      boolean bl = false;
      Iterator var3 = this.byClass.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Class<?>, List<T>> entry = (Entry)var3.next();
         if (entry.getKey().isInstance(object)) {
            bl |= entry.getValue().add(object);
         }
      }

      return bl;
   }

   public boolean remove(Object object) {
      boolean bl = false;
      Iterator var3 = this.byClass.entrySet().iterator();

      while(var3.hasNext()) {
         Entry<Class<?>, List<T>> entry = (Entry)var3.next();
         if (entry.getKey().isInstance(object)) {
            List<T> list = entry.getValue();
            bl |= list.remove(object);
         }
      }

      return bl;
   }

   public boolean contains(Object object) {
      return this.find(object.getClass()).contains(object);
   }

   public <S> Collection<S> find(Class<S> class_) {
      if (!this.baseClass.isAssignableFrom(class_)) {
         throw new IllegalArgumentException("Don't know how to search for " + class_);
      } else {
         List<T> list = this.byClass.computeIfAbsent(class_, (class_x) -> {
            Stream<T> var10000 = this.allInstances.stream();
            return var10000.filter(class_x::isInstance).collect(Collectors.toList());
         });
         return (Collection<S>) Collections.unmodifiableCollection(list);
      }
   }

   public Iterator<T> iterator() {
      return (Iterator)(this.allInstances.isEmpty() ? Collections.emptyIterator() : Iterators.unmodifiableIterator(this.allInstances.iterator()));
   }

   public List<T> getAllInstances() {
      return ImmutableList.copyOf((Collection)this.allInstances);
   }

   public int size() {
      return this.allInstances.size();
   }
}
