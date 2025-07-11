package resilience.mainservice.main.payment;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process-webflux")
    public Mono<PaymentResponse> processPayment(@RequestBody PaymentRequest request) {
        return paymentService.processPaymentFlux(request);
    }

    @PostMapping("/process-mvc")
    public PaymentResponse processPaymentMvc(@RequestBody PaymentRequest request) {
        return paymentService.processPaymentMvc(request);
    }
}
