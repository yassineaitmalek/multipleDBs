package com.test.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.core.env.Environment;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
@AllArgsConstructor
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = SQLite.DB
    + BasePackage.EMF, transactionManagerRef = SQLite.DB
        + BasePackage.TM, basePackages = { BasePackage.BASE_REPO + SQLite.DB })

public class SQLite {

  private final Environment env;

  public static final String DB = "sqlite";

  @Bean(DB + BasePackage.DSP)
  @ConfigurationProperties("spring." + DB + ".datasource")
  public DataSourceProperties getDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean(DB + BasePackage.DS)
  @ConfigurationProperties("spring." + DB + ".datasource.configuration")
  public DataSource getDataSourceDataSource(
      @Qualifier(DB + BasePackage.DSP) DataSourceProperties dataSourceProperties) {
    log.info("Loading DB {} DataSource", DB);
    return dataSourceProperties.initializeDataSourceBuilder().build();
  }

  @Bean(DB + BasePackage.JPA)
  public Map<String, String> getJpaProperties() {
    Map<String, String> properties = new HashMap<>();
    properties.put("hibernate.hbm2ddl.auto", env.getProperty(String.format("spring.%s.jpa.hibernate.ddl-auto", DB)));
    properties.put("hibernate.show_sql", env.getProperty(String.format("spring.%s.jpa.show-sql", DB)));
    properties.put("hibernate.dialect", env.getProperty(String.format("spring.%s.jpa.database-platform", DB)));
    properties.put("hibernate.generate-ddl", env.getProperty(String.format("spring.%s.jpa.generate-ddl", DB)));
    properties.put("sql.init.mode", env.getProperty(String.format("spring.%s.sql.init.mode", DB)));
    return properties;
  }

  @Bean(name = DB + BasePackage.EMF)
  public LocalContainerEntityManagerFactoryBean getEntityManagerFactory(EntityManagerFactoryBuilder builder,
      @Qualifier(DB + BasePackage.DS) DataSource dataSource,
      @Qualifier(DB + BasePackage.JPA) Map<String, String> jpaProperties) {
    log.info("Loading DB {} EntityManger & JPA", DB);
    return builder
        .dataSource(dataSource)
        .properties(jpaProperties)
        .packages(BasePackage.BASE_MODEL + DB)
        .build();
  }

  @Bean(name = DB + BasePackage.TM)
  public PlatformTransactionManager getTransactionManager(
      @Qualifier(DB + BasePackage.EMF) LocalContainerEntityManagerFactoryBean entityManagerFactory) {
    log.info("Loading DB {} TransactionManager", DB);
    return Optional.ofNullable(entityManagerFactory)
        .map(LocalContainerEntityManagerFactoryBean::getObject)
        .map(JpaTransactionManager::new)
        .orElse(null);
  }

}
