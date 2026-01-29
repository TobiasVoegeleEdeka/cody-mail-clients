package com.example.demo;

import com.google.protobuf.ByteString; 
import net.devh.boot.grpc.client.inject.GrpcClient;
import de.edeka.codymail.gateway.grpc.GrpcMailRequest;
import de.edeka.codymail.gateway.grpc.GrpcMailResponse;
import de.edeka.codymail.gateway.grpc.GrpcAttachment;
import de.edeka.codymail.gateway.grpc.MailGatewayServiceGrpc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
public class MailTestController {

    @GrpcClient("mailServer")
    private MailGatewayServiceGrpc.MailGatewayServiceBlockingStub mailStub;

    @GetMapping("/test-send")
    public String triggerMail(@RequestParam(defaultValue = "Test Subject") String subject) {

        // 1. Attachment erstellen 
        GrpcAttachment dummyPdf = GrpcAttachment.newBuilder()
                .setName("test.txt")      
                .setMimeType("text/plain")
                .setContent(ByteString.copyFrom("Inhalt der Datei", StandardCharsets.UTF_8)) 
                .build();

        // 2. Request erstellen & Attachment hinzufügen
        GrpcMailRequest request = GrpcMailRequest.newBuilder()
                .setAppTag("eakz")
                .setSubject(subject)
                .setBodyContent("Hallo aus Spring Boot mit gRPC!")
                .addRecipients("tobias.voegele@edeka.de")
                
          
                .addAttachments(dummyPdf) 
                
                .build();

        try {
            GrpcMailResponse response = mailStub.sendMail(request);
            return "Status: " + response.getStatus();
        } catch (Exception e) {
            return "Fehler: " + e.getMessage();
        }
    }
}