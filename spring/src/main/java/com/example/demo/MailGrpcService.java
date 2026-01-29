package com.example.demo;

import com.google.protobuf.ByteString;
import de.edeka.codymail.gateway.grpc.*;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

/**
 * In src/main/proto liegt die proto file
 * 
 * <strong>MailGrpcService</strong><br>
 * Dieser Service kapselt die komplette gRPC-Kommunikation mit dem Mail-Gateway.
 * <p>
 * Für gRPC-Neulinge:
 * Statt REST-Endpunkte (URLs) aufzurufen, rufen wir hier Methoden auf einem Java-Objekt auf (dem Stub).
 * Unter der Haube wandelt gRPC diesen Aufruf in Netzwerk-Pakete um.
 * </p>
 */
@Service
public class MailGrpcService {

    /**
     * Der <strong>Stub</strong> (zu Deutsch: Stummel/Stellvertreter).
     * <p>
     * Dies ist das wichtigste Objekt. Wir sprechen nur mit diesem Stub. Er sieht aus wie ein
     * lokales Java-Objekt, aber jeder Aufruf hieran wird übers Netzwerk an den echten Server (Mail-Gateway) geschickt.
     * </p>
     * <ul>
     * <li><strong>BlockingStub:</strong> Der Aufruf blockiert (wartet), bis der Server geantwortet hat.</li>
     * <li><strong>@GrpcClient("mailServer"):</strong> Spring Boot sucht in der application.properties
     * nach der Adresse für 'mailServer' und verbindet diesen Stub automatisch.</li>
     * </ul>
     */
    @GrpcClient("mailServer")
    private MailGatewayServiceGrpc.MailGatewayServiceBlockingStub mailStub;

    /**
     * Sendet eine E-Mail mit PDF-Anhang via gRPC.
     *
     * @param recipient Die E-Mail-Adresse des Empfängers.
     * @param subject   Der Betreff der E-Mail.
     * @param body      Der Textinhalt der E-Mail.
     * @param filename  Der Name der Datei im Anhang (z.B. "rechnung.pdf").
     * @param pdfData   Der eigentliche Dateiinhalt als Java Byte-Array.
     */
    public void sendMailWithPdf(String recipient, String subject, String body, String filename, byte[] pdfData) {

        // -----------------------------------------------------------------------
        // SCHRITT 1: Attachment (Anhang) vorbereiten
        // -----------------------------------------------------------------------
        // In gRPC (Protobuf) gibt es kein 'new Attachment()'.
        // Wir müssen immer einen "Builder" nutzen. Das ist wie ein Formular,
        // das wir Zeile für Zeile ausfüllen.
        GrpcAttachment attachment = GrpcAttachment.newBuilder()
                .setName(filename)           // Setzt den Dateinamen (entspricht 'string name = 1' im Proto)
                .setMimeType("application/pdf") // Setzt den Typ (wichtig für E-Mail-Clients)

                // WICHTIG: Protobuf versteht keine normalen Java byte[] Arrays.
                // Wir müssen sie in 'ByteString' umwandeln. Das ist der Datentyp für Binärdaten in gRPC.
                .setContent(ByteString.copyFrom(pdfData)) 
                
                .build(); // .build() versiegelt das Objekt. Jetzt ist es fertig und unveränderbar.


        // -----------------------------------------------------------------------
        // SCHRITT 2: Den Request (die eigentliche Nachricht) bauen
        // -----------------------------------------------------------------------
        GrpcMailRequest request = GrpcMailRequest.newBuilder()
                .setAppTag("alv")               // Metadaten: Wer schickt das?
                .setSubject(subject)            // Einfaches Feld setzen
                .setBodyContent(body)           // Einfaches Feld setzen
                
                // .add... (statt .set...):
                // Dies ist eine Liste (repeated field). Wir können 'addRecipients'
                // mehrmals aufrufen, um mehrere Empfänger hinzuzufügen.
                .addRecipients(recipient)
                
                // Hier hängen wir das oben gebaute Attachment-Objekt an.
                .addAttachments(attachment)
                
                .build(); // Request fertig machen.


        // -----------------------------------------------------------------------
        // SCHRITT 3: Der Netzwerk-Aufruf (RPC)
        // -----------------------------------------------------------------------
        try {
            System.out.println("Sende gRPC Request an Server...");

            // Hier passiert die Magie:
            // 1. Der Stub nimmt das 'request'-Objekt.
            // 2. Er serialisiert es (macht Nullen und Einsen daraus).
            // 3. Er schickt es via HTTP/2 an den Docker-Container.
            // 4. Er wartet auf die Antwort.
            GrpcMailResponse response = mailStub.sendMail(request);

            // Wenn wir hier ankommen, hat der Server geantwortet.
            // Wir können nun den Status prüfen (z.B. SUCCESS oder FAILURE).
            System.out.println("Mail gesendet an " + recipient + ". Server-Antwort: " + response.getStatus());

        } catch (Exception e) {
            // Exceptions passieren hier bei Netzwerkfehlern (Server down)
            // oder wenn der Server einen gRPC-Error (z.B. INVALID_ARGUMENT) zurückwirft.
            System.err.println("Fehler beim Senden an " + recipient + ": " + e.getMessage());
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