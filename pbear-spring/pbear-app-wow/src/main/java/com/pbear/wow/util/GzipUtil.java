package com.pbear.wow.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {
  public static byte[] compress(String value) throws IOException {
    if ((value == null) || (value.isEmpty())) {
      return null;
    }
    ByteArrayOutputStream obj = new ByteArrayOutputStream();
    GZIPOutputStream gzip = new GZIPOutputStream(obj);
    gzip.write(value.getBytes(StandardCharsets.UTF_8));
    gzip.flush();
    gzip.close();
    return obj.toByteArray();
  }

  public static String decompress(byte[] compressedData) throws IOException {
    final StringBuilder outStr = new StringBuilder();
    if ((compressedData == null) || (compressedData.length == 0)) {
      return null;
    }
    if (!isCompressed(compressedData)) {
      return new String(compressedData);
    }

    final GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressedData));
    try (final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, StandardCharsets.UTF_8))) {
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        outStr.append(line);
      }
      return outStr.toString();
    }
  }

  public static boolean isCompressed(final byte[] compressed) {
    return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
  }
}
