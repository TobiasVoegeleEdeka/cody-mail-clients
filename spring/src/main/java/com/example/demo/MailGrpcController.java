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
public class MailGrpcController {

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

// Beispiel Code Direkt Aufruf 
// package com.example.demo.service;

// import com.google.protobuf.ByteString;
// import de.edeka.codymail.gateway.grpc.*;
// import net.devh.boot.grpc.client.inject.GrpcClient;
// import org.springframework.stereotype.Service;

// @Service
// public class MailGrpcService {

//     @GrpcClient("mailServer")
//     private MailGatewayServiceGrpc.MailGatewayServiceBlockingStub mailStub;

//     public void sendMailWithPdf(String recipient, String subject, String body, String filename, byte[] pdfData) {
        
//         // 1. Attachment bauen
//         GrpcAttachment attachment = GrpcAttachment.newBuilder()
//                 .setName(filename)
//                 .setMimeType("application/pdf") // Wichtig: korrekter Mime-Type für PDFs
//                 .setContent(ByteString.copyFrom(pdfData))
//                 .build();

//         // 2. Request bauen
//         GrpcMailRequest request = GrpcMailRequest.newBuilder()
//                 .setAppTag("alv")
//                 .setSubject(subject)
//                 .setBodyContent(body)
//                 .addRecipients(recipient)
//                 .addAttachments(attachment)
//                 .build();

//         // 3. Senden
//         try {
//             GrpcMailResponse response = mailStub.sendMail(request);
//             System.out.println("Mail gesendet an " + recipient + ". Status: " + response.getStatus());
//         } catch (Exception e) {
//             System.err.println("Fehler beim Senden an " + recipient + ": " + e.getMessage());
//         }
//     }
// }