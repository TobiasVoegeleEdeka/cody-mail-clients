package com.example.demo;

import com.example.demo.dto.GraphqlRequestBody;
import com.example.demo.dto.RestMailRequest; 
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import java.util.Map;

@Service
public class MailGraphqlService {

    private final RestClient restClient;

    public MailGraphqlService(RestClient.Builder builder) {
      
        this.restClient = builder
                .baseUrl("http://localhost:8085")
                .build();
    }

    public String sendMailViaGraphql(String subject) {
    
        String mutation = """
            mutation SendMail($req: MailRequestInput) {
                sendMail(request: $req) {
                    status
                }
            }
            """;


        var inputVariables = new RestMailRequest(
                "alv",
                subject,
                "Hallo! Dies ist eine Mutation via GraphQL.",
                java.util.List.of("tobias.voegele@edeka.de")
        );

        GraphqlRequestBody body = new GraphqlRequestBody(
                mutation,
                Map.of("req", inputVariables) 
        );

        try {
   
            String rawResponse = restClient.post()
                    .uri("/graphql")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class); 

            return "GraphQL Antwort: " + rawResponse;

        } catch (Exception e) {
            return "Fehler beim GraphQL-Call: " + e.getMessage();
        }
    }
    
 
    public String triggerHardcodedMutation() {
        String mutation = """
            mutation {
                sendHardcodedTestMail
            }
            """;
            
        GraphqlRequestBody body = new GraphqlRequestBody(mutation, null);
        
        return restClient.post()
                .uri("/graphql")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);
    }
}