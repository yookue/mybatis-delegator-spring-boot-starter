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

package com.yookue.springstarter.mybatisdelegator.composer.impl;


import java.util.List;
import jakarta.annotation.Nonnull;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.mybatis.spring.boot.autoconfigure.MybatisLazyConfiguration;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ResourceLoader;
import com.yookue.springstarter.mybatisdelegator.composer.MybatisConfigurationDelegator;


/**
 * Composer implementation for mybatis configurator delegator
 *
 * @author David Hsing
 */
@SuppressWarnings({"rawtypes", "unused"})
public class MybatisConfigurationDelegatorImpl implements MybatisConfigurationDelegator {
    private final MybatisLazyConfiguration configuration;

    public MybatisConfigurationDelegatorImpl(@Nonnull ObjectProvider<Interceptor[]> interceptors, @Nonnull ObjectProvider<TypeHandler[]> typeHandlers,
        @Nonnull ObjectProvider<LanguageDriver[]> languageDrivers, @Nonnull ResourceLoader resourceLoader,
        @Nonnull ObjectProvider<DatabaseIdProvider> databaseIdProviders, @Nonnull ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizers) {
        configuration = new MybatisLazyConfiguration(interceptors, typeHandlers, languageDrivers, resourceLoader, databaseIdProviders, configurationCustomizers);
    }

    @Override
    public SqlSessionFactory sqlSessionFactory(@Nonnull DataSource dataSource, @Nonnull MybatisProperties properties) throws Exception {
        return configuration.sqlSessionFactory(dataSource, properties);
    }

    @Override
    public SqlSessionTemplate sqlSessionTemplate(@Nonnull SqlSessionFactory factory, @Nonnull MybatisProperties properties) {
        return configuration.sqlSessionTemplate(factory, properties);
    }
}
