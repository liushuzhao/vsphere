package cnitsec.common.connection;

@SuppressWarnings("deprecation")
public class ConnectionManager {
	public static BasicConnection connection;
	private static MyTrustManager myTrustManager;
	
	private static final String url = "https://192.168.101.33/sdk";
	private static final String username = "administrator@vsphere.local";
	private static final String password = "Yaxin123!";

	static {
		myTrustManager = new MyTrustManager(); 
		//����һ��֤�飬���Ƽ��ķ���
		myTrustManager.trustAll(); 		
		
		connection = new BasicConnection();
		connection.setUrl(url);
	    connection.setUsername(username);
	    connection.setPassword(password);
	    if(!connection.connect().isConnected())
	    	System.err.println("����ʧ��");
	}
}
