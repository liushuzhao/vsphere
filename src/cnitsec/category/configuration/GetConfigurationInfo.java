/*
 * 从vsphere获取配置信息类(Datastore,Network,HostSystem,VirtualMachine)证据，
 * 并保存到数据库中去		
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
	 * MOR.getValue() 和 持久化对象的映射,
	 * 持久化对象和数据库中的实体对应
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
		vmAttributesArr.add("config.uuid");							//虚拟机的uuid
    	vmAttributesArr.add("summary.config.memorySizeMB");			//内存大小(MB)
    	vmAttributesArr.add("summary.config.name");					//虚拟机名称
    	vmAttributesArr.add("summary.config.numCpu");				//虚拟cpu个数
    	vmAttributesArr.add("config.guestFullName");				//客户机操作系统
    	vmAttributesArr.add("config.networkShaper.averageBps");		//平均带宽，bit每秒
    	vmAttributesArr.add("config.networkShaper.peakBps");		//峰值带宽，bit每秒
    	vmAttributesArr.add("summary.config.numEthernetCards");		//虚拟网卡个数
    	vmAttributesArr.add("summary.config.numVirtualDisks");		//虚拟磁盘个数
    	vmAttributesArr.add("summary.storage.committed");			//虚拟机所置备的存储空间
    	vmAttributesArr.add("summary.storage.unshared");			//虚拟机未共享的存储
    //	vmAttributesArr.add("runtime.powerState");					//电源状态
    }
	
	private void setDSAttributesList(List<String> dsAttributesArr)
	{
		dsAttributesArr.add("info.name");				//datastore名字	
		dsAttributesArr.add("info.freeSpace");			//datastore的空闲空间（ 单位bytes）		
		dsAttributesArr.add("info.timestamp");			//datastore配置信息的更新时间	
		dsAttributesArr.add("info.url");				//datastore位置
		dsAttributesArr.add("info.maxFileSize");		//单个文件最大大小
		dsAttributesArr.add("summary.capacity");		//容量（bytes）
	//	dsAttributesArr.add("summary.accessible");		//datastore是否可连接
		dsAttributesArr.add("summary.type");			//文件系统类型，如VMFS和NFS
		dsAttributesArr.add("summary.uncommitted");		//Total additional storage space, in bytes, potentially used by all virtual machines on this datastore.
    }

	private void setHOSTAttributesList(List<String> hostAttributesArr)
	{
		hostAttributesArr.add("hardware.systemInfo.uuid");				//Hardware BIOS identification
		hostAttributesArr.add("summary.config.name");  					//物理机名称	
		hostAttributesArr.add("summary.hardware.vendor");				//生产厂家		
		hostAttributesArr.add("summary.quickStats.overallCpuUsage");	//cpu总使用量 (MHz)
		hostAttributesArr.add("hardware.cpuInfo.hz");					//单个cpu频率
		hostAttributesArr.add("hardware.cpuInfo.numCpuThreads");		//cpu线程数
		hostAttributesArr.add("summary.hardware.model");				//硬件型号
	//	hostAttributesArr.add("runtime.connectionState");				//主机连接状态
		hostAttributesArr.add("summary.hardware.cpuModel");				//cpu型号
		hostAttributesArr.add("hardware.cpuInfo.numCpuCores");			//cpu核心数
		hostAttributesArr.add("hardware.memorySize");					//内存大小(Bytes)
		hostAttributesArr.add("runtime.bootTime");						//启动时间
	//	hostAttributesArr.add("runtime.powerState");					//电源状态
		hostAttributesArr.add("summary.quickStats.overallMemoryUsage");	//内存使用量(MB)
		hostAttributesArr.add("summary.hardware.numHBAs");				//总线适配器个数
		hostAttributesArr.add("summary.hardware.numNics");				//网卡个数
		hostAttributesArr.add("summary.config.port");  					//端口数目		
    }

	private void setNetworkAttributesList(List<String> networkAttributesArr)
	{
		networkAttributesArr.add("name");				//网络名
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
		 * 调用云平台提供的接口获得所有主机、虚拟机、网络、datastore对象和其属性map
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
	    	 * 物理机的datastore属性
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
	    	 * 物理机的network属性
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
	         * 虚拟机的host属性
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
	    	 * 虚拟机的network属性
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
	    	 * 虚拟机的datastore属性
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
	         * network的vm属性
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
	    	 * network的host属性
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
	         * datastore的host属性
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
	    	 * datastore的vm属性
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

	//初始化数据库中4张表
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


