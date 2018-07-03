package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.SectorList;

public class SectorAllocationPolicyHoneyBee extends PowerVmAllocationPolicyAbstract{
	
	protected List<? extends Sector> sectorList;
	protected List<? extends Vm> vmList;
	protected HashMap<Integer,Double> fitnessMap = null;
	protected int vmsToSchedule;
	protected int fractionScheduled;
	protected Sector sectorChosen;
	protected boolean quotaRemaining = false;
	protected int index = 0;
	
	protected Integer getFitSectorId() {
		double min = Double.MAX_VALUE;
		int minKey = -1;
		for(Entry<Integer,Double> entry : fitnessMap.entrySet()) {
			if(entry.getValue() < min) {
				min = entry.getValue();
				minKey = entry.getKey();
			}
		}
		return minKey;
	}
	
	//constructor takes three arguments, host list, sector list and the VM list
	public SectorAllocationPolicyHoneyBee(List<? extends Host> list, List<? extends Sector> sectorList,List<? extends Vm> vmList)
	{	
		super(list);
		this.sectorList = sectorList;
		this.vmList = vmList;
		vmsToSchedule = this.vmList.size();
	}
	
	//allocates a host for a VM
	//takes two arguments: reference to the required VM and the host to allocate on
	public boolean allocateHostForVm(Vm vm, EnhancedHost host)
	{
		if (host == null) {
			Log.formatLine("%.2f: No suitable host found for VM #" + vm.getId() + "\n", CloudSim.clock());
			return false;
		}
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}
		Log.formatLine(
				"%.2f: Creation of VM #" + vm.getId() + " on the host #" + host.getId() + " failed\n",
				CloudSim.clock());
		return false;
	}
	
	//allocates a VM
	public boolean allocateHostForVm(Vm vm) {
		return allocateHostForVm(vm, findEnhancedHostForVm(vm));
	}
	
	//find a host for the VM
	public EnhancedHost findEnhancedHostForVm(Vm vm) {
		EnhancedHost toReturn = null;
		//self regulating policy
		//possible revision: this is a static approach
		/*
		* The scheduling process iterates until all the VMs have been scheduled
		* In each iteration, we are updating the fitness values of all the food sources(aka sectors)
		* once we have identified the sector for that particular iteration
		* we go ahead and allocate the VMs among the hosts present in that sector.
		*/
		if(!quotaRemaining || ((sectorChosen != null) && !(sectorChosen.canSupportVm(vm)))) {
			index = 0;
			fitnessMap = new HashMap<Integer,Double>(); 
			for(Sector sector : sectorList) {
				fitnessMap.put(sector.getSectorId(), sector.fitness());
			}

			fractionScheduled = (int)0.6 * vmsToSchedule; //60% of available VMs go into the sector that will be chosen
			sectorChosen = SectorList.getSectorById(sectorList, getFitSectorId()); //selects the sector to schedule the VMs inside.
			/*
			 * Its important to note that the sector that gets chosen fulfills a condition set by the fitness function
			 * In the simple fitness function, the sector that gets chosen, is the one with the most free resources available 
			 * and happens to be cooled. Depending on your requirement, you could change the fitness function defined in Sector class
			 * to fit your scheduling requirement
			 */
			
			quotaRemaining = true;
		}
		
		//once the sector is chosen, we need to schedule among the hosts.
		if(sectorChosen != null) 
		{
			for (EnhancedHost host :sectorChosen.getSectorHostList() ){
				if (host.isSuitableForVm(vm)) {
					toReturn =  (EnhancedHost) host;
				}
			}
		}
		//NOT SURE IF THE FOLLOWING PART NEEDS TO BE THERE
		// couldn't find a host in a cooled sector, find elsewhere
		if(toReturn == null) {
			for (Host host :this.getHostList() ){
				if (host.isSuitableForVm(vm)) {
					toReturn =  (EnhancedHost) host;
				}
			}
			sectorChosen  = SectorList.getSectorById(sectorList,toReturn.getSectorId());
			if(sectorChosen != null && sectorChosen.getCoolingStatus() == 0)
			{
				sectorChosen.setCoolingStatus(1);
			}
		}
		//updates the member attributes of the scheduling policy
		vmsToSchedule -=1;
		index += 1;
		//checks if all the VMs within the fraction have been sent to the sector, if it has
		// then it will begin a new iteration when the next VM comes in to be scheduled.
		if(index == fractionScheduled) {
			quotaRemaining = false;
		}
		return toReturn;
	}
	
	//POSSIBLE REVISION
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// This policy does not optimize the VM allocation
		return null;
	}
}
