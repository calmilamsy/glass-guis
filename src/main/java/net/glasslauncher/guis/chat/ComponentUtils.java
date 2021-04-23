package net.glasslauncher.guis.chat;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.entity.EntityBase;
import net.minecraft.entity.player.PlayerBase;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ComponentUtils {
   public static MutableComponent mergeStyles(MutableComponent mutableComponent, Style style) {
      if (style.isEmpty()) {
         return mutableComponent;
      } else {
         Style style2 = mutableComponent.getStyle();
         if (style2.isEmpty()) {
            return mutableComponent.setStyle(style);
         } else {
            return style2.equals(style) ? mutableComponent : mutableComponent.setStyle(style2.applyTo(style));
         }
      }
   }

   public static MutableComponent updateForEntity(@Nullable CommandSourceStack commandSourceStack, Component component, @Nullable EntityBase entity, int i) throws CommandSyntaxException {
      if (i > 100) {
         return component.copy();
      } else {
         MutableComponent mutableComponent = component instanceof ContextAwareComponent ? ((ContextAwareComponent)component).resolve(commandSourceStack, entity, i + 1) : component.plainCopy();
         Iterator var5 = component.getSiblings().iterator();

         while(var5.hasNext()) {
            Component component2 = (Component)var5.next();
            mutableComponent.append((Component)updateForEntity(commandSourceStack, component2, entity, i + 1));
         }

         return mutableComponent.withStyle(resolveStyle(commandSourceStack, component.getStyle(), entity, i));
      }
   }

   private static Style resolveStyle(@Nullable CommandSourceStack commandSourceStack, Style style, @Nullable EntityBase entity, int i) throws CommandSyntaxException {
      HoverEvent hoverEvent = style.getHoverEvent();
      if (hoverEvent != null) {
         Component component = (Component)hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
         if (component != null) {
            HoverEvent hoverEvent2 = new HoverEvent(HoverEvent.Action.SHOW_TEXT, updateForEntity(commandSourceStack, component, entity, i + 1));
            return style.withHoverEvent(hoverEvent2);
         }
      }

      return style;
   }

   public static Component getDisplayName(PlayerBase gameProfile) {
      if (gameProfile.name != null) {
         return new TextComponent(gameProfile.name);
      } else {
         return new TextComponent(String.valueOf(gameProfile.entityId));
      }
   }

   public static Component formatList(Collection<String> collection) {
      return formatAndSortList(collection, (string) -> {
         return (new TextComponent(string)).withStyle(ChatFormatting.GREEN);
      });
   }

   public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> collection, Function<T, Component> function) {
      if (collection.isEmpty()) {
         return TextComponent.EMPTY;
      } else if (collection.size() == 1) {
         return (Component)function.apply(collection.iterator().next());
      } else {
         List<T> list = Lists.newArrayList((Iterable)collection);
         list.sort(Comparable::compareTo);
         return formatList(list, function);
      }
   }

   public static <T> MutableComponent formatList(Collection<T> collection, Function<T, Component> function) {
      if (collection.isEmpty()) {
         return new TextComponent("");
      } else if (collection.size() == 1) {
         return ((Component)function.apply(collection.iterator().next())).copy();
      } else {
         MutableComponent mutableComponent = new TextComponent("");
         boolean bl = true;

         for(Iterator var4 = collection.iterator(); var4.hasNext(); bl = false) {
            T object = var4.next();
            if (!bl) {
               mutableComponent.append((Component)(new TextComponent(", ")).withStyle(ChatFormatting.GRAY));
            }

            mutableComponent.append((Component)function.apply(object));
         }

         return mutableComponent;
      }
   }

   public static MutableComponent wrapInSquareBrackets(Component component) {
      return new TranslatableComponent("chat.square_brackets", component);
   }

   public static Component fromMessage(Message message) {
      return (Component)(message instanceof Component ? (Component)message : new TextComponent(message.getString()));
   }
}
