package zju.homework.augmentedstudio.Utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

/**
 * Created by stardust on 2017/1/2.
 */

class ByteBufferDeserializer extends JsonDeserializer<ByteBuffer>{
    @Override
    public ByteBuffer deserialize(JsonParser jp, DeserializationContext ctx) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        double[] array = (double[]) Util.jsonToObject(node.get(0).toString(), double[].class);

        ByteBuffer bb = ByteBuffer.allocateDirect(4 * array.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (double d : array)
            bb.putFloat((float)d);
        bb.rewind();
        return bb;
    }
}

class ByteBufferSerializer extends JsonSerializer<ByteBuffer>{
    @Override
    public void serialize(ByteBuffer byteBuffer, JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
        jg.writeStartArray();
        DoubleBuffer intBuffer = byteBuffer.asDoubleBuffer();
        final double[] arr = new double[intBuffer.remaining()];
        intBuffer.get(arr);
        jg.writeArray(arr, 0, arr.length);
        jg.writeEndArray();
    }
}

@JsonSerialize(using = ByteBufferSerializer.class)
@JsonDeserialize(using = ByteBufferDeserializer.class)
public class ByteBufferMixin {
}
