import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;


@WebSocket
public class ServerWebSocketHandler {
	
	private Session outbound;
	
	public ServerWebSocketHandler(){
		super();
		System.out.println("init: code ");
	}
	@OnWebSocketClose
	public void onClose( int statusCode, String reason){
		System.out.println("Closing: code "+statusCode);
	}
	
	@OnWebSocketError
	public void onError(Throwable t){
		System.out.println("Error: code "+t.getMessage());
	}
	
	@OnWebSocketConnect
	public void onConnect(Session session){
		this.outbound = session;
		System.out.println("Connecting to:  "+session.getRemoteAddress().getAddress());


		Timer timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {
		  @Override
		  public void run() {
			  System.out.println("Hello Again! ");
			  try {
				outbound.getRemote().sendString("Hello client! ");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		}, 5000, 5000);
	}
	
	@OnWebSocketMessage
	public void onMessage(byte[] data, int offset, int lenght){
//		ServerApp.userMap.put(key, value);
		Object receivedObject = receivedMessage(data);
        if(receivedObject instanceof ObjectWrapper){
        	String _message = (String)((ObjectWrapper)receivedObject).getKey();
        	System.out.println("New Message:  "+_message);
        }
		System.out.println("Bytes:  "+data);
	}
	
	@OnWebSocketMessage
	public void onMessage(String message){
		System.out.println("Message:  "+message);
		
	}
	
	public Object receivedMessage(byte[] byts) {
     
        ObjectInputStream istream = null;
        Object obj = null;
        try {
            istream = new ObjectInputStream(new ByteArrayInputStream(byts));
            obj = istream.readObject();
     
            if(obj instanceof ObjectWrapper){
            	String _message = (String)((ObjectWrapper)obj).getKey();
            	System.out.println("New Message:  "+_message);
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
        catch(ClassNotFoundException e){
            e.printStackTrace();
        }
		return obj;
    }
}
