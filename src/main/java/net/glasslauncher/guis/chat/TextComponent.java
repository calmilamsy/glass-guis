package net.glasslauncher.guis.chat;

public class TextComponent extends BaseComponent {
   public static final Component EMPTY = new TextComponent("");
   private final String text;

   public TextComponent(String string) {
      this.text = string;
   }

   public String getText() {
      return this.text;
   }

   public String getContents() {
      return this.text;
   }

   public TextComponent plainCopy() {
      return new TextComponent(this.text);
   }

   public boolean equals(Object object) {
      if (this == object) {
         return true;
      } else if (!(object instanceof TextComponent)) {
         return false;
      } else {
         TextComponent textComponent = (TextComponent)object;
         return this.text.equals(textComponent.getText()) && super.equals(object);
      }
   }

   public String toString() {
      return "TextComponent{text='" + this.text + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }
}
