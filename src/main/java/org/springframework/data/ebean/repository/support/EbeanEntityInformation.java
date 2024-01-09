package org.springframework.data.ebean.repository.support;

import io.ebean.Database;
import lombok.NonNull;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.core.EntityInformation;

/**
 * Extension of {@link EntityInformation} to capture information about ebean entity.
 * @author Xuegui Yuan
 */
public class EbeanEntityInformation<T, ID> implements EntityInformation<T, ID> {

    private final Database ebeanServer;

    private final @NonNull
    Class<T> domainClass;

    private Class<ID> idClass;

    public EbeanEntityInformation(Database ebeanServer, Class<T> domainClass) {
        this.ebeanServer = ebeanServer;
        this.domainClass = domainClass;

        Class<?> idClass = ResolvableType.forClass(domainClass).resolveGeneric(0);

        if (idClass == null) {
            throw new IllegalArgumentException(String.format("Could not resolve identifier type for %s!", domainClass));
        }

        this.idClass = (Class<ID>) idClass;
    }

    @Override
    public boolean isNew(T entity) {
        return ebeanServer.beanState(entity).isNew();
    }

    @Override
    public ID getId(T entity) {
        return (ID) ebeanServer.beanId(entity);
    }

    @Override
    public Class<ID> getIdType() {
        return this.idClass;
    }

    @Override
    public Class<T> getJavaType() {
        return this.domainClass;
    }
}
