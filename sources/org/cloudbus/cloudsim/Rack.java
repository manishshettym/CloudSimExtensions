package org.cloudbus.cloudsim;

import java.util.List;

public class Rack 
{
	private List<Integer> rackCoords; 
	private String rackName; 
	private int rackId; 
	private int coolingStatus;
	private List<EnhancedHost> rackHostList;
	
	
	
	public Rack(int rackId, List<EnhancedHost> racksHosts, int status)
	{
		this.rackId = rackId;
		this.rackHostList = racksHosts;
		this.rackName ="";
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
		this.rackName = name;
	}
	
	//returns the name of the sector
	public String getName() {
		return rackName;
	}
	
	//returns the sectorId
	public int getRackId() {
		return rackId;
	}
	
	//sets the sectorId
	public void setRackId(int id) {
		rackId = id;
	}
	
	//returns the list of sectorCoords
	public List<Integer> getRackCoords() {
		return rackCoords;
	}
	
	public List<EnhancedHost> getRackHostList()
	{
		return this.rackHostList;
	}

	public int getCoolingStatus() {
		return coolingStatus;
	}

	public void setCoolingStatus(int coolingStatus) {
		this.coolingStatus = coolingStatus;
	}

	public int freePesPerRack() {
		if(rackHostList == null) {
			return -1;
		}
		int freePes = 0;
		for(EnhancedHost host: rackHostList)
		{
			freePes += host.getNumberOfFreePes();
		}
		return freePes;
	}
	
	public int freeRamPerRack() {
		if(rackHostList == null) {
			return -1;
		}
		int ramAvail = 0;
		for(EnhancedHost host : rackHostList) {
			ramAvail += host.getRamProvisioner().getAvailableRam();
		}
		return ramAvail;
	}
	
	public double freeMipsPerRack() {
		if(rackHostList == null) {
			return -1;
		}
		double freeMips = 0;
		for(EnhancedHost host : rackHostList) {
			freeMips += host.getAvailableMips();
		}
		return freeMips;
	}
	//naive fitness function
	public double fitness() {
		double result = 0;
		int freePes = this.freePesPerSector();
		int ramAvail = this.freeRamPerSector();
		double freeMips = this.freeMipsPerSector();
		result = 1 / (freePes + ramAvail + freeMips + coolingStatus);
		return result; //lower the fitness value,better the fit
	}

    public double coolFitness() {
		double result = 0;
		double maxPower = 0.0;
		double sectorCooling = 0.0;
		double timeDiff = 4.20;
		if(this.coolingStatus == 1) {
			for(EnhancedHost host : sectorHostList) {
				sectorCooling += host.getEnergyLinearInterpolation(1, 1, timeDiff); //needs fix
			}
			sectorCooling = 1.33 * sectorCooling;
		}
		/*
		 * getting the max power consumed by all hosts in that sector
		 * we would want to minimize this
		 * this is useful for datacenters with heterogeneous hosts
		 * this way we chose the sector which would consume the least power
		 */
		for(EnhancedHost host : sectorHostList) {
			maxPower += host.getMaxPower();
		}
		maxPower = 1 / maxPower;
		result = result + 1 / (maxPower + sectorCooling);
		return result;
	}

	public boolean canSupportVm(Vm vm) {
		if((this.freeMipsPerSector() >= vm.getCurrentRequestedMaxMips()) &&(this.freeRamPerSector() >= vm.getCurrentRequestedRam()) && (this.freePesPerSector() >= vm.getNumberOfPes())) {
			return true;
		}
		return false;
	}
}
