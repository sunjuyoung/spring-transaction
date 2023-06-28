package com.example.springtx.apply;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Log4j2
@SpringBootTest
public class TxBasicTest {

    @Autowired
    BasicService basicService;


    @Test
    void proxyCheck(){
        log.info("{}",basicService.getClass());

        Assertions.assertTrue(AopUtils.isAopProxy(basicService));

    }

    @Test
    void txTest(){
        basicService.tx();
        basicService.noTx();
    }


    @TestConfiguration
    static class TxApplyBasicConfig{
        @Bean
        public BasicService basicService(){
            return new BasicService();
        }
    }

    @Log4j2
    static class BasicService{

        @Transactional
        public void tx(){
         log.info("tx");
            boolean actualTransactionActive =
                    TransactionSynchronizationManager.isActualTransactionActive(); //트랜잭션 확인
            log.info("tx active:{}",actualTransactionActive);
        }
      //  @Transactional
        public void noTx(){
            log.info("no tx");
            boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("no tx active:{}",actualTransactionActive);
        }
    }
}
