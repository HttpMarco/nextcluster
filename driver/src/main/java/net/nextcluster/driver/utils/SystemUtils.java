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

package net.nextcluster.driver.utils;

import com.sun.management.OperatingSystemMXBean;
import lombok.experimental.UtilityClass;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;

@UtilityClass
public final class SystemUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00");

    public static double cpuUsage() {
        final var factory = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return Double.parseDouble(DECIMAL_FORMAT.format(MathUtils.percentage(factory.getCpuLoad())));
    }

    public static long memoryUsage() {
        final var runtime = Runtime.getRuntime();
        final var maxMemory = runtime.maxMemory() / (1024 ^ 2);
        final var freeMemory = runtime.freeMemory() / (1024 ^ 2);
        return (maxMemory) - freeMemory;
    }
}
