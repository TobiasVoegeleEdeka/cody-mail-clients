package com.example.demo.dto;


public record RestMailResponse(
    String status,
    String errorMessage 
) {}