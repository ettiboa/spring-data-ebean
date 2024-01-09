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

package org.springframework.data.ebean.repository.support;

import io.ebean.*;
import io.ebean.text.PathProperties;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.ebean.repository.EbeanRepository;
import org.springframework.data.ebean.util.Converters;
import org.springframework.data.ebean.util.ExampleExpressionBuilder;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Default implementation of the {@link org.springframework.data.repository.CrudRepository} interface. This will offer
 * you a more sophisticated interface than the plain {@link io.ebean.Database} .
 *
 * @param <T>  the type of the entity to handle
 * @param <ID> the type of the entity's identifier
 * @author Xuegui Yuan
 */
@Repository
@Transactional(rollbackFor = Exception.class)
public class SimpleEbeanRepository<T, ID> implements EbeanRepository<T, ID> {

    private static final String ID_MUST_NOT_BE_NULL = "The given id must not be null!";
    private static final String PROP_MUST_NOT_BE_NULL = "The given property must not be null!";
    public static final String THE_GIVEN_ITERABLE_OF_ENTITIES_NOT_BE_NULL = "The given Iterable of entities not be null!";

    private Database ebeanServer;

    private Class<T> entityType;


    /**
     * Creates a new {@link SimpleEbeanRepository} to manage objects of the given domain type.
     *
     * @param entityType  must not be {@literal null}.
     * @param ebeanServer must not be {@literal null}.
     */
    public SimpleEbeanRepository(Class<T> entityType, Database ebeanServer) {
        this.entityType = entityType;
        this.ebeanServer = ebeanServer;
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        PagedList<T> pagedList = db().find(getEntityType())
                .setMaxRows(pageable.getPageSize())
                .setFirstRow((int) pageable.getOffset())
                .orderBy(Converters.convertToEbeanOrderBy(pageable.getSort()).toStringFormat())
                .findPagedList();
        return Converters.convertToSpringDataPage(pagedList, pageable.getSort());
    }

    @Override
    public Database db() {
        return ebeanServer;
    }

    private Class<T> getEntityType() {
        return entityType;
    }

    @Override
    public Database db(Database db) {
        this.ebeanServer = db;
        return this.ebeanServer;
    }

    @Override
    public UpdateQuery<T> updateQuery() {
        return db().update(getEntityType());
    }

    @Override
    public SqlUpdate sqlUpdateOf(String sql) {
        return db().sqlUpdate(sql);
    }

    public <S extends T> S save(S s) {
        db().save(s);
        return s;
    }

    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, THE_GIVEN_ITERABLE_OF_ENTITIES_NOT_BE_NULL);
        db().saveAll((Collection<?>) entities);
        return entities;
    }

    @Override
    public <S extends T> S update(S s) {
        db().update(s);
        return s;
    }

    @Override
    public Iterable<T> updateAll(Iterable<T> entities) {
        Assert.notNull(entities, THE_GIVEN_ITERABLE_OF_ENTITIES_NOT_BE_NULL);
        db().updateAll((Collection<?>) entities);
        return entities;
    }

    public void deleteById(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        db().delete(getEntityType(), id);
    }

    @Override
    public void deletePermanentById(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        db().deletePermanent(getEntityType(), id);
    }

    public void delete(T t) {
        db().delete(t);
    }

    @Override
    public void deleteAllById(Iterable<? extends ID> ids) {
        db().deleteAll(getEntityType(), (Collection<?>) ids);
    }

    @Override
    public void deletePermanent(T t) {
        db().deletePermanent(t);
    }

    public void deleteAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, THE_GIVEN_ITERABLE_OF_ENTITIES_NOT_BE_NULL);
        db().deleteAll((Collection<?>) entities);
    }

    @Override
    public void deletePermanentAll(Iterable<? extends T> entities) {
        Assert.notNull(entities, THE_GIVEN_ITERABLE_OF_ENTITIES_NOT_BE_NULL);
        db().deleteAllPermanent((Collection<?>) entities);
    }

    public void deleteAll() {
        query().delete();
    }

    @Override
    public void deletePermanentAll() {
        query().setIncludeSoftDeletes().delete();
    }

    public Optional<T> findById(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        return query().where().idEq(id).findOneOrEmpty();
    }

    @Override
    public Optional<T> findById(String fetchPath, ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        return query(fetchPath)
                .where()
                .idEq(id)
                .findOneOrEmpty();
    }

    @Override
    public Optional<T> findByProperty(String propertyName, Object propertyValue) {
        Assert.notNull(propertyName, PROP_MUST_NOT_BE_NULL);
        return query()
                .where()
                .eq(propertyName, propertyValue)
                .findOneOrEmpty();
    }

    @Override
    public Optional<T> findByProperty(String fetchPath, String propertyName, Object propertyValue) {
        Assert.notNull(propertyName, PROP_MUST_NOT_BE_NULL);
        return query(fetchPath)
                .where()
                .eq(propertyName, propertyValue)
                .findOneOrEmpty();
    }

    @Override
    public List<T> findAllByProperty(String propertyName, Object propertyValue) {
        return query()
                .where()
                .eq(propertyName, propertyValue)
                .findList();
    }

    @Override
    public List<T> findAllByProperty(String fetchPath, String propertyName, Object propertyValue) {
        return query(fetchPath)
                .where()
                .eq(propertyName, propertyValue)
                .findList();
    }

    @Override
    public List<T> findAllByProperty(String fetchPath, String propertyName, Object propertyValue, Sort sort) {
        return query(fetchPath, sort)
                .where()
                .eq(propertyName, propertyValue)
                .findList();
    }

    @Override
    public List<T> findAllById(Iterable<ID> ids) {
        Assert.notNull(ids, "The given Iterable of Id's must not be null!");
        return query()
                .where()
                .idIn((Collection<?>) ids)
                .findList();
    }

    @Override
    public List<T> findAll() {
        return query()
                .findList();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return query()
                .orderBy(Converters.convertToEbeanOrderBy(sort).toStringFormat())
                .findList();
    }

    @Override
    public List<T> findAll(String fetchPath) {
        return query(fetchPath)
                .findList();
    }

    @Override
    public List<T> findAll(String fetchPath, Iterable<ID> ids) {
        Assert.notNull(ids, "The given Iterable of Id's must not be null!");
        return query(fetchPath)
                .where()
                .idIn((Collection<?>) ids)
                .findList();
    }

    @Override
    public List<T> findAll(String fetchPath, Sort sort) {
        return query(fetchPath, sort)
                .findList();
    }

    @Override
    public Page<T> findAll(String fetchPath, Pageable pageable) {
        PagedList<T> pagedList = query(fetchPath)
                .setMaxRows(pageable.getPageSize())
                .setFirstRow((int) pageable.getOffset())
                .orderBy(Converters.convertToEbeanOrderBy(pageable.getSort()).toStringFormat())
                .findPagedList();
        return Converters.convertToSpringDataPage(pagedList, pageable.getSort());
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        return queryByExample(example).findList();
    }

    @Override
    public <S extends T> List<S> findAll(String fetchPath, Example<S> example) {
        return queryByExample(fetchPath, example)
                .findList();
    }

    @Override
    public <S extends T> List<S> findAll(String fetchPath, Example<S> example, Sort sort) {
        return queryByExample(fetchPath, example, sort)
                .findList();
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return queryByExample(null, example, sort)
                .findList();
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        return queryByExample(example).findOneOrEmpty();
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        PagedList<S> pagedList = queryByExample(example)
                .setMaxRows(pageable.getPageSize())
                .setFirstRow((int) pageable.getOffset())
                .orderBy(Converters.convertToEbeanOrderBy(pageable.getSort()).toStringFormat())
                .findPagedList();
        return Converters.convertToSpringDataPage(pagedList, pageable.getSort());
    }

    @Override
    public <S extends T> Page<S> findAll(String fetchPath, Example<S> example, Pageable pageable) {
        PagedList<S> pagedList = queryByExample(fetchPath, example)
                .setMaxRows(pageable.getPageSize())
                .setFirstRow((int) pageable.getOffset())
                .orderBy(Converters.convertToEbeanOrderBy(pageable.getSort()).toStringFormat())
                .findPagedList();
        return Converters.convertToSpringDataPage(pagedList, pageable.getSort());
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        return queryByExample(example).findCount();
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return queryByExample(example).findCount() > 0;
    }

    @Override
    public <S extends T, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    public boolean existsById(ID id) {
        Assert.notNull(id, ID_MUST_NOT_BE_NULL);
        return query().where().idEq(id).findCount() > 0;
    }

    public long count() {
        return query().findCount();
    }

    private Query<T> query() {
        return db().find(getEntityType());
    }

    private Query<T> query(String fetchPath) {
        Query<T> query = query();
        if (StringUtils.hasText(fetchPath)) {
            query.apply(PathProperties.parse(fetchPath));
        }
        return query;
    }

    private Query<T> query(String fetchPath, Sort sort) {
        if (sort == null) {
            return query(fetchPath);
        } else {
            return query(fetchPath).orderBy(Converters.convertToEbeanOrderBy(sort).toStringFormat());
        }
    }

    private <S extends T> Query<S> queryByExample(Example<S> example) {
        return db().find(example.getProbeType()).where(ExampleExpressionBuilder.exampleExpression(db(), example));
    }

    private <S extends T> Query<S> queryByExample(String fetchPath, Example<S> example) {
        Query<S> query = queryByExample(example);
        if (StringUtils.hasText(fetchPath)) {
            query.apply(PathProperties.parse(fetchPath));
        }
        return query;
    }

    private <S extends T> Query<S> queryByExample(String fetchPath, Example<S> example, Sort sort) {
        Query<S> query = queryByExample(fetchPath, example);
        if (sort != null) {
            query.orderBy(Converters.convertToEbeanOrderBy(sort).toStringFormat());
        }
        return query;
    }

}
