package com.example.banking;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableMongoRepositories(basePackages="com.example.banking.address")
public class DatabaseConfiguration {
    @Bean(destroyMethod="close") @Primary
    HikariDataSource personalDataSource(@Value("${banking.mysql.url}") String url,
        @Value("${banking.mysql.username}") String username, @Value("${banking.mysql.password}") String password) {
        return source(url,username,password);
    }
    @Bean(destroyMethod="close")
    HikariDataSource bankDataSource(@Value("${banking.postgres.url}") String url,
        @Value("${banking.postgres.username}") String username, @Value("${banking.postgres.password}") String password) {
        return source(url,username,password);
    }
    private HikariDataSource source(String url, String username, String password) {
        var ds=new HikariDataSource(); ds.setJdbcUrl(url); ds.setUsername(username); ds.setPassword(password);
        ds.setMaximumPoolSize(5); ds.setConnectionTimeout(5000); return ds;
    }
    private LocalContainerEntityManagerFactoryBean factory(DataSource ds, String unit, String scan) {
        var emf=new LocalContainerEntityManagerFactoryBean(); emf.setDataSource(ds);
        emf.setPersistenceUnitName(unit); emf.setPackagesToScan(scan);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        emf.setJpaPropertyMap(java.util.Map.of("hibernate.hbm2ddl.auto","update")); return emf;
    }
    @Bean @Primary
    LocalContainerEntityManagerFactoryBean personalEntityManagerFactory(@Qualifier("personalDataSource") DataSource ds) {
        return factory(ds,"personal","com.example.banking.personal");
    }
    @Bean
    LocalContainerEntityManagerFactoryBean bankEntityManagerFactory(@Qualifier("bankDataSource") DataSource ds) {
        return factory(ds,"bank","com.example.banking.bank");
    }
    @Bean @Primary
    JpaTransactionManager personalTransactionManager(@Qualifier("personalEntityManagerFactory") EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
    @Bean
    JpaTransactionManager bankTransactionManager(@Qualifier("bankEntityManagerFactory") EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
    @Bean(destroyMethod="close") MongoClient mongoClient(@Value("${banking.mongo.uri}") String uri) { return MongoClients.create(uri); }
    @Bean MongoTemplate mongoTemplate(MongoClient client,@Value("${banking.mongo.database}") String db) { return new MongoTemplate(client,db); }

    @Configuration
    @EnableJpaRepositories(basePackages="com.example.banking.personal", entityManagerFactoryRef="personalEntityManagerFactory",transactionManagerRef="personalTransactionManager")
    static class PersonalJpa {}
    @Configuration
    @EnableJpaRepositories(basePackages="com.example.banking.bank", entityManagerFactoryRef="bankEntityManagerFactory",transactionManagerRef="bankTransactionManager")
    static class BankJpa {}
}
