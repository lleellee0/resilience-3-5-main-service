package resilience.mainservice.main.mail;

import reactor.core.publisher.Mono;

public interface MailServiceClient {
    Mono<String> sendMail(String email);
}