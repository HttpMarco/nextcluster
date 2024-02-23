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

package net.nextcluster.plugin.misc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class IngameMessages {

    private boolean minimessage = true;
    private String prefix = "<dark_gray>» <bold><gray>next<white>Cluster<reset> <dark_gray>● <reset>";
    private String help = """
        %prefix% <gray>Cluster-Commands
        <gold>/cluster <service> stop <gray>Stops a running service
        <gold>/cluster <service> execute [command]<gray>Executes a command in the service
        """.replace("%prefix%", prefix);
    private String serviceStarted = """
        %prefix% <gray>Service <gold>%service% <gray>has been <green>started
        """.replace("%prefix%", prefix);
    private String serviceStopped = """
        %prefix% <gray>Service <gold>%service% <gray>has been <red>stopped
        """.replace("%prefix%", prefix);
    private String serviceNotFound = """
        %prefix% <gray>Service <gold>%service% <gray>not found
        """.replace("%prefix%", prefix);
    private String commandExecutedOnService = """
        %prefix% <gray>Command <yellow>%command% <gray>executed on service %service%<gold>
        """.replace("%prefix%", prefix);

}
