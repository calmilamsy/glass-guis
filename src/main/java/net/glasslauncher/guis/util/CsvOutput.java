package net.glasslauncher.guis.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvOutput {
   private final Writer output;
   private final int columnCount;

   private CsvOutput(Writer writer, List<String> list) throws IOException {
      this.output = writer;
      this.columnCount = list.size();
      this.writeLine(list.stream());
   }

   public static Builder builder() {
      return new Builder();
   }

   public void writeRow(Object... objects) throws IOException {
      if (objects.length != this.columnCount) {
         throw new IllegalArgumentException("Invalid number of columns, expected " + this.columnCount + ", but got " + objects.length);
      } else {
         this.writeLine(Stream.of(objects));
      }
   }

   private void writeLine(Stream<?> stream) throws IOException {
      this.output.write((String)stream.map(CsvOutput::getStringValue).collect(Collectors.joining(",")) + "\r\n");
   }

   private static String getStringValue(@Nullable Object object) {
      return StringEscapeUtils.escapeCsv(object != null ? object.toString() : "[null]");
   }

   public static class Builder {
      private final List<String> headers = Lists.newArrayList();

      public Builder addColumn(String string) {
         this.headers.add(string);
         return this;
      }

      public CsvOutput build(Writer writer) throws IOException {
         return new CsvOutput(writer, this.headers);
      }
   }
}
