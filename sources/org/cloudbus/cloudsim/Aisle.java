package org.cloudbus.cloudsim;

import java.util.List;

public class Aisle 
{
	private List<Integer> aisleCoords; 
	private String aisleName; 
	private int aisleId; 
	private int coolingStatus;
	private List<Rack> aisleRackList;
	public int sector;
	
	private Datacenter datacenter;
	
	
	
	public Aisle(int aisleId, List<Rack> aisleRacks, int status)
	{
		this.aisleId = aisleId;
		this.aisleRackList = aisleRacks;
		this.aisleName ="";
		setCoolingStatus(0);
		
	}
	
	//copy of aisle
	public Aisle(Aisle ai)
	{   
		
		this.aisleId = ai.aisleId;
		this.aisleRackList = ai.getAisleRackList();
		this.aisleName ="";
		setCoolingStatus(0);
		
	}
	
	
	/*public Sector(int sectorId,int c1_aisle,
			int c1_rack,
			int c2_aisle,
			int c2_rack
			) {
		this.sectorId = sectorId;
		sectorCoords = new ArrayList<Integer>();
		sectorCoords.add(c1_aisle);
		sectorCoords.add(c1_rack);
		sectorCoords.add(c2_aisle);
		sectorCoords.add(c2_rack);
		setCoolingStatus(0);
	}*/
	
	//sets the name of the sector
	public void setName(String name) {
		this.aisleName = name;
	}
	
	//returns the name of the sector
	public String getName() {
		return aisleName;
	}
	
	//returns the sectorId
	public int getAisleId() {
		return aisleId;
	}
	
	//sets the sectorId
	public int getSector() {
		return sector;
	}
	public void setSector(int sector) {
		this.sector = sector;
	}
	
	//set datacenter
	public void setDatacenter(Datacenter datacenter)
	{
		this.datacenter = datacenter;
	}
		
	//get datacenter
	public Datacenter getDatacenter()
	{
		return datacenter;
	}
	
	
	//returns the list of sectorCoords
	public List<Integer> getAisleCoords() {
		return aisleCoords;
	}
	
	public List<Rack> getAisleRackList()
	{
		return this.aisleRackList;
	}

	public int getCoolingStatus() {
		return coolingStatus;
	}

	public void setCoolingStatus(int coolingStatus) {
		this.coolingStatus = coolingStatus;
	}

	public int freePesPerAisle() {
		if(aisleRackList == null) 
		{
			return -1;
		}
		int freePes = 0;
		
		for(Rack rack : aisleRackList) 
		{
			for(EnhancedHost host : rack.getRackHostList()) 
			{
				freePes += host.getNumberOfFreePes();
			}
		}

		return freePes;
	}
	
	public int freeRamPerAisle() 
	{
		if(aisleRackList == null) 
		{
			return -1;
		}
		int freeRam = 0;
		
		for(Rack rack : aisleRackList) 
		{
			for(EnhancedHost host : rack.getRackHostList()) 
			{
				freeRam += host.getRamProvisioner().getAvailableRam();
			}
		}

		return freeRam;
	}
	
	public double freeMipsPerAisle() 
	{
		if(aisleRackList == null) 
		{
			return -1;
		}
		int freeMips = 0;
		
		for(Rack rack : aisleRackList) 
		{
			for(EnhancedHost host : rack.getRackHostList()) 
			{
				freeMips += host.getAvailableMips();
			}
		}

		return freeMips;
	}
	
	//naive fitness function
	public double fitness() {
		double result = 0;
		int freePes = this.freePesPerAisle();
		int ramAvail = this.freeRamPerAisle();
		double freeMips = this.freeMipsPerAisle();
		result = 1 / (freePes + ramAvail + freeMips + coolingStatus);
		return result; //lower the fitness value,better the fit
	}

    public double coolFitness() {
		double result = 0;
		double maxPower = 0.0;
		double rackCooling = 0.0;
		double timeDiff = 4.20;
		if(this.coolingStatus == 1) {
		
			for(Rack rack: getAisleRackList())
			{
				for(EnhancedHost host : rack.getRackHostList()) 
				{
					rackCooling += host.getEnergyLinearInterpolation(1, 1, timeDiff); //needs fix
				}
			}
			
			rackCooling = 1.33 * rackCooling;
		}
		/*
		 * getting the max power consumed by all hosts in that rack
		 * we would want to minimize this
		 * this is useful for datacenters with heterogeneous hosts
		 * this way we chose the sector which would consume the least power
		 * */
		for(Rack rack: getAisleRackList())
		{
			for(EnhancedHost host : rack.getRackHostList()) {
				maxPower += host.getMaxPower();
			}
		}
		maxPower = 1 / maxPower;
		result = result + 1 / (maxPower + rackCooling);
		return result;
	}

	
	
}
