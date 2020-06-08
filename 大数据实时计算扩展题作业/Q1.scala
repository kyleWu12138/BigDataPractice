import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object Main {
  val target = 'b'

  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //Linux or Mac:nc -l 9999
    //Windows:nc -l -p 9999
    val text = env.socketTextStream("localhost", 9999)
    //    val stream = text.flatMap {
    //      _.toLowerCase.split("\\W+").filter {
    //        _.contains(target)
    //      }
    //    }.map {
    //      ("发现目标："+_)
    //    }
    val stream = text.flatMap(line => line.split(""))
      .filter(line => line.contains(target))
      .map(c => WordCount(c, 1))
      .keyBy(0)
      .timeWindow(Time.seconds(60))
      .sum(1)
      .print()

    env.execute("Window Stream WordCount")
  }

  case class WordCount(word: String, cnt: Long)

}


import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.scala._
//object SocketWindowWordCount {

//  def main(args: Array[String]) : Unit = {
//
//    // the port to connect to
//     val port: Int = try {
//       ParameterTool.fromArgs(args).getInt("port")
//     } catch {
//       case e: Exception => {
//         System.err.println("No port specified. Please run 'SocketWindowWordCount --port <port>'")
//         return
//       }
//     }

// get the execution environment
//    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
//
//    // get input data by connecting to the socket
//    val text = env.socketTextStream("192.168.52.130", 9999, '\n')
//
//    // parse the data, group it, window it, and aggregate the counts
//    val windowCounts = text
//      .flatMap { w => w.split("\\s") }
//      .map { w => WordWithCount(w, 1) }
//      .keyBy("word")
////      .timeWindow(Time.seconds(5), Time.seconds(1))
//      .sum("count")
//
//    // print the results with a single thread, rather than in parallel
//    windowCounts.print().setParallelism(1)
//    println(windowCounts)
//    env.execute("Socket Window WordCount")
//  }
//
//  // Data type for words with count
//  case class WordWithCount(word: String, count: Long)
//
//}