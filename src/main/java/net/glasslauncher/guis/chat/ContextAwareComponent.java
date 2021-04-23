package net.glasslauncher.guis.chat;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.entity.EntityBase;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface ContextAwareComponent {
   MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable EntityBase entity, int i) throws CommandSyntaxException;
}
