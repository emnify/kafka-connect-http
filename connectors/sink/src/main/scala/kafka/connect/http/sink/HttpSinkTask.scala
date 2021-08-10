/*
 * Copyright 2020 Kafka Connect Http
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kafka.connect.http.sink

import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.connect.sink.{SinkRecord, SinkTask}
import java.util

import scala.collection.mutable
import scala.jdk.CollectionConverters._

class HttpSinkTask extends SinkTask with Logging {
  var writer: Option[HttpWriter]                = None
  var lastOffsets: mutable.Map[TopicPart, Long] = mutable.Map.empty

  override def start(props: util.Map[String, String]): Unit = {
    logger.info(s"starting http sink connector task: $props")
    writer = Some(HttpSinkConfig(props)).map(HttpWriter.make(Some(context)))
  }

  override def put(records: util.Collection[SinkRecord]): Unit = {
    logger.debug(s"received ${records.size()} records with offsets = ${records.asScala
      .map(_.kafkaOffset())
      .mkString(",")} and partitions=${records.asScala.map(_.kafkaPartition()).mkString(",")}")
    val recs = records.asScala.filter(r => r != null && r.value() != null && shouldSend(r)).toList
    if (recs.nonEmpty) {
      writer.foreach(_.put(recs))
    }
  }

  /*
   Guard to filter out records already sent.
   Kafka connect sometimes calls put for records that should have been already committed
   Committed offset is the offset for the next record
   */
  private def shouldSend(r: SinkRecord) = {
    logger.info(s"Should send offset=${r.kafkaOffset()} when lastOffsets are = ${lastOffsets.mkString(",")}")
    val lastOffset = lastOffsets.get(TopicPart(r.topic(), r.kafkaPartition()))
    lastOffset.fold(true)(offset => offset - 1 < r.kafkaOffset())
  }

  override def stop(): Unit =
    logger.info(s"stopping http sink connector task")

  override def version(): String =
    getClass.getPackage.getImplementationVersion

  // commit only if everything has been flushed correctly
  override def preCommit(currentOffsets: util.Map[TopicPartition, OffsetAndMetadata]): util.Map[TopicPartition, OffsetAndMetadata] = {
    logger.debug(s"Pre-commit called with = ${currentOffsets.asScala.mkString(",")}")
    writer.foreach(_.flush())
    lastOffsets = currentOffsets.asScala.map(e => TopicPart(e._1.topic(), e._1.partition()) -> e._2.offset())
    currentOffsets
  }

  override def flush(currentOffsets: util.Map[TopicPartition, OffsetAndMetadata]): Unit = {
    logger.trace(s"Flush called with = ${currentOffsets.asScala.mkString(",")}")
    val count = preCommit(currentOffsets)
    logger.debug(s"Flushed ${count.size()} records.")
  }
}

case class TopicPart(topic: String, partition: Int)
