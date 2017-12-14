/*
 * Copyright 2008-2017 the original author or authors.
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

package org.springframework.data.ebean.repository.query;

import io.ebean.EbeanServer;
import io.ebean.PagedList;
import io.ebean.Query;
import io.ebean.QueryIterator;
import io.ebean.SqlQuery;
import io.ebean.SqlUpdate;
import io.ebean.Update;
import java.util.List;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.repository.core.support.SurroundingTransactionDetectorMethodInterceptor;
import org.springframework.data.repository.query.ParameterAccessor;
import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.ParametersParameterAccessor;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.data.util.StreamUtils;
import org.springframework.util.Assert;

/**
 * Set of classes to contain query execution strategies. Depending (mostly) on the return type of a
 * {@link org.springframework.data.repository.query.QueryMethod} a {@link AbstractStringBasedEbeanQuery} can be executed
 * in various flavors.
 *
 * @author Xuegui Yuan
 */
public abstract class AbstractEbeanQueryExecution {

  /**
   * Executes the given {@link AbstractStringBasedEbeanQuery} with the given {@link ParameterBinder}.
   *
   * @param query  must not be {@literal null}.
   * @param values must not be {@literal null}.
   * @return
   */
  public Object execute(AbstractEbeanQuery query, Object[] values) {
    Assert.notNull(query, "AbstractEbeanQuery must not be null!");
    Assert.notNull(values, "Values must not be null!");

    return doExecute(query, values);
  }

  /**
   * Method to implement {@link AbstractStringBasedEbeanQuery} executions by single enum values.
   *
   * @param query
   * @param values
   * @return
   */
  protected abstract Object doExecute(AbstractEbeanQuery query, Object[] values);

  /**
   * Executes the query to return a simple collection of entities.
   */
  static class CollectionExecutionAbstract extends AbstractEbeanQueryExecution {

    @Override
    protected Object doExecute(AbstractEbeanQuery query, Object[] values) {
      Object q = query.createQuery(values);
      if (q instanceof Query) {
        Query ormQuery = (Query) q;
        return ormQuery.findList();
      } else if (q instanceof SqlQuery) {
        SqlQuery sqlQuery = (SqlQuery) q;
        return sqlQuery.findList();
      } else {
        throw new InvalidEbeanQueryMethodException("query must be Query or SqlQuery");
      }
    }
  }

  /**
   * Executes the query to return a {@link Slice} of entities.
   *
   * @author Xuegui Yuan
   */
  static class SlicedExecutionAbstract extends AbstractEbeanQueryExecution {

    private final Parameters<?, ?> parameters;

    /**
     * Creates a new {@link SlicedExecutionAbstract} using the given {@link Parameters}.
     *
     * @param parameters must not be {@literal null}.
     */
    public SlicedExecutionAbstract(Parameters<?, ?> parameters) {
      this.parameters = parameters;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ebean.repository.query.AbstractEbeanQueryExecution#doExecute(org.springframework.data.ebean.repository.query.AbstractEbeanQuery, java.lang.Object[])
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Object doExecute(AbstractEbeanQuery query, Object[] values) {

      ParametersParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
      Pageable pageable = accessor.getPageable();

      Object createQuery = query.createQuery(values);
      List<Object> resultList = null;
      int pageSize = pageable.getPageSize();
      if (createQuery instanceof Query) {
        Query ormQuery = (Query) createQuery;
        ormQuery.setMaxRows(pageSize + 1);

        ormQuery.findList();
      } else if (createQuery instanceof SqlQuery) {
        SqlQuery sqlQuery = (SqlQuery) createQuery;

        sqlQuery.setMaxRows(pageSize + 1);

        sqlQuery.findList();
      } else {
        throw new InvalidEbeanQueryMethodException("query must be Query or SqlQuery");
      }

      boolean hasNext = resultList != null && resultList.size() > pageSize;

      return new SliceImpl<Object>(hasNext ? resultList.subList(0, pageSize) : resultList, pageable, hasNext);
    }
  }

  /**
   * Executes the {@link AbstractStringBasedEbeanQuery} to return a {@link org.springframework.data.domain.Page} of
   * entities.
   */
  static class PagedExecutionAbstract extends AbstractEbeanQueryExecution {

    private final Parameters<?, ?> parameters;

    public PagedExecutionAbstract(Parameters<?, ?> parameters) {

      this.parameters = parameters;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object doExecute(final AbstractEbeanQuery repositoryQuery, final Object[] values) {
      ParameterAccessor accessor = new ParametersParameterAccessor(parameters, values);
      Object createQuery = repositoryQuery.createQuery(values);

      if (createQuery instanceof Query) {
        Query ormQuery = (Query) createQuery;
        PagedList pagedList = ormQuery.findPagedList();
        return PageableExecutionUtils.getPage(pagedList.getList(), accessor.getPageable(), () -> pagedList.getTotalCount());
      } else if (createQuery instanceof SqlQuery) {
        SqlQuery sqlQuery = (SqlQuery) createQuery;
        // TODO page
        return sqlQuery.findList();
      } else {
        throw new InvalidEbeanQueryMethodException("query must be Query or SqlQuery");
      }
    }
  }


  /**
   * Executes a {@link AbstractStringBasedEbeanQuery} to return a single entity.
   */
  static class SingleEntityExecutionAbstract extends AbstractEbeanQueryExecution {

    @Override
    protected Object doExecute(AbstractEbeanQuery query, Object[] values) {
      Object createQuery = query.createQuery(values);
      if (createQuery instanceof Query) {
        Query ormQuery = (Query) createQuery;
        return ormQuery.findOne();
      } else if (createQuery instanceof SqlQuery) {
        SqlQuery sqlQuery = (SqlQuery) createQuery;
        return sqlQuery.findOne();
      } else {
        throw new InvalidEbeanQueryMethodException("query must be Query or SqlQuery");
      }
    }
  }

  /**
   * Executes a update query such as an update, insert or delete.
   */
  static class UpdateExecutionAbstract extends AbstractEbeanQueryExecution {

    private final EbeanServer ebeanServer;

    /**
     * Creates an execution that automatically clears the given {@link EbeanServer} after execution if the given
     * {@link EbeanServer} is not {@literal null}.
     *
     * @param ebeanServer
     */
    public UpdateExecutionAbstract(EbeanQueryMethod method, EbeanServer ebeanServer) {

      Class<?> returnType = method.getReturnType();

      boolean isVoid = void.class.equals(returnType) || Void.class.equals(returnType);
      boolean isInt = int.class.equals(returnType) || Integer.class.equals(returnType);

      Assert.isTrue(isInt || isVoid, "Modifying queries can only use void or int/Integer as return type!");

      this.ebeanServer = ebeanServer;
    }

    @Override
    protected Object doExecute(AbstractEbeanQuery query, Object[] values) {
      Object createQuery = query.createQuery(values);
      if (createQuery instanceof Query) {
        Query ormQuery = (Query) createQuery;
        return ormQuery.update();
      } else if (createQuery instanceof SqlUpdate) {
        SqlUpdate sqlUpdate = (SqlUpdate) createQuery;
        return sqlUpdate.execute();
      } else if (createQuery instanceof Update) {
        Update update = (Update) createQuery;
        return update.execute();
      } else {
        throw new InvalidEbeanQueryMethodException("query not supported");
      }
    }
  }

  /**
   * {@link AbstractEbeanQueryExecution} removing entities matching the query.
   *
   * @author Xuegui Yuan
   */
  static class DeleteExecutionAbstract extends AbstractEbeanQueryExecution {

    private final EbeanServer ebeanServer;

    public DeleteExecutionAbstract(EbeanServer ebeanServer) {
      this.ebeanServer = ebeanServer;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ebean.repository.query.AbstractEbeanQueryExecution#doExecute(org.springframework.data.ebean.repository.query.AbstractEbeanQuery, java.lang.Object[])
     */
    @Override
    protected Object doExecute(AbstractEbeanQuery ebeanQuery, Object[] values) {
      Object createQuery = ebeanQuery.createQuery(values);
      if (createQuery instanceof Query) {
        Query ormQuery = (Query) createQuery;
        ormQuery.delete();

        return ormQuery.delete();
      } else {
        throw new InvalidEbeanQueryMethodException("query must be Query or SqlQuery");
      }
    }
  }

  /**
   * {@link AbstractEbeanQueryExecution} performing an exists check on the query.
   *
   * @author Xuegui Yuan
   */
  static class ExistsExecutionAbstract extends AbstractEbeanQueryExecution {

    @Override
    protected Object doExecute(AbstractEbeanQuery ebeanQuery, Object[] values) {
      Object createQuery = ebeanQuery.createQuery(values);
      if (createQuery instanceof Query) {
        Query ormQuery = (Query) createQuery;
        return ormQuery.findCount() > 0;
      } else if (createQuery instanceof SqlQuery) {
        SqlQuery sqlQuery = (SqlQuery) createQuery;
        // TODO check
        return sqlQuery.findOne().getLong("c") > 0;
      } else {
        throw new InvalidEbeanQueryMethodException("query must be Query or SqlQuery");
      }
    }
  }

  /**
   * {@link AbstractEbeanQueryExecution} executing a Java 8 Stream.
   *
   * @author Xuegui Yuan
   */
  static class StreamExecutionAbstract extends AbstractEbeanQueryExecution {

    private static final String NO_SURROUNDING_TRANSACTION = "You're trying to execute a streaming query method without a surrounding transaction that keeps the connection open so that the Stream can actually be consumed. Make sure the code consuming the stream uses @Transactional or any other way of declaring a (read-only) transaction.";

    /*
     * (non-Javadoc)
     * @see org.springframework.data.ebean.repository.query.AbstractEbeanQueryExecution#doExecute(org.springframework.data.ebean.repository.query.AbstractEbeanQuery, java.lang.Object[])
     */
    @Override
    protected Object doExecute(final AbstractEbeanQuery ebeanQuery, Object[] values) {
      if (!SurroundingTransactionDetectorMethodInterceptor.INSTANCE.isSurroundingTransactionActive()) {
        throw new InvalidDataAccessApiUsageException(NO_SURROUNDING_TRANSACTION);
      }

      Object createQuery = ebeanQuery.createQuery(values);
      if (createQuery instanceof Query) {
        Query ormQuery = (Query) createQuery;
        QueryIterator<Object> iter = ormQuery.findIterate();

        return StreamUtils.createStreamFromIterator(iter);
      } else if (createQuery instanceof SqlQuery) {
        throw new InvalidEbeanQueryMethodException("query must be Query");
      } else {
        throw new InvalidEbeanQueryMethodException("query must be Query or SqlQuery");
      }
    }
  }
}