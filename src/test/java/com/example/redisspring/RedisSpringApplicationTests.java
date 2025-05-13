package com.example.redisspring;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLongReactive;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest
class RedisSpringApplicationTests {

	@Autowired
	private ReactiveStringRedisTemplate template;

	@Autowired
	private RedissonReactiveClient client;

	// Spring Data Redis의 ReactiveValueOperations를 사용하여 Redis의 counter를 비동기적으로 증가시키기.
	// 1. dependency: spring-boot-starter-data-redis-reactive
	// + 실행결과: 10479 ms, 10848 ms, 11354 ms
	// 127.0.0.1:6379> get user:1:visit
	// "1500000"
	// 2. dependency: redisson-spring-boot-starter 로 변경
	// + 실행결과: 20732 ms, 20930 ms, 21587 ms
	@RepeatedTest(3)
	void springDataRedisTest() {
		ReactiveValueOperations<String, String> valueOperations = this.template.opsForValue();
		long before = System.currentTimeMillis();
		Mono<Void> mono = Flux.range(1, 500000)
				.flatMap(i -> valueOperations.increment("user:1:visit"))
				.then();
		StepVerifier.create(mono)
				.verifyComplete();
		long after = System.currentTimeMillis();
		System.out.println((after - before) + " ms");
	}

	// Redisson 라이브러리의 RAtomicLongReactive를 사용하여 Redis의 atomic long counter를 비동기적으로 증가시키기.
	// 실행결과: 21537 ms, 19782 ms, 20630 ms
	@RepeatedTest(3)
	void redissonTest() {
		RAtomicLongReactive atomicLong = this.client.getAtomicLong("user:2:visit");
		long before = System.currentTimeMillis();
		Mono<Void> mono = Flux.range(1, 500000)
				.flatMap(i -> atomicLong.incrementAndGet())
				.then();
		StepVerifier.create(mono)
				.verifyComplete();
		long after = System.currentTimeMillis();
		System.out.println((after - before) + " ms");
	}

}
