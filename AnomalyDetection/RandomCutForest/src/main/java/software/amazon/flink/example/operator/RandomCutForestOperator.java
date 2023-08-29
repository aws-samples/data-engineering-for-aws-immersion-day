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

package software.amazon.flink.example.operator;

import com.amazon.randomcutforest.RandomCutForest;
import com.amazon.randomcutforest.state.RandomCutForestMapper;
import com.amazon.randomcutforest.state.RandomCutForestState;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.Preconditions;

import java.util.Collections;
import java.util.Iterator;

public class RandomCutForestOperator<T, R> extends ProcessFunction<T, R> implements CheckpointedFunction {
    public static final InputDataMapper<Float> SIMPLE_FLOAT_INPUT_DATA_MAPPER = (input) -> new double[]{input};
    public static final ResultMapper<Float, Tuple2<Float, Double>> SIMPLE_TUPLE_RESULT_DATA_MAPPER = Tuple2::of;

    private transient RandomCutForest rcf;
    private transient ListState<RandomCutForestState> rcfState;

    private static final ListStateDescriptor<RandomCutForestState> rcfStateDescriptor =
            new ListStateDescriptor<>("random-cut-forest.state", RandomCutForestState.class);
    private static final RandomCutForestMapper rcfMapper = new RandomCutForestMapper();

    static {
        rcfMapper.setSaveExecutorContextEnabled(true);
    }

    private final int dimensions;
    private final int shingleSize;
    private final int numberOfTrees;
    private final int sampleSize;
    private final int reportAnomaliesAfter;
    private final InputDataMapper<T> inputDataMapper;
    private final ResultMapper<T, R> resultMapper;

    RandomCutForestOperator(
            int dimensions,
            int shingleSize,
            int numberOfTrees,
            int sampleSize,
            int reportAnomaliesAfter,
            InputDataMapper<T> inputDataMapper,
            ResultMapper<T, R> resultMapper) {
        this.dimensions = dimensions;
        this.shingleSize = shingleSize;
        this.numberOfTrees = numberOfTrees;
        this.sampleSize = sampleSize;
        this.reportAnomaliesAfter = reportAnomaliesAfter;
        this.inputDataMapper = inputDataMapper;
        this.resultMapper = resultMapper;
    }

    @Override
    public void processElement(T value, ProcessFunction<T, R>.Context ctx, Collector<R> out) {
        double[] inputData = inputDataMapper.apply(value);
        Preconditions.checkArgument(inputData.length == dimensions);

        double score = rcf.getAnomalyScore(inputData);
        rcf.update(inputData);

        out.collect(resultMapper.apply(value, score));
    }

    private RandomCutForest initializeRcf() {
        return RandomCutForest.builder()
                .dimensions(dimensions * shingleSize)
                .shingleSize(shingleSize)
                .internalShinglingEnabled(true)
                .numberOfTrees(numberOfTrees)
                .sampleSize(sampleSize)
                .outputAfter(reportAnomaliesAfter)
                .build();
    }

    @Override
    public void snapshotState(FunctionSnapshotContext context) throws Exception {
        RandomCutForestState randomCutForestState = rcfMapper.toState(rcf);
        rcfState.update(Collections.singletonList(randomCutForestState));
    }

    @Override
    public void initializeState(FunctionInitializationContext context) throws Exception {
        rcfState = context.getOperatorStateStore().getListState(rcfStateDescriptor);
        Iterator<RandomCutForestState> rcfStateIterator = rcfState.get().iterator();
        if (rcfStateIterator.hasNext()) {
            rcf = rcfMapper.toModel(rcfStateIterator.next());
        } else {
            rcf = initializeRcf();
        }
    }

    public static <T, R> RandomCutForestOperatorBuilder<T, R> builder() {
        return new RandomCutForestOperatorBuilder<>();
    }
}
