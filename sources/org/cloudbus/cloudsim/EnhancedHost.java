package org.cloudbus.cloudsim;

import java.util.List;

import org.cloudbus.cloudsim.power.PowerHostUtilizationHistory;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/*This class represents an Host in a datacenter that is power aware
 * and physically aware(topological awareness)
 * EnhancedHost extends PowerHostUtilizationHistory(remembers utilization)
 * This in turn extends PowerHost(incorporates a model for power usage)
 * This in turn extends HostDynamicWorkload(supports dynamic workload)
 * This is finally extended from the Host class.
 */

public class EnhancedHost extends PowerHostUtilizationHistory{
	private int rack;
	private int aisle;
	private int sectorId;
	
	//constructor to create copy of an existing host
	public EnhancedHost(EnhancedHost host)
	{
		super(host.getId(),host.getRamProvisioner(),host.getBwProvisioner(),host.getStorage(),host.getPeList(),host.getVmScheduler(),host.getPowerModel());
		this.aisle = host.aisle;
		this.rack = host.rack;
		
	}
	
	
	public EnhancedHost(int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel
			) {
		super(id,ramProvisioner,bwProvisioner,storage,peList,vmScheduler,powerModel);
		this.rack = 0;
		this.aisle = 0;
	}
	public EnhancedHost(int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel,
			int rack,
			int aisle) {
		super(id,ramProvisioner,bwProvisioner,storage,peList,vmScheduler,powerModel);
		this.rack = rack;
		this.aisle = aisle;
	}
	
	public EnhancedHost(int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel,
			int SectorId) {
		super(id,ramProvisioner,bwProvisioner,storage,peList,vmScheduler,powerModel);
		this.setSectorId(SectorId);
	}
	
	/*
	 * Getter and setter methods for rack and aisle
	 * Other methods are obtained from the parent class.
	 */
	public int getRack() {
		return this.rack;
	}
	public int getAisle() {
		return this.aisle;
	}
	public void setRack(int rack) {
		this.rack = rack;
	}
	public void setAisle(int aisle) {
		this.aisle = aisle;
	}
	public int getSectorId() {
		return sectorId;
	}
	public void setSectorId(int sectorId) {
		this.sectorId = sectorId;
	}
}
