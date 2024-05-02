package com.pbear.subway.business.temp;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.KafkaMessagePublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TempCont {
  private final KafkaMessagePublisher kafkaMessagePublisher;

  @GetMapping("/xx")
  public Object xx() {
    this.kafkaMessagePublisher.publish(
        MessageType.REQUEST, "subway.stations", null, "testData"
    );
    return Map.of("", "");
  }
}
