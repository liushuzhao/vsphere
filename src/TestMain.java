
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vmware.vim25.InvalidPropertyFaultMsg;
import com.vmware.vim25.InvalidStateFaultMsg;
import com.vmware.vim25.RuntimeFaultFaultMsg;

import cnitsec.category.configuration.GetConfigurationInfo;
import cnitsec.category.monitoring.GetPerformanceData;
import cnitsec.category.operating.GetEvents;


public class TestMain {
	public static void main(String[] args) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, InvalidStateFaultMsg {
		@SuppressWarnings("resource")
		ApplicationContext ctx = new
				ClassPathXmlApplicationContext("applicationContext-hibernate.xml");
		
	GetConfigurationInfo getConfigurationInfo = ctx.getBean("getConfigurationInfo",GetConfigurationInfo.class);
		getConfigurationInfo.get();
//		GetEvents getEvents = ctx.getBean("getEvents",GetEvents.class);
//		getEvents.get();
//		GetPerformanceData getPerformanceData = ctx.getBean("getPerformanceData",GetPerformanceData.class);
//		System.out.print(getPerformanceData.get("cpu.usage.AVERAGE", "HostSystem", "192.168.1.183", 300));
	}
}
