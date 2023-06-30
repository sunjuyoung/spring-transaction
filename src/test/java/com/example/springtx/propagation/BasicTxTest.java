package com.example.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

    @TestConfiguration
    static class BasicTxConfig{

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource){
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit(){
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 커밋");
        txManager.commit(status);
        log.info("트랜잭션 커밋 종료");

    }


    @Test
    void rollback(){
        log.info("트랜잭션 시작");
        TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 rollback");
        txManager.rollback(status);
        log.info("트랜잭션 rollback 종료");

    }

    @Test
    void double_commit(){
        log.info("트랜잭션 1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션 1 커밋");
        txManager.commit(tx1);

        log.info("트랜잭션 2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션 2 커밋");
        txManager.commit(tx2);
    }

    @DisplayName("내부 트랜잭션 테스트")
    @Test
    void inner_commit(){
        log.info("외부 트랜잭션  시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer isNewTransaction() ={} ", outer.isNewTransaction());// 처음 시작된 트랜잭션 인지 확인

        extracted(); //내부 트랜잭션 호출
        //이미 외부 트랜잭션 진행 중
        //내부 트랜잭션은
        //외부 트랜잭션에 참여한다
        /**
         * 내부 트랜잭션이 외부 트랜잭션에 참여한다는 뜻은 내부 트랜잭션이 외부 트랜잭션을 그대로 이어
         * 받아서 따른다는 뜻이다.
         * 다른 관점으로 보면 외부 트랜잭션의 범위가 내부 트랜잭션까지 넓어진다는 뜻이다.
         * 외부에서 시작된 물리적인 트랜잭션의 범위가 내부 트랜잭션까지 넓어진다는 뜻이다.
         * 정리하면 외부 트랜잭션과 내부 트랜잭션이 하나의 물리 트랜잭션으로 묶이는 것이다.
         * 내부 트랜잭션은 이미 진행중인 외부 트랜잭션에 참여한다. 이 경우 신규 트랜잭션이 아니다
         *
         * 그런데 코드를 잘 보면 커밋을 두 번 호출했다. 트랜잭션을 생각해보면 하나의 커넥션에 커밋은 한번만
         * 호출할 수 있다. 커밋이나 롤백을 하면 해당 트랜잭션은 끝나버린다
         *
         *스프링은 이렇게 여러 트랜잭션이 함께 사용되는 경우, 처음 트랜잭션을 시작한 외부 트랜잭션이 실제 물리
         * 트랜잭션을 관리하도록 한다. 이를 통해 트랜잭션 중복 커밋 문제를 해결한다.
         */

        log.info("외부 트랜잭션  커밋");
        txManager.commit(outer);
    }

    private void extracted() {
        log.info("내부 트랜잭션  시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner isNewTransaction() ={} ", inner.isNewTransaction());
        log.info("내부 트랜잭션  커밋");
        txManager.commit(inner);
    }


    @DisplayName("외부 롤백 테스트")
    @Test
    void outer_rollback(){
        log.info("외부 트랜잭션  시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer isNewTransaction() ={} ", outer.isNewTransaction());// 처음 시작된 트랜잭션 인지 확인

        extracted();


        log.info("외부 트랜잭션  롤백..");
        txManager.rollback(outer);
    }

    @DisplayName("내부 롤백 테스트")
    @Test
    void inner_rollback(){
        log.info("외부 트랜잭션  시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer isNewTransaction() ={} ", outer.isNewTransaction());// 처음 시작된 트랜잭션 인지 확인

        log.info("내부 트랜잭션  시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner isNewTransaction() ={} ", inner.isNewTransaction());
        log.info("내부 트랜잭션  롤백");
        txManager.rollback(inner);  //Participating transaction failed
        //rollback-only 마킹한다


        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);//외부는 커밋을 했지만 rollback-only 마크를 보고 롤백한다
    }


    @DisplayName("REQUIRED_NEW 내부 트랜잭션 롤백 테스트")
    @Test
    void inner_rollback_require_new(){
        log.info("외부 트랜잭션  시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute()); //conn0
        log.info("outer isNewTransaction() ={} ", outer.isNewTransaction());// 처음 시작된 트랜잭션 인지 확인

        log.info("내부 트랜잭션  시작");
        //옵션 설정
        DefaultTransactionAttribute defintion = new DefaultTransactionAttribute();
        defintion.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        //기본값 PROPAGATION_REQUIRED 참여
        //PROPAGATION_REQUIRES_NEW 새로 시작한다

        TransactionStatus inner = txManager.getTransaction(defintion);

        log.info("inner isNewTransaction() ={} ", inner.isNewTransaction());  //true  //conn1
        log.info("내부 트랜잭션  롤백");
        txManager.rollback(inner);


        log.info("외부 트랜잭션  커밋..");
        txManager.commit(outer);
    }

}
