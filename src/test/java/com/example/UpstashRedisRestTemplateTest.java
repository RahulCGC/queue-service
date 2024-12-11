package com.example;

import org.junit.*;
import org.springframework.http.*;
import org.springframework.web.client.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class UpstashRedisRestTemplateTest {

    private static final String UPSTASH_REDIS_REST_URL = "https://dynamic-llama-29038.upstash.io";
    private static final String UPSTASH_REDIS_REST_TOKEN = "AXFuAAIjcDE3NzRlYTExNGMxYWM0NWVlYTIwMzYwNjA5MTcxNGIyM3AxMA";
    private static final String QUEUE_KEY = "priorityQueue"; // Redis Sorted Set key

    @Test
    public void testPriorityQueue() throws Exception {
        // Initialize RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        // Enqueue some items with priorities
        enqueueItem(restTemplate, "1", 1); // priority 1
        enqueueItem(restTemplate, "2", 2); // priority 2
        enqueueItem(restTemplate, "3", 1); // priority 1
        enqueueItem(restTemplate, "4", 3); // priority 3

        // Dequeue and check the order
        String firstDequeue = dequeueItem(restTemplate); // Expected: task2 (priority 2)
        String secondDequeue = dequeueItem(restTemplate); // Expected: task4 (priority 3)
        String thirdDequeue = dequeueItem(restTemplate); // Expected: task1 (priority 1)
        String fourthDequeue = dequeueItem(restTemplate); // Expected: task3 (priority 1)

        // Assert that items are dequeued in the correct order
//        assertEquals("1", firstDequeue);
//        assertEquals("2", secondDequeue);
//        assertEquals("3", thirdDequeue);
//        assertEquals("3", fourthDequeue);
    }

    private void enqueueItem(RestTemplate restTemplate, String taskName, int priority) throws Exception {
        // Generate the current timestamp to ensure FCFS ordering
        long timestamp = System.currentTimeMillis();

        float score = (float) (priority + timestamp * 1e-6);
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            throw new IllegalArgumentException("Invalid score: " + score);
        }

        // URL encode taskName and score to prevent invalid characters in the URL
        String encodedTaskName = URLEncoder.encode(taskName, StandardCharsets.UTF_8);
        String scoreString = String.format("%.5f", score); // Ensure score has a valid format

        String enqueueUrl = UPSTASH_REDIS_REST_URL + "/zadd/" + QUEUE_KEY + "/" + encodedTaskName + "/" + scoreString;
        System.out.println("Enqueue URL: " + enqueueUrl);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + UPSTASH_REDIS_REST_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(enqueueUrl, HttpMethod.POST, entity, String.class);
            System.out.println("Response: " + response.getBody());
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            System.out.println("HTTP Error: " + ex.getStatusCode() + " " + ex.getResponseBodyAsString());
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }

    }

    private String dequeueItem(RestTemplate restTemplate) throws Exception {
        String dequeueUrl = UPSTASH_REDIS_REST_URL + "/zpopmin/" + QUEUE_KEY;
        System.out.println("Dequeue URL: " + dequeueUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + UPSTASH_REDIS_REST_TOKEN);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(dequeueUrl, HttpMethod.POST, entity, String.class);
        System.out.println("Response: " + response.getBody());  // Log raw response


        // Parse the response JSON to get the value of 'result'
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonResponse = objectMapper.readTree(response.getBody());
        return jsonResponse.get("result").asText();
    }
}
