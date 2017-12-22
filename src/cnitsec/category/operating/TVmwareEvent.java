package cnitsec.category.operating;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import cnitsec.category.configuration.*;

@Entity
@Table(name = "t_vmware_event")
public class TVmwareEvent {
	@Id
	private Integer event_key;
	private Date createdTime;
	
	@ManyToOne(targetEntity=TDatastoreConfigurationInfo.class )
	@JoinColumn(name="datastore_id" , referencedColumnName="datastore_id" , nullable=true)
	private TDatastoreConfigurationInfo datastore;
	
	@ManyToOne(targetEntity=THostConfigurationInfo.class)
	@JoinColumn(name="host_id" , referencedColumnName="host_id" , nullable=true)
	private THostConfigurationInfo host;
	
	@ManyToOne(targetEntity=TVmConfigurationInfo.class)
	@JoinColumn(name="vm_id" , referencedColumnName="vm_id" , nullable=true)
	private TVmConfigurationInfo vm;
	
	@ManyToOne(targetEntity=TNetworkConfigurationInfo.class)
	@JoinColumn(name="network_id" , referencedColumnName="network_id" , nullable=true)
	private TNetworkConfigurationInfo network;
	
	private String user;
	private String fullName;
	
	public Integer getEvent_key() {
		return event_key;
	}
	public void setEvent_key(Integer event_key) {
		this.event_key = event_key;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	public TDatastoreConfigurationInfo getDatastore() {
		return datastore;
	}
	public void setDatastore(TDatastoreConfigurationInfo datastore) {
		this.datastore = datastore;
	}
	public THostConfigurationInfo getHost() {
		return host;
	}
	public void setHost(THostConfigurationInfo host) {
		this.host = host;
	}
	public TVmConfigurationInfo getVm() {
		return vm;
	}
	public void setVm(TVmConfigurationInfo vm) {
		this.vm = vm;
	}
	public TNetworkConfigurationInfo getNetwork() {
		return network;
	}
	public void setNetwork(TNetworkConfigurationInfo network) {
		this.network = network;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}
