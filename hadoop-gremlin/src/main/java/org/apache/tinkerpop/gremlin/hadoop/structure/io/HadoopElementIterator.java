/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.hadoop.structure.io;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.task.JobContextImpl;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.tinkerpop.gremlin.hadoop.Constants;
import org.apache.tinkerpop.gremlin.hadoop.structure.HadoopGraph;
import org.apache.tinkerpop.gremlin.hadoop.structure.util.ConfUtil;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.io.Storage;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public abstract class HadoopElementIterator<E extends Element> implements Iterator<E>, AutoCloseable {

    protected final HadoopGraph graph;
    protected final Queue<RecordReader<NullWritable, VertexWritable>> readers = new LinkedList<>();

    public HadoopElementIterator(final HadoopGraph graph) {
        try {
            this.graph = graph;
            final Configuration configuration = ConfUtil.makeHadoopConfiguration(this.graph.configuration());
            final InputFormat<NullWritable, VertexWritable> inputFormat = ConfUtil.getReaderAsInputFormat(configuration);
            if (inputFormat instanceof FileInputFormat) {
                final Storage storage = FileSystemStorage.open(configuration);
                if (!this.graph.configuration().containsKey(Constants.GREMLIN_HADOOP_INPUT_LOCATION))
                    return; // there is no input location and thus, no data (empty graph)
                if (!Constants.getSearchGraphLocation(this.graph.configuration().getInputLocation(), storage).isPresent())
                    return; // there is no data at the input location (empty graph)
                configuration.set(Constants.MAPREDUCE_INPUT_FILEINPUTFORMAT_INPUTDIR, Constants.getSearchGraphLocation(this.graph.configuration().getInputLocation(), storage).get());
            }
            final List<InputSplit> splits = inputFormat.getSplits(new JobContextImpl(configuration, new JobID(UUID.randomUUID().toString(), 1)));
            for (final InputSplit split : splits) {
                this.readers.add(inputFormat.createRecordReader(split, new TaskAttemptContextImpl(configuration, new TaskAttemptID())));
            }
        } catch (final Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            for (final RecordReader reader : this.readers) {
                reader.close();
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
