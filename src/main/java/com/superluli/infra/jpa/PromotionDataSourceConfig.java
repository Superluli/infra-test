package com.superluli.infra.jpa;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "promotionEntityManagerFactory", transactionManagerRef = "promotionTransactionManager", basePackages = "com.samsung.cloud.promotion.apiserver.persistence")
public class PromotionDataSourceConfig {

	@Autowired
	private JpaProperties jpaProperties;

	@Bean(name = "promotionDataSource")
	@Primary // Pull in the JPA configuration via this data source's definition.
	@ConfigurationProperties(prefix = "app.datasource.promotion")
	public DataSource promotionDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Primary
	@Bean(name = "promotionEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factoryBuilder,
			@Qualifier("promotionDataSource") DataSource fds) {
		return factoryBuilder.dataSource(fds).packages("com.samsung.cloud.promotion.apiserver.persistence")
				.persistenceUnit("promotionPersistenceUnit")
				// Using Hibernate and Not using JTA. (Change the next line if
				// your context is different.)
				.properties(this.jpaProperties.getHibernateProperties(fds)).build();
	}

	@Primary
	@Bean(name = "promotionTransactionManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("promotionEntityManagerFactory") EntityManagerFactory factory) {
		return new JpaTransactionManager(factory);
	}

}
