package org.cloudbus.cloudsim.examples.power.planetlab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Aisle;
import org.cloudbus.cloudsim.DatacenterBroker;
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
	int [] failurezones = new int[6];
	
	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;
	
	
	private Map<String, Sector> vmSectorTable =  new HashMap<String, Sector>();;
	private Map<String, Aisle> vmAisleTable =  new HashMap<String, Aisle>();;
	private Map<String, Rack> vmRackTable =  new HashMap<String, Rack>();;
	
	private Map<String, EnhancedHost> vmTable = new HashMap<String, EnhancedHost>();
	
	
	public FzonesVmAllocationPolicy(List<? extends Host> hostList,List<? extends Rack> rackList,List<? extends Aisle> aisleList ,List<? extends Sector> sectorList , int []failurezones) {
		super(hostList); 
		this.rackList = rackList;
		this.aisleList = aisleList;
		this.sectorList = sectorList;
		this.failurezones = failurezones;
		
		setFreePes(new ArrayList<Integer>());
		for(Sector sector: sectorList)
		{
			for(Aisle aisle: sector.getSectorAisleList())
			{
				for(Rack rack : aisle.getAisleRackList())
				{
					for (Host host : rack.getRackHostList()) 
					{
						getFreePes().add(host.getNumberOfPes());

					}
				}
			}
		}
		

		setVmTable(new HashMap<String, EnhancedHost>());
		setUsedPes(new HashMap<String, Integer>());
	}

	@Override
	public boolean allocateHostForVm(Vm vm) 
	{
		return allocateHostForVm(vm, findEnhancedHostForVm(vm));
	}
	

	public EnhancedHost findEnhancedHostForVm(Vm vm)
	{
	
		//Needs changes to be made according to Fzones
		/*
		 * Level 1 : DC failure -> this change has been implemented in createVminDC
		 * Level 2 : Sector -> here
		 * Level 3 : Aisle -> here
		 * Level 4 : Rack -> here
		 * Level 5 : Host -> here
		 * 
		 * Make sure to follow top down prio approach
		 * Keep track of copies
		 * Create copies for all Level >= 2 in single case on ProcessResourceCharacteristics()
		 */
		
		int requiredPes = vm.getNumberOfPes();
		
		EnhancedHost host = null;
		
		Log.printLine("Vm "+ vm.getId() + " requires " + requiredPes + " Pes");
		boolean result = false;
		int tries = 0;
		
		List<Integer> freePesTmp = new ArrayList<Integer>();
		for (Integer freePes : getFreePes()) {
			freePesTmp.add(freePes);
		}
		
		
		Log.printLine("Free Pes are : " + freePesTmp + " Size " + freePesTmp.size() );
		
		//Level 2 check:
		if(failurezones[2]==1)
		{
			Sector sector = null;
			do //choose a sector
			{	
				int secidx =0;
				int vid = vm.getId();
				ArrayList<ArrayList <Vm>> vcopy = (ArrayList) DatacenterBroker.vmcopies;
				int original = vcopy.get(0).size();
				
				if(original<=vid)
				{
					if(original==vid)
					{	
						Vm prev = DatacenterBroker.vmList.get(vid-1);
						secidx = getSector(prev).getSectorId() + 1 ;
					}
					
					else
					{
						Vm prev = DatacenterBroker.vmList.get(vid-1);
						secidx = getSector(prev).getSectorId();
						
					}
					
				}
				
				sector = getSectorById(secidx);
				
				
				
				for(Aisle aisle : sector.getSectorAisleList())
				{
					for(Rack r : aisle.getAisleRackList())
					{
						Log.printLine(r.getRackId());
					}
					Log.printLine("___________________________________________");
					for(Rack rack : aisle.getAisleRackList())
					{
						for(EnhancedHost h : rack.getRackHostList())
						{
							Log.printLine(h.getId());
						}
						
						if (!getVmTable().containsKey(vm.getUid())) 
						{ // if this vm was not created
							
								do {
								
									int selectedhostpes;
									int idx= rack.getRackHostList().get(0).getId();
									Log.printLine("IDX:" + idx);
									
									selectedhostpes=freePesTmp.get(idx);
									if(selectedhostpes==0)
									{
										idx++;
										selectedhostpes=freePesTmp.get(idx);
									}
									
									for( EnhancedHost h : rack.getRackHostList())
									{
										if(h.getId() == idx)
										{
											host = h;
											break;
										}
											
									}
									result = host.isSuitableForVm(vm);
									
									
									if (result) 
									{ // if vm were succesfully created in the host
										Log.printLine("Host"+ idx);
										getVmTable().put(vm.getUid(), host);
										getVmSectorTable().put(vm.getUid(), sector);
										getVmAisleTable().put(vm.getUid(), aisle);
										getVmRackTable().put(vm.getUid(), rack);
										getUsedPes().put(vm.getUid(), requiredPes);
										getFreePes().set(idx, getFreePes().get(idx) - requiredPes);
										result = true;
										break;
									} else {
										freePesTmp.set(idx, Integer.MIN_VALUE);
									}
									tries++;
									
								}while(!result && tries < host.getNumberOfFreePes());
							
						}
						
						if(result==true)
						{	
							Log.printLine("Rack:" + rack.getRackId());
							break;
						}
							
					}
					if(result==true)
					{
						Log.printLine("Aisle:" + aisle.getAisleId());
						break;
					}
				}
				
				secidx++;
			}while(!result );
			Log.printLine("Sector :" + sector.getSectorId());
		}
		
		//Level 3 check:
		else if(failurezones[3]==1)
		{
			
		}
				
		//Level 4 check:
		else if(failurezones[4]==1)
		{
			
		}
		
		//Level 5 check:
		else if(failurezones[5]==1)
		{
			
		}
		
		
		return host;
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
	

	public Map<String, EnhancedHost> getVmTable() {
		return vmTable;
	}

	/**
	 * Sets the vm table.
	 * 
	 * @param vmTable the vm table
	 */
	protected void setVmTable(Map<String, EnhancedHost> vmTable) {
		this.vmTable = vmTable;
	}

	/**
	 * Gets the used pes.
	 * 
	 * @return the used pes
	 */
	protected Map<String, Integer> getUsedPes() {
		return usedPes;
	}

	/**
	 * Sets the used pes.
	 * 
	 * @param usedPes the used pes
	 */
	protected void setUsedPes(Map<String, Integer> usedPes) {
		this.usedPes = usedPes;
	}

	/**
	 * Gets the free pes.
	 * 
	 * @return the free pes
	 */
	protected List<Integer> getFreePes() {
		return freePes;
	}

	/**
	 * Sets the free pes.
	 * 
	 * @param freePes the new free pes
	 */
	protected void setFreePes(List<Integer> freePes) {
		this.freePes = freePes;
	}
	
	//Sector Table
	public Map<String, Sector> getVmSectorTable() {
		return vmSectorTable;
	}

	protected void setVmSectorTable(Map<String, Sector> vmSectorTable) {
		this.vmSectorTable = vmSectorTable;
	}
	
	
	//Aisle Table
	public Map<String, Aisle> getVmAisleTable() {
		return vmAisleTable;
	}

	protected void setVmAisleTable(Map<String, Aisle> vmAisleTable) {
		this.vmAisleTable = vmAisleTable;
	}
	
	
	//Rack Table
	public Map<String, Rack> getVmRackTable() {
		return vmRackTable;
	}

	protected void setVmRackTable(Map<String, Rack> vmRackTable) {
		this.vmRackTable = vmRackTable;
	}
	
	
	//Get the sector,aisle,rack,host using vm :
	public Host getHost(Vm vm) {
		return getVmTable().get(vm.getUid());
	}
	
	public Sector getSector(Vm vm) {
		return getVmSectorTable().get(vm.getUid());
	}
	
	public Aisle getAisle(Vm vm) {
		return getVmAisleTable().get(vm.getUid());
	}
	
	public Rack getRack(Vm vm) {
		return getVmRackTable().get(vm.getUid());
	}
	
	
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) {
		// This policy does not optimize the VM allocation
		return null;
	}

}

