package com.tecsup.security.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Module hibernateModule() {
        Hibernate6Module module = new Hibernate6Module();
        // By default don't force lazy loading. If you want to automatically serialize
        // lazy associations you can enable FORCE_LAZY_LOADING (caution: may trigger N+1).
        // module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);
        return module;
    }
}
