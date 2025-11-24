package com.tfg.tfg.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.DeserializationFeature;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

        @Bean
        public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // Create ObjectMapper with proper configuration for Redis serialization
                ObjectMapper objectMapper = new ObjectMapper();

                // Ignore unknown properties (like computed getters 'kda', 'kdaRatio') during
                // deserialization
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                objectMapper.activateDefaultTyping(
                                BasicPolymorphicTypeValidator.builder()
                                                .allowIfBaseType(Object.class)
                                                .build(),
                                ObjectMapper.DefaultTyping.EVERYTHING,
                                JsonTypeInfo.As.WRAPPER_OBJECT);

                // Create serializer with configured ObjectMapper
                GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

                // Default configuration: 1 hour TTL, JSON serialization
                RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1))
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(RedisSerializationContext.SerializationPair
                                                .fromSerializer(serializer))
                                .disableCachingNullValues();

                // Specific configurations for different caches
                Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

                // Summoners cache: 10 minutes (to keep rank/level relatively fresh)
                cacheConfigurations.put("summoners", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));

                // Masteries cache: 1 hour (doesn't change too often)
                cacheConfigurations.put("masteries", defaultCacheConfig.entryTtl(Duration.ofHours(1)));

                // Matches cache: 24 hours (finished matches don't change)
                cacheConfigurations.put("matches", defaultCacheConfig.entryTtl(Duration.ofHours(24)));

                // Champions cache: 24 hours (static data)
                cacheConfigurations.put("champions", defaultCacheConfig.entryTtl(Duration.ofHours(24)));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(defaultCacheConfig)
                                .withInitialCacheConfigurations(cacheConfigurations)
                                .build();
        }
}
