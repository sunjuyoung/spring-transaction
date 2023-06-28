package com.example.springtx.ex;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class RollbackTest {

    @Autowired
    RollbackService rollbackService;

    //unchecked exception 은 롤백이 일어난다

    @Test
    void test(){
        Assertions.assertThatThrownBy(()->rollbackService.rollback())
                .isInstanceOf(RuntimeException.class);
    }

    //checked exception 은 롤백이 일어나지 않는다
    @Test
    void test2(){
        Assertions.assertThatThrownBy(()->rollbackService.noRollback())
                .isInstanceOf(RollbackService.MyException.class);
    }

    //checked exception 롤백 설정
    @Test
    void test3(){
        Assertions.assertThatThrownBy(()->rollbackService.rollbackFor())
                .isInstanceOf(RollbackService.MyException.class);
    }


    @TestConfiguration
    static class RollbackTestConfig{
         @Bean
         public RollbackService rollbackService(){
             return new RollbackService();
         }
    }


    @Slf4j
    static class RollbackService{

        @Transactional
        public void rollback(){
            log.info("rollback unchecked exception");
            throw new RuntimeException();
        }

        @Transactional
        public void noRollback() throws MyException {
            log.info("noRollback checked exception");
            throw new MyException();
        }

        @Transactional(rollbackFor = MyException.class)
        public void rollbackFor() throws MyException {
            log.info("rollbackFor checked exception");
            throw new MyException();
        }

        private class MyException extends Exception {
        }
    }
}
