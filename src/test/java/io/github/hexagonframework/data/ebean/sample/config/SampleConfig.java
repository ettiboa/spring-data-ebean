package io.github.hexagonframework.data.ebean.sample.config;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.DatabaseConfig;
import io.ebean.spring.txn.SpringJdbcTransactionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.ebean.querychannel.EbeanQueryChannelService;
import org.springframework.data.ebean.querychannel.QueryChannelService;
import org.springframework.data.ebean.repository.config.EnableEbeanRepositories;
import io.github.hexagonframework.data.ebean.sample.domain.UserDomainService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author Xuegui Yuan
 */
@Configuration
@EnableEbeanRepositories(value = "io.github.hexagonframework.data.ebean.sample")
@EnableTransactionManagement
public class SampleConfig {
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public QueryChannelService ebeanQueryChannelService(Database database) {
        return new EbeanQueryChannelService(database);
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Primary
    public DatabaseConfig defaultDatabaseConfig() {
        DatabaseConfig config = new DatabaseConfig();

        config.setDataSource(dataSource());
        config.setExternalTransactionManager(new SpringJdbcTransactionManager());

        config.loadFromProperties();
        config.setDefaultServer(true);
        config.setRegister(true);
        //config.setAutoCommitMode(false);
        config.setExpressionNativeIlike(true);
        config.addPackage("io.github.hexagonframework.data.ebean.sample.domain");

        config.setCurrentUserProvider(new CurrentUserProvider() {
            @Override
            public Object currentUser() {
                return "test"; // just for test, can rewrite to get the currentUser from threadLocal
            }
        });

        return config;
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;NON_KEYWORDS=USER");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    @Bean
    public DatabaseFactory ebeanServerFactoryBean() {
        return new DatabaseFactory();
    }

    @Bean
    @Primary
    public Database defaultDatabase(DatabaseConfig defaultEbeanServerConfig) {
        return DatabaseFactory.create(defaultEbeanServerConfig);
    }

    @Bean
    public UserDomainService userDomainService() {
        return new UserDomainService();
    }
}
