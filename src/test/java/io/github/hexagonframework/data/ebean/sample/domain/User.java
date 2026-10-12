/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hexagonframework.data.ebean.sample.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Domain class representing a person emphasizing the use of {@code AbstractEntity}. No declaration
 * of an id is required. The id is typed by the parameterizable superclass.
 *
 * @author Xuegui Yuan
 */
@Entity
@Table(name = "user")
@NamedQuery(name = "withManagerById",
    query = "select (fullName,address) fetch manager (fullName) where fullName.lastName = :lastName order by id desc")
@Getter
@Setter
public class User extends AbstractAggregateRoot {

    @Embedded
    private FullName fullName;
    private int age;
    private boolean active;

    @Column(nullable = false, unique = true)
    private String emailAddress;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(name = "user_colleague",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "colleague_id"))
    private Set<User> colleagues = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "user_role",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToOne
    private User manager;

    @Embedded
    private Address address;

    @Lob
    private byte[] binaryData;

    @Temporal(TemporalType.DATE)
    private Date dateOfBirth;

    /**
     * Creates a new empty instance of {@code User}.
     */
    public User() {
    }

    /**
     * Creates a new instance of {@code User} with preinitialized values for firstName, lastName,
     * email address and roles.
     *
     * @param firstName
     * @param lastName
     * @param emailAddress
     * @param roles
     */
    public User(String firstName, String lastName, String emailAddress, Role... roles) {
        this.fullName = new FullName(firstName, lastName);
        this.emailAddress = emailAddress;
        this.active = true;
        this.roles.addAll(Arrays.asList(roles));
    }

    public void changeEmail(String emailAddress) {
        this.emailAddress = emailAddress;
        UserEmailChangedEvent emailChangedEvent = new UserEmailChangedEvent(this);
        this.registerEvent(emailChangedEvent);
    }

    /**
     * Adds a new colleague to the user. Adding the user himself as colleague is a no-op.
     *
     * @param collegue
     */
    public void addColleague(User collegue) {

        // Prevent from adding the user himself as colleague.
        if (this.equals(collegue)) {
            return;
        }

        colleagues.add(collegue);
        collegue.getColleagues().add(this);
    }

    /**
     * Removes a colleague from the list of colleagues.
     *
     * @param colleague
     */
    public void removeColleague(User colleague) {
        colleagues.remove(colleague);
        colleague.getColleagues().remove(this);
    }
}
