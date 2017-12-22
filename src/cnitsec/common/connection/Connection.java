package cnitsec.common.connection;


import com.vmware.vim25.*;

import java.net.URL;
import java.util.Map;

public interface Connection {
    // getters and setters
   
    void setUrl(String url);

    String getUrl();

    String getHost();

    Integer getPort();

   
    void setUsername(String username);

    String getUsername();

   
    void setPassword(String password);

    String getPassword();

    VimService getVimService();

    VimPortType getVimPort();

    ServiceContent getServiceContent();

    UserSession getUserSession();

    String getServiceInstanceName();

    @SuppressWarnings("rawtypes")
	Map getHeaders();

    ManagedObjectReference getServiceInstanceReference();

    
    Connection connect();

    boolean isConnected();

    
    Connection disconnect();

    URL getURL();
}
