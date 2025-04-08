package resilience.mainservice.main.mail;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class MailServiceClientWebClient implements MailServiceClient {

    private final WebClient webClient;

    public MailServiceClientWebClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }

    @Override
    public Mono<String> sendMail(String email) { // 반환 타입을 Mono<String>으로 변경
        EmailRequest request = new EmailRequest(email);
        return webClient.post()
                .uri("/mail/send")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is5xxServerError(), clientResponse ->
                        clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(new MailSendException("서버 에러 발생: " + errorBody)))
                )
                .bodyToMono(String.class); // .block() 제거
    }
}
