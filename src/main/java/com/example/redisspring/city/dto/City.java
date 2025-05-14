package com.example.redisspring.city.dto;

import lombok.Data;
import lombok.ToString;

/* 데이터 구조 = = = = =
{
  "zip": "10001",
  "lat": 40.75065,
  "lng": -73.99718,
  "city": "New York",
  "stateId": "NY",
  "stateName": "New York",
  "population": 24117,
  "density": 15153.7,
  "temperature": 95
}
*/
@Data
@ToString
public class City {
    private String zip;
    private String city;
    private String stateName;
    private int temperature;
}
