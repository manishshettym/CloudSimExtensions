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
import org.cloudbus.cloudsim.power.PowerVmAllocationPolicyAbstract;

public class FzonesVmAllocationPolicy extends PowerVmAllocationPolicyAbstract
{
	protected List<? extends Sector> sectorList;
	protected List<?extends Aisle> aisleList;
	protected List<?extends Rack> rackList;
	//protected List<?extends EnhancedHost> hostList;
	int [] failurezones = new int[6];
	
	/** The used pes. */
	private Map<String, Integer> usedPes;

	/** The free pes. */
	private List<Integer> freePes;
	
	
	private Map<String, Sector> vmSectorTable =  new HashMap<String, Sector>();;
	private Map<String, Aisle> vmAisleTable =  new HashMap<String, Aisle>();;
	private Map<String, Rack> vmRackTable =  new HashMap<String, Rack>();;
	
	private Map<String, EnhancedHost> vmTable = new HashMap<String, EnhancedHost>();
	
	
	public FzonesVmAllocationPolicy(List<? extends EnhancedHost> hostList,List<? extends Rack> rackList,List<? extends Aisle> aisleList ,List<? extends Sector> sectorList , int []failurezones) {
		super(hostList); 
		//this.hostList = hostList;
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
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public EnhancedHost findEnhancedHostForVm(Vm vm)
	{
		Log.printLine("HIIIIIIIIIII");
	
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
		
		
// CHECKING THE APPROPRIATE FAILURE ZONE CASE 
		
		//Level 2 check:
		if(failurezones[2]==1)
		{
			Sector sector = null;
			int secidx =0;
			do //choose a sector
			{	
				
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
						
						if(getFreePesInSector(getSectorById(secidx)) == 0 ) //ch
						{
							secidx++;
						}
						
					}
					
				}
				
				sector = getSectorById(secidx);
				secidx++;
				
				
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
								int idx= rack.getRackHostList().get(0).getId(); //ch
								do {
								
									int selectedhostpes;
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
									
									Log.printLine("host pes: " + host.getNumberOfFreePes() + " vm pes " + vm.getNumberOfPes() );
									boolean r1 = (getFreePesInHost(host)==0)? false : true; //ch
									result = host.isSuitableForVm(vm) && r1 ;  //ch
									
					
									
									if (result) 
									{ // if vm were succesfully created in the host
										Log.printLine("Host"+ idx);
										
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
				
				
			}while(!result);
			if(result==true)
			{	
				Log.printLine("Sector :" + sector.getSectorId());
			}
		}
		
		//Level 3 check:
		else if(failurezones[3]==1)
		{
			for(Sector sector: sectorList)
			{	
				for(Aisle aisle : sector.getSectorAisleList())
				{
					Log.printLine("AISLE"+ aisle.getAisleId());
				}
				
				int aisleidx = sector.getSectorAisleList().get(0).getAisleId();
				Aisle aisle = null;
				do
				{
					
					int vid = vm.getId();
					ArrayList<ArrayList <Vm>> vcopy = (ArrayList) DatacenterBroker.vmcopies;
					int original = vcopy.get(0).size();
					
					if(original<=vid)
					{
						if(original==vid)
						{
						
							Vm prev = DatacenterBroker.vmList.get(vid-1);
							aisleidx = getAisle(prev).getAisleId() + 1 ;
							if(!sector.getSectorAisleList().contains(getAisleById(aisleidx)))
							{
								sector = sectorList.get(sector.getSectorId()+1);
							}
							
						}
						
						else
						{
							Vm prev = DatacenterBroker.vmList.get(vid-1);
							aisleidx = getAisle(prev).getAisleId();
							
							
							if(getFreePesInAisle(getAisleById(aisleidx)) == 0)
							{
								aisleidx++;
							}
							if(!sector.getSectorAisleList().contains(getAisleById(aisleidx)))
							{
								sector = sectorList.get(sector.getSectorId()+1);
							}
							
						}
						
					}
					
					aisle = getAisleById(aisleidx);
					aisleidx++;	
					
				
					
					for(Rack rack : aisle.getAisleRackList())
					{
						/*for(EnhancedHost h : rack.getRackHostList())
						{
							Log.printLine(h.getId());
						}*/
						
						if (!getVmTable().containsKey(vm.getUid())) 
						{ // if this vm was not created
							int idx= rack.getRackHostList().get(0).getId();
								do {
								
									int selectedhostpes;
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
									
									boolean r1 = (getFreePesInHost(host)==0)? false : true; //ch
									result = host.isSuitableForVm(vm) && r1 ;  //ch
									
									if (result) 
									{ // if vm were succesfully created in the host
										Log.printLine("Host"+ idx);
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
					
				
				} while(!result);
				if(result==true)
				{
					Log.printLine("Sector:" + sector.getSectorId());
					break;
				}
			}
			
		}
				
		//Level 4 check:
		else if(failurezones[4]==1)
		{
			
			for(Sector sector : sectorList)
			{
				for(Aisle aisle : sector.getSectorAisleList())
				{	
					int rackidx =aisle.getAisleRackList().get(0).getRackId();
					Rack rack = null;
					do //choose a rack
					{	
						
						int vid = vm.getId();
						ArrayList<ArrayList <Vm>> vcopy = (ArrayList) DatacenterBroker.vmcopies;
						int original = vcopy.get(0).size();
						
						if(original<=vid)
						{
							if(original==vid)
							{	
								Vm prev = DatacenterBroker.vmList.get(vid-1);
								rackidx = getRack(prev).getRackId() + 1 ;
								
								if(!aisle.getAisleRackList().contains(getRackById(rackidx)))
								{
									aisle = aisleList.get(aisle.getAisleId()+1);
								}
								
								if(!sector.getSectorAisleList().contains(aisle))
								{
									sector=sectorList.get(sector.getSectorId()+1);
								}
								
							}
							
							else
							{
								Vm prev = DatacenterBroker.vmList.get(vid-1);
								rackidx = getRack(prev).getRackId();
								
								if(getFreePesInRack(getRackById(rackidx)) == 0)
								{
									rackidx++;
								}
								if(!aisle.getAisleRackList().contains(getRackById(rackidx)))
								{
									aisle = aisleList.get(aisle.getAisleId()+1);
								}
								
								if(!sector.getSectorAisleList().contains(aisle))
								{
									sector=sectorList.get(sector.getSectorId()+1);
								}
								
								
								
								
							}
							
						}
						
						rack = getRackById(rackidx);

						if(!aisle.getAisleRackList().contains(getRackById(rackidx)))
						{
							aisle = aisleList.get(aisle.getAisleId()+1);
						}
						
						if(!sector.getSectorAisleList().contains(aisle))
						{
							sector=sectorList.get(sector.getSectorId()+1);
						}
						rackidx++;
						
						
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
									boolean r1 = (getFreePesInHost(host)==0)? false : true; //ch
									result = host.isSuitableForVm(vm) && r1 ; 
									
									
									if (result) 
									{ // if vm were succesfully created in the host
										Log.printLine("Host:"+ idx);
										
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
						
					}while(!result);
					if(result==true)
					{
						Log.printLine("Aisle:" + aisle.getAisleId());
						break;
					}
					
				}
				if(result==true)
				{
					Log.printLine("Sector:" + sector.getSectorId());
					break;
				}
			}
			
		}
		
			
		
			
		//Level 5 check:
		else if(failurezones[5]==1)
		{
			Log.printLine("HIIIIIIIIIII");
			
			for(Sector sector : sectorList)
			{
				for(Aisle aisle : sector.getSectorAisleList())
				{
					for(Rack rack: aisle.getAisleRackList())
					{
						
					
					do //choose a host
					{	
						int hostidx =0;
						int vid = vm.getId();
						ArrayList<ArrayList <Vm>> vcopy = (ArrayList) DatacenterBroker.vmcopies;
						int original = vcopy.get(0).size();
						
						if(original<=vid)
						{
							if(original==vid)
							{	
								Vm prev = DatacenterBroker.vmList.get(vid-1);
								hostidx = getHost(prev).getId() + 1 ;
							}
							
							else
							{
								Vm prev = DatacenterBroker.vmList.get(vid-1);
								hostidx = getHost(prev).getId();
								
							}
							
						}
						
						host = getHostById(hostidx);
						tries++;
						
						
						if (!getVmTable().containsKey(vm.getUid())) 
						{ // if this vm was not created
							
								
									result = host.isSuitableForVm(vm);
									
									
									if (result) 
									{ // if vm were succesfully created in the host
										Log.printLine("Host:"+ hostidx);
										getVmTable().put(vm.getUid(), host);
										getVmSectorTable().put(vm.getUid(), sector);
										getVmAisleTable().put(vm.getUid(), aisle);
										getVmRackTable().put(vm.getUid(), rack);
										getUsedPes().put(vm.getUid(), requiredPes);
										getFreePes().set(hostidx, getFreePes().get(hostidx) - requiredPes);
										result = true;
										break;
									} else {
										freePesTmp.set(hostidx, Integer.MIN_VALUE);
									}
									
						}
							
						
						
					hostidx++;		
					}while(!result  && tries < host.getNumberOfFreePes());
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
			if(result==true)
			{
				Log.printLine("Sector:" + sector.getSectorId());
				break;
			}
		
		}
		
		
		return host;
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
			
			//other tables add here
			
			
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
			int sectorPes = sector.freePesPerSector() ;
			if(sector.getCoolingStatus() == 1 && sectorPes < minPes && sectorPes > minNeededPes)
			{
				
				for(Aisle aisle : sector.getSectorAisleList())
				{
					for(Rack rack : aisle.getAisleRackList())
					{
						for(EnhancedHost host : rack.getRackHostList()) {
							if(host.getNumberOfFreePes() >= minNeededPes)
							{
								minPesSectorId = sector.getSectorId();
								minPes = sector.freePesPerSector();
								break;
							}
								
						}
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
	
	public Aisle getAisleById(int id)
	{
		for(Aisle a : this.aisleList)
		{
			if(a.getAisleId() == id)
			{
				return a;
			}
		}
		return null;
	}
	
	
	public Rack getRackById(int id)
	{
		for(Rack r : this.rackList)
		{
			if(r.getRackId() == id)
			{
				return r;
			}
		}
		return null;
	}
	
	public EnhancedHost getHostById(int id)
	{
		for(EnhancedHost h : hostList)
		{
			if(h.getId() == id)
			{
				return h;
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
	
	
	//Getting free pes in entities
	protected int getFreePesInSector(Sector sector) 
	{
		int totalfree=0;
		
		for(Aisle  aisle : sector.getSectorAisleList())
		{
			for(Rack rack : aisle.getAisleRackList())
			{
				for(Host host : rack.getRackHostList())
				{
					totalfree += getFreePes().get(host.getId());
				}
			}
		}
		
		return totalfree;
	}
	
	protected int getFreePesInAisle(Aisle aisle) 
	{
		int totalfree=0;
		
			for(Rack rack : aisle.getAisleRackList())
			{
				for(Host host : rack.getRackHostList())
				{
					totalfree += getFreePes().get(host.getId());
				}
			}
		
		return totalfree;
	}
	
	protected int getFreePesInRack(Rack rack) 
	{
		int totalfree=0;
		
			
				for(Host host : rack.getRackHostList())
				{
					totalfree += getFreePes().get(host.getId());
				}
			
		
		return totalfree;
	}
	
	
	
	public int getFreePesInHost(Host host) 
	{
		int totalfree=0;
		totalfree += getFreePes().get(host.getId());
		return totalfree;
	}
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
	@Override
	public EnhancedHost getHost(Vm vm) {
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

