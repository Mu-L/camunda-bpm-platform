/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class PluginsRootResourceIT extends AbstractWebIntegrationTest {

  @Parameter(0)
  public String assetName;

  @Parameter(1)
  public boolean assetAllowed;

  @Before
  public void createClient() throws Exception {
    createClient(getWebappCtxPath());
  }

  @Parameters(name = "Test instance: {index}. Asset: {0}, Allowed: {1}")
  public static Collection<Object[]> getAssets() {
    return Arrays.asList(new Object[][]{
        {"app/plugin.js", true},
        {"app/plugin.css", true},
        {"app/asset.js", false},
        {"../..", false},
        {"../../annotations-api.jar", false},
    });
  }

  @Test
  public void shouldGetAssetIfAllowed() {
    // when
    HttpResponse<String> response = Unirest.get(appBasePath + "api/admin/plugin/adminPlugins/static/" + assetName).asString();

    // then
    assertResponse(assetName, response);
  }

  protected void assertResponse(String asset, HttpResponse<String> response) {
    if (assetAllowed) {
      assertEquals(200, response.getStatus());
    } else {
      assertEquals(403, response.getStatus());
      assertTrue(response.getHeaders().getFirst("Content-Type").startsWith("application/json"));
      String responseEntity = response.getBody();
      assertTrue(responseEntity.contains("\"type\":\"RestException\""));
      assertTrue(responseEntity.contains("\"message\":\"Not allowed to load the following file '" + asset + "'.\""));
    }
  }

}
