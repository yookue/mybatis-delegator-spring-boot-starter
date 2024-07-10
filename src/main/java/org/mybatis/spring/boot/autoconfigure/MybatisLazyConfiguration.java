/*
 *    Copyright (c) 2015-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.mybatis.spring.boot.autoconfigure;


import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import com.yookue.commonplexus.springutil.util.ClassPathWraps;
import jakarta.annotation.Nonnull;


/**
 * Mybatis lazy configuration
 * <p>
 * {@link EnableAutoConfiguration Auto-Configuration} for Mybatis. Contributes a {@link SqlSessionFactory} and a {@link SqlSessionTemplate}.
 * <p>
 * If {@link org.mybatis.spring.annotation.MapperScan} is used, or a configuration file is specified as a property,
 * those will be considered, otherwise this configuration will attempt to register mappers based on the interface
 * definitions in or under the root configuration package.
 *
 * @author Eddú Meléndez
 * @author Josh Long
 * @author Kazuki Shimizu
 * @author Eduardo Macarrón
 * @see org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
 */
// @org.springframework.context.annotation.Configuration
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnSingleCandidate(DataSource.class)
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisLanguageDriverAutoConfiguration.class})
@SuppressWarnings("rawtypes")
public class MybatisLazyConfiguration /*implements InitializingBean*/ {
    private final Interceptor[] interceptors;
    private final TypeHandler[] typeHandlers;
    private final LanguageDriver[] languageDrivers;
    private final ResourceLoader resourceLoader;
    private final DatabaseIdProvider databaseIdProvider;
    private final List<ConfigurationCustomizer> configurationCustomizers;

    public MybatisLazyConfiguration(@Nonnull ObjectProvider<Interceptor[]> interceptors, @Nonnull ObjectProvider<TypeHandler[]> typeHandlers, @Nonnull ObjectProvider<LanguageDriver[]> languageDrivers, @Nonnull ResourceLoader resourceLoader, @Nonnull ObjectProvider<DatabaseIdProvider> databaseIdProvider, @Nonnull ObjectProvider<List<ConfigurationCustomizer>> configurationCustomizers) {
        // this.properties = properties;
        this.interceptors = interceptors.getIfAvailable();
        this.typeHandlers = typeHandlers.getIfAvailable();
        this.languageDrivers = languageDrivers.getIfAvailable();
        this.resourceLoader = resourceLoader;
        this.databaseIdProvider = databaseIdProvider.getIfAvailable();
        this.configurationCustomizers = configurationCustomizers.getIfAvailable();
    }

    private void checkConfigFileExists(@Nonnull MybatisProperties properties) {
        if (properties.isCheckConfigLocation() && StringUtils.hasText(properties.getConfigLocation())) {
            // David Hsing modified on 2021-08-13
            Resource resource;
            if (ClassPathWraps.startsWithClasspath(properties.getConfigLocation()) || ClassPathWraps.startsWithClasspathStar(properties.getConfigLocation())) {
                resource = new ClassPathResource(ClassPathWraps.removeClasspathPrefix(properties.getConfigLocation()), resourceLoader.getClassLoader());
            } else {
                resource = resourceLoader.getResource(properties.getConfigLocation());    // $NON-NLS-1$
            }
            Assert.state(resource.exists(), "Cannot find config location: " + resource + " (please add config file or check your Mybatis configuration)");
        }
    }

    // @Bean
    // @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(@Nonnull DataSource dataSource, @Nonnull MybatisProperties properties) throws Exception {
        checkConfigFileExists(properties);

        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (StringUtils.hasText(properties.getConfigLocation())) {
            // David Hsing modified on 2021-08-13
            if (ClassPathWraps.startsWithClasspath(properties.getConfigLocation()) || ClassPathWraps.startsWithClasspathStar(properties.getConfigLocation())) {
                factory.setConfigLocation(new ClassPathResource(ClassPathWraps.removeClasspathPrefix(properties.getConfigLocation()), resourceLoader.getClassLoader()));
            } else {
                factory.setConfigLocation(resourceLoader.getResource(properties.getConfigLocation()));
            }
        }
        applyConfiguration(factory, properties);
        if (!ObjectUtils.isEmpty(properties.getConfigurationProperties())) {
            factory.setConfigurationProperties(properties.getConfigurationProperties());
        }
        if (!ObjectUtils.isEmpty(this.interceptors)) {
            factory.setPlugins(this.interceptors);
        }
        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }
        if (StringUtils.hasText(properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        if (properties.getTypeAliasesSuperType() != null) {
            factory.setTypeAliasesSuperType(properties.getTypeAliasesSuperType());
        }
        if (StringUtils.hasText(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }
        if (!ObjectUtils.isEmpty(this.typeHandlers)) {
            factory.setTypeHandlers(this.typeHandlers);
        }
        if (!ObjectUtils.isEmpty(properties.getMapperLocations()) && !ObjectUtils.isEmpty(properties.resolveMapperLocations())) {
            factory.setMapperLocations(properties.resolveMapperLocations());
        }
        Set<String> factoryPropertyNames = Stream.of(new BeanWrapperImpl(SqlSessionFactoryBean.class).getPropertyDescriptors()).map(PropertyDescriptor::getName).collect(Collectors.toSet());
        Class<? extends LanguageDriver> defaultLanguageDriver = properties.getDefaultScriptingLanguageDriver();
        if (factoryPropertyNames.contains("scriptingLanguageDrivers") && !ObjectUtils.isEmpty(this.languageDrivers)) {
            // Need to mybatis-spring 2.0.2+
            factory.setScriptingLanguageDrivers(this.languageDrivers);
            if (defaultLanguageDriver == null && this.languageDrivers.length == 1) {
                defaultLanguageDriver = this.languageDrivers[0].getClass();
            }
        }
        if (factoryPropertyNames.contains("defaultScriptingLanguageDriver")) {
            // Need to mybatis-spring 2.0.2+
            factory.setDefaultScriptingLanguageDriver(defaultLanguageDriver);
        }

        return factory.getObject();
    }

    private void applyConfiguration(@Nonnull SqlSessionFactoryBean factory, @Nonnull MybatisProperties properties) {
        MybatisProperties.CoreConfiguration coreConfiguration = properties.getConfiguration();
        Configuration configuration = null;
        if (coreConfiguration != null || !StringUtils.hasText(properties.getConfigLocation())) {
            configuration = new Configuration();
        }
        if (configuration != null && coreConfiguration != null) {
            coreConfiguration.applyTo(configuration);
        }
        if (configuration != null && !CollectionUtils.isEmpty(this.configurationCustomizers)) {
            for (ConfigurationCustomizer customizer : this.configurationCustomizers) {
                customizer.customize(configuration);
            }
        }
        factory.setConfiguration(configuration);
    }

    // @Bean
    // @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(@Nonnull SqlSessionFactory sqlSessionFactory, @Nonnull MybatisProperties properties) {
        checkConfigFileExists(properties);
        ExecutorType executorType = properties.getExecutorType();
        if (executorType != null) {
            return new SqlSessionTemplate(sqlSessionFactory, executorType);
        } else {
            return new SqlSessionTemplate(sqlSessionFactory);
        }
    }
}
