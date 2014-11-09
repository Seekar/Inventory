import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class ServerWebSocketHandler extends DAL{
	
	private Session session;
	private User user;
	private HashMap<String, Command> handlerMethods = new HashMap<String, Command>();
	
	public ServerWebSocketHandler(){
		super();
		handlerMethods.put("login", new CommandAdapter(){

			@Override
			public void runMethod(Object o) {
				login(o);
			}
		});

		System.out.println("init: code ");
	}
	@OnWebSocketClose
	public void onClose( int statusCode, String reason){
		System.out.println("Closing: code "+statusCode);
		ServerApp.userMap.remove(this.user.getUser_Id());
	}
	
	@OnWebSocketError
	public void onError(Throwable t){
		System.out.println("Error: code "+t.getMessage());
	}
	
	@OnWebSocketConnect
	public void onConnect(Session s){
		this.session = s;
		System.out.println("Connecting to:  "+this.session.getRemoteAddress().getAddress());


//		Timer timer = new Timer();
//
//		timer.scheduleAtFixedRate(new TimerTask() {
//		  @Override
//		  public void run() {
//			  System.out.println("Hello Again! ");
//			  send("Hello client! ");
//		  }
//		}, 5000, 5000);
	}
	
	@OnWebSocketMessage
	public void onMessage(byte[] data, int offset, int lenght){

		Object receivedObject = unwrapReceivedMessage(data);
	  if(receivedObject instanceof ObjectWrapper){
	  	ObjectWrapper wrappedObject = (ObjectWrapper)receivedObject;
	  	String _message = (String)(wrappedObject.getKey());
	  	Object unwrappedObject = wrappedObject.getObj();
	  	System.out.println("Server Receved New Message:  "+_message);
	  	System.out.println("bytes:  "+data);
	  	System.out.println("offset:  "+offset);
	  	System.out.println("lenght:  "+lenght);
	  	System.out.println("message:  "+lenght);
	  	this.handlerMethods.getOrDefault(_message, new CommandAdapter(){
		  	@Override public void runMethod(){
		  		System.out.println("refusing:  ");
		  		refuseConnection();
		  	}
			}).runMethod(unwrappedObject);
	  }
	  else{
	  	refuseConnection();
	  }
		System.out.println("Bytes:  "+data);
	}
	
	@OnWebSocketMessage
	public void onMessage(String message){
		System.out.println("Message:  "+message);
		
	}
	
	public Object unwrapReceivedMessage(byte[] byts) {
     
        ObjectInputStream istream = null;
        Object obj = null;
        try {
            istream = new ObjectInputStream(new ByteArrayInputStream(byts));
            obj = istream.readObject();
     
            if(obj instanceof ObjectWrapper){
            	String _message = (String)((ObjectWrapper)obj).getKey();
            	System.out.println("Server Receved New Message:  "+_message);
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
	
	private void login(Object receivedObject){
		if( receivedObject instanceof User){
			this.user = (User)receivedObject;
			
//			String q = " SELECT "+DbMap.User.user_id+","+DbMap.User.first_name+","+
//					DbMap.User.last_name+","+DbMap.User.email+","+DbMap.User.password+","+DbMap.User.created_at+","+DbMap.User.updated_at+","+DbMap.User.status_id+","+
//					DbMap.Store_member.user_id+","+DbMap.Store_member.store_id+","+DbMap.Store_member.type_id+","+DbMap.Store_member.status_id+","+DbMap.Store.store_id+","+
//					DbMap.Store.name+","+ DbMap.Store.street_address+","+DbMap.Store.city+","+DbMap.Store.state+","+DbMap.Store.zip_code+","+DbMap.Store.phone_number+","+
//					DbMap.Store.status_id+" FROM "+DbMap.User.user_table+" INNER JOIN "+DbMap.Store_member.store_member_table+" ON "+DbMap.User.user_id+"="+DbMap.Store_member.user_id+
//					" INNER JOIN "+DbMap.Store.store_table+" ON "+DbMap.Store.store_id+"="+DbMap.Store_member.store_id+" WHERE "+DbMap.User.email+"=? AND "+DbMap.User.password+"=?";
			String qry = 
					select(new String[]{
							DbMap.User.user_id,DbMap.User.first_name,DbMap.User.last_name,DbMap.User.email,DbMap.User.password,DbMap.User.created_at,DbMap.User.updated_at,
							DbMap.User.status_id, DbMap.Store_member.user_id, DbMap.Store_member.store_id, DbMap.Store_member.type_id, DbMap.Store_member.status_id, DbMap.Store.store_id, 
							DbMap.Store.name,DbMap.Store.street_address,DbMap.Store.city,DbMap.Store.state,DbMap.Store.zip_code,DbMap.Store.phone_number,DbMap.Store.status_id
							})+
					from(
							DbMap.User.user_table
							)+
					innerJoin(
							DbMap.Store_member.store_member_table
							)+
					on(
							DbMap.User.user_id, DbMap.Store_member.user_id
							)+
					innerJoin(
							DbMap.Store.store_table
							)+
					on(
							DbMap.Store.store_id,DbMap.Store_member.store_id
							)+
					where(new String[]{
							DbMap.User.email,DbMap.User.password
							});
			String[] parameters = new String[]{user.getUser_email(),user.getUser_password()};
			ArrayList<LinkedHashMap<String, String>> qryResults = this.getQryResults(qry, parameters);
			qryResults.get(0);
System.out.println("qry results "+ qryResults.get(0));
			this.user = new User(qryResults.get(0));
			
			if(this.user.getUser_Id()!=0){
				ServerApp.userMap.put(this.user.getUser_Id(), this.session);
				this.send("login", this.user);
				//return user products, orders
				Store_Member storeMember = new Store_Member(qryResults.get(0));
				
				if(storeMember.getStore_id()!=0){
					Store store = new Store(qryResults.get(0));
					this.send("store", store);
//					String q = "SELECT "+DbMap.Store_product.store_id+","+DbMap.Store_product.product_upc+","+DbMap.Store_product.quantity+","+DbMap.Store_product.price+","+
//							DbMap.Store_product.min_quantity+","+DbMap.Store_product.status_id+","+DbMap.Product.upc+","+DbMap.Product.name+","+DbMap.Product.description+
//							" FROM "+DbMap.Store_product.store_product_table+" INNER JOIN "+DbMap.Product.product_table+" ON "+DbMap.Product.upc+"="+DbMap.Store_product.product_upc+
//							" WHERE "+DbMap.Store_product.store_id+"=?";
					
					qry = 
							select(new String[]{
									DbMap.Store_product.store_id,DbMap.Store_product.product_upc,DbMap.Store_product.quantity,DbMap.Store_product.price,DbMap.Store_product.min_quantity,
									DbMap.Store_product.status_id,DbMap.Product.upc,DbMap.Product.name,DbMap.Product.description})+
							from(
									DbMap.Store_product.store_product_table
									)+
							innerJoin(
									DbMap.Product.product_table
									)+
							on(
									DbMap.Product.upc,DbMap.Store_product.product_upc
									)+
							where(new String[]{
									DbMap.Store_product.store_id
									});
						parameters = new String[]{ new Integer(store.getStore_id()).toString() };
						qryResults = this.getQryResults(qry, parameters);
						LinkedHashMap<String, Store_Product> storeProductsMap = new LinkedHashMap<String, Store_Product>();
						LinkedHashMap<String, Product> productsMap = new LinkedHashMap<String, Product>();
						for(int i=0;i<qryResults.size();i++){
							HashMap<String, String> row = qryResults.get(i);
							Store_Product storeProduct = new Store_Product(row);
							storeProductsMap.put(new Integer(storeProduct.getProduct_upc()).toString(), storeProduct);
							Product product = new Product(row);
							productsMap.put(new Integer(product.getProduct_upc()).toString(), product);
						}
						this.send("products", productsMap);
						this.send("store_products", storeProductsMap);
				}
				else{
					
				}
			}
			else{
				this.send("login_failed");
			};
		};
	}
	
	private void refuseConnection() {
		System.out.println("refusing connection:  ");
			this.send("connection refused");
	}
	
	private void send(String key, Object object){
		ObjectWrapper data = new ObjectWrapper(key, object);
		try {
			System.out.println("Sending an object with key: " +key);
			this.session.getRemote().sendBytes(data.getBuffer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void send(String message){
		try {
			System.out.println("Sending the following message: " +message);
			this.session.getRemote().sendString(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
