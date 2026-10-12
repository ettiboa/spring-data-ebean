package org.springframework.data.ebean.querychannel;

import static org.assertj.core.api.Assertions.assertThat;

import io.ebean.Query;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import io.github.ettiboa.ebean.sample.config.SampleConfig;
import io.github.ettiboa.ebean.sample.domain.User;
import io.github.ettiboa.ebean.sample.domain.UserInfo;
import io.github.ettiboa.ebean.sample.domain.UserRepository;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * @author Xuegui Yuan
 */
@SpringJUnitConfig(classes = SampleConfig.class)
class EbeanQueryChannelServiceIntegrationTest {

    // Test fixture
    protected User user;
    @Autowired
    private QueryChannelService queryChannel;
    @Autowired
    private UserRepository repository;

    @BeforeEach
    public void initUser() {
        repository.deleteAll();
        user = new User("QueryChannel", "Test", "testquerychannel@163.com");
        user.setAge(29);
        user = repository.save(user);
    }


    @Test
    void createSqlQueryMappingColumns() {
        String sql1 = "select first_name, last_name, email_address from user where last_name= :lastName";
        String sql2 = "select first_name as firstName, last_name as lastName, email_address as emailAddress from user where last_name= :lastName";
        Map<String, String> columnsMapping = new HashMap<>();
        columnsMapping.put("first_name", "firstName");
        columnsMapping.put("last_name", "lastName");

        Query<UserInfo> query1 = queryChannel.createSqlQuery(UserInfo.class,
            sql1);
        Query<UserInfo> query2 = queryChannel.createSqlQuery(UserInfo.class,
            sql2);
        Query<UserInfo> query3 = queryChannel.createSqlQueryMappingColumns(UserInfo.class,
            sql1, columnsMapping);

        query1.setParameter("lastName", "Test");
        query2.setParameter("lastName", "Test");
        query3.setParameter("lastName", "Test");
        UserInfo userInfo1 = query1.findOne();
        UserInfo userInfo2 = query2.findOne();
        UserInfo userInfo3 = query3.findOne();
        assertThat(userInfo1).isNotNull();
        assertThat(userInfo1.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userInfo1.getEmailAddress()).isEqualTo("testquerychannel@163.com");

        assertThat(userInfo2).isNotNull();
        assertThat(userInfo2.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userInfo2.getEmailAddress()).isEqualTo("testquerychannel@163.com");

        assertThat(userInfo3).isNotNull();
        assertThat(userInfo3.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userInfo3.getEmailAddress()).isEqualTo("testquerychannel@163.com");
    }

    @Test
    void createNamedQuery() {
        UserInfo userInfo = queryChannel.createSqlQuery(UserInfo.class,
            "select first_name, last_name, email_address from user where email_address = :emailAddress order by id desc")
            .setParameter("emailAddress", "testquerychannel@163.com").findOne();

        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userInfo.getEmailAddress()).isEqualTo("testquerychannel@163.com");
    }

    @Test
    void createNamedQueryWhere() {
        UserInfo userInfo = queryChannel.createSqlQuery(UserInfo.class,
                "select first_name, last_name, email_address from user").where()
            .eq("emailAddress", "testquerychannel@163.com").findOne();

        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userInfo.getEmailAddress()).isEqualTo("testquerychannel@163.com");
    }

    @Test
    void createDtoQuery() {
        String sql = "select first_name, last_name, email_address from user where email_address = :emailAddress";
        UserDTO userDTO = queryChannel.createDtoQuery(UserDTO.class, sql)
            .setParameter("emailAddress", "testquerychannel@163.com")
            .findOne();

        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userDTO.getEmailAddress()).isEqualTo("testquerychannel@163.com");
    }

    @Test
    void createNamedDtoQuery() {
        UserDTO userDTO = queryChannel.createDtoQuery(UserDTO.class,
            "select first_name, last_name, email_address from user where email_address = :emailAddress")
            .setParameter("emailAddress", "testquerychannel@163.com")
            .findOne();

        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userDTO.getEmailAddress()).isEqualTo("testquerychannel@163.com");
    }

    @Test
    void query_queryObject() {
        UserQuery userQuery = new UserQuery();
        userQuery.setEmailAddress("testquerychannel@163.com");
        userQuery.setAgeStart(1);
        userQuery.setAgeEnd(30);

        User user = queryChannel.createQuery(User.class, userQuery)
            .findOne();
        assertThat(user).isNotNull();
        assertThat(user.getEmailAddress()).isEqualTo("testquerychannel@163.com");

        UserDTO userDTO = queryChannel.createQuery(User.class, userQuery)
            .asDto(UserDTO.class)
            .setRelaxedMode()
            .findOne();
        assertThat(userDTO).isNotNull();
        assertThat(userDTO.getEmailAddress()).isEqualTo("testquerychannel@163.com");
    }

    @Test
    void applyQueryObject() {
        UserQuery userQuery = new UserQuery();
        userQuery.setEmailAddress("testquerychannel@163.com");
        userQuery.setAgeStart(1);
        userQuery.setAgeEnd(30);
        UserInfo userInfo = EbeanQueryChannelService
            .applyWhere(
                queryChannel.createSqlQuery(UserInfo.class,
                    "select first_name, last_name, email_address from user").where(),
                userQuery
            )
            .findOne();

        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getFirstName()).isEqualTo("QueryChannel");
        assertThat(userInfo.getEmailAddress()).isEqualTo("testquerychannel@163.com");
    }
}