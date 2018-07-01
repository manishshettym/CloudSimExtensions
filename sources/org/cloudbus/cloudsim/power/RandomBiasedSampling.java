package org.cloudbus.cloudsim.power;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.SectorList;

//Each sector has a walk in length value(WIL) which is being used as its id
//Each vm is allocated a WIL value
//Sector is picked for a vm if the vm's WIL value >= sector's WIL
//If no host is found within a sector to be assigned to the vm, 
//then the vm's WIL value is increased by 1
public class RandomBiasedSampling extends PowerVmAllocationPolicyAbstract
{
	protected List<? extends Sector> sectorList;
	protected List<? extends Vm> VmList; 
	private int vmWIL[];
	private int sectorIds[];
	
	public RandomBiasedSampling(List<? extends Host> list, List<? extends Sector> sectorList,List<? extends Vm> VmList)
	{ 
		super(list);
		this.sectorList = sectorList;
		this.VmList = VmList;
		vmWIL = new int[this.VmList.size()];
		sectorIds = new int[this.sectorList.size()];
		int i = 0;
		for(Sector sector : this.sectorList)
		{
			sectorIds[i] = sector.getSectorId();
			i++;
		}
		Random rand = new Random();
		for( Vm vm : VmList)
		{
			i = rand.nextInt(sectorList.size());
			vmWIL[vm.getId()] = sectorIds[i];
		}
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		return allocateHostForVm(vm, findEnhancedHostForVm(vm));
	}
	
	@SuppressWarnings("unused")
	public EnhancedHost findEnhancedHostForVmWrapped(Vm vm)
	{
		EnhancedHost toReturn = null;
		for( Sector sector : sectorList)
		{
			if( vmWIL[vm.getId()] == sector.getSectorId())
			{
					if(sector.getCoolingStatus() == 0)
					{
						sector.setCoolingStatus(1);
					}
					List<EnhancedHost> hosts = getHostsBySector(sector.getSectorId());
					for (EnhancedHost host : hosts )
					{
						if (host.isSuitableForVm(vm)) 
						{
							toReturn =  (EnhancedHost) host;
						}
					}
			}
		}
		return toReturn;
	}
	
	@SuppressWarnings("unused")
	public EnhancedHost findEnhancedHostForVm(Vm vm)
	{
		EnhancedHost toReturn = null;
		toReturn = findEnhancedHostForVmWrapped(vm);
		while( toReturn == null)
		{
			vmWIL[vm.getId()]--;
			toReturn = findEnhancedHostForVmWrapped(vm);
		}
		return toReturn; 
	}
	
	
	public boolean allocateHostForVm(Vm vm, EnhancedHost host)
	{
		if (host == null) {
			/*Log.formatLine("%.2f: No suitable host found for VM #" + vm.getId() + "\n", CloudSim.clock());
			return false;*/
		}
		if (host.vmCreate(vm)) { // if vm has been succesfully created in the host
			getVmTable().put(vm.getUid(), host);
			Log.formatLine(
					"%.2f: VM #" + vm.getId() + " has been allocated to the host #" + host.getId(),
					CloudSim.clock());
			return true;
		}
		/*Log.formatLine(
				"%.2f: Creation of VM #" + vm.getId() + " on the host #" + host.getId() + " failed\n",
				CloudSim.clock());*/
		return false;
	}
	
	//gets all hosts within a sector
	public List<EnhancedHost> getHostsBySector(int sectorId)
	{
		for(Sector sec : sectorList)
		{
			if(sec.getSectorId() == sectorId)
			{
				return sec.getSectorHostList();
			}
		}
		return null;
	}
	
	public int getFreePes(int sectorId)
	{
		List<EnhancedHost> hosts = getHostsBySector(sectorId);
		if(hosts == null) {
			return 0;
		}
		int freePes = 0;
		for(EnhancedHost host: hosts)
		{
			freePes += host.getNumberOfFreePes();
		}
		return freePes;
	}	
	
	public Sector getSectorById(int id)
	{
		for(Sector sec : this.sectorList)
		{
			if(sec.getSectorId() == id)
			{
				return sec;
			}
		}
		return null;
	}
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// This policy does not optimize the VM allocation
		return null;
	}
}
