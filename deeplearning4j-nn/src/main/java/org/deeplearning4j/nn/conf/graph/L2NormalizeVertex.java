/*-
 *
 *  * Copyright 2016 Skymind,Inc.
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *        http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package org.deeplearning4j.nn.conf.graph;

import lombok.Data;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.inputs.InvalidInputTypeException;
import org.deeplearning4j.nn.conf.memory.LayerMemoryReport;
import org.deeplearning4j.nn.conf.memory.MemoryReport;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.optimize.api.IterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.shade.jackson.annotation.JsonProperty;

import java.util.Collection;

/**
 * L2NormalizeVertex performs L2 normalization on a single input.
 *
 * Can be configured to normalize a single dimension, or normalize across
 * all dimensions except zero by leaving dimension blank or setting it to -1.
 *
 * @author Justin Long (crockpotveggies)
 * @author Alex Black (AlexDBlack)
 */
@Data
public class L2NormalizeVertex extends GraphVertex {
    public static final double DEFAULT_EPS = 1e-8;

    protected int[] dimension;
    protected double eps;

    public L2NormalizeVertex() {
        this(null, DEFAULT_EPS);
    }

    public L2NormalizeVertex(@JsonProperty("dimension") int[] dimension, @JsonProperty("eps") double eps) {
        this.dimension = dimension;
        this.eps = eps;
    }



    @Override
    public L2NormalizeVertex clone() {
        return new L2NormalizeVertex(dimension, eps);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof L2NormalizeVertex))
            return false;
        return ((L2NormalizeVertex) o).dimension == dimension;
    }

    @Override
    public int hashCode() {
        return 123081189;
    }

    @Override
    public int numParams(boolean backprop) {
        return 0;
    }

    @Override
    public int minVertexInputs() {
        return 1;
    }

    @Override
    public int maxVertexInputs() {
        return 1;
    }

    @Override
    public Layer instantiate(NeuralNetConfiguration conf,
                             Collection<IterationListener> iterationListeners,
                             String name, int idx, int numInputs, INDArray layerParamsView,
                             boolean initializeParams) {

        return new org.deeplearning4j.nn.graph.vertex.impl.L2NormalizeVertex(name, idx, numInputs, dimension, eps);
    }

    @Override
    public InputType[] getOutputType(int layerIndex, InputType... vertexInputs) throws InvalidInputTypeException {
        if (vertexInputs.length == 1)
            return vertexInputs;
        InputType first = vertexInputs[0];

        return new InputType[]{first}; //Same output shape/size as
    }

    @Override
    public MemoryReport getMemoryReport(InputType... inputTypes) {
        InputType outputType = getOutputType(-1, inputTypes)[0];
        //norm2 value (inference working mem): 1 per example during forward pass

        //Training working mem: 2 per example + 2x input size + 1 per example (in addition to epsilons)
        int trainModePerEx = 3 + 2 * inputTypes[0].arrayElementsPerExample();

        return new LayerMemoryReport.Builder(null, L2NormalizeVertex.class, inputTypes[0], outputType)
                        .standardMemory(0, 0) //No params
                        .workingMemory(0, 1, 0, trainModePerEx).cacheMemory(0, 0) //No caching
                        .build();
    }
}