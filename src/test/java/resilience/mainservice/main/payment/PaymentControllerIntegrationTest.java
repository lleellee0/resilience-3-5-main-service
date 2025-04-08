package resilience.mainservice.main.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@TestPropertySource(properties = {
        "server.tomcat.max-threads=10",
        "server.tomcat.accept-count=5"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PaymentControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WebTestClient webTestClient;

    /**
     * 다수의 요청을 동시에 /payments/process-mvc 로 보내어 톰캣의 요청 처리 스레드가 고갈되는 상황을 재현합니다.
     */
    @Test
    public void testProcessMvcThreadExhaustion() throws Exception {
        int numberOfRequests = 100;  // 보내고자 하는 요청 수

        // newCachedThreadPool() 사용으로 클라이언트 측 스레드 제한 없이 동시 요청 발생
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    PaymentRequest request = new PaymentRequest();
                    request.setOrderId("order-" + ThreadLocalRandom.current().nextInt(10000));
                    request.setAmount(100.0);

                    // /payments/process-mvc 엔드포인트 호출 (블로킹 방식)
                    ResponseEntity<String> response = restTemplate.postForEntity(
                            "/payments/process-mvc", request, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        System.out.println(response.getStatusCode());
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업이 완료될 때까지 대기
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("MVC Endpoint - Success: " + successCount.get() + ", Failure: " + failureCount.get());
    }

    /**
     * /payments/process-webflux 엔드포인트에 대해 다수의 요청을 보내어 논블로킹 방식의 특성을 확인합니다.
     * 이 방식은 Tomcat의 스레드 고갈과 무관하게 대부분의 요청이 성공해야 합니다.
     */
    @Test   // 실행할 때 spring-boot-starter-web 의존성 pom.xml에서 주석처리하고 의존성 새로고침 후 실행!!
    public void testProcessWebfluxNoThreadExhaustion() throws Exception {
        int numberOfRequests = 100;  // 보내고자 하는 요청 수

        // newCachedThreadPool() 사용으로 클라이언트 측 스레드 제한 없이 동시 요청 발생
        ExecutorService executor = Executors.newCachedThreadPool();
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    PaymentRequest request = new PaymentRequest();
                    request.setOrderId("order-" + ThreadLocalRandom.current().nextInt(10000));
                    request.setAmount(100.0);

                    // /payments/process-webflux 엔드포인트 호출 (블로킹 방식)
                    ResponseEntity<String> response = restTemplate.postForEntity(
                            "/payments/process-webflux", request, String.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    } else {
                        System.out.println(response.getStatusCode());
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failureCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업이 완료될 때까지 대기
        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("WebFlux Endpoint - Success: " + successCount.get() + ", Failure: " + failureCount.get());
    }
}
