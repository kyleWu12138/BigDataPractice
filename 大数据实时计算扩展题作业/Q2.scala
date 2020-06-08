import java.util.{Properties, UUID}

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessAllWindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

object Main {
  /**
   * 输入的主题名称
   */
  val inputTopic = "mn_buy_ticket_demo2"
  /**
   * kafka地址
   */
  val bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037"

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val kafkaProperties = new Properties()
    kafkaProperties.put("bootstrap.servers", bootstrapServers)
    kafkaProperties.put("group.id", UUID.randomUUID().toString)
    kafkaProperties.put("auto.offset.reset", "earliest")
    kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    val kafkaConsumer = new FlinkKafkaConsumer010[String](inputTopic,
      new SimpleStringSchema, kafkaProperties)
    kafkaConsumer.setCommitOffsetsOnCheckpoints(true)
    val inputKafkaStream = env.addSource(kafkaConsumer)
        inputKafkaStream.map(line => {
          var new_line = line.replace("\"", "")
          (new_line.substring(new_line.lastIndexOf(":") + 1, new_line.length() - 1), 1)
        })
          .keyBy(0)
          .timeWindow(Time.seconds(30))
          .sum(1)
            .print()
//    inputKafkaStream.map(line => {
//      var new_line = line.replace("\"", "")
//      (new_line.substring(new_line.lastIndexOf(":") + 1, new_line.length() - 1), 1)})
//      .keyBy(0)
//        .timeWindow(Time.seconds(30))
//        .sum(1)
//        .process(new ProcessAllWindowFunction[] {})

    env.execute()
  }
}