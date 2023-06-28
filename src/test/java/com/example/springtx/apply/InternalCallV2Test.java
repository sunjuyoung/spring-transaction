package com.example.springtx.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;


/**
 *  클래스로 분리하는 방법을 주로 사용한다
 */
@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired
    CallService callService;


    @Test
    void proxyCheck(){
        log.info("{}",callService.getClass());
        Assertions.assertTrue(AopUtils.isAopProxy(callService));
    }

    @Test
    @DisplayName("외부에서 호출하는 경우")
    void txTest(){
        callService.external();
    }

    @Test
    void txTest2(){
       // callService.internal();
    }

    @TestConfiguration
    static class InternalCallV1Config{
        @Bean
        public CallService callService(){
            return new CallService(internalService());
        }

        @Bean
        public InternalService internalService(){
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService{

        private final InternalService internalService;

        public void external(){
            log.info("external v2");
            printTxInfo();
            internalService.internal(); //내부 호출 에서 외부호출로 변경
        }
//
//        @Transactional
//        public void internal(){
//            log.info("internal");
//            printTxInfo();
//        }


        private void printTxInfo(){
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active:{}",actualTransactionActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly(); //readOnly 확인
            log.info("tx readOnly:{}",readOnly);
        }
    }

    static class InternalService{

        @Transactional
        public void internal(){
            log.info("internal v2");
            printTxInfo();
        }

        private void printTxInfo(){
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active:{}",actualTransactionActive);
            boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly(); //readOnly 확인
            log.info("tx readOnly:{}",readOnly);
        }
    }
}
