package com.meteor.eyewear;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:meteor-concurrency;MODE=Oracle;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class StockConcurrencyTest {
    @Autowired MeteorService service;
    @Autowired Products products;
    @Autowired Orders orders;

    @Test
    void twoConcurrentOrdersCannotBuyTheLastFrameTwice() throws Exception {
        var staff = service.current("staff@meteor.example");
        var customer = service.current("customer@meteor.example");
        ProductForm form = new ProductForm();
        form.setType("FRAME");
        form.setModel("Last Pair");
        form.setBrand("Meteor");
        form.setFrameShape("Round");
        form.setFrameColour("Black");
        form.setQuantity(1);
        form.setPrice(new BigDecimal("100.00"));
        Product frame = service.saveProduct(staff, null, form);
        Product lens = service.catalog("LENS", null).get(0);
        int oldLensQuantity = lens.getQuantity();
        long oldOrderCount = orders.count();
        CountDownLatch ready = new CountDownLatch(2), start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<String> buy =
                () -> {
                    ready.countDown();
                    if (!start.await(5, TimeUnit.SECONDS))
                        throw new IllegalStateException("Start timeout");
                    OrderForm order = new OrderForm();
                    order.setFrameId(frame.getId());
                    order.setLensId(lens.getId());
                    order.setDelivery("OUTLET");
                    order.setOutlet("Merlimau");
                    try {
                        service.createOrder(staff, customer.getId(), order);
                        return "created";
                    } catch (BusinessException e) {
                        return e.getMessage();
                    }
                };
        try {
            Future<String> first = executor.submit(buy), second = executor.submit(buy);
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            List<String> results =
                    List.of(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertThat(results)
                    .containsExactlyInAnyOrder(
                            "created", "There is not enough stock for this order.");
            assertThat(products.findById(frame.getId()).orElseThrow().getQuantity()).isZero();
            assertThat(products.findById(lens.getId()).orElseThrow().getQuantity())
                    .isEqualTo(oldLensQuantity - 1);
            assertThat(orders.count()).isEqualTo(oldOrderCount + 1);
        } finally {
            executor.shutdownNow();
        }
    }
}
