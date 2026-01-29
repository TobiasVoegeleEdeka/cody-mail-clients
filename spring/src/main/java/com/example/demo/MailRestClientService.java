package com.example.demo;

import com.example.demo.dto.RestMailRequest;
import com.example.demo.dto.RestMailResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

@Service
public class MailRestClientService {

    private final RestClient restClient;

    public MailRestClientService(RestClient.Builder builder) {
     
        this.restClient = builder
                .baseUrl("http://localhost:8085") 
                .build();
    }

    public String sendMailViaRest(String subject) {
  
        RestMailRequest request = new RestMailRequest(
                "eakz",
                subject,
                "Hallo! Dies ist ein REST-Aufruf von Spring Boot an Quarkus.",
                java.util.List.of("tobias.voegele@edeka.de")
        );

        try {
     
            RestMailResponse response = restClient.post()
                    .uri("/api/v1/mail/send")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RestMailResponse.class); 

            return "Erfolg! Server antwortet: " + response.status();

        } catch (Exception e) {
            return "Fehler beim REST-Call: " + e.getMessage();
        }
    }
}