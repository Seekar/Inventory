import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;



@WebSocket
public class ClientWebsocketHandler {
	private final CountDownLatch closeLatch;
	  @SuppressWarnings("unused")
	    private Session session;
	  
	public ClientWebsocketHandler(){
		this.closeLatch = new CountDownLatch(1);
	
	}
	
	public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException{
		return this.closeLatch.await(duration,  unit);
	}
	
	@OnWebSocketClose
	public void onClose(int statusCode, String reason){
		System.out.println("Connection closed: "+statusCode);
		this.session = null;
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session){
		System.out.println("Got connect: %s%n"+session);
		this.session = session;
		try{
			User u = new User();
			ObjectWrapper data = new ObjectWrapper("connect", u);

//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ObjectOutputStream oos = new ObjectOutputStream(bos);
//		    oos.writeObject(data);
//		    oos.flush(); 
//		    oos.close();
//			ByteBuffer buff = getBuffer(data);
			session.getRemote().sendBytes(data.getBuffer());
			
			Future<Void> fut;
//			fut = session.getRemote().sendBytesByFuture(data)
			fut = session.getRemote().sendStringByFuture("first message sent by client");
			fut.get(2, TimeUnit.SECONDS);
			fut = session.getRemote().sendStringByFuture("second message sent by client");
			fut.get(2, TimeUnit.SECONDS);
//			session.close(StatusCode.NORMAL, "done");
		}catch(Throwable t){
			t.printStackTrace();
		}
	}
	
    @OnWebSocketMessage
    public void onMessage(String msg) {
        System.out.printf("Got msg: %s%n", msg);
    }

}
