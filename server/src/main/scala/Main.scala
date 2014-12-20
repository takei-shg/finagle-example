import com.twitter.finagle._
import com.twitter.finagle.dispatch.SerialServerDispatcher
import com.twitter.finagle.netty3.Netty3Listener
import com.twitter.finagle.server.{Listener, StdStackServer, StackServer}
import com.twitter.finagle.transport.Transport
import com.twitter.util._
import org.jboss.netty.channel.{ChannelPipelineFactory, Channels}
import org.jboss.netty.handler.codec.frame.{DelimiterBasedFrameDecoder, Delimiters}
import org.jboss.netty.handler.codec.string.{StringDecoder, StringEncoder}
import org.jboss.netty.util.CharsetUtil

object Main extends App {

  val service = new Service[String, String] {
    def apply(request: String) = Future.value(request)
  }
  val server = SimpleEchoServer.serve(":8080", service)
  Await.result(server)
}

object SimpleEchoServer {

  case class Server(
    stack: Stack[ServiceFactory[String, String]] = StackServer.newStack,
    params: Stack.Params = StackServer.defaultParams
  ) extends StdStackServer[String, String, Server] {
    protected type In = String
    protected type Out = String

    protected def copy1(
      stack: Stack[ServiceFactory[String, String]] = this.stack,
      params: Stack.Params = this.params
    ): Server = copy(stack, params)

    protected def newListener(): Listener[String, String] =
      Netty3Listener(StringServerPipeline, params)

    protected def newDispatcher(
      transport: Transport[String, String],
      service: Service[String, String]
    ) = new SerialServerDispatcher(transport, service)
  }

  val server = new Server()

  def serve(port: String, service: Service[String, String]): ListeningServer =
    new Server().serve(port, service)
}

object StringServerPipeline extends ChannelPipelineFactory {
  def getPipeline = {
    val pipeline = Channels.pipeline()
    pipeline.addLast("line", new DelimiterBasedFrameDecoder(100, Delimiters.lineDelimiter: _*))
    pipeline.addLast("stringDecoder", new StringDecoder(CharsetUtil.UTF_8))
    pipeline.addLast("stringEncoder", new StringEncoder(CharsetUtil.UTF_8))
    pipeline
  }
}
