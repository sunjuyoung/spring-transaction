package com.example.springtx.order;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;

    @Test
    public void test() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("잔고부족");

        orderService.order(order);

        Order findOrder = orderRepository.findById(order.getId()).get();

       // Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("완료");
        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }



    @DisplayName("unchecked 예외를 발생시키는 경우 롤백")
    @Test
    public void test2() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("ex");
        Assertions.assertThatThrownBy(()-> orderService.order(order)).isInstanceOf(RuntimeException.class);
    }


    @DisplayName("checked 예외를 발생시키는 경우")
    @Test
    public void test3() throws NotEnoughMoneyException {
        Order order = new Order();
        order.setUsername("잔고부족");

        Assertions.assertThatThrownBy(()-> orderService.order(order)).isInstanceOf(NotEnoughMoneyException.class);

        Order findOrder = orderRepository.findById(order.getId()).get();

        Assertions.assertThat(findOrder.getPayStatus()).isEqualTo("대기");
    }
}