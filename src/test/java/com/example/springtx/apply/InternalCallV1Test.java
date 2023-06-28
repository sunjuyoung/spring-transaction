package com.example.springtx.apply;

import lombok.extern.log4j.Log4j2;
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

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

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
        callService.internal();
    }

    @TestConfiguration
    static class InternalCallV1Config{
        @Bean
        public CallService callService(){
            return new CallService();
        }
    }

    @Slf4j
    static class CallService{


        /**
         * 문제 원인
         * 자바 언어에서 메서드 앞에 별도의 참조가 없으면 this 라는 뜻으로 자기 자신의 인스턴스를 가리킨다.
         * 결과적으로 자기 자신의 내부 메서드를 호출하는 this.internal() 이 되는데, 여기서 this 는 자기
         * 자신을 가리키므로, 실제 대상 객체( target )의 인스턴스를 뜻한다. 결과적으로 이러한 내부 호출은
         * 프록시를 거치지 않는다. 따라서 트랜잭션을 적용할 수 없다. 결과적으로 target 에 있는 internal() 을
         * 직접 호출하게 된 것이다
         */
        public void external(){
            log.info("external");
            printTxInfo();
            internal(); //내부 호출 트랜잭션 적용 안됨
        }


        @Transactional
         public void internal(){
            log.info("internal");
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
