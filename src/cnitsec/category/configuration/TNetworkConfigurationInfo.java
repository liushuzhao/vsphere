package cnitsec.category.configuration;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "t_network_configuration_info")
public class TNetworkConfigurationInfo {
	@Id
	@GenericGenerator(name="my" , strategy="increment")
	@GeneratedValue(generator="my")
	private Integer network_id;
	private String network_name;
	@ManyToMany(targetEntity=THostConfigurationInfo.class,mappedBy="networks")
	private Set<THostConfigurationInfo> hosts = new HashSet<>();
	@ManyToMany(targetEntity=TVmConfigurationInfo.class,mappedBy="networks")
	private Set<TVmConfigurationInfo> vms = new HashSet<>();
	
	public int getNetwork_id() {
		return network_id;
	}

	public void setNetwork_id(int network_id) {
		this.network_id = network_id;
	}

	public Set<THostConfigurationInfo> getHosts() {
		return hosts;
	}

	public void setHosts(Set<THostConfigurationInfo> hosts) {
		this.hosts = hosts;
	}

	public Set<TVmConfigurationInfo> getVms() {
		return vms;
	}

	public void setVms(Set<TVmConfigurationInfo> vms) {
		this.vms = vms;
	}

	public void setNetwork_id(Integer network_id) {
		this.network_id = network_id;
	}

	public String getNetwork_name() {
		return network_name;
	}

	public void setNetwork_name(String network_name) {
		this.network_name = network_name;
	}
}
