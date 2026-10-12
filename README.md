# Spring Data Ebean

> Ebean implementation for Spring Data.

**This is a maintained fork of [hexagonframework/spring-data-ebean](https://github.com/hexagonframework/spring-data-ebean).**
The upstream project is no longer maintained; this fork carries it forward on a modern stack.

The primary goal of the [Spring Data](https://spring.io/projects/spring-data) project is to make it easier to build
Spring-powered applications that use data access technologies. This module deals with enhanced support for
[Ebean ORM](https://ebean.io) based data access layers.

## Requirements

| Component           | Version |
|---------------------|---------|
| Java                | 21+     |
| Spring Framework    | 7.x     |
| Spring Data Commons | 4.x     |
| Ebean               | 16.x    |

## Features

* Ebean implementation of CRUD methods for JPA entities
* Dynamic query generation from query method names and the `@Query` annotation
* Support for the query channel service (DTO and raw SQL queries)
* Transparent triggering of Ebean queries by query methods
* Domain base classes providing basic properties
* Transparent auditing (created, last changed)
* Easy Spring integration with a custom namespace

## Quick Start

Create a Maven project (Spring Boot 4.x recommended) and add the dependency:

```xml
<dependency>
    <groupId>io.github.ettiboa</groupId>
    <artifactId>spring-data-ebean</artifactId>
    <version>3.0.0-SNAPSHOT</version>
</dependency>
```

Define a table entity:

```java
@Entity
@Getter
@Setter
public class User {
  @Id
  @GeneratedValue
  private Long id;
  private String firstname;
  private String lastname;
  @Column(nullable = false, unique = true)
  private String emailAddress;
}
```

Create a repository interface:

```java
public interface UserRepository extends EbeanRepository<User, Long> {

    @Query("where emailAddress = :emailAddress order by id desc")
    User findUserByEmailAddressEqualsOql(@Param("emailAddress") String emailAddress);

    @Query(nativeQuery = true, value = "select * from user where email_address = :emailAddress order by id desc")
    User findUserByEmailAddressEquals(@Param("emailAddress") String emailAddress);

    List<User> findAllByEmailAddressAndLastname(String emailAddress, String lastname);
}
```

Enable repositories and expose the Ebean `Database`:

```java
@Configuration
@EnableEbeanRepositories
@EnableTransactionManagement
public class EbeanConfig {

    @Bean
    public Database database(DatabaseConfig config) {
        return DatabaseFactory.create(config);
    }
}
```

## Credits

* Original project and code: [hexagonframework/spring-data-ebean](https://github.com/hexagonframework/spring-data-ebean)
  by Xuegui Yuan.
* Fork maintenance: Ettiboa Adouakou.

## License

[Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
