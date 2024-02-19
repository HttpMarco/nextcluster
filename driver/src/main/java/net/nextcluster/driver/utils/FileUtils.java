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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FileUtils {

    @SneakyThrows
    private static void copyInternal(String internal, String destination, boolean override) {
        try (InputStream stream = ClassLoader.getSystemClassLoader().getResourceAsStream(internal)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + internal);
            }
            final var file = new File(internal);
            final Path path = Path.of(destination);
            if (file.isDirectory()) {
                for (File listFile : Objects.requireNonNull(file.listFiles())) {
                    if (!override && Files.exists(path)) {
                        return;
                    }
                    Files.copy(listFile.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                if (!override && Files.exists(path)) {
                    return;
                }
                Files.copy(stream, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    @SneakyThrows
    public static void copyInternal(String internal, String destination) {
        copyInternal(internal, destination, false);
    }

    @SneakyThrows
    public static void copyInternalOverride(String internal, String destination) {
        copyInternal(internal, destination, true);
    }

}
