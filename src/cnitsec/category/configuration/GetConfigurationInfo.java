/*
 * ��vsphere��ȡ������Ϣ��(Datastore,Network,HostSystem,VirtualMachine)֤�ݣ�
 * �����浽���ݿ���ȥ		
 */

package cnitsec.category.configuration;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.vmware.vim25.ArrayOfDatastoreHostMount;
import com.vmware.vim25.ArrayOfManagedObjectReference;
import com.vmware.vim25.DatastoreHostMount;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;
import com.vmware.vim25.VirtualMachineRuntimeInfo;

import cnitsec.common.connection.BasicConnection;

public class GetConfigurationInfo {
	private SessionFactory sessionFactory;
	private BasicConnection con;
	/*
	 * MOR.getValue() �� �־û������ӳ��,
	 * �־û���������ݿ��е�ʵ���Ӧ
	 */
	private Map<String,TVmConfigurationInfo> vmMap = new HashMap<>();
	private Map<String,THostConfigurationInfo> hostMap = new HashMap<>();
	private Map<String,TDatastoreConfigurationInfo> datastoreMap = new HashMap<>();
	private Map<String,TNetworkConfigurationInfo> networkMap = new HashMap<>();

	private List<ObjectContent> retrievePropertiesAllObjects(
            List<PropertyFilterSpec> listpfs) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

        RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

        List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

        RetrieveResult rslts =
                con.getVimPort().retrievePropertiesEx(
                		con.getServiceContent().getPropertyCollector(), 
                		listpfs,
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
            rslts =
            		con.getVimPort().continueRetrievePropertiesEx(
            				con.getServiceContent().getPropertyCollector(), 
            				token);
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
	
	private List<DynamicProperty> getDynamicPropArray(
	        ManagedObjectReference ref, String type, String propertyString) throws RuntimeFaultFaultMsg, InvalidPropertyFaultMsg {

	        PropertySpec propertySpec = new PropertySpec();
	        propertySpec.setAll(Boolean.FALSE);
	        propertySpec.getPathSet().add(propertyString);
	        propertySpec.setType(type);

	        ObjectSpec objectSpec = new ObjectSpec();
	        objectSpec.setObj(ref);
	        objectSpec.setSkip(Boolean.FALSE);
	        
	        PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
	        propertyFilterSpec.getPropSet().add(propertySpec);
	        propertyFilterSpec.getObjectSet().add(objectSpec);
	        
	        List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
	        listpfs.add(propertyFilterSpec);
	        
	        List<ObjectContent> listobjcont = retrievePropertiesAllObjects(listpfs);
	        
	        ObjectContent contentObj = listobjcont.get(0);
	        List<DynamicProperty> objList = contentObj.getPropSet();
	        return objList;
	    }
	
	private void setVMAttributesList(List<String> vmAttributesArr)
	{
		vmAttributesArr.add("config.uuid");							//�������uuid
    	vmAttributesArr.add("summary.config.memorySizeMB");			//�ڴ��С(MB)
    	vmAttributesArr.add("summary.config.name");					//���������
    	vmAttributesArr.add("summary.config.numCpu");				//����cpu����
    	vmAttributesArr.add("config.guestFullName");				//�ͻ�������ϵͳ
    	vmAttributesArr.add("config.networkShaper.averageBps");		//ƽ������bitÿ��
    	vmAttributesArr.add("config.networkShaper.peakBps");		//��ֵ����bitÿ��
    	vmAttributesArr.add("summary.config.numEthernetCards");		//������������
    	vmAttributesArr.add("summary.config.numVirtualDisks");		//������̸���
    	vmAttributesArr.add("summary.storage.committed");			//��������ñ��Ĵ洢�ռ�
    	vmAttributesArr.add("summary.storage.unshared");			//�����δ����Ĵ洢
    //	vmAttributesArr.add("runtime.powerState");					//��Դ״̬
    }
	
	private void setDSAttributesList(List<String> dsAttributesArr)
	{
		dsAttributesArr.add("info.name");				//datastore����	
		dsAttributesArr.add("info.freeSpace");			//datastore�Ŀ��пռ䣨 ��λbytes��		
		dsAttributesArr.add("info.timestamp");			//datastore������Ϣ�ĸ���ʱ��	
		dsAttributesArr.add("info.url");				//datastoreλ��
		dsAttributesArr.add("info.maxFileSize");		//�����ļ�����С
		dsAttributesArr.add("summary.capacity");		//������bytes��
	//	dsAttributesArr.add("summary.accessible");		//datastore�Ƿ������
		dsAttributesArr.add("summary.type");			//�ļ�ϵͳ���ͣ���VMFS��NFS
		dsAttributesArr.add("summary.uncommitted");		//Total additional storage space, in bytes, potentially used by all virtual machines on this datastore.
    }

	private void setHOSTAttributesList(List<String> hostAttributesArr)
	{
		hostAttributesArr.add("hardware.systemInfo.uuid");				//Hardware BIOS identification
		hostAttributesArr.add("summary.config.name");  					//���������	
		hostAttributesArr.add("summary.hardware.vendor");				//��������		
		hostAttributesArr.add("summary.quickStats.overallCpuUsage");	//cpu��ʹ���� (MHz)
		hostAttributesArr.add("hardware.cpuInfo.hz");					//����cpuƵ��
		hostAttributesArr.add("hardware.cpuInfo.numCpuThreads");		//cpu�߳���
		hostAttributesArr.add("summary.hardware.model");				//Ӳ���ͺ�
	//	hostAttributesArr.add("runtime.connectionState");				//��������״̬
		hostAttributesArr.add("summary.hardware.cpuModel");				//cpu�ͺ�
		hostAttributesArr.add("hardware.cpuInfo.numCpuCores");			//cpu������
		hostAttributesArr.add("hardware.memorySize");					//�ڴ��С(Bytes)
		hostAttributesArr.add("runtime.bootTime");						//����ʱ��
	//	hostAttributesArr.add("runtime.powerState");					//��Դ״̬
		hostAttributesArr.add("summary.quickStats.overallMemoryUsage");	//�ڴ�ʹ����(MB)
		hostAttributesArr.add("summary.hardware.numHBAs");				//��������������
		hostAttributesArr.add("summary.hardware.numNics");				//��������
		hostAttributesArr.add("summary.config.port");  					//�˿���Ŀ		
    }

	private void setNetworkAttributesList(List<String> networkAttributesArr)
	{
		networkAttributesArr.add("name");				//������
    }
	
	private Map<ManagedObjectReference, Map<String, Object>> obtainAllVms() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		List<String> vmAttributesArr = new ArrayList<String>();
		setVMAttributesList(vmAttributesArr);
		Map<ManagedObjectReference, Map<String, Object>> results =
				con.getMOREFs.inContainerByType(con.getServiceContent().getRootFolder(),
						"VirtualMachine",vmAttributesArr.toArray(new String[]{}));
		return results;
	}
	
	private Map<ManagedObjectReference, Map<String, Object>> obtainAllDatastores() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		List<String> dsAttributesArr = new ArrayList<String>();
		setDSAttributesList(dsAttributesArr);
		Map<ManagedObjectReference, Map<String, Object>> results = 
				con.getMOREFs.inContainerByType(con.getServiceContent().getRootFolder(),
						"Datastore",dsAttributesArr.toArray(new String[]{}));
		return results;
	}
	
	private Map<ManagedObjectReference, Map<String, Object>> obtainAllNetworks() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		List<String> networkAttributesArr = new ArrayList<String>();
		setNetworkAttributesList(networkAttributesArr);
		Map<ManagedObjectReference, Map<String, Object>> results = 
				con.getMOREFs.inContainerByType(con.getServiceContent().getRootFolder(),
						"Network",networkAttributesArr.toArray(new String[]{}));
		return results;
	}
	
	private Map<ManagedObjectReference, Map<String, Object>> obtainAllHosts() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		List<String> hostAttributesArr = new ArrayList<String>();
		setHOSTAttributesList(hostAttributesArr);
		Map<ManagedObjectReference, Map<String, Object>> results = 
				con.getMOREFs.inContainerByType(con.getServiceContent().getRootFolder(),
						"HostSystem",hostAttributesArr.toArray(new String[]{}));
		return results;
	}
	
	public void get() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		/*
		 * ������ƽ̨�ṩ�Ľӿڻ����������������������硢datastore�����������map
		 */
		Map<ManagedObjectReference, Map<String, Object>> vms = obtainAllVms();             
        Map<ManagedObjectReference, Map<String, Object>> dss = obtainAllDatastores();       
        Map<ManagedObjectReference, Map<String, Object>> networks = obtainAllNetworks();      
        Map<ManagedObjectReference, Map<String, Object>> hosts = obtainAllHosts();
                      
		for (ManagedObjectReference host : hosts.keySet()) {
			THostConfigurationInfo hostConfigurationInfo;
			if(hostMap.containsKey(host.getValue()))
				hostConfigurationInfo = hostMap.get(host.getValue());
			else
				hostConfigurationInfo = new THostConfigurationInfo();
			
			Map<String, Object> hostprops = hosts.get(host);
            hostConfigurationInfo.setHost_name((String) hostprops.get("summary.config.name"));
            hostConfigurationInfo.setUuid((String) hostprops.get("hardware.systemInfo.uuid"));
            hostConfigurationInfo.setVendor((String) hostprops.get("summary.hardware.vendor"));
            hostConfigurationInfo.setOverallCpuUsage((Integer)hostprops.get("summary.quickStats.overallCpuUsage"));
            hostConfigurationInfo.setCpuHz((Long)hostprops.get("hardware.cpuInfo.hz"));
            hostConfigurationInfo.setNumCpuThreads((Short)hostprops.get("hardware.cpuInfo.numCpuThreads"));
            hostConfigurationInfo.setModel((String)hostprops.get("summary.hardware.model"));
          //hostConfigurationInfo.setConnectionState((String)hostprops.get("runtime.connectionState"));
          //hostAttributesArr.add("runtime.connectionState");				
            hostConfigurationInfo.setCpuModel((String)hostprops.get("summary.hardware.cpuModel"));				
            hostConfigurationInfo.setNumCpuCores((Short)hostprops.get("hardware.cpuInfo.numCpuCores"));			
            hostConfigurationInfo.setMemorySize((Long)hostprops.get("hardware.memorySize"));					
            XMLGregorianCalendar boottime = (XMLGregorianCalendar)hostprops.get("runtime.bootTime");
            GregorianCalendar gc = boottime.toGregorianCalendar();
            hostConfigurationInfo.setBootTime(gc.getTime());						
    	//	hostAttributesArr.add("runtime.powerState");					
            hostConfigurationInfo.setOverallMemoryUsage((Integer)hostprops.get("summary.quickStats.overallMemoryUsage"));
            hostConfigurationInfo.setNumHBAs((Integer)hostprops.get("summary.hardware.numHBAs"));				
            hostConfigurationInfo.setNumNics((Integer)hostprops.get("summary.hardware.numNics"));	
            hostConfigurationInfo.setNumPort((Integer)hostprops.get("summary.config.port")); 
            if(!hostMap.containsKey(host.getValue()))
            	hostMap.put(host.getValue(), hostConfigurationInfo);
		}

		for (ManagedObjectReference vm : vms.keySet()) {
			TVmConfigurationInfo vmConfigurationInfo;
			if(vmMap.containsKey(vm.getValue()))
				vmConfigurationInfo = vmMap.get(vm.getValue());
			else
				vmConfigurationInfo = new TVmConfigurationInfo();
			
			Map<String, Object> vmprops = vms.get(vm);
            vmConfigurationInfo.setVm_name((String) vmprops.get("summary.config.name"));
            vmConfigurationInfo.setNumCpu((Integer) vmprops.get("summary.config.numCpu"));
            vmConfigurationInfo.setMemorySizeMB((Integer) vmprops.get("summary.config.memorySizeMB"));
            vmConfigurationInfo.setUuid((String) vmprops.get("config.uuid"));
            vmConfigurationInfo.setGuestFullName((String) vmprops.get("config.guestFullName"));				
            vmConfigurationInfo.setAverageBps((Long) vmprops.get("config.networkShaper.averageBps"));		
            vmConfigurationInfo.setPeakBps((Long) vmprops.get("config.networkShaper.peakBps"));		
            vmConfigurationInfo.setNumEthernetCards((Integer) vmprops.get("summary.config.numEthernetCards"));		
            vmConfigurationInfo.setNumVirtualDisks((Integer) vmprops.get("summary.config.numVirtualDisks"));		
            vmConfigurationInfo.setCommitted((Long) vmprops.get("summary.storage.committed"));			
            vmConfigurationInfo.setUnshared((Long) vmprops.get("summary.storage.unshared"));			
           //vmConfigurationInfo.setPowerState((enum) vmprops.get("runtime.powerState"));
            if(!vmMap.containsKey(vm.getValue()))
            	vmMap.put(vm.getValue(), vmConfigurationInfo);
		}
		
		for (ManagedObjectReference network : networks.keySet()) {
			TNetworkConfigurationInfo networkConfigurationInfo;
			if(networkMap.containsKey(network.getValue()))
				networkConfigurationInfo = networkMap.get(network.getValue());
			else
				networkConfigurationInfo = new TNetworkConfigurationInfo();
			
			Map<String, Object> networkprops = networks.get(network);
            networkConfigurationInfo.setNetwork_name((String) networkprops.get("name"));
            if(!networkMap.containsKey(network.getValue()))
            	networkMap.put(network.getValue(), networkConfigurationInfo);
		}
		
		for (ManagedObjectReference ds : dss.keySet()) {
			TDatastoreConfigurationInfo dsConfigurationInfo;
			if(datastoreMap.containsKey(ds.getValue()))
				dsConfigurationInfo = datastoreMap.get(ds.getValue());
			else
				dsConfigurationInfo = new TDatastoreConfigurationInfo();
			
			Map<String, Object> dsprops = dss.get(ds);
            dsConfigurationInfo.setDatastore_name((String) dsprops.get("info.name"));
            dsConfigurationInfo.setFreeSpace((Long) dsprops.get("info.freeSpace"));
            XMLGregorianCalendar timestamp = (XMLGregorianCalendar)dsprops.get("info.timestamp");
            GregorianCalendar gc = timestamp.toGregorianCalendar();
            dsConfigurationInfo.setTimestamp(gc.getTime());	
            dsConfigurationInfo.setUrl((String) dsprops.get("info.url"));				
            dsConfigurationInfo.setMaxFileSize((Long) dsprops.get("info.maxFileSize"));		
            dsConfigurationInfo.setCapacity((Long) dsprops.get("summary.capacity"));		
          //dsConfigurationInfo.setAccessible((Boolean) dsprops.get("summary.accessible"));		
            dsConfigurationInfo.setType((String) dsprops.get("summary.type"));			
            dsConfigurationInfo.setUncommitted((Long) dsprops.get("summary.uncommitted"));
            if(!datastoreMap.containsKey(ds.getValue()))
            	datastoreMap.put(ds.getValue(), dsConfigurationInfo);
		}
		
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		
		for (ManagedObjectReference host : hosts.keySet()) {
	    	/*
	    	 * �������datastore����
	    	 */
	    	List<DynamicProperty> HOSTdatastore =
	                getDynamicPropArray(host, host.getType(),
	                        "datastore");
	    	ArrayOfManagedObjectReference datastores = 
	                ((ArrayOfManagedObjectReference) (HOSTdatastore.get(0)).getVal());
	    	List<ManagedObjectReference> hostdatastores = datastores.getManagedObjectReference();
	    	for(ManagedObjectReference ds : hostdatastores){
	    		hostMap.get(host.getValue()).getDatastores().add(datastoreMap.get(ds.getValue()));
	    	}
	    	/*
	    	 * �������network����
	    	 */
	    	List<DynamicProperty> HOSTnetwork =
	                getDynamicPropArray(host, host.getType(),
	                        "network");
	    	ArrayOfManagedObjectReference hostnetworks = 
	                ((ArrayOfManagedObjectReference) (HOSTnetwork.get(0)).getVal());
	    	List<ManagedObjectReference> HOSTnetworks = hostnetworks.getManagedObjectReference();
	    	for(ManagedObjectReference nw : HOSTnetworks){
	    		hostMap.get(host.getValue()).getNetworks().add(networkMap.get(nw.getValue()));
	    	}
	    	session.saveOrUpdate(hostMap.get(host.getValue()));
		}
		
		for (ManagedObjectReference vm : vms.keySet()) {
            /*
	         * �������host����
	         */
	        List<DynamicProperty> VMruntime =
	                getDynamicPropArray(vm, vm.getType(),
	                        "runtime");
	    	VirtualMachineRuntimeInfo runtime =  
	    			((VirtualMachineRuntimeInfo) (VMruntime.get(0)).getVal());
	    	ManagedObjectReference host = runtime.getHost();
	    	for (String hostvalue : hostMap.keySet()) {
	    		if(host.getValue().equals(hostvalue)){
	    			vmMap.get(vm.getValue()).setHost(hostMap.get(hostvalue));
	    		}
	    	}
			
	    	/*
	    	 * �������network����
	    	 */
	    	List<DynamicProperty> VMnetwork =
	                getDynamicPropArray(vm, vm.getType(),
	                        "network");
	    	ArrayOfManagedObjectReference networklist = 
	                ((ArrayOfManagedObjectReference) (VMnetwork.get(0)).getVal());
	    	List<ManagedObjectReference> VMnetworks = networklist.getManagedObjectReference();
	    	
	    	for(ManagedObjectReference network : VMnetworks){
	    		vmMap.get(vm.getValue()).getNetworks().add(networkMap.get(network.getValue()));
	    	}
	   
	    	/*
	    	 * �������datastore����
	    	 */
	    	List<DynamicProperty> VMdatastore =
	                getDynamicPropArray(vm, vm.getType(),
	                        "datastore");
	    	ArrayOfManagedObjectReference datastores = 
	                ((ArrayOfManagedObjectReference) (VMdatastore.get(0)).getVal());
	    	List<ManagedObjectReference> VMdatastores = datastores.getManagedObjectReference();
	    	for(ManagedObjectReference datastore : VMdatastores){
	    		vmMap.get(vm.getValue()).getDatastores().add(datastoreMap.get(datastore.getValue()));
	    	}	    	
	    	session.saveOrUpdate(vmMap.get(vm.getValue()));	    	
		}
	
		for (ManagedObjectReference network : networks.keySet()) {
			/*
	         * network��vm����
	         */
	        List<DynamicProperty> NETWORKvm =
	                getDynamicPropArray(network, network.getType(),
	                        "vm");
	    	ArrayOfManagedObjectReference networkvms =  
	    			((ArrayOfManagedObjectReference) (NETWORKvm.get(0)).getVal());
	    	List<ManagedObjectReference> NETWORKvms = networkvms.getManagedObjectReference();
	    	
	    	for(ManagedObjectReference vm : NETWORKvms){
	    		networkMap.get(network.getValue()).getVms().add(vmMap.get(vm.getValue()));
	    	}
	    	
	    	/*
	    	 * network��host����
	    	 */
	    	List<DynamicProperty> NETWORKhost =
	                getDynamicPropArray(network, network.getType(),
	                        "host");
	    	ArrayOfManagedObjectReference networkhosts =  
	    			((ArrayOfManagedObjectReference) (NETWORKhost.get(0)).getVal());
	    	List<ManagedObjectReference> NETWORKhosts = networkhosts.getManagedObjectReference();
	    	for(ManagedObjectReference host : NETWORKhosts){
	    		networkMap.get(network.getValue()).getHosts().add(hostMap.get(host.getValue()));
	    	}
	    	session.saveOrUpdate(networkMap.get(network.getValue()));
		}
	
		for (ManagedObjectReference ds : dss.keySet()) {
	        /*
	         * datastore��host����
	         */
	        List<DynamicProperty> datastorehost =
	                getDynamicPropArray(ds, ds.getType(),
	                        "host");
	    	ArrayOfDatastoreHostMount datastorehosts = 
	                ((ArrayOfDatastoreHostMount) (datastorehost.get(0)).getVal());
	    	List<DatastoreHostMount> Datastorehosts = datastorehosts.getDatastoreHostMount();
	    	for(DatastoreHostMount ahost : Datastorehosts){
	    		ManagedObjectReference host = ahost.getKey();
	    		datastoreMap.get(ds.getValue()).getHosts().add(hostMap.get(host.getValue()));
	    	}
	    	
	    	/*
	    	 * datastore��vm����
	    	 */
	    	List<DynamicProperty> dsvm =
	                getDynamicPropArray(ds, ds.getType(),
	                        "vm");
	    	ArrayOfManagedObjectReference datastorevms =  
	    			((ArrayOfManagedObjectReference) (dsvm.get(0)).getVal());
	    	List<ManagedObjectReference> dsvms = datastorevms.getManagedObjectReference();
	    	for(ManagedObjectReference vm : dsvms){
	    		datastoreMap.get(ds.getValue()).getVms().add(vmMap.get(vm.getValue()));
	    	}
	    	session.saveOrUpdate(datastoreMap.get(ds.getValue()));
		}
		transaction.commit();
		session.close();
	}

	//��ʼ�����ݿ���4�ű�
  	public void initAllTables() throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
  		Session session = sessionFactory.openSession();
  		Transaction transaction = session.beginTransaction();
  		String deleteDatastoreHost = "delete from t_datastore_host";
  		String deleteDatastoreVm = "delete from t_datastore_vm";
  		String deleteNetworkVm = "delete from t_vm_network";
  		String deleteHostNetwork = "delete from t_host_network";
  		String deleteEvents = "delete TVmwareEvent";
  		String deleteDatastore = "delete TDatastoreConfigurationInfo";
  		String deleteHost = "delete THostConfigurationInfo";
  		String deleteNetwork = "delete TNetworkConfigurationInfo";
  		String deleteVm = "delete TVmConfigurationInfo";
  		session.createSQLQuery(deleteDatastoreHost).executeUpdate();
  		session.createSQLQuery(deleteDatastoreVm).executeUpdate();
  		session.createSQLQuery(deleteNetworkVm).executeUpdate();
  		session.createSQLQuery(deleteHostNetwork).executeUpdate();
  		session.createQuery(deleteEvents).executeUpdate();
  		session.createQuery(deleteVm).executeUpdate();
  		session.createQuery(deleteDatastore).executeUpdate();
  		session.createQuery(deleteHost).executeUpdate();
  		session.createQuery(deleteNetwork).executeUpdate();
  		transaction.commit();
  		session.close();
  		
  		get();
  	}
    
	public Map<String, TVmConfigurationInfo> getVmMap() {
		return vmMap;
	}

	public void setVmMap(Map<String, TVmConfigurationInfo> vmMap) {
		this.vmMap = vmMap;
	}

	public Map<String, THostConfigurationInfo> getHostMap() {
		return hostMap;
	}

	public void setHostMap(Map<String, THostConfigurationInfo> hostMap) {
		this.hostMap = hostMap;
	}

	public Map<String, TDatastoreConfigurationInfo> getDatastoreMap() {
		return datastoreMap;
	}

	public void setDatastoreMap(Map<String, TDatastoreConfigurationInfo> datastoreMap) {
		this.datastoreMap = datastoreMap;
	}

	public Map<String, TNetworkConfigurationInfo> getNetworkMap() {
		return networkMap;
	}

	public void setNetworkMap(Map<String, TNetworkConfigurationInfo> networkMap) {
		this.networkMap = networkMap;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void setCon(BasicConnection con) {
		this.con = con;
	}
}


