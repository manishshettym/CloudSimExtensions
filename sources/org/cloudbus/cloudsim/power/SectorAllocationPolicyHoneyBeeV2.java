package org.cloudbus.cloudsim.power;

import java.util.HashMap;
import java.util.List;

import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.SectorList;

public class SectorAllocationPolicyHoneyBeeV2 extends SectorAllocationPolicyHoneyBee {	
		
	public SectorAllocationPolicyHoneyBeeV2(List<? extends Host> list, List<? extends Sector> sectorList,List<? extends Vm> vmList) {		
		super(list,sectorList,vmList);
	}
	
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
				fitnessMap.put(sector.getSectorId(), sector.coolFitness());
			}

			fractionScheduled = (int)0.2 * super.vmsToSchedule; //20% of available VMs go into the sector that will be chosen
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
			/*
			 * Its important to note that the sector that gets chosen fulfills a condition set by the fitness function
			 * In the simple fitness function, the sector that gets chosen, is the one with the most free resources available 
			 * and happens to be cooled. Depending on your requirement, you could change the fitness function defined in Sector class
			 * to fit your scheduling requirement
			 */
			quotaRemaining = true;
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
	
}
