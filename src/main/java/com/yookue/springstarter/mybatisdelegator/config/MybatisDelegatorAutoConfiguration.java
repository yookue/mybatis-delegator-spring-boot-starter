/*
 * Copyright (c) 2020 Yookue Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yookue.springstarter.mybatisdelegator.config;


import java.util.List;
import jakarta.annotation.Nonnull;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcOperations;
import com.yookue.springstarter.mybatisdelegator.composer.MybatisConfigurationDelegator;
import com.yookue.springstarter.mybatisdelegator.composer.impl.MybatisConfigurationDelegatorImpl;
import com.yookue.springstarter.mybatisdelegator.property.MybatisDelegatorProperties;


/**
 * Configuration for {@link com.yookue.springstarter.mybatisdelegator.composer.MybatisConfigurationDelegator}
 *
 * @author David Hsing
 * @see com.yookue.springstarter.mybatisdelegator.composer.MybatisConfigurationDelegator
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = MybatisDelegatorAutoConfiguration.PROPERTIES_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(value = {DataSource.class, JdbcOperations.class, SqlSession.class})
@EnableConfigurationProperties(value = MybatisDelegatorProperties.class)
@SuppressWarnings({"rawtypes", "SpringFacetCodeInspection"})
public class MybatisDelegatorAutoConfiguration {
    public static final String PROPERTIES_PREFIX = "spring.mybatis-delegator";    // $NON-NLS-1$
    public static final String CONFIGURATION_DELEGATOR = "mybatisConfigurationDelegator";    // $NON-NLS-1$

    @Bean(name = CONFIGURATION_DELEGATOR)
    @ConditionalOnMissingBean(name = CONFIGURATION_DELEGATOR)
    public MybatisConfigurationDelegator configurationDelegator(@Nonnull ObjectProvider<Interceptor[]> interceptors, @Nonnull ObjectProvider<TypeHandler[]> typeHandlers, @Nonnull ObjectProvider<LanguageDriver[]> languageDrivers, @Nonnull ResourceLoader resourceLoader, @Nonnull ObjectProvider<DatabaseIdProvider> databaseIdProviders, @Nonnull ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizers) {
        return new MybatisConfigurationDelegatorImpl(interceptors, typeHandlers, languageDrivers, resourceLoader, databaseIdProviders, configurationCustomizers);
    }
}
