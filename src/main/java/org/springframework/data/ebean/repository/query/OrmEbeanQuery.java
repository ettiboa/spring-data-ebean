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

package org.springframework.data.ebean.repository.query;

import io.ebean.Database;
import org.springframework.data.ebean.annotation.Query;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.expression.spel.standard.SpelExpressionParser;

/**
 * {@link RepositoryQuery} implementation that inspects a {@link org.springframework.data.repository.query.QueryMethod}
 * for the existence of an {@link Query} annotation and creates a Ebean
 * {@link io.ebean.Query} from it.
 *
 * @author Xuegui Yuan
 */
final class OrmEbeanQuery extends AbstractStringBasedEbeanQuery {

    /**
     * Creates a new {@link OrmEbeanQuery} encapsulating the query annotated on the given {@link EbeanQueryMethod}.
     *
     * @param method                    must not be {@literal null}
     * @param ebeanServer               must not be {@literal null}
     * @param evaluationContextProvider must not be {@literal null}
     * @param parser                    must not be {@literal null}
     */
    public OrmEbeanQuery(EbeanQueryMethod method, Database ebeanServer, QueryMethodEvaluationContextProvider evaluationContextProvider,
                         SpelExpressionParser parser) {
        this(method, ebeanServer, method.getAnnotatedQuery(), evaluationContextProvider, parser);
    }

    /**
     * Creates a new {@link OrmEbeanQuery} that encapsulates a simple query string.
     *
     * @param method                    must not be {@literal null}
     * @param ebeanServer               must not be {@literal null}
     * @param queryString               must not be {@literal null} or empty
     * @param evaluationContextProvider must not be {@literal null}
     * @param parser                    must not be {@literal null}
     */
    public OrmEbeanQuery(EbeanQueryMethod method, Database ebeanServer, String queryString,
                         QueryMethodEvaluationContextProvider evaluationContextProvider, SpelExpressionParser parser) {
        super(method, ebeanServer, queryString, evaluationContextProvider, parser);
    }
}
