package com.fingolfintek.lmdb;

import com.fingolfintek.lmdb.tx.LMDBTransactionManager;
import javaslang.control.Try;
import org.fusesource.lmdbjni.Env;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ConditionalOnClass(Env.class)
@EnableTransactionManagement
@EnableConfigurationProperties(LMDBProperties.class)
public class LMDBAutoConfiguration {

    private final LMDBProperties properties;

    public LMDBAutoConfiguration(LMDBProperties properties) {
        this.properties = properties;
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(Env.class)
    public Env lmdbEnvironment() {
        return Try.of(properties::getDbPathAsFile)
                .map(file -> file.exists() || file.mkdirs())
                .filter(dirExists -> dirExists)
                .mapTry(none -> new Env(properties.getDbPath()))
                .get();
    }
    
    @Bean
    @ConditionalOnMissingBean(LMDBCodec.class)
    public LMDBCodec lmdbCodec() {
        return new KryoLMDBCodec();
    }

    @Bean
    @ConditionalOnMissingBean(KeyGenerator.class)
    public KeyGenerator keyGenerator() {
        return new KeyGenerator();
    }

    @Bean
    @ConditionalOnMissingBean(LMDBTemplate.class)
    public LMDBTemplate lmdbTemplate() {
        return new LMDBTemplate(lmdbTransactionManager(), lmdbCodec(), keyGenerator());
    }

    @Bean
    @ConditionalOnMissingBean(LMDBTransactionManager.class)
    public LMDBTransactionManager lmdbTransactionManager() {
        return new LMDBTransactionManager(lmdbEnvironment());
    }

}
