/*
 * MIT License
 *
 * Copyright (c) 2024 nextCluster
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.nextcluster.plugin.rest;

import dev.httpmarco.osgon.files.configuration.gson.JsonUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.nextcluster.driver.NextCluster;
import net.nextcluster.plugin.NextClusterPlugin;
import spark.Spark;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RestServer {

    private static final Integer REST_PORT = Integer.parseInt(System.getProperty("rest.port", "8080"));

    public static void init() {
        NextCluster.LOGGER.info("Starting REST-Server on port {}...", REST_PORT);

        Spark.port(REST_PORT);
        Spark.get(
            "/information",
            (request, response) -> JsonUtils.toPrettyJson(NextClusterPlugin.instance().currentInformation())
        );
        Spark.post("/execute", (request, response) -> {
            NextCluster.LOGGER.info("Execute command: '{}'...", request.body());
            NextClusterPlugin.instance().dispatchCommand(request.body());
            response.status(200);
            return true;
        });
    }

}
