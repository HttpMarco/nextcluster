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

package net.nextcluster.driver.networking.packets;

import io.netty5.buffer.Buffer;
import lombok.AllArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@AllArgsConstructor
public class ByteBuffer {

    private Buffer buffer;

    public ByteBuffer writeInt(int value) {
        this.buffer.writeInt(value);
        return this;
    }

    public int readInt() {
        return this.buffer.readInt();
    }

    public ByteBuffer writeString(String value) {
        var bytes = value.getBytes();
        this.buffer.writeInt(bytes.length);
        this.buffer.writeBytes(bytes);
        return this;
    }

    public String readString() {
        return this.buffer.readCharSequence(this.buffer.readInt(), StandardCharsets.UTF_8).toString();
    }

    public ByteBuffer writeBoolean() {
        this.buffer.writeBoolean(true);
        return this;
    }

    public boolean readBoolean() {
        return this.buffer.readBoolean();
    }

    public ByteBuffer writeUUID(UUID uuid) {
        this.buffer.writeLong(uuid.getMostSignificantBits());
        this.buffer.writeLong(uuid.getLeastSignificantBits());
        return this;
    }

    public UUID readUUID() {
        return new UUID(this.buffer.readLong(), this.buffer.readLong());
    }

    public ByteBuffer writeEnum(Enum<?> value) {
        this.buffer.writeInt(value.ordinal());
        return this;
    }

    public <T extends Enum<T>> T readEnum(Class<T> clazz) {
        return clazz.getEnumConstants()[this.buffer.readInt()];
    }


    public ByteBuffer writeLong(long value) {
        this.buffer.writeLong(value);
        return this;
    }

    public long readLong() {
        return this.buffer.readLong();
    }

    public ByteBuffer writeFloat(float value) {
        this.buffer.writeFloat(value);
        return this;
    }

    public float readFloat() {
        return this.buffer.readFloat();
    }

    public ByteBuffer writeDouble(double value) {
        this.buffer.writeDouble(value);
        return this;
    }

    public double readDouble() {
        return this.buffer.readDouble();
    }
}
