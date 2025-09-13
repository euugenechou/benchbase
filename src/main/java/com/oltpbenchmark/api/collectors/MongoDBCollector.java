/*
 * Copyright 2020 by OLTPBenchmark Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.oltpbenchmark.api.collectors;

import java.sql.*;

public final class MongoDBCollector extends DBCollector {

  public MongoDBCollector(String oriDBUrl, String username, String password) {
    try (Connection conn = DriverManager.getConnection(oriDBUrl, username, password)) {
      DatabaseMetaData metadata = conn.getMetaData();

      // Collect DBMS version from JDBC metadata
      try {
        this.version =
            metadata.getDatabaseProductName() + " " + metadata.getDatabaseProductVersion();
      } catch (SQLException e) {
        LOG.warn("Unable to collect MongoDB version: {}", e.getMessage());
      }

      // For MongoDB JDBC, we primarily rely on connection metadata
      // rather than SQL queries since MongoDB's JDBC support is limited
      try {
        dbParameters.put("database_product_name", metadata.getDatabaseProductName());
        dbParameters.put("database_product_version", metadata.getDatabaseProductVersion());
        dbParameters.put("driver_name", metadata.getDriverName());
        dbParameters.put("driver_version", metadata.getDriverVersion());
        dbParameters.put("jdbc_major_version", String.valueOf(metadata.getJDBCMajorVersion()));
        dbParameters.put("jdbc_minor_version", String.valueOf(metadata.getJDBCMinorVersion()));
        dbParameters.put("max_connections", String.valueOf(metadata.getMaxConnections()));
        dbParameters.put("supports_transactions", String.valueOf(metadata.supportsTransactions()));
      } catch (SQLException e) {
        LOG.warn("Unable to collect MongoDB metadata: {}", e.getMessage());
      }

      // MongoDB-specific metrics would typically require native driver calls
      // For now, we'll note that this is a MongoDB instance
      dbMetrics.put("database_type", "mongodb");
      dbMetrics.put("jdbc_based", "true");

    } catch (SQLException e) {
      LOG.error("Error while collecting MongoDB parameters: {}", e.getMessage());
    }
  }
}
