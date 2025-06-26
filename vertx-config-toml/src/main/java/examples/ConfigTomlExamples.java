/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package examples;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigStoreOptions;

/**
 * @author <a href="https://azam.io">Azamshul Azizy</a>
 */
public class ConfigTomlExamples {


  public void example1(Vertx vertx) {
    ConfigStoreOptions store = new ConfigStoreOptions()
      .setType("file")
      .setFormat("toml")
      .setConfig(new JsonObject()
        .put("path", "my-config.toml")
      );

    ConfigRetriever retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(store));
  }
}
