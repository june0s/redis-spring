package com.example.redisspring.city.service;

import com.example.redisspring.city.client.CityClient;
import com.example.redisspring.city.dto.City;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CityService {

    @Autowired
    private CityClient cityClient;
    private RMapReactive<String, City> cityMap;

    public CityService(RedissonReactiveClient client) {
        this.cityMap = client.getMap("city", new TypedJsonJacksonCodec(String.class, City.class));
    }

    /*
        1. Get from cache
        2. if empty - get from db or sourcr
                      then put it in cache
        return
    */
    public Mono<City> getCity(final String zipCode) {
        return this.cityMap.get(zipCode)
                .switchIfEmpty(this.cityClient.getCity(zipCode)
                        .flatMap(c -> this.cityMap.fastPut(zipCode, c)
                                .thenReturn(c))
                );
    }
    // 주기적으로 업데이트하므로, redis 에 없는 경우 errorResume 으로 처리
//    public Mono<City> getCity(final String zipCode) {
//        return this.cityMap.get(zipCode)
//                .onErrorResume(ex -> this.cityClient.getCity(zipCode));
//    }

    @Scheduled(fixedRate = 10_000)
    public void update() {
        // 아래 방법도 update 된다. 그런데 강사님 방식처럼 한 번에 밀어 넣는 편이 좋을 것 같다.
        long start = System.currentTimeMillis();
//        Flux<City> flux = this.cityClient.getAllCity();
//        flux.flatMap(c -> this.cityMap.get(c.getZip())
//                .switchIfEmpty(this.cityMap.fastPut(c.getZip(), c).thenReturn(c))
//        ).subscribe();

        this.cityClient.getAllCity()
                .collectList()
                .map(list -> list.stream().collect(Collectors.toMap(City::getZip, Function.identity())))
                .flatMap(this.cityMap::putAll)
                .subscribe();
        long end = System.currentTimeMillis();
        System.out.println("updated takes = " + (end-start) + " ms");
    }
}
