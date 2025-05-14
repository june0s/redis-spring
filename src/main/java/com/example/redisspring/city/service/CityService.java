package com.example.redisspring.city.service;

import com.example.redisspring.city.client.CityClient;
import com.example.redisspring.city.dto.City;
import org.redisson.api.RMapCacheReactive;
import org.redisson.api.RMapReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@Service
public class CityService {

    @Autowired
    private CityClient cityClient;
    private RMapCacheReactive<String, City> cityMap;

    public CityService(RedissonReactiveClient client) {
        this.cityMap = client.getMapCache("city", new TypedJsonJacksonCodec(String.class, City.class));
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
                        .flatMap(c -> this.cityMap.fastPut(zipCode, c, 10, TimeUnit.SECONDS)
                                .thenReturn(c))
                );
    }

}
