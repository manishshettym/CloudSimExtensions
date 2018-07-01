package org.cloudbus.cloudsim;

import java.util.List;


import java.util.ArrayList;

/*Class used to represent a sector/zone in a datacenter
 * The sector is determined by the top left and bottom right positions
 * of its rectangular region, coordinates specified as a list
 * containing aisle_tl,rack_tl,aisle_br,rack_br(tl - top_left,br - bottom_right)
 */
public class Sector {

	private List<Integer> sectorCoords; //list of sector-coords
	private String sectorName; //name of the sector
	private int sectorId; //id of the sector
	private int coolingStatus;
	private List<EnhancedHost> sectorHostList;
	private int failureZoneId;
	public Sector(int sectorId, List<EnhancedHost> sectorHosts, int status)
	{
		this.sectorId = sectorId;
		this.sectorHostList = sectorHosts;
		this.sectorName ="";
		setCoolingStatus(0);
		
	}
	public Sector(int sectorId, List<EnhancedHost> sectorHosts, int status, int failureZoneId)
	{
		this.sectorId = sectorId;
		this.sectorHostList = sectorHosts;
		this.sectorName ="";
		setCoolingStatus(0);
		this.setFailureZoneId(failureZoneId);
	}
	
	
	public Sector(int sectorId,int c1_aisle,
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
	}
	
	//sets the name of the sector
	public void setName(String name) {
		this.sectorName = name;
	}
	
	//returns the name of the sector
	public String getName() {
		return sectorName;
	}
	
	//returns the sectorId
	public int getSectorId() {
		return sectorId;
	}
	
	//sets the sectorId
	public void setSectorId(int id) {
		sectorId = id;
	}
	
	//returns the list of sectorCoords
	public List<Integer> getSectorCoords() {
		return sectorCoords;
	}
	
	public List<EnhancedHost> getSectorHostList()
	{
		return this.sectorHostList;
	}

	public int getCoolingStatus() {
		return coolingStatus;
	}

	public void setCoolingStatus(int coolingStatus) {
		this.coolingStatus = coolingStatus;
	}

	public int freePesPerSector() {
		if(sectorHostList == null) {
			return -1;
		}
		int freePes = 0;
		for(EnhancedHost host: sectorHostList)
		{
			freePes += host.getNumberOfFreePes();
		}
		return freePes;
	}
	
	public int freeRamPerSector() {
		if(sectorHostList == null) {
			return -1;
		}
		int ramAvail = 0;
		for(EnhancedHost host : sectorHostList) {
			ramAvail += host.getRamProvisioner().getAvailableRam();
		}
		return ramAvail;
	}
	
	public double freeMipsPerSector() {
		if(sectorHostList == null) {
			return -1;
		}
		double freeMips = 0;
		for(EnhancedHost host : sectorHostList) {
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
	public int getFailureZoneId() {
		return failureZoneId;
	}
	public void setFailureZoneId(int failureZoneId) {
		this.failureZoneId = failureZoneId;
	}

	
}
