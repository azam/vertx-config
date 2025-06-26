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
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="https://azam.io">Azamshul Azizy</a>
 */
@RunWith(VertxUnitRunner.class)
public class TomlMultipleVerticlesTest {
  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown() {
    vertx.close();
  }

  private static class ConfigYamlVerticle extends VerticleBase {
    private ConfigRetriever retriever;

    @Override
    public Future<?> start() throws Exception {
      ConfigStoreOptions store = new ConfigStoreOptions()
        .setType("file")
        .setFormat("toml")
        .setConfig(new JsonObject()
          .put("path", "src/test/resources/simple.toml")
        );
      retriever = ConfigRetriever.create(vertx,
        new ConfigRetrieverOptions().addStore(store));
      return retriever.getConfig()
        .expecting(json -> "value".equals(json.getString("key")));
    }

    @Override
    public Future<?> stop() {
      return retriever.close();
    }
  }

  @Test
  public void testReadYamlConcurrent(TestContext testContext) {
    int instances = 4;
    vertx.deployVerticle(ConfigYamlVerticle::new, new DeploymentOptions().setInstances(instances))
      .onComplete(testContext.asyncAssertSuccess(va -> vertx.undeploy(va).onComplete(testContext.asyncAssertSuccess())));
  }

}
