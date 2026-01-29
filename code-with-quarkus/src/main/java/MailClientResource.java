package de.edeka.client;

import com.google.protobuf.ByteString;
import de.edeka.codymail.gateway.grpc.GrpcAttachment;
import de.edeka.codymail.gateway.grpc.GrpcMailRequest;
import de.edeka.codymail.gateway.grpc.MailGatewayServiceGrpc; // Automatisch generiert
import io.quarkus.grpc.GrpcClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.nio.charset.StandardCharsets;

@Path("/client-test")
public class MailClientResource {

    // "mailServer" ist der Name, den wir gleich in der Config nutzen
    @GrpcClient("mailServer")
    MailGatewayServiceGrpc.MailGatewayServiceBlockingStub mailStub;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String triggerMail(@QueryParam("subject") String subject) {
        
        // Java 21 'var' für weniger Boilerplate
        var safeSubject = (subject == null || subject.isEmpty()) ? "Quarkus Client Test" : subject;

        // Attachment bauen
        var pdfAttachment = GrpcAttachment.newBuilder()
                .setFileName("test_dokument.txt")
                .setMimeType("text/plain")
                .setData(ByteString.copyFrom("Das ist ein Test-Anhang aus dem Quarkus Client.", StandardCharsets.UTF_8))
                .build();

        // Request bauen
        var request = GrpcMailRequest.newBuilder()
                .setAppTag("alv")
                .setSubject(safeSubject)
                .setBodyContent("Hallo! Dies ist ein gRPC Call von Quarkus zu Quarkus.")
                .addRecipients("tobias.voegele@edeka.de")
                .addAttachments(pdfAttachment) // Das neue Feld nutzen
                .build();

        try {
            // Der eigentliche gRPC Aufruf
            var response = mailStub.sendMail(request);
            return "Server antwortete: " + response.getStatus() + " (Msg: " + response.getErrorMessage() + ")";
        } catch (Exception e) {
            return "gRPC Fehler: " + e.getMessage();
        }
    }
}