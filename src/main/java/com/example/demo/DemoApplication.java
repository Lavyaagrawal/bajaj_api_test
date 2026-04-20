package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    CommandLineRunner run(WebClient.Builder builder) {
        return args -> {

            WebClient client = builder.build();

            // STEP 1: Generate Webhook
            Map<String, String> request = new HashMap<>();
            request.put("name", "Lavya Agrawal");
            request.put("regNo", "ADT23SOCB0561");
            request.put("email", "lavyaagrawal123@gmail.com"); // ✅ fixed email

            Map response = client.post()
                    .uri("https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            System.out.println("Webhook Response: " + response);

            String webhook = (String) response.get("webhook");
            String token = (String) response.get("accessToken");

            // STEP 2: SQL QUERY (ODD regNo → Question 1)
            String finalQuery = "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE DAY(p.PAYMENT_TIME) <> 1 ORDER BY p.AMOUNT DESC LIMIT 1";

            Map<String, String> answer = new HashMap<>();
            answer.put("finalQuery", finalQuery);

            // STEP 3: Submit Answer
            String result = client.post()
                    .uri(webhook)
                    .header("Authorization",  token)
                    .header("Content-Type", "application/json")
                    .bodyValue(answer)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Final Submission: " + result);
        };
    }
}