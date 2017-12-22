package cnitsec.category.configuration;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "t_host_configuration_info")
public class THostConfigurationInfo {
	@Id
	@GenericGenerator(name="my" , strategy="increment")
	@GeneratedValue(generator="my")
	private Integer host_id;
	private String uuid;
	private String host_name;
	private	String vendor;
	private Integer overallCpuUsage;
	private Long cpuHz;
	private Short numCpuThreads;
	private String connectionState;
	private String model;
	private String cpuModel;
	private Short numCpuCores;
	private Long memorySize;
	private Date bootTime;
	private Integer overallMemoryUsage;
	private Integer numHBAs;
	private Integer numNics;
	private Integer numPort;
	@OneToMany(targetEntity=TVmConfigurationInfo.class
		,mappedBy="host")
	private Set<TVmConfigurationInfo> vms 
		= new HashSet<TVmConfigurationInfo>();
	
	@ManyToMany(targetEntity=TDatastoreConfigurationInfo.class)
	@JoinTable(name="t_datastore_host",
		joinColumns=@JoinColumn(name="host_id",
				referencedColumnName="host_id"),
		inverseJoinColumns=@JoinColumn(name="datastore_id",
				referencedColumnName="datastore_id")
	)
	private Set<TDatastoreConfigurationInfo> datastores = new HashSet<>();
	@ManyToMany(targetEntity=TNetworkConfigurationInfo.class)
	@JoinTable(name="t_host_network",
	joinColumns=@JoinColumn(name="host_id",
			referencedColumnName="host_id"),
	inverseJoinColumns=@JoinColumn(name="network_id",
			referencedColumnName="network_id")
	)
	private Set<TNetworkConfigurationInfo> networks = new HashSet<>();
	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public Long getCpuHz() {
		return cpuHz;
	}

	public void setCpuHz(Long cpuHz) {
		this.cpuHz = cpuHz;
	}

	public Short getNumCpuThreads() {
		return numCpuThreads;
	}

	public void setNumCpuThreads(Short numCpuThreads) {
		this.numCpuThreads = numCpuThreads;
	}

	public String getConnectionState() {
		return connectionState;
	}

	public void setConnectionState(String connectionState) {
		this.connectionState = connectionState;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getOverallCpuUsage() {
		return overallCpuUsage;
	}

	public void setOverallCpuUsage(Integer overallCpuUsage) {
		this.overallCpuUsage = overallCpuUsage;
	}

	public String getCpuModel() {
		return cpuModel;
	}

	public void setCpuModel(String cpuModel) {
		this.cpuModel = cpuModel;
	}

	public void setNumCpuCores(Short numCpuCoures) {
		this.numCpuCores = numCpuCoures;
	}

	public Long getMemorySize() {
		return memorySize;
	}

	public void setMemorySize(Long memorySize) {
		this.memorySize = memorySize;
	}

	public Date getBootTime() {
		return bootTime;
	}

	public void setBootTime(Date bootTime) {
		this.bootTime = bootTime;
	}

	public Integer getOverallMemoryUsage() {
		return overallMemoryUsage;
	}

	public void setOverallMemoryUsage(Integer overallMemoryUsage) {
		this.overallMemoryUsage = overallMemoryUsage;
	}

	public Integer getNumHBAs() {
		return numHBAs;
	}

	public void setNumHBAs(Integer numHBAs) {
		this.numHBAs = numHBAs;
	}

	public Integer getNumNics() {
		return numNics;
	}

	public void setNumNics(Integer numNics) {
		this.numNics = numNics;
	}

	public Integer getNumPort() {
		return numPort;
	}

	public void setNumPort(Integer numPort) {
		this.numPort = numPort;
	}

	public Integer getHost_id() {
		return host_id;
	}

	public void setHost_id(Integer host_id) {
		this.host_id = host_id;
	}

	public Short getNumCpuCores() {
		return numCpuCores;
	}

	public Set<TVmConfigurationInfo> getVms() {
		return vms;
	}

	public void setVms(Set<TVmConfigurationInfo> vms) {
		this.vms = vms;
	}

	public Set<TDatastoreConfigurationInfo> getDatastores() {
		return datastores;
	}

	public void setDatastores(Set<TDatastoreConfigurationInfo> datastores) {
		this.datastores = datastores;
	}

	public Set<TNetworkConfigurationInfo> getNetworks() {
		return networks;
	}

	public void setNetworks(Set<TNetworkConfigurationInfo> networks) {
		this.networks = networks;
	}

	public String getHost_name() {
		return host_name;
	}

	public void setHost_name(String host_name) {
		this.host_name = host_name;
	}
}
