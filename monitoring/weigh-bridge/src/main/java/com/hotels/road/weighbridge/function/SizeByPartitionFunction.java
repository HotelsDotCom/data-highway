/**
 * Copyright (C) 2016-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.road.weighbridge.function;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Component;

import com.hotels.road.weighbridge.function.ReplicaByPartitionFunction.Replica;

@Component
public class SizeByPartitionFunction {
  public Map<TopicPartition, Long> apply(Map<TopicPartition, Replica> partitionsAndLogDir) {
    Map<TopicPartition, Long> result = new HashMap<>();
    for (Map.Entry<TopicPartition, Replica> entry : partitionsAndLogDir.entrySet()) {
      TopicPartition topicPartition = entry.getKey();
      Replica replica = entry.getValue();

      Path path = new File(replica.getLogDir(), topicPartition.toString()).toPath();
      try {
        long size = Files.walk(path).filter(Files::isRegularFile).mapToLong(p -> p.toFile().length()).sum();
        result.put(topicPartition, size);
      } catch (IOException e) {
        throw new UncheckedIOException("Unable to determine size of " + path, e);
      }
    }
    return result;
  }
}
