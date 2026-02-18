package org.proj.vidal.service;

import org.proj.vidal.model.GenerateWebhookRequest;
import org.proj.vidal.model.GenerateWebhookResponse;
import org.proj.vidal.model.SubmitSolutionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@Service
public class WebhookService {

    @Autowired
    private RestTemplate restTemplate;

    private final String GENERATE_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public void startFlow() {

        GenerateWebhookRequest request =
                new GenerateWebhookRequest("Bheemashankar Diwakar", "REG123", "bheemashankard17@gmail.com");

        ResponseEntity<GenerateWebhookResponse> response =
                restTemplate.postForEntity(
                        GENERATE_URL,
                        request,
                        GenerateWebhookResponse.class
                );

        GenerateWebhookResponse body = response.getBody();

        String webhookUrl = body.getWebHook();
        String token = body.getAccessToken();

        int lastTwoDigits = Integer.parseInt(
                request.getRegNo().substring(request.getRegNo().length() - 2)
        );

        String finalQuery;

        if (lastTwoDigits % 2 == 0) {
            finalQuery = getQuestion2Query();
        } else {
            finalQuery = getQuestion1Query();
        }
        sendSolution(webhookUrl, token, finalQuery);
    }

private String getQuestion1Query() {

    return """
                SELECT d.DEPARTMENT_NAME,
                       MAX(total_salary) AS SALARY,
                       emp_name AS EMPLOYEE_NAME,
                       age AS AGE
                FROM (
                    SELECT e.EMP_ID,
                           d.DEPARTMENT_NAME,
                           CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS emp_name,
                           TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS age,
                           SUM(p.AMOUNT) AS total_salary
                    FROM EMPLOYEE e
                    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                    JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID
                    WHERE DAY(p.PAYMENT_TIME) <> 1
                    GROUP BY e.EMP_ID, d.DEPARTMENT_NAME
                ) sub
                GROUP BY DEPARTMENT_NAME;
                """;
}

private String getQuestion2Query() {

    return """
                SELECT d.DEPARTMENT_NAME,
                       AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURDATE())) AS AVERAGE_AGE,
                       SUBSTRING_INDEX(
                           GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME)
                           ORDER BY e.EMP_ID SEPARATOR ', '), ', ', 10
                       ) AS EMPLOYEE_LIST
                FROM EMPLOYEE e
                JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID
                WHERE p.AMOUNT > 70000
                GROUP BY d.DEPARTMENT_NAME, d.DEPARTMENT_ID
                ORDER BY d.DEPARTMENT_ID DESC;
                """;
}
private void sendSolution(String url, String token, String finalQuery) {

    SubmitSolutionRequest request =
            new SubmitSolutionRequest(finalQuery);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", token);

    HttpEntity<SubmitSolutionRequest> entity =
            new HttpEntity<>(request, headers);

    ResponseEntity<String> response =
            restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

    System.out.println("Submission Response: " + response.getBody());
}
}
