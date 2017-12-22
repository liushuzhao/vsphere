package cnitsec.category.operating;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.vmware.vim25.ArrayOfEvent;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.Event;
import com.vmware.vim25.EventFilterSpec;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import cnitsec.category.configuration.GetConfigurationInfo;

import cnitsec.common.connection.BasicConnection;

public class GetEvents {
	private SessionFactory sessionFactory;
    private BasicConnection con;
    //sigleton
    private GetConfigurationInfo getConfigurationInfo;
	
    private PropertyFilterSpec createEventFilterSpec(ManagedObjectReference eventHistoryCollector) throws InvalidStateFaultMsg, RuntimeFaultFaultMsg {
        PropertySpec propSpec = new PropertySpec();
        propSpec.setAll(new Boolean(false));
        propSpec.getPathSet().add("latestPage");
        propSpec.setType(eventHistoryCollector.getType());

        ObjectSpec objSpec = new ObjectSpec();
        objSpec.setObj(eventHistoryCollector);
        objSpec.setSkip(new Boolean(false));

        PropertyFilterSpec spec = new PropertyFilterSpec();
        spec.getPropSet().add(propSpec);
        spec.getObjectSet().add(objSpec);
        return spec;
    }
    
    private List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {
    	ManagedObjectReference propCollector = con.getServiceContent().getPropertyCollector();
    	
        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        RetrieveResult rslts =
        		con.getVimPort().retrievePropertiesEx(propCollector, listpfs,
                        propObjectRetrieveOpts);
        if (rslts != null && rslts.getObjects() != null
                && !rslts.getObjects().isEmpty()) {
            listobjcontent.addAll(rslts.getObjects());
        }
        String token = null;
        if (rslts != null && rslts.getToken() != null) {
            token = rslts.getToken();
        }
        while (token != null && !token.isEmpty()) {
            rslts = con.getVimPort().continueRetrievePropertiesEx(propCollector, token);
            token = null;
            if (rslts != null) {
                token = rslts.getToken();
                if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
                    listobjcontent.addAll(rslts.getObjects());
                }
            }
        }

        return listobjcontent;
    }
    
    private void insertIntoDB(Session session,Event anEvent) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
    	TVmwareEvent event = new TVmwareEvent();
    	
    	XMLGregorianCalendar createdTime = (XMLGregorianCalendar)anEvent.getCreatedTime();
        GregorianCalendar gc = createdTime.toGregorianCalendar();
        event.setCreatedTime(gc.getTime());
        if(anEvent.getDs()!=null&&anEvent.getDs().getDatastore()!=null)
        	event.setDatastore(getConfigurationInfo.getDatastoreMap().get(anEvent.getDs().getDatastore().getValue()));
        if(anEvent.getNet()!=null&&anEvent.getNet().getNetwork()!=null)
        	event.setNetwork(getConfigurationInfo.getNetworkMap().get(anEvent.getNet().getNetwork().getValue()));
        if(anEvent.getVm()!=null&&anEvent.getVm().getVm()!=null)
        	event.setVm(getConfigurationInfo.getVmMap().get(anEvent.getVm().getVm().getValue()));
        if(anEvent.getHost()!=null&&anEvent.getHost().getHost()!=null)
        	event.setHost(getConfigurationInfo.getHostMap().get(anEvent.getHost().getHost().getValue()));
        event.setEvent_key(anEvent.getKey());
        if(anEvent.getFullFormattedMessage().length() < 255)
        	event.setFullName(anEvent.getFullFormattedMessage());
        event.setUser(anEvent.getUserName());
        
        session.save(event);
    }
    
    public void get() throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg, InvalidStateFaultMsg {
    	//先更新一遍配置信息类数据库
    	getConfigurationInfo.get();
    	   
    	EventFilterSpec eventFilter = new EventFilterSpec();
    	ManagedObjectReference eventManager = con.getServiceContent().getEventManager();
    	ManagedObjectReference eventHistoryCollector = 
    			con.getVimPort().createCollectorForEvents(eventManager, eventFilter);
    	con.getVimPort().resetCollector(eventHistoryCollector);
    	
    	ArrayList<PropertyFilterSpec> listpfs =
                new ArrayList<PropertyFilterSpec>();
    	PropertyFilterSpec eventFilterSpec = createEventFilterSpec(eventHistoryCollector);
        listpfs.add(eventFilterSpec);
        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);      
 
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
       
        Query q = session.createQuery("from TVmwareEvent order by id desc");
        q.setMaxResults(1);
        int max_id;
        if(q.uniqueResult() != null)
        	max_id = ((TVmwareEvent)q.uniqueResult()).getEvent_key();
        else max_id = 0;
        
        if (listobjcont != null) {
        	for (ObjectContent oc : listobjcont) {
        		List<DynamicProperty> dps = oc.getPropSet();
        		if (dps != null) {
	        		for (DynamicProperty dp : dps) {
	        			ArrayOfEvent arrayEvents = (ArrayOfEvent) dp.getVal();
	        			ArrayList<Event> eventList = (ArrayList<Event>) arrayEvents.getEvent(); 
	                    for (int i = 0; i < eventList.size() && eventList.get(i).getKey() > max_id; i++) {
	                        insertIntoDB(session,eventList.get(i));
	                    }          
	        		}
        		}
        	}            
        } else {
            System.err.println("No Events retrieved!");
            return;
        }

        while(true)
        {
        	ArrayList<Event> eventList = new ArrayList<Event>();
        	eventList = (ArrayList<Event>) con.getVimPort().readPreviousEvents(eventHistoryCollector, 1000);
        	int i;
        	for (i = 0; i < eventList.size() && eventList.get(i).getKey() > max_id; i++) {
        		insertIntoDB(session,eventList.get(i));
            }
        	if(i < eventList.size())
        		break;
        	if(eventList.size() < 1000)
        		break;
        }
        tx.commit();
        session.close();
    }

  
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setCon(BasicConnection con) {
		this.con = con;
	}

	public void setGetConfigurationInfo(GetConfigurationInfo getConfigurationInfo) {
		this.getConfigurationInfo = getConfigurationInfo;
	}
}
