package org.springframework.data.ebean.sample.config;

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
import org.springframework.data.ebean.sample.domain.Address;
import org.springframework.data.ebean.sample.domain.FullName;
import org.springframework.data.ebean.sample.domain.Role;
import org.springframework.data.ebean.sample.domain.User;
import org.springframework.data.ebean.sample.domain.UserDomainService;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * @author Xuegui Yuan
 */
@Configuration
@EnableEbeanRepositories(value = "org.springframework.data.ebean.sample")
@EnableTransactionManagement
public class SampleConfig {
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public QueryChannelService ebeanQueryChannelService(Database ebeanServer) {
        return new EbeanQueryChannelService(ebeanServer);
    }

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Bean
    @Primary
    public DatabaseConfig defaultEbeanServerConfig() {
        DatabaseConfig config = new DatabaseConfig();

        config.setDataSource(dataSource());
        config.setExternalTransactionManager(new SpringJdbcTransactionManager());

        config.loadFromProperties();
        config.setDefaultServer(true);
        config.setRegister(true);
        //config.setAutoCommitMode(false);
        config.setExpressionNativeIlike(true);
        config.addClass(Address.class);
        config.addClass(FullName.class);
        config.addClass(Role.class);
        config.addClass(User.class);

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
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    @Primary
    public Database defaultEbeanServer(DatabaseConfig defaultEbeanServerConfig) {
        return DatabaseFactory.create(defaultEbeanServerConfig);
    }

    @Bean
    public UserDomainService userDomainService() {
        return new UserDomainService();
    }
}
