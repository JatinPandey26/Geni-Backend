package com.geni.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class Schema {
    Map<String, FieldSchema> fields;
    Class<?> sourceClass;
}
