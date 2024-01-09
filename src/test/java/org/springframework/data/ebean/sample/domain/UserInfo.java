package org.springframework.data.ebean.sample.domain;

import io.ebean.annotation.Sql;
import javax.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Xuegui Yuan
 */
@Entity
@Sql
@Getter
@Setter
public class UserInfo {

    private String firstName;
    private String lastName;
    private String emailAddress;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
