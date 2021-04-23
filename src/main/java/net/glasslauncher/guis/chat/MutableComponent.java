package net.glasslauncher.guis.chat;

import java.util.function.UnaryOperator;

public interface MutableComponent extends Component {
   MutableComponent setStyle(Style style);

   default MutableComponent append(String string) {
      return this.append((Component)(new TextComponent(string)));
   }

   MutableComponent append(Component component);

   default MutableComponent withStyle(UnaryOperator<Style> unaryOperator) {
      this.setStyle((Style)unaryOperator.apply(this.getStyle()));
      return this;
   }

   default MutableComponent withStyle(Style style) {
      this.setStyle(style.applyTo(this.getStyle()));
      return this;
   }

   default MutableComponent withStyle(ChatFormatting... chatFormattings) {
      this.setStyle(this.getStyle().applyFormats(chatFormattings));
      return this;
   }

   default MutableComponent withStyle(ChatFormatting chatFormatting) {
      this.setStyle(this.getStyle().applyFormat(chatFormatting));
      return this;
   }
}
