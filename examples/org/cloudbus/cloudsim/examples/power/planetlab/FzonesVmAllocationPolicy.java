package org.cloudbus.cloudsim.examples.power.planetlab;

import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Aisle;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Rack;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.SectorList;
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

public class FzonesVmAllocationPolicy extends PowerVmAllocationPolicyAbstract
{
	protected List<? extends Sector> sectorList;
	protected List<?extends Aisle> aisleList;
	protected List<?extends Rack> rackList;
	protected List<?extends EnhancedHost> hostList;
	int [] failurezones = new int[12];
	
	
	
	public FzonesVmAllocationPolicy(List<? extends Host> hostList,List<? extends Rack> rackList,List<? extends Aisle> aisleList ,List<? extends Sector> sectorList , int []failurezones) {
		super(hostList); 
		this.rackList = rackList;
		this.aisleList = aisleList;
		this.sectorList = sectorList;
		this.failurezones = failurezones;
	}

	@Override
	public boolean allocateHostForVm(Vm vm) 
	{
		return allocateHostForVm(vm, findEnhancedHostForVm(vm));
	}
	

	public EnhancedHost findEnhancedHostForVm(Vm vm)
	{
		
		EnhancedHost toReturn = null;
		Sector sector = SectorList.get
		
		Sector sector =  SectorList.getSectorById(sectorList,getCooledSectorWithLeastFreePes(vm.getNumberOfPes()));
		

	
		if(  sector != null) 
		{
			for (EnhancedHost host :sector.getSectorHostList() ){
			if (host.isSuitableForVm(vm)) {
					toReturn =  (EnhancedHost) host;
			}
			}
		}

		
		// couldn't find a host in a cooled sector, find elsewhere
		//else
		//{
			for (Host host :this.getHostList() ){
				if (host.isSuitableForVm(vm)) {
					toReturn =  (EnhancedHost) host;
				}
			}
			if(toReturn==null) {return null;}
			sector  = getSectorById(toReturn.getSectorId());
			if(sector != null && sector.getCoolingStatus() == 0)
			{
				sector.setCoolingStatus(1);
			}
		//}
		return toReturn;
	}
	
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
	
	public int getCooledSectorWithLeastFreePes(int minNeededPes)
	{
		int minPesSectorId=-1;
		int minPes = 1000000;
		
		for(Sector sector : sectorList) 
		{
			int sectorPes = getFreePes(sector.getSectorId()); 
			if(sector.getCoolingStatus() == 1 && sectorPes < minPes && sectorPes > minNeededPes)
			{
				for(EnhancedHost host : sector.getSectorHostList()) {
					if(host.getNumberOfFreePes() >= minNeededPes)
					{
						minPesSectorId = sector.getSectorId();
						minPes = getFreePes(sector.getSectorId());
						break;
					}
						
				}
			}
		}
		return minPesSectorId;
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

