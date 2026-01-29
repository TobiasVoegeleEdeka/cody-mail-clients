package com.example.demo;




import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestTestController {

    private final MailRestClientService mailRestClientService;

    public RestTestController(MailRestClientService mailRestClientService) {
        this.mailRestClientService = mailRestClientService;
    }

    @GetMapping("/test-rest")
    public String triggerRestCall(@RequestParam(defaultValue = "REST Test") String subject) {
        return mailRestClientService.sendMailViaRest(subject);
    }
}