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
import software.amazon.flink.example.operator.RandomCutForestOperator;
import com.amazonaws.services.kinesisanalytics.runtime.KinesisAnalyticsRuntime;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kinesis.sink.KinesisStreamsSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kinesis.FlinkKinesisConsumer;
import org.apache.flink.streaming.connectors.kinesis.config.AWSConfigConstants;
import org.apache.flink.streaming.connectors.kinesis.config.ConsumerConfigConstants;

import java.util.Properties;

public class FlinkStreamingJob {
    private static DataStream<InputData> createSource(
            final StreamExecutionEnvironment env,
            final String inputStreamName,
            final String region,
            final String initialPosition) {
        Properties inputProperties = new Properties();
        inputProperties.setProperty(ConsumerConfigConstants.AWS_REGION, region);
        inputProperties.setProperty(ConsumerConfigConstants.STREAM_INITIAL_POSITION, initialPosition);
        return env.addSource(new FlinkKinesisConsumer<>(inputStreamName, new DataSerializationSchema(), inputProperties));
    }

    private static KinesisStreamsSink<OutputData> createSink(final String outputStreamName, final String region) {
        Properties outputProperties = new Properties();
        outputProperties.setProperty(AWSConfigConstants.AWS_REGION, region);

        return KinesisStreamsSink.<OutputData>builder()
                .setKinesisClientProperties(outputProperties)
                .setSerializationSchema(new DataSerializationSchema())
                .setStreamName(outputProperties.getProperty("OUTPUT_STREAM", outputStreamName))
                .setPartitionKeyGenerator(element -> String.valueOf(element.hashCode()))
                .build();
    }

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties applicationProperties = KinesisAnalyticsRuntime.getApplicationProperties().get("RcfExampleEnvironment");
        String region = applicationProperties.getProperty("region", "us-west-2");
        String inputStreamName = applicationProperties.getProperty("inputStreamName", "ExampleInputStream-RCF");
        String outputStreamName = applicationProperties.getProperty("outputStreamName", "ExampleOutputStream-RCF");
        String initialPosition = applicationProperties.getProperty("initialPosition", "TRIM_HORIZON");

        DataStream<InputData> source = createSource(env, inputStreamName, region, initialPosition);

        RandomCutForestOperator<InputData, OutputData> randomCutForestOperator =
                RandomCutForestOperator.<InputData, OutputData>builder()
                        .setDimensions(1)
                        .setShingleSize(1)
                        .setSampleSize(10)
                        .setNumberOfTrees(5)
                        .setInputDataMapper((inputData) -> new double[]{inputData.getCtr()})
                        .setResultMapper(((inputData, score) -> new OutputData(inputData.getTime(), inputData.getCtr(), score)))
                        .build();

        source
                .process(randomCutForestOperator, TypeInformation.of(OutputData.class)).setParallelism(1)
                .sinkTo(createSink(outputStreamName, region));

        env.execute();
    }
}
