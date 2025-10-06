package com.sparta.camp.java.FinalProject.common.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;


public abstract class JsonAttributeConverter<T> implements AttributeConverter<T, String> {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Class<T> clazz;

  protected JsonAttributeConverter(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Override
  public String convertToDatabaseColumn(T attribute) {
    if (attribute == null) return null;
    try {
      return objectMapper.writeValueAsString(attribute);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to serialize " + clazz.getSimpleName(), e);
    }
  }

  @Override
  public T convertToEntityAttribute(String json) {
    if (json == null) return null;
    try {
      return objectMapper.readValue(json, clazz);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Failed to deserialize JSON to " + clazz.getSimpleName(), e);
    }
  }

}
