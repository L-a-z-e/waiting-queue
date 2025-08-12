package com.laze.flowcontrol.service;

import com.laze.flowcontrol.EmbeddedRedis;
import com.laze.flowcontrol.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueueServiceTest {

    @Autowired
    private UserQueueService userQueueService;

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @BeforeEach
    public void beforeEach() {
        ReactiveRedisConnection reactiveConnection = redisTemplate.getConnectionFactory().getReactiveConnection();
        reactiveConnection.serverCommands().flushAll().subscribe();
    }

    @Test
    void registerWaitingQueue() {
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 101L))
                .expectNext(2L)
                .verifyComplete();
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 102L))
                .expectNext(3L)
                .verifyComplete();

    }

    @Test
    void alreadyRegisterWaitingQueue() {
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 100L))
                .expectNext(1L)
                .verifyComplete();

        StepVerifier.create(userQueueService.registerWaitingQueue("default", 100L))
                .expectError(ApplicationException.class)
                .verify();

    }

    @Test
    void emptyAllowUser() {
        StepVerifier.create(userQueueService.allowUser("default", 100L))
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void allowUser() {
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 100L)
                .then(userQueueService.registerWaitingQueue("default", 101L))
                .then(userQueueService.registerWaitingQueue("default", 102L))
                                .then(userQueueService.allowUser("default", 2L))
                )
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void allowUser2() {
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 100L)
                        .then(userQueueService.registerWaitingQueue("default", 101L))
                        .then(userQueueService.registerWaitingQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 5L))
                )
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void allowUserAfterRegisterWaitingQueue() {
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 100L)
                        .then(userQueueService.registerWaitingQueue("default", 101L))
                        .then(userQueueService.registerWaitingQueue("default", 102L))
                        .then(userQueueService.allowUser("default", 3L))
                        .then(userQueueService.registerWaitingQueue("default", 5L))
                )
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void isNotAllowed() {
        StepVerifier.create(userQueueService.isAllowed("default", 100L))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void isAllowed() {
        StepVerifier.create(userQueueService.registerWaitingQueue("default", 100L)
                        .then(userQueueService.allowUser("default", 2L))
                        .then(userQueueService.isAllowed("default", 100L)))
                .expectNext(true)
                .verifyComplete();
    }
}