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
@EnableJpaRepositories(entityManagerFactoryRef = "infraTestEntityManagerFactory", transactionManagerRef = "infraTestTxnManager", basePackages = "com.superluli.infra.jpa")
public class InfraTestDataSourceConfig {

	@Autowired
	private JpaProperties jpaProperties;

	
	@Bean(name = "infraTestDataSource") // put name here to be avoid of conflicts with other data source
	@Primary // Pull in the JPA configuration via this data source's definition.
	@ConfigurationProperties(prefix = "app.datasource.infraTest")
	public DataSource promotionDataSource() {
		return DataSourceBuilder.create().build();
	}

	@Primary
	@Bean(name = "infraTestEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(EntityManagerFactoryBuilder factoryBuilder,
			@Qualifier("infraTestDataSource") DataSource fds) {
		return factoryBuilder.dataSource(fds).packages("com.superluli.infra.jpa")
				.persistenceUnit("infraTestPersistenceUnit")
				// Using Hibernate and Not using JTA. (Change the next line if
				// your context is different.)
				.properties(this.jpaProperties.getHibernateProperties(fds)).build();
	}

	@Primary
	@Bean(name = "infraTestTxnManager")
	public PlatformTransactionManager transactionManager(
			@Qualifier("infraTestEntityManagerFactory") EntityManagerFactory factory) {
		return new JpaTransactionManager(factory);
	}

}
