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

/**
 * 트랜잭션 우선순위
 */
@Log4j2
@SpringBootTest
public class TxLevelTest {

    @Autowired
    LevelService levelService;


    @Test
    void proxyCheck(){
        log.info("{}",levelService.getClass());

        Assertions.assertTrue(AopUtils.isAopProxy(levelService));

    }

    @Test
    void txTest(){
        levelService.wirte();
        levelService.read();
    }


    @TestConfiguration
    static class TxLevelBasicConfig{
        @Bean
        public LevelService basicService(){
            return new LevelService();
        }
    }

    @Log4j2
    @Transactional(readOnly = true)
    static class LevelService{

        @Transactional(readOnly = false)
        public void wirte(){
         log.info("call wirte");
            printTxInfo();
        }
      //  @Transactional
        public void read(){
            log.info("call read");
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
