package resilience.mainservice.main.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class); // 로깅 추가 권장
    private final MailServiceClient mailServiceClient;

    public MailService(MailServiceClient mailServiceClient) {
        this.mailServiceClient = mailServiceClient;
    }

    // 반환 타입을 Mono<String>으로 변경
    public Mono<String> sendMail(String email) {
        logger.info("메일 발송 요청: {}", email); // 요청 로그
        return mailServiceClient.sendMail(email)
                .doOnSuccess(response -> logger.info("메일 발송 성공: {}", response)) // 성공 로그
                .doOnError(error -> logger.error("메일 발송 실패: {}", email, error)) // 실패 로그
                .onErrorMap(ex -> { // 필요시 예외 변환
                    if (!(ex instanceof MailSendException)) {
                        return new MailSendException("메일 전송 중 오류 발생: " + email, ex);
                    }
                    return ex;
                });
    }
}
