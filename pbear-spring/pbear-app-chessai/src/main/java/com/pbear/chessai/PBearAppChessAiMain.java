package com.pbear.chessai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class PBearAppChessAiMain {
  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(PBearAppChessAiMain.class, args);
  }
}
