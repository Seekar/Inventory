package com.inventoryApp;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;



public class ServerApp {
	static final LinkedHashMap<Integer, ServerWebSocketHandler> userMap = new LinkedHashMap<Integer, ServerWebSocketHandler>();
	public static void main(String[] args) throws Exception {

		Server server = new Server(8080);	
		
		ContextHandler contextHandler = new ContextHandler();
		contextHandler.setContextPath("/");
		contextHandler.setHandler(new WebSocketHandler() {
			
		  @Override
		  public void configure(WebSocketServletFactory factory) {
		  	factory.getPolicy().setIdleTimeout(Integer.MAX_VALUE);
		    factory.setCreator(new WebSocketCreator() {
		      public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
		        String query = req.getRequestURI().toString();
		        if ((query == null) || (query.length() <= 0)) {
		          try {
		            resp.sendForbidden("Unspecified query");
		          } catch (IOException e) {
		        	System.out.println("stopped");
		          }
		          System.out.println("Unable to create websocket connection");
		          return null;
		        }
		        return new ServerWebSocketHandler();
		      }

		    });
		  }
		});	
		
		
		
//		AbstractHandler abstractHandler = new AbstractHandler(){
//
//		    public void handle(String target,
//                    Request baseRequest,
//                    HttpServletRequest request,
//                    HttpServletResponse response) 
//     throws IOException, ServletException
// {
// 	 System.out.println("received");
//     response.setContentType("text/html;charset=utf-8");
//     response.setStatus(HttpServletResponse.SC_OK);
//     baseRequest.setHandled(true);
//     response.getWriter().println("<h1>Hello World</h1>");
// }
//
//
//			
//		};
		
		
		
	
		
	
//		WebSocketHandler wsHandler = new WebSocketHandler(){
//			@Override
//			public void configure(WebSocketServletFactory factory){
//				factory.register(MyWebSocketHandler.class);
//			}
//		};

//		
////		@SuppressWarnings("serial")
////
////		class MyEchoServlet extends WebSocketServlet {
////		 
////		    @Override
////		    public void configure(WebSocketServletFactory factory) {
////		        factory.getPolicy().setIdleTimeout(10000);
////		        factory.register(MyWebSocketHandler.class);
////		    }
////		}
////		MyEchoServlet wsHandler = new MyEchoServlet();
//		

//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
//        context.setContextPath("/");
//        server.setHandler(context);
//        
//		ServerContainer wscontainer = WebSocketServerContainerInitializer.configureContext(context);
//		wscontainer.addEndpoint(MyWebSocketHandler.class);
		
		
		server.setHandler(contextHandler);
		server.setStopTimeout(86400000);
		server.start();
		server.join();
		
	}
	static boolean sendToUser(int key, String message, Object object){
		if(userMap.containsKey(key)){
			ServerWebSocketHandler memberSocket = userMap.get(key);
			if(memberSocket!=null){
				memberSocket.send(message, object);
				return true;
			}
		}
		return false;
	}
	static boolean sendToUser(int key, String message){
		if(userMap.containsKey(key)){
			ServerWebSocketHandler memberSocket = userMap.get(key);
			if(memberSocket!=null){
				memberSocket.send(message);
				return true;
			}
		}
		return false;
	}
}
