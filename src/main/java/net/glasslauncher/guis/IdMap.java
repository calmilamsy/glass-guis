package net.glasslauncher.guis;

import org.jetbrains.annotations.Nullable;

public interface IdMap<T> extends Iterable<T> {
   int getId(T object);

   @Nullable
   T byId(int i);
}
