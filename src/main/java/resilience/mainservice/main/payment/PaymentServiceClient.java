package resilience.mainservice.main.payment;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(WebClient.Builder webClientBuilder) {
        // 결제 서비스의 엔드포인트로 기본 URL 설정
        this.webClient = webClientBuilder.baseUrl("http://localhost:8082").build();
    }

    public Mono<PaymentResponse> processPaymentFlux(PaymentRequest request) {
        return webClient.post()
                .uri("/payments/process")
                .bodyValue(request)  // PaymentRequest를 JSON 바디로 전달
                .retrieve()
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody ->
                                        Mono.error(new PaymentServiceErrorException("결제 서비스 에러: " + errorBody))
                                )
                )
                .bodyToMono(PaymentResponse.class)
//                아래와 같이 개별적으로 Timeout도 가능함.
//                .timeout(Duration.ofSeconds(15))
                ;
    }

    public PaymentResponse processPaymentMvc(PaymentRequest request) {
        return webClient.post()
                .uri("/payments/process")
                .bodyValue(request)  // PaymentRequest를 JSON 바디로 전달
                .retrieve()
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody ->
                                        Mono.error(new PaymentServiceErrorException("결제 서비스 에러: " + errorBody))
                                )
                )
                .bodyToMono(PaymentResponse.class)
//                .timeout(Duration.ofSeconds(15)) // 필요에 따라 Timeout 설정 가능
                .block(); // 블로킹 호출로 MVC 방식 구현
    }
}
