package com.fingolfintek.lmdb;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import javaslang.control.Try;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class KryoLMDBCodec implements LMDBCodec {

    private final Kryo kryo = new Kryo();

    @Override
    public byte[] marshal(Object value) {
        return Try.of(() -> doMarshal(value)).get();
    }

    private byte[] doMarshal(Object value) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
             Output output = new Output(stream)) {
            kryo.writeClassAndObject(output, value);
            output.flush();
            return stream.toByteArray();
        }
    }

    @Override
    public Object unmarshal(byte[] dbValue) {
        return Optional.ofNullable(dbValue)
                .map(this::doUnmarshal).orElse(null);
    }

    private Object doUnmarshal(byte[] dbValue) {
        try (Input input = new Input(dbValue)) {
            return kryo.readClassAndObject(input);
        }
    }

}
