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
import org.apache.flink.util.Preconditions;

import java.util.Optional;

public class RandomCutForestOperatorBuilder<T, R> {
    private static final int DEFAULT_DIMENSIONS = 1;
    private static final int DEFAULT_SHINGLES = 1;
    private static final int DEFAULT_NUMBER_OF_TREES = RandomCutForest.DEFAULT_NUMBER_OF_TREES;
    private static final int DEFAULT_SAMPLE_SIZE = RandomCutForest.DEFAULT_SAMPLE_SIZE;

    private Integer dimensions;
    private Integer shingleSize;
    private Integer numberOfTrees;
    private Integer sampleSize;
    private Integer reportAnomaliesAfter;

    private InputDataMapper<T> inputDataMapper;
    private ResultMapper<T, R> resultMapper;

    RandomCutForestOperatorBuilder() {}

    public RandomCutForestOperatorBuilder<T, R> setDimensions(int dimensions) {
        Preconditions.checkArgument(dimensions > 0, "Number of dimensions must be positive.");
        this.dimensions = dimensions;
        return this;
    }

    public RandomCutForestOperatorBuilder<T, R> setShingleSize(Integer shingleSize) {
        Preconditions.checkArgument(dimensions > 0, "Shingle size must be positive.");
        this.shingleSize = shingleSize;
        return this;
    }

    public RandomCutForestOperatorBuilder<T, R> setReportAnomaliesAfter(Integer reportAnomaliesAfter) {
        this.reportAnomaliesAfter = reportAnomaliesAfter;
        return this;
    }

    public RandomCutForestOperatorBuilder<T, R> setNumberOfTrees(Integer numberOfTrees) {
        Preconditions.checkArgument(numberOfTrees > 0, "Number of trees must be positive.");
        this.numberOfTrees = numberOfTrees;
        return this;
    }

    public RandomCutForestOperatorBuilder<T, R> setSampleSize(Integer sampleSize) {
        Preconditions.checkArgument(sampleSize > 0, "Sample size must be positive.");
        this.sampleSize = sampleSize;
        return this;
    }

    public RandomCutForestOperatorBuilder<T, R> setInputDataMapper(InputDataMapper<T> inputDataMapper) {
        this.inputDataMapper = inputDataMapper;
        return this;
    }

    public RandomCutForestOperatorBuilder<T, R> setResultMapper(ResultMapper<T, R> resultMapper) {
        this.resultMapper = resultMapper;
        return this;
    }

    public RandomCutForestOperator<T, R> build() {
        int dimensionsValue = Optional.ofNullable(dimensions).orElse(DEFAULT_DIMENSIONS);
        int shinglesValue = Optional.ofNullable(shingleSize).orElse(DEFAULT_SHINGLES);
        int sampleSizeValue = Optional.ofNullable(sampleSize).orElse(DEFAULT_SAMPLE_SIZE);
        return new RandomCutForestOperator<>(
                dimensionsValue,
                shinglesValue,
                Optional.ofNullable(numberOfTrees).orElse(DEFAULT_NUMBER_OF_TREES),
                sampleSizeValue,
                Optional.ofNullable(reportAnomaliesAfter).orElse(sampleSizeValue),
                Optional.ofNullable(inputDataMapper)
                        .orElseThrow(() -> new IllegalArgumentException("inputDataMapped must be set")),
                Optional.ofNullable(resultMapper)
                        .orElseThrow(() -> new IllegalArgumentException("resultMapper must be set"))
        );
    }
}
