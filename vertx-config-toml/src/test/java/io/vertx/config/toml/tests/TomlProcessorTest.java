/*
 * Copyright (c) 2025 Azamshul Azizy and others
 *
 * Azamshul Azizy licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config.toml.tests;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;
import io.vertx.core.file.FileSystemException;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="https://azam.io">Azamshul Azizy</a>
 */
@RunWith(VertxUnitRunner.class)
public class TomlProcessorTest {

  private ConfigRetriever retriever;
  private Vertx vertx;

  @Before
  public void setUp(TestContext tc) {
    vertx = Vertx.vertx();
    vertx.exceptionHandler(tc.exceptionHandler());
  }

  @After
  public void tearDown() {
    retriever.close();
    vertx.close();
  }

  @Test
  public void testEmptyToml(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("toml")
                .setConfig(new JsonObject().put("path", "src/test/resources/empty.toml"))));

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.failed()).isFalse();
      assertThat(ar.result()).isNotNull();
      assertThat(ar.result()).isEmpty();
      async.complete();
    });
  }

  @Test
  public void testPlainText(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("toml")
                .setConfig(new JsonObject().put("path", "src/test/resources/plain.txt"))));

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isFalse();
      assertThat(ar.failed()).isTrue();
      assertThat(ar.cause()).isNotNull().isInstanceOf(DecodeException.class);
      async.complete();
    });
  }

  @Test
  public void testFileNotFound(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions()
                .setType("file")
                .setFormat("toml")
                .setConfig(new JsonObject().put("path", "src/test/resources/missing-file.toml"))));

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isFalse();
      assertThat(ar.failed()).isTrue();
      assertThat(ar.cause()).isNotNull().isInstanceOf(FileSystemException.class);
      async.complete();
    });
  }

  @Test
  public void testTypesTomlFile(TestContext tc) {
    Async async = tc.async();
    retriever = ConfigRetriever.create(vertx,
      new ConfigRetrieverOptions().addStore(
        new ConfigStoreOptions()
          .setType("file")
          .setFormat("toml")
          .setConfig(new JsonObject().put("path", "src/test/resources/types.toml"))));

    retriever.getConfig().onComplete(ar -> {
      assertThat(ar.succeeded()).isTrue();
      assertThat(ar.failed()).isFalse();

      JsonObject json = ar.result();
      assertThat(json).isNotNull();

      // Top level
      assertThat(json.getString("top_string")).isEqualTo("top_string");
      assertThat(json.getJsonArray("top_string_array")).containsExactly("string0", "string1", "string2");
      assertThat(json.getInteger("top_integer")).isEqualTo(1);
      assertThat(json.getDouble("top_float")).isEqualTo(1.1);
      assertThat(json.getBoolean("top_boolean")).isTrue();

      // Temporal types
      assertThat(json.getString("top_offset_datetime1")).isEqualTo("2020-01-01T12:00:00Z");
      assertThat(json.getString("top_offset_datetime2")).isEqualTo("2020-01-01T12:00:00+13:00");
      assertThat(json.getString("top_local_datetime")).isEqualTo("2020-01-01T12:00:00");
      assertThat(json.getString("top_local_date")).isEqualTo("2020-01-01");
      assertThat(json.getString("top_local_time")).isEqualTo("12:00:00");

      // Table
      JsonObject table = json.getJsonObject("table");
      assertThat(table).isNotNull();
      assertThat(table.getString("table_string")).isEqualTo("table_string");
      assertThat(table.getJsonArray("table_string_array")).containsExactly("string0", "string1", "string2");
      assertThat(table.getInteger("table_integer")).isEqualTo(2);
      assertThat(table.getDouble("table_float")).isEqualTo(2.2);
      assertThat(table.getBoolean("table_boolean")).isTrue();

      // Sub-table
      JsonObject subtable = table.getJsonObject("subtable");
      assertThat(subtable).isNotNull();
      assertThat(subtable.getString("subtable_string")).isEqualTo("subtable_string");
      assertThat(subtable.getJsonArray("subtable_string_array")).containsExactly("string0", "string1", "string2");
      assertThat(subtable.getInteger("subtable_integer")).isEqualTo(3);
      assertThat(subtable.getDouble("subtable_float")).isEqualTo(3.3);
      assertThat(subtable.getBoolean("subtable_boolean")).isTrue();

      // Table array
      JsonArray tableArray = json.getJsonArray("table_array");
      assertThat(tableArray).isNotNull();
      assertThat(tableArray).hasSize(2);

      // First element of table array
      JsonObject tableArray0 = tableArray.getJsonObject(0);
      assertThat(tableArray0.getString("table_array_string")).isEqualTo("table_array_string0");
      assertThat(tableArray0.getJsonArray("table_array_string_array")).containsExactly("string0", "string1", "string2");
      assertThat(tableArray0.getInteger("table_array_integer")).isEqualTo(4);
      assertThat(tableArray0.getDouble("table_array_float")).isEqualTo(4.4);
      assertThat(tableArray0.getBoolean("table_array_boolean")).isTrue();

      // Second element of table array
      JsonObject tableArray1 = tableArray.getJsonObject(1);
      assertThat(tableArray1.getString("table_array_string")).isEqualTo("table_array_string1");
      assertThat(tableArray1.getJsonArray("table_array_string_array")).containsExactly("string0", "string1", "string2");
      assertThat(tableArray1.getInteger("table_array_integer")).isEqualTo(5);
      assertThat(tableArray1.getDouble("table_array_float")).isEqualTo(5.5);
      assertThat(tableArray1.getBoolean("table_array_boolean")).isTrue();

      async.complete();
    });
  }
}
