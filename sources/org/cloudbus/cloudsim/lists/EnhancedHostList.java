package org.cloudbus.cloudsim.lists;

import java.util.List;

import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

/*Class used to represent the EnhancedHostList used by EnhancedDatacenter
 * These methods are used to obtain information about the hosts in the datacenter
 */

@SuppressWarnings("unused")
public class EnhancedHostList extends HostList{
	
	
	//create a copy of host list
	
	
	
	

	/*These methods override the methods defined in HostList
	 * Basically they act as helper methods for obtaining hosts 
	 * with desired number of PEs and so on
	 */
	
	//returns the total number of PEs available in a specified rack
	public static <T extends EnhancedHost> int getNumberOfPesPerRack(List<T> hostList,int rack) {
		int numberOfPes = 0;
		for (T host : hostList) {
			if(host.getRack() == rack)
				numberOfPes += host.getPeList().size();
		}
		return numberOfPes;
	}

	//returns the total number of PEs available in a specified aisle
	public static <T extends EnhancedHost> int getNumberOfPesPerAisle(List<T> hostList,int aisle) {
		int numberOfPes = 0;
		for (T host : hostList) {
			if(host.getAisle() == aisle)
				numberOfPes += host.getPeList().size();
		}
		return numberOfPes;
	}
	
	//returns the total number of free PEs in a specified rack
	public static <T extends EnhancedHost> int getNumberOfFreePesPerRack(List<T> hostList,int rack) {
		int numberOfFreePes = 0;
		for (T host : hostList) {
			if(host.getRack() == rack)
				numberOfFreePes += PeList.getNumberOfFreePes(host.getPeList());
		}
		return numberOfFreePes;
	}
	
	//returns the total number of free PEs in a specified aisle
	public static <T extends EnhancedHost> int getNumberOfFreePesPerAisle(List<T> hostList,int aisle) {
		int numberOfFreePes = 0;
		for (T host : hostList) {
			if(host.getAisle() == aisle)
				numberOfFreePes += PeList.getNumberOfFreePes(host.getPeList());
		}
		return numberOfFreePes;
	}
	
	//returns the total number of busy PEs in a specified rack
	public static <T extends EnhancedHost> int getNumberOfBusyPesPerRack(List<T> hostList,int rack) {
		int numberOfBusyPes = 0;
		for (T host : hostList) {
			if(host.getRack() == rack)
				numberOfBusyPes += PeList.getNumberOfBusyPes(host.getPeList());
		}
		return numberOfBusyPes;
	}
	
	//returns the total number of busy PEs in a specified aisle
	public static <T extends EnhancedHost> int getNumberOfBusyPesPerAisle(List<T> hostList,int aisle) {
		int numberOfBusyPes = 0;
		for (T host : hostList) {
			if(host.getAisle() == aisle)
				numberOfBusyPes += PeList.getNumberOfBusyPes(host.getPeList());
		}
		return numberOfBusyPes;
	}
	
	//returns a host with atleast one free PEs from a specified rack
	public static <T extends EnhancedHost> T getHostWithFreePeFromRack(List<T> hostList,int rack) {
		return getHostWithFreePeFromRack(hostList, 1,rack);
	}
	
	//returns a host with specified number of free PEs from a specified rack
	public static <T extends EnhancedHost> T getHostWithFreePeFromRack(List<T> hostList, int pesNumber,int rack) {
		for (T host : hostList) {
			if ((host.getRack() == rack) && (PeList.getNumberOfFreePes(host.getPeList()) >= pesNumber)) {
				return host;
			}
		}
		return null;
	}
	
	//returns a host with at least one free PE from a specified aisle
	public static <T extends EnhancedHost> T getHostWithFreePeFromAisle(List<T> hostList,int aisle) {
		return getHostWithFreePeFromAisle(hostList, 1,aisle);
	}
	
	//returns a host with specified number of free PEs from a specified aisle
	public static <T extends EnhancedHost> T getHostWithFreePeFromAisle(List<T> hostList, int pesNumber,int aisle) {
		for (T host : hostList) {
			if ((host.getAisle() == aisle) && (PeList.getNumberOfFreePes(host.getPeList()) >= pesNumber)) {
				return host;
			}
		}
		return null;
	}
	
	/*implementing RAM methods here
	 * The way this works is that it gets the host's RAMProvisioner
	 * and checks whether the host can satisfy the RAM requirements
	 * if it can, it will the host
	 */
	
	//returns a host which meets the RAM requirements
	//this is a first fit approach which returns the first host that meets the RAM requirements
	//this method can be overridden in any extended class to incorporate different approaches
	public static<T extends EnhancedHost> T getHostWithAvailableRam(List<T> hostList,int ramRequirement) {
		for(T host : hostList) {
			int availableRam = host.getRamProvisioner().getAvailableRam();
			if(availableRam >= ramRequirement) {
				return host;
			}
		}
		return null;
	}
	
	//returns a host from a specified rack that meets the RAM requirements
	//again this uses a first fit approach
	public static<T extends EnhancedHost> T getHostWithAvailableRamPerRack(List<T> hostList,int ramRequirement,int rack) {
		for(T host : hostList) {
			int availableRam = host.getRamProvisioner().getAvailableRam();
			if((host.getRack() == rack) && availableRam >= ramRequirement) {
				return host;
			}
		}
		return null;
	}
	
	//returns a host from a specified aisle that meets the RAM requirements
	//this uses a first fit approach
	public static<T extends EnhancedHost> T getHostWithAvailableRamPerAisle(List<T> hostList,int ramRequirement,int aisle) {
		for(T host : hostList) {
			int availableRam = host.getRamProvisioner().getAvailableRam();
			if((host.getAisle() == aisle) && availableRam >= ramRequirement) {
				return host;
			}
		}
		return null;
	}
	
	//returns a host which has specified number of PEs and available RAM
	public static<T extends EnhancedHost> T getHostWithFreePesAvailableRam(List<T> hostList,int pesNumber,int ramRequirement) {
		for(T host : hostList) {
			if ((PeList.getNumberOfFreePes(host.getPeList()) >= pesNumber) && host.getRamProvisioner().getAvailableRam() >= ramRequirement) {
				return host;
			}
		}
		return null;
	}
	//returns a host with at least one free PE and specified RAM
	public static<T extends EnhancedHost> T getHostWithFreePesAvailableRam(List<T> hostList,int ramRequirement) {
		return getHostWithFreePesAvailableRam(hostList,1,ramRequirement);
	}
	
	//returns a host with specified number of PEs and available RAM from a rack
	public static<T extends EnhancedHost> T getHostWithFreePesAvailableRamPerRack(List<T> hostList,int rack,int pesNumber,int ramRequirement) {
		for(T host : hostList) {
			if ((host.getRack() == rack) && (PeList.getNumberOfFreePes(host.getPeList()) >= pesNumber) && host.getRamProvisioner().getAvailableRam() >= ramRequirement) {
				return host;
			}
		}
		return null;
	}
	
	//returns a host with at least one free PE and specified RAM from a rack
	public static<T extends EnhancedHost> T getHostWithFreePesAvailableRamPerRack(List<T> hostList,int rack,int ramRequirement) {
		return getHostWithFreePesAvailableRamPerRack(hostList,rack,1,ramRequirement);
	}
	
	//returns a host with specified number of PEs and available RAM from an aisle
	public static<T extends EnhancedHost> T getHostWithFreePesAvailableRamPerAisle(List<T> hostList,int aisle,int pesNumber,int ramRequirement) {
		for(T host : hostList) {
			if ((host.getAisle() == aisle) && (PeList.getNumberOfFreePes(host.getPeList()) >= pesNumber) && host.getRamProvisioner().getAvailableRam() >= ramRequirement) {
				return host;
			}
		}
		return null;
	}
		
	//returns a host with at least one free PE and specified RAM from an aisle
	public static<T extends EnhancedHost> T getHostWithFreePesAvailableRamPerAisle(List<T> hostList,int aisle,int ramRequirement) {
		return getHostWithFreePesAvailableRamPerAisle(hostList,aisle,1,ramRequirement);
	}
}
