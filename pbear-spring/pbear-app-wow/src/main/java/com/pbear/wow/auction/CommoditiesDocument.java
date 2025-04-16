package com.pbear.wow.auction;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.wow.data.Auction;
import com.pbear.wow.util.GzipUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "commodities")
@TypeAlias("CommoditiesDocument")
@Slf4j
public class CommoditiesDocument {
  @Id
  private ObjectId id;
  private String collectedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")); // yyyyMMdd
  private String collectedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH")); // HH
  private Binary gzipData;

  public CommoditiesDocument(final String auctionsJson) {
    this.setData(auctionsJson);
  }

  public void setData(final String auctionsJson) {
    try {
      byte[] compressedData = GzipUtil.compress(auctionsJson);
      if (compressedData != null) {
        this.gzipData = new Binary(compressedData);
      }
    } catch (IOException e) {
      log.error("fail to compress data", e);
    }
  }

  public List<Auction> getAuctions(final ObjectMapper objectMapper) {
    try {
      if (this.gzipData == null) {
        return null;
      }
      String jsonArray = GzipUtil.decompress(this.gzipData.getData());
      if (jsonArray == null) {
        return null;
      }
      return objectMapper.readValue(jsonArray, new TypeReference<>(){});
    } catch (Exception e) {
      log.error("fail to decompress data", e);
      return null;
    }
  }
}
