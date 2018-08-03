/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intel.hibench.common.streaming.metrics

import java.util.Properties
import java.util.concurrent.Callable

import com.codahale.metrics.Histogram
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.TopicPartition

import scala.collection.JavaConverters._

class FetchJob(zkConnect: String, topic: String, partition: Int, startingOffsetForEachPartition: Int,
               recNum: Long, histogram: Histogram) extends Callable[FetchJobResult] {

  override def call(): FetchJobResult = {
    val result = new FetchJobResult()
    val consumer = new KafkaConsumer[String, String](consumerProperties)


    // set starting offset for each partition
    if (startingOffsetForEachPartition >= 0) {
      val topicPartition = new TopicPartition(topic, partition)
      consumer.assign(Seq(topicPartition).asJava)
      consumer.seek(topicPartition, startingOffsetForEachPartition)
    } else {
      consumer.subscribe(List(topic).asJava)
    }

    var lastSuccessfulFetch = System.currentTimeMillis()
    var totalRecs = 0
    while (((totalRecs < recNum) && recNum > 0 || recNum < 0)  && System.currentTimeMillis() - lastSuccessfulFetch < 30000) {

      val records = consumer.poll(1000).asScala.filter(_.offset != 0)
      for (record: ConsumerRecord[String, String] <- records) {

        if (totalRecs < recNum){
        val times = record.value().split(":")
        val startTime = times(0).toLong
        val endTime = times(1).toLong
        // correct negative value which might be caused by difference of system time
        histogram.update(Math.max(0, endTime - startTime))
        result.update(startTime, endTime)
        }

        totalRecs += 1
      }

      if (records.nonEmpty) {
        lastSuccessfulFetch = System.currentTimeMillis()
      }

    }

    println(s"Collected ${result.count} results ...")
    result
  }

  private def consumerProperties: Properties = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("group.id", "test")
    props.put("enable.auto.commit", "false")
    props.put("auto.commit.interval.ms", "1000")
    props.put("session.timeout.ms", "30000")
    props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    props.put(ConsumerConfig.STREAMS_ZEROOFFSET_RECORD_ON_EOF_CONFIG, "true")
    props.put("auto.offset.reset", "earliest")

    props
  }
}

class FetchJobResult(var minTime: Long, var maxTime: Long, var count: Long) {

  def this() = this(Long.MaxValue, Long.MinValue, 0)

  def update(startTime: Long, endTime: Long): Unit = {
    count += 1

    if (startTime < minTime) {
      minTime = startTime
    }

    if (endTime > maxTime) {
      maxTime = endTime
    }
  }
}
