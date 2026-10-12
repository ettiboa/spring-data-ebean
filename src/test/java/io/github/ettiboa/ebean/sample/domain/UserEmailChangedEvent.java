package io.github.ettiboa.ebean.sample.domain;

/**
 * @author Xuegui Yuan
 */
public class UserEmailChangedEvent extends DomainEvent {

    public UserEmailChangedEvent(Object source) {
        super(source);
    }
}
