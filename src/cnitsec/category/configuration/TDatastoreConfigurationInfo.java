package cnitsec.category.configuration;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;



@Entity
@Table(name = "t_datastore_configuration_info")
public class TDatastoreConfigurationInfo {
	@Id
	@GenericGenerator(name="my" , strategy="increment")
	//@GenericGenerator是hibernate的自定义主键生成器
	//@GeneratedValue标识这个变量由某个生成器自己处理
	//@Parameter是参数的意思,应该是这个生成器的方法需要输入参数,name是参数名右边是值
	@GeneratedValue(generator="my")
	private int datastore_id;
	private String datastore_name;
	private Long freeSpace;
	@Temporal(TemporalType.TIMESTAMP)
	private Date timestamp;
	private String url;
	private Long maxFileSize;
	private Long capacity;
//	private Boolean accessible;
	private String type;
	private Long uncommitted;
	
	@ManyToMany(targetEntity=THostConfigurationInfo.class,mappedBy="datastores")
	private Set<THostConfigurationInfo> hosts = new HashSet<>();
	
	@ManyToMany(targetEntity=TVmConfigurationInfo.class,mappedBy="datastores")
	private Set<TVmConfigurationInfo> vms = new HashSet<>();
	
	
	public Long getFreeSpace() {
		return freeSpace;
	}

	public void setFreeSpace(Long freeSpace) {
		this.freeSpace = freeSpace;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(Long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public Long getCapacity() {
		return capacity;
	}

	public void setCapacity(Long capacity) {
		this.capacity = capacity;
	}

/*	public Boolean getAccessible() {
		return accessible;
	}

	public void setAccessible(Boolean accessible) {
		this.accessible = accessible;
	}
*/
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Long getUncommitted() {
		return uncommitted;
	}

	public void setUncommitted(Long uncommitted) {
		this.uncommitted = uncommitted;
	}

	public int getDatastore_id() {
		return datastore_id;
	}

	public void setDatastore_id(int datastore_id) {
		this.datastore_id = datastore_id;
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

	public String getDatastore_name() {
		return datastore_name;
	}

	public void setDatastore_name(String datastore_name) {
		this.datastore_name = datastore_name;
	}
}
