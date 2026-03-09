package com.subham.microservices.configuration;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ObservationConfig {

  @Bean
  ObservedAspect observedAspect(ObservationRegistry registry) {
    return new ObservedAspect(registry);
  }
}
