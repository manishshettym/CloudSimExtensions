package org.cloudbus.cloudsim;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.lists.EnhancedHostList;
import org.cloudbus.cloudsim.lists.SectorList;

/*Class is used to represent the characteristics of an Enhanced Datacenter
 * Additions include a list of EnhancedHosts(as opposed to regular hosts)
 * As well a list of sectors in the datacenter
 */

public class EnhancedDatacenterCharacteristics extends DatacenterCharacteristics {
	
	private List<? extends EnhancedHost> hostList;
	private int aisles;
	private int racksPerAisle;
	private int hostsPerRack;

	private List<? extends Sector> sectorList;
	
	public EnhancedDatacenterCharacteristics(
			String architecture,
			String os,
			String vmm,
			List<? extends Host> hostList,
			List<? extends Sector> sectorList,
			double timeZone,
			double costPerSec,
			double costPerMem,
			double costPerStorage,
			double costPerBw) {
		super(architecture,os,vmm,hostList,timeZone,costPerSec,costPerMem,costPerStorage,costPerBw);
		setSectorList(sectorList);
	}
	
	protected <T extends Sector> void setSectorList(List<T> sectorList) {
		this.sectorList = sectorList;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Sector> List<T> getSectorList(){
		return (List<T>) sectorList;
	}
	
	//getter and setter methods for the hostsPerRack,racksPerAisle and number of aisles.
	
	@SuppressWarnings("unchecked")
	public List<? extends EnhancedHost> getHostList() {
		return hostList;
	}

	public int getAisles() {
		return aisles;
	}

	public void setAisles(int aisles) {
		this.aisles = aisles;
	}

	public int getRacksPerAisle() {
		return racksPerAisle;
	}

	public void setRacksPerAisle(int racksPerAisle) {
		this.racksPerAisle = racksPerAisle;
	}

	public int getHostsPerRack() {
		return hostsPerRack;
	}

	public void setHostsPerRack(int hostsPerRack) {
		this.hostsPerRack = hostsPerRack;
	}

	//get host with at least one free Pe
	@Override
	public EnhancedHost getHostWithFreePe() {
		return EnhancedHostList.getHostWithFreePe(getHostList());
	}
	//get host with specified number of PEs
	@Override
	public EnhancedHost getHostWithFreePe(int peNumber) {
		return EnhancedHostList.getHostWithFreePe(getHostList(), peNumber);
	}
	
	//get host with at least one free Pe from a specified rack
	public EnhancedHost getHostWithFreePeFromRack(int rack) {
		return EnhancedHostList.getHostWithFreePeFromRack(getHostList(), rack);
	}
	//get host with specified number of PEs from a specific rack
	public EnhancedHost getHostWithFreePeFromRack(int rack,int peNumber) {
		return EnhancedHostList.getHostWithFreePeFromRack(getHostList(),peNumber,rack);
	}
	
	//get host with at least one free Pe from a specified aisle
	public EnhancedHost getHostWithFreePeFromAisle(int aisle) {
		return EnhancedHostList.getHostWithFreePeFromAisle(getHostList(), aisle);
	}
	//get host with specified number of PEs from a specific aisle
	public EnhancedHost getHostWithFreePeFromAisle(int aisle,int peNumber) {
		return EnhancedHostList.getHostWithFreePeFromAisle(getHostList(),peNumber,aisle);
	}
	//get the total number of PEs from all hosts
	@Override
	public int getNumberOfPes() {
		return EnhancedHostList.getNumberOfPes(getHostList());
	}
		
	//get the number of PEs from all hosts in a rack
	public int getNumberOfPesRack(int rack) {
		return EnhancedHostList.getNumberOfPesPerRack(getHostList(),rack);
	}
	//get the number of PEs from all hosts in an aisle
	public int getNumberOfPesAisle(int aisle) {
		return EnhancedHostList.getNumberOfPesPerAisle(getHostList(), aisle);
	}
	//get the number of free PEs from all hosts
	@Override
	public int getNumberOfFreePes() {
		return EnhancedHostList.getNumberOfFreePes(getHostList());
	}
	//get the number of free PEs from all hosts in a rack
	public int getNumberOfFreePesRack(int rack) {
		return EnhancedHostList.getNumberOfFreePesPerRack(getHostList(),rack);
	}
	//get the number of free PEs from all hosts in an aisle
	public int getNumberOfFreePesAisle(int aisle) {
		return EnhancedHostList.getNumberOfFreePesPerAisle(getHostList(),aisle);
	}
	//get the number of busy PEs from all hosts
	@Override
	public int getNumberOfBusyPes() {
		return EnhancedHostList.getNumberOfBusyPes(getHostList());
	}
	//get the number of busy PEs from all hosts in a rack
	public int getNumberOfBusyPesRack(int rack) {
		return EnhancedHostList.getNumberOfBusyPesPerRack(getHostList(),rack);
	}
	//get the number of busy PEs from all hosts in an aisle
	public int getNumberOfBusyPesAisle(int aisle) {
		return EnhancedHostList.getNumberOfBusyPesPerAisle(getHostList(),aisle);
	}
	
	//sets the PE status
	@Override
	public boolean setPeStatus(int status,int hostId,int peId) {
		return EnhancedHostList.setPeStatus(getHostList(),status,hostId,peId);
	}
	
	//checks if a specified host exists within the specified sector
	public <T extends Sector>boolean hostInSector(EnhancedHost host,List<T> sectorList,int sectorId) {
		Sector sector = SectorList.getSectorById(sectorList, sectorId);
		List<Integer> sectorCoords = sector.getSectorCoords();
		if(host.getAisle() >= sectorCoords.get(0) && host.getAisle() <= sectorCoords.get(2)) {
			if(host.getRack() >= sectorCoords.get(1) && host.getRack() <= sectorCoords.get(3)) {
				return true;
			}
		}
		return false;
	}
	
	//gets all hosts within a sector
	
	/*PROBABLY VERY BUGGY
	 * Revise before use
	 */
	
	public List<EnhancedHost> getHostsBySector(int sectorId) {
		List<EnhancedHost> newList = new ArrayList<EnhancedHost>();
		for(EnhancedHost host : hostList) {
			if(hostInSector(host,sectorList,sectorId)) {
				newList.add(host);
			}
		}
		return newList;
	}
	
	//RAM methods
	
	//get host with available RAM
	public EnhancedHost getHostWithAvailableRam(int ramRequirement) {
		return EnhancedHostList.getHostWithAvailableRam(getHostList(), ramRequirement);
	}
	
	//get host with available RAM from a specified rack
	public EnhancedHost getHostWithAvailableRamFromRack(int rack,int ramRequirement) {
		return EnhancedHostList.getHostWithAvailableRamPerRack(getHostList(), ramRequirement, rack);
	}
	
	//get host with available RAM from a specified aisle
	public EnhancedHost getHostWithAvailableRamFromAisle(int aisle,int ramRequirement) {
		return EnhancedHostList.getHostWithAvailableRamPerAisle(getHostList(), ramRequirement, aisle);
	}
	
	//get host with at least one free Pe and available RAM
	public EnhancedHost getHostWithFreePeAvailableRam(int ramRequirement) {
		return EnhancedHostList.getHostWithFreePesAvailableRam(getHostList(), ramRequirement);
	}
	//get host with specified number of PEs
	public EnhancedHost getHostWithFreePeAvailableRam(int peNumber,int ramRequirement) {
		return EnhancedHostList.getHostWithFreePesAvailableRam(getHostList(), peNumber, ramRequirement);
	}
		
	//get host with at least one free Pe and available RAM from a specified rack
	public EnhancedHost getHostWithFreePeAvailableRamFromRack(int rack,int ramRequirement) {
		return EnhancedHostList.getHostWithFreePesAvailableRamPerRack(getHostList(), rack, ramRequirement);
	}
	//get host with specified number of PEs and available RAM from a specific rack
	public EnhancedHost getHostWithFreePeAvailableRamFromRack(int rack,int peNumber,int ramRequirement) {
		return EnhancedHostList.getHostWithFreePesAvailableRamPerRack(getHostList(), rack, peNumber, ramRequirement);
	}
		
	//get host with at least one free Pe and available RAM from a specified aisle
	public EnhancedHost getHostWithFreePeAvailableRamFromAisle(int aisle,int ramRequirement) {
		return EnhancedHostList.getHostWithFreePesAvailableRamPerAisle(getHostList(), aisle, ramRequirement);
	}
	
	//get host with specified number of PEs and available RAM from a specific aisle
	public EnhancedHost getHostWithFreePeAvailableRamFromAisle(int aisle,int peNumber,int ramRequirement) {
		return EnhancedHostList.getHostWithFreePesAvailableRamPerAisle(getHostList(), aisle, peNumber, ramRequirement);
	}
	
}
