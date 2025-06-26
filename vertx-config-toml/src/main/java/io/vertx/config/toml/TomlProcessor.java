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

package io.vertx.config.toml;

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * A processor using tomlj to read TOML files.
 *
 * @author <a href="https://azam.io">Azamshul Azizy</a>
 */
public class TomlProcessor implements ConfigProcessor {
  static final String NAME = "toml";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Future<JsonObject> process(Vertx vertx, JsonObject configuration, Buffer input) {
    if (input.length() == 0) {
      // the parser does not support empty files, which should be managed to be homogeneous
      return ((ContextInternal) vertx.getOrCreateContext()).succeededFuture(new JsonObject());
    }

    // Use executeBlocking even if the bytes are in memory
    return vertx.executeBlocking(() -> {
      try {
        final TomlParseResult toml = Toml.parse(input.toString(StandardCharsets.UTF_8));
        if (toml.hasErrors()) {
          throw new DecodeException("Failed to decode TOML: " + toml.errors());
        }
        return intoJsonObject(toml);
      } catch (ClassCastException e) {
        throw new DecodeException("Failed to decode TOML", e);
      }
    });
  }

  /**
   * TomlTable JsonObject conversion.
   *
   * @param toml TOML table
   * @return JsonObject instance or null if the input is null
   */
  private static JsonObject intoJsonObject(TomlTable toml) {
    if (toml == null) {
      return null;
    }

    final JsonObject json = new JsonObject();

    for (Map.Entry<String, Object> entry : toml.entrySet()) {
      final Object value = entry.getValue();
      if (value != null) {
        if (value instanceof TomlTable) {
          json.put(entry.getKey(), intoJsonObject((TomlTable) value));
        } else if (value instanceof TomlArray) {
          json.put(entry.getKey(), intoJsonArray((TomlArray) value));
        } else {
          json.put(entry.getKey(), intoJsonValue(value));
        }
      } else {
        json.putNull(entry.getKey());
      }
    }

    return json;
  }

  /**
   * TomlArray to JsonArray conversion.
   *
   * @param toml TOML array
   * @return JsonArray instance or null if the input is null
   */
  private static JsonArray intoJsonArray(TomlArray toml) {
    if (toml == null) {
      return null;
    }

    final JsonArray json = new JsonArray();

    for (int i = 0; i < toml.size(); i++) {
      final Object value = toml.get(i);
      if (value != null) {
        if (value instanceof TomlTable) {
          json.add(intoJsonObject((TomlTable) value));
        } else if (value instanceof TomlArray) {
          json.add(intoJsonArray((TomlArray) value));
        } else {
          json.add(intoJsonValue(value));
        }
      } else {
        json.addNull();
      }
    }

    return json;
  }

  /**
   * tomlj specific value to JsonObject supported value conversion.
   *
   * @param toml TOML object
   * @return JsonObject supported value instance or null if the input is null
   */
  private static Object intoJsonValue(Object toml) {
    if (toml == null) {
      return null;
    }

    if (toml instanceof TomlTable) {
      return intoJsonObject((TomlTable) toml);
    } else if (toml instanceof TomlArray) {
      return intoJsonArray((TomlArray) toml);
    } else if (toml instanceof OffsetDateTime) {
      // OffsetDateTime to Instant conversion is lossy (offset is lost)
      // so we convert it to ISO_DATE_TIME string format
      return ((OffsetDateTime) toml).format(DateTimeFormatter.ISO_DATE_TIME);
    } else if (toml instanceof LocalDateTime) {
      // LocalDateTime to Instant conversion is system dependent
      // so we convert it to ISO_LOCAL_DATE_TIME string format
      return ((LocalDateTime) toml).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } else if (toml instanceof LocalDate) {
      return ((LocalDate) toml).format(DateTimeFormatter.ISO_DATE);
    } else if (toml instanceof LocalTime) {
      return ((LocalTime) toml).format(DateTimeFormatter.ISO_TIME);
    }

    return toml;
  }

}
