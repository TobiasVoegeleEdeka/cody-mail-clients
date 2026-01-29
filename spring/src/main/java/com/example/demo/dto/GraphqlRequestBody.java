package com.example.demo.dto;

import java.util.Map;


public record GraphqlRequestBody(
    String query,
    Map<String, Object> variables
) {}