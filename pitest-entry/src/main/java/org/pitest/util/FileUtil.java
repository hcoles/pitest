package org.pitest.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class FileUtil {

  public static final String OUTPUT_ENCODING_KEY = "pit.output_encoding";

  public static final String INPUT_ENCODING_KEY = "pit.input_encoding";

    /** The default input file charset. */
   public static final Charset INPUT_CHARSET;

   /** The default output file charset.*/
   public static final Charset OUTPUT_CHARSET;

   static {
       //-Dpit.file_encoding=UTF-8
       String encoding = System.getProperty("pit.file_encoding");
       if (StringUtil.isNullOrEmpty(encoding)) {
          encoding = System.getProperty("file.encoding", "UTF-8");
       }
       final Charset defaultCharset = Charset.forName(encoding);
       //-Dpit.input_encoding=UTF-8
       INPUT_CHARSET = propToCharset(INPUT_ENCODING_KEY, defaultCharset);
       //-Dpit.output_encoding=UTF-8
       OUTPUT_CHARSET = propToCharset(OUTPUT_ENCODING_KEY, defaultCharset);
   }

   public static String readToString(final InputStream is)
      throws java.io.IOException {
    final StringBuilder fileData = new StringBuilder(1000);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
      char[] buf = new char[1024];
      int numRead = 0;

      while ((numRead = reader.read(buf)) != -1) {
        final String readData = String.valueOf(buf, 0, numRead);
        fileData.append(readData);
        buf = new char[1024];
      }
      return fileData.toString();
    }
  }

  public static String randomFilename() {
    return System.currentTimeMillis()
        + ("" + Math.random()).replaceAll("\\.", "");
  }

  private static Charset propToCharset(final String propKey, final Charset defaultCharset) {
      final String encoding = System.getProperty(propKey, null);
      return StringUtil.isNullOrEmpty(encoding) ? defaultCharset : Charset.forName(encoding);
  }

}
