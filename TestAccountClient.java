import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class TestAccountClient {
    public static void main(String[] args) {
        String token = args[0];
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:8081/api/accounts/profile",
                    HttpMethod.GET,
                    entity,
                    String.class
            );
            System.out.println("RESPONSE: " + response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
