package com.cmdlee.quizsushi.global.config.infra.redis;

import com.cmdlee.quizsushi.member.domain.model.RefreshTokenData;
import com.cmdlee.quizsushi.quiz.challenge.model.LeaderboardEntry;
import com.cmdlee.quizsushi.quiz.challenge.model.ChallengeSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, RefreshTokenData> refreshTokenRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, RefreshTokenData> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<RefreshTokenData> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, RefreshTokenData.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, ChallengeSession> challengeSessionRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, ChallengeSession> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<ChallengeSession> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, ChallengeSession.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.afterPropertiesSet();

        return template;
    }

    @Bean
    public RedisTemplate<String, LeaderboardEntry> leaderboardRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, LeaderboardEntry> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));

        return template;
    }
}