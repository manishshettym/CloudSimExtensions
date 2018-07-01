package org.cloudbus.cloudsim.power;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Sector;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
public class HillClimbingAlgorithm extends PowerVmAllocationPolicyAbstract
{
	protected List<? extends Sector> sectorList;
	private double mincost = Double.MAX_VALUE;
    private double cost = mincost;
    double currentTime = CloudSim.clock();
    double timeDiff = currentTime - getLastProcessTime();
	private double lastProcessTime;

	public HillClimbingAlgorithm(List<? extends Host> list, List<? extends Sector> sectorList)
    {
		super(list);
		this.sectorList = sectorList;
	}
	
	protected void setLastProcessTime(double lastProcessTime) {
		this.lastProcessTime = lastProcessTime;
	}
	protected double getLastProcessTime() {
		return lastProcessTime;
	}

	@Override
	public boolean allocateHostForVm(Vm vm) {
		return allocateHostForVm(vm, findEnhancedHostForVm(vm));
	}
	
	public EnhancedHost findEnhancedHostForVm(Vm vm)
	{
		EnhancedHost toReturn = null;
		while( toReturn == null)
		{
			Random rand = new Random();
			int i = rand.nextInt(sectorList.size()) + 2;
			for(Sector sector: sectorList)
			{
				i--;
				if(i<5)
				{
					for (EnhancedHost host :sector.getSectorHostList() )
					{
						
						if (host.isSuitableForVm(vm))
						{
							cost = host.getPowerModel().getPower(getMaxUtilizationAfterAllocation(host, vm));
							if(cost<mincost)
								toReturn =  (EnhancedHost) host;
						}
					}
					break;
				}
			}
		}
		return toReturn;
	}

	protected double getMaxUtilizationAfterAllocation(PowerHost host, Vm vm) {
		double requestedTotalMips = vm.getCurrentRequestedTotalMips();
		double hostUtilizationMips = getUtilizationOfCpuMips(host);
		double hostPotentialUtilizationMips = hostUtilizationMips + requestedTotalMips;
		double pePotentialUtilization = hostPotentialUtilizationMips / host.getTotalMips();
		return pePotentialUtilization;
	}
	
	protected double getUtilizationOfCpuMips(PowerHost host) {
		double hostUtilizationMips = 0;
		for (Vm vm2 : host.getVmList()) {
			if (host.getVmsMigratingIn().contains(vm2)) {
				// calculate additional potential CPU usage of a migrating in VM
				hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2) * 0.9 / 0.1;
			}
			hostUtilizationMips += host.getTotalAllocatedMipsForVm(vm2);
		}
		return hostUtilizationMips;
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