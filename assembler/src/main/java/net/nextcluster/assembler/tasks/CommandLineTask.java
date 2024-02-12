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

package net.nextcluster.assembler.tasks;

import lombok.*;
import lombok.experimental.Accessors;

import static net.nextcluster.driver.NextCluster.LOGGER;

@Accessors(fluent = true)
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandLineTask implements Runnable {

    private final String[] command;
    private Process process;

    private CommandLineTask(String command) {
        this.command = command.split(" ", -1);
    }

    public static void run(String command) {
        new CommandLineTask(command).run();
    }

    public static void run(String... command) {
        new CommandLineTask(command).run();
    }

    @SneakyThrows
    @Override
    public void run() {
        this.process = Runtime.getRuntime().exec(this.command);

        String line;
        final var reader = this.process.inputReader();
        while ((line = reader.readLine()) != null) {
            LOGGER.info(line);
        }

        final var errorReader = this.process.errorReader();
        while ((line = errorReader.readLine()) != null) {
            LOGGER.error(line);
        }

        final int code = this.process.waitFor();
        if (code != 0) {
            throw new RuntimeException("Failed to build image. Exit code: " + code);
        }
    }
}
