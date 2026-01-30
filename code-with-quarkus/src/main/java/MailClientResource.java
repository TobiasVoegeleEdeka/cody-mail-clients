

import com.google.protobuf.ByteString;
import de.edeka.codymail.gateway.grpc.GrpcAttachment;
import de.edeka.codymail.gateway.grpc.GrpcMailRequest;
import de.edeka.codymail.gateway.grpc.MailGatewayServiceGrpc; 
import io.quarkus.grpc.GrpcClient;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import java.nio.charset.StandardCharsets;

@Path("/client-test")
public class MailClientResource {


    @GrpcClient("mailServer")
    MailGatewayServiceGrpc.MailGatewayServiceBlockingStub mailStub;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @RunOnVirtualThread
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
                .addAttachments(pdfAttachment) 
                .build();

        try {
            // Der Aufruf blockiert code-technisch.
            // ABER: Weil  auf einem Virtual Thread , "parkt" die JVM diesen Thread
            // extrem effizient. Der echte OS-Thread wird sofort frei für andere Arbeit.
            // Sobald die Antwort da ist, springt der Virtual Thread wieder an.
            var response = mailStub.sendMail(request);
            
            // Thread-Info ausgeben zum Beweis
            return "Status: " + response.getStatus() + 
                   "\nAusgeführt auf: " + Thread.currentThread(); 
           
        } catch (Exception e) {
            return "Fehler: " + e.getMessage();
        }
    }
}