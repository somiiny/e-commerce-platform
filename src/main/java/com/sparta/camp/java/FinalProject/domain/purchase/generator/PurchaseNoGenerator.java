package com.sparta.camp.java.FinalProject.domain.purchase.generator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class PurchaseNoGenerator {

  public PurchaseNoGenerator() {
  }

  public static String generate() {
    return "PUR-" +
        LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) +
        "-" +
        UUID.randomUUID().toString().substring(0, 6).toUpperCase();
  }
}
