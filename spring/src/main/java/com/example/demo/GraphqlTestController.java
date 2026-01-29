package com.example.demo;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GraphqlTestController {

    private final MailGraphqlService graphqlService;

    public GraphqlTestController(MailGraphqlService graphqlService) {
        this.graphqlService = graphqlService;
    }

    @GetMapping("/test-graphql")
    public String testGraphql(@RequestParam(defaultValue = "GraphQL Subject") String subject) {
        return graphqlService.sendMailViaGraphql(subject);
    }
    
    @GetMapping("/test-graphql-hard")
    public String testGraphqlHard() {
        return graphqlService.triggerHardcodedMutation();
    }
}