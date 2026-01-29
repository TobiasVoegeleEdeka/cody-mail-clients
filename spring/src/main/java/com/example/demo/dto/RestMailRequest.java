package com.example.demo.dto;

import java.util.List;


public record RestMailRequest(
    String appTag,
    String subject,
    String bodyContent,
    List<String> recipients
) {}

