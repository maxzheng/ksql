/*
 * Copyright 2018 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql;

import io.confluent.ksql.KsqlExecutionContext.ExecuteResult;
import io.confluent.ksql.internal.KsqlEngineMetrics;
import io.confluent.ksql.logging.processing.ProcessingLogContext;
import io.confluent.ksql.metastore.MutableMetaStore;
import io.confluent.ksql.parser.KsqlParser.ParsedStatement;
import io.confluent.ksql.services.ServiceContext;
import io.confluent.ksql.util.KsqlConfig;
import io.confluent.ksql.util.QueryMetadata;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class KsqlEngineTestUtil {

  private KsqlEngineTestUtil() {
  }

  public static KsqlEngine createKsqlEngine(
      final ServiceContext serviceContext,
      final MutableMetaStore metaStore
  ) {
    return new KsqlEngine(
        serviceContext,
        ProcessingLogContext.create(),
        "test_instance_",
        metaStore,
        KsqlEngineMetrics::new
    );
  }

  public static KsqlEngine createKsqlEngine(
      final ServiceContext serviceContext,
      final MutableMetaStore metaStore,
      final KsqlEngineMetrics engineMetrics
  ) {
    return new KsqlEngine(
        serviceContext,
        ProcessingLogContext.create(),
        "test_instance_",
        metaStore,
        ignored -> engineMetrics
    );
  }

  public static List<QueryMetadata> execute(
      final KsqlEngine engine,
      final String sql,
      final KsqlConfig ksqlConfig,
      final Map<String, Object> overriddenProperties
  ) {
    final List<ParsedStatement> statements = engine.parse(sql);

    final KsqlExecutionContext sandbox = engine.createSandbox();
    statements.forEach(stmt -> sandbox.execute(sandbox.prepare(stmt), ksqlConfig, overriddenProperties));

    return statements.stream()
        .map(stmt -> engine.execute(engine.prepare(stmt), ksqlConfig, overriddenProperties))
        .map(ExecuteResult::getQuery)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
