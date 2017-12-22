package cnitsec.category.monitoring;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.datatype.XMLGregorianCalendar;

import com.vmware.vim25.ArrayOfPerfCounterInfo;
import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PerfCounterInfo;
import com.vmware.vim25.PerfEntityMetric;
import com.vmware.vim25.PerfEntityMetricBase;
import com.vmware.vim25.PerfMetricId;
import com.vmware.vim25.PerfMetricIntSeries;
import com.vmware.vim25.PerfMetricSeries;
import com.vmware.vim25.PerfQuerySpec;
import com.vmware.vim25.PerfSampleInfo;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import cnitsec.common.connection.BasicConnection;

public class GetPerformanceData {
	private BasicConnection con;

	public Map<Date,Long> get(String counter ,String entityType , String entityName,int intervalId) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg{
		Map<Date,Long> ret = new TreeMap<>();
		
		HashMap<String, Integer> countersIdMap = new HashMap<String, Integer>();
		HashMap<Integer, PerfCounterInfo> countersInfoMap = 
				new HashMap<Integer, PerfCounterInfo>();
		
		ManagedObjectReference performanceMgrRef = 
				con.getServiceContent().getPerfManager();
		ManagedObjectReference pCollectorRef = 
				con.getServiceContent().getPropertyCollector();
		
		Map<String, ManagedObjectReference> entityNametoMor = con.getMOREFs.inFolderByType(con.getServiceContent()
                .getRootFolder(), entityType, new RetrieveOptions());
		
        ManagedObjectReference entityMor = entityNametoMor.get(entityName);
        
		ObjectSpec oSpec = new ObjectSpec();
		oSpec.setObj(performanceMgrRef);
		
		PropertySpec pSpec = new PropertySpec();
		pSpec.setType("PerformanceManager");
		pSpec.getPathSet().add("perfCounter");
		
		PropertyFilterSpec fSpec = new PropertyFilterSpec();
		fSpec.getObjectSet().add(oSpec);
		fSpec.getPropSet().add(pSpec);
		
		List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
		fSpecList.add(fSpec);
	
		RetrieveOptions ro = new RetrieveOptions();
		
		RetrieveResult props = con.getVimPort().retrievePropertiesEx(pCollectorRef,fSpecList,ro);
		 
		List<PerfCounterInfo> perfCounters = new ArrayList<PerfCounterInfo>();
		if (props != null) {
			for (ObjectContent oc : props.getObjects()) {
				List<DynamicProperty> dps = oc.getPropSet();
				if (dps != null) {
					for (DynamicProperty dp : dps) {
						perfCounters = ((ArrayOfPerfCounterInfo)dp.getVal()).getPerfCounterInfo();
					}
				}
			}
		}
	
		for(PerfCounterInfo perfCounter : perfCounters) {
			Integer counterId = new Integer(perfCounter.getKey());
			countersInfoMap.put(counterId, perfCounter);
			String counterGroup = perfCounter.getGroupInfo().getKey();
			String counterName = perfCounter.getNameInfo().getKey();
			String counterRollupType = perfCounter.getRollupType().toString();
			String fullCounterName = counterGroup + "." + counterName + "." + counterRollupType;
			countersIdMap.put(fullCounterName, counterId);
		}
		
		List<PerfMetricId> perfMetricIds = new ArrayList<PerfMetricId>();
		PerfMetricId metricId = new PerfMetricId();
		metricId.setCounterId(countersIdMap.get(counter));
		metricId.setInstance("*");
		perfMetricIds.add(metricId);
		
		PerfQuerySpec querySpecification = new PerfQuerySpec();
		querySpecification.setEntity(entityMor);
		querySpecification.setIntervalId(intervalId);
		querySpecification.setFormat("normal");
		querySpecification.getMetricId().addAll(perfMetricIds);
		 
		List<PerfQuerySpec> pqsList = new ArrayList<PerfQuerySpec>();
		pqsList.add(querySpecification);
		 
		List<PerfEntityMetricBase> retrievedStats = con.getVimPort().queryPerf(performanceMgrRef, pqsList);
		
		for(PerfEntityMetricBase singleEntityPerfStats : retrievedStats) {
			PerfEntityMetric entityStats = (PerfEntityMetric)singleEntityPerfStats;
			List<PerfMetricSeries> metricsValues = entityStats.getValue();
		 
			if(metricsValues.isEmpty()) {
				System.err.println("No stats retrieved. ");
			}
			
			List<PerfSampleInfo> TimeInfoAboutStats = entityStats.getSampleInfo();
			
			List<Date> time = new ArrayList<>();
			for(PerfSampleInfo psi : TimeInfoAboutStats){
				XMLGregorianCalendar timestamp = psi.getTimestamp();
				GregorianCalendar gc = timestamp.toGregorianCalendar();
	            time.add(gc.getTime());	
			}
			
			List<Long> values = new ArrayList<>();
			for(PerfMetricSeries pms : metricsValues) {
				PerfMetricIntSeries pmis = (PerfMetricIntSeries)pms;
				values = pmis.getValue();
			}
			
			for(int i=0;i<time.size();i++){
				ret.put(time.get(i), values.get(i));
			
			}
		}
		return ret;
	}

	public void setCon(BasicConnection con) {
		this.con = con;
	}
}
