package cnitsec.category.configuration;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "t_vm_configuration_info")
public class TVmConfigurationInfo {
	@Id
	@GenericGenerator(name="my" , strategy="increment")
	@GeneratedValue(generator="my")
	private Integer vm_id;
	private String uuid;
	private String vm_name;
	private Integer numCpu;
	private Integer memorySizeMB;
	private String guestFullName;
	private Long averageBps;
	private Long peakBps;
	private Integer numEthernetCards;
	private Integer numVirtualDisks;
	private Long committed;
	private Long unshared;
	@ManyToOne(targetEntity=THostConfigurationInfo.class)
	@JoinColumn(name="host_id",referencedColumnName="host_id",nullable=false)
	@Cascade(CascadeType.ALL)
	private THostConfigurationInfo host;
	
	@ManyToMany(targetEntity=TDatastoreConfigurationInfo.class)
	@JoinTable(name="t_datastore_vm",
	joinColumns=@JoinColumn(name="vm_id",
			referencedColumnName="vm_id"),
	inverseJoinColumns=@JoinColumn(name="datastore_id",
			referencedColumnName="datastore_id")
	)
	private Set<TDatastoreConfigurationInfo> datastores = new HashSet<>();
	
	@ManyToMany(targetEntity=TNetworkConfigurationInfo.class)
	@JoinTable(name="t_vm_network",
	joinColumns=@JoinColumn(name="vm_id",
			referencedColumnName="vm_id"),
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
	
	public void setNumCpu(Integer cpuNum){
		this.numCpu = cpuNum;
	}
	public Integer getNumCpu(){
		return this.numCpu;
	}

	public Integer getMemorySizeMB() {
		return memorySizeMB;
	}

	public void setMemorySizeMB(Integer memorySizeMB) {
		this.memorySizeMB = memorySizeMB;
	}

	public String getGuestFullName() {
		return guestFullName;
	}

	public void setGuestFullName(String guestFullName) {
		this.guestFullName = guestFullName;
	}

	public Long getAverageBps() {
		return averageBps;
	}

	public void setAverageBps(Long averageBps) {
		this.averageBps = averageBps;
	}

	public Long getPeakBps() {
		return peakBps;
	}

	public void setPeakBps(Long peakBps) {
		this.peakBps = peakBps;
	}

	public Integer getNumEthernetCards() {
		return numEthernetCards;
	}

	public void setNumEthernetCards(Integer numEthernetCards) {
		this.numEthernetCards = numEthernetCards;
	}

	public Integer getNumVirtualDisks() {
		return numVirtualDisks;
	}

	public void setNumVirtualDisks(Integer numVirtualDisks) {
		this.numVirtualDisks = numVirtualDisks;
	}

	public Long getCommitted() {
		return committed;
	}

	public void setCommitted(Long committed) {
		this.committed = committed;
	}

	public Long getUnshared() {
		return unshared;
	}

	public void setUnshared(Long unshared) {
		this.unshared = unshared;
	}

	public THostConfigurationInfo getHost() {
		return host;
	}

	public void setHost(THostConfigurationInfo host) {
		this.host = host;
	}

	public Integer getVm_id() {
		return vm_id;
	}

	public void setVm_id(Integer vm_id) {
		this.vm_id = vm_id;
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

	public String getVm_name() {
		return vm_name;
	}

	public void setVm_name(String vm_name) {
		this.vm_name = vm_name;
	}
}
