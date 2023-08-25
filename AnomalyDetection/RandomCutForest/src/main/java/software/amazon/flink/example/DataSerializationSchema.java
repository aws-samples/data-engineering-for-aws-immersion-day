/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.amazon.flink.example;

import software.amazon.flink.example.model.InputData;
import software.amazon.flink.example.model.OutputData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;

public class DataSerializationSchema implements DeserializationSchema<InputData>, SerializationSchema<OutputData> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public InputData deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, InputData.class);
    }
    @Override
    public byte[] serialize(OutputData element) {
        try {
            return objectMapper.writeValueAsString(element).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize object " + element.toString(), e);
        }
    }

    @Override
    public boolean isEndOfStream(InputData nextElement) {
        return false;
    }

    @Override
    public TypeInformation<InputData> getProducedType() {
        return TypeInformation.of(InputData.class);
    }
}
