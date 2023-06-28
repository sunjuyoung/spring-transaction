package com.example.springtx.apply;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest
public class InitTxTest {

    @Autowired
    Hello hello;

    @Test
    void go(){
       // hello.initV1();
    }

    @TestConfiguration
    static class InitTxConfig{

        @Bean
        Hello hello(){
            return new Hello();
        }
    }

    @Slf4j
    static class Hello{

        /**
         * 초기화 코드(예: @PostConstruct )와 @Transactional 을 함께 사용하면 트랜잭션이 적용되지 않는다
         * 초기화 코드가 먼저 호출되고, 그 다음에 트랜잭션 AOP가 적용되기 때문이다. 따라서 초기화
         * 시점에는 해당 메서드에서 트랜잭션을 획득할 수 없다.
         *
         */
        @PostConstruct
        @Transactional
        public void initV1(){
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("initV1:{}",isActive);
        }

        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2(){
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("initV2 ApplicationReadyEvent :{}",isActive);


        }
    }
}
