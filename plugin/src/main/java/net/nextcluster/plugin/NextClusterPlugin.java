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

package net.nextcluster.plugin;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import net.nextcluster.driver.resource.config.NextConfig;
import net.nextcluster.driver.resource.config.misc.ConfigProperty;
import net.nextcluster.driver.resource.service.ServiceInformation;
import net.nextcluster.plugin.misc.IngameMessages;
import net.nextcluster.plugin.rest.RestServer;
import net.nextcluster.prevm.NextClusterLoadable;
import net.nextcluster.prevm.PreVM;

@Getter
@Accessors(fluent = true)
public abstract class NextClusterPlugin implements NextClusterLoadable {

    @Getter
    private static NextClusterPlugin instance;

    @Setter
    private String motd;
    @Setter
    private int maxPlayers;

    protected NextConfig<IngameMessages> messages;

    protected void init() {
        instance = this;

        messages = NextConfig.builder(IngameMessages.class)
            .withId("ingame-messages")
            .withProperties(ConfigProperty.OBSERVE, ConfigProperty.UPDATE_ORIGINAL)
            .withDefault(IngameMessages::new)
            .register();
        if (!messages.exists()) {
            messages.value(new IngameMessages());
        }

        //TODO remove?
        RestServer.init();
        PreVM.supplyLoadable(this);
    }

    public abstract ServiceInformation currentInformation();

    public abstract void dispatchCommand(String command);

    @SneakyThrows
    @Override
    public Class<?> classByName(String name) {
        return Class.forName(name);
    }
}
