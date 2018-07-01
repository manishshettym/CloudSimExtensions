package org.cloudbus.cloudsim.lists;


import java.util.List;

import org.cloudbus.cloudsim.EnhancedHost;

import org.cloudbus.cloudsim.Sector;

/*Class is used to represent the methods defined on the Sector list used 
 * by the enhanced Datacenter
 */

public class SectorList {
	
	
	public static<T extends Sector> T getSectorById(List<T> sectorList,int sectorId) {
		for(T sector : sectorList) {
			if(sector.getSectorId() == sectorId) {
				return sector;
			}
		}
		return null;
	}
	
	public static<T extends Sector> T getSectorByName(List<T> sectorList,String sectorName) {
		for(T sector : sectorList) {
			if(sector.getName() == sectorName) {
				return sector;
			}
		}
		return null;
	}
	public static<T extends Sector> List<EnhancedHost> getHostsBySector(List<T> sectorList,int sectorId)
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
	
	public static<T extends Sector> int getFreePes(List<T> sectorList,int sectorId)
	{
		for(Sector sec : sectorList)
		{
			if(sec.getSectorId() == sectorId)
			{
				return sec.freePesPerSector();
			}
		}
		return -1; //could not find the required sector
	}
	
	public static<T extends Sector> int getCooledSectorWithLeastFreePes(List<T> sectorList,int minNeededPes)
	{
		int minPesSectorId=-1;
		int minPes = 1000000;
		for(Sector sector : sectorList) 
		{
			int sectorPes = SectorList.getFreePes(sectorList,sector.getSectorId()); 
			if(sector.getCoolingStatus() == 1 && sectorPes < minPes && sectorPes > minNeededPes)
			{
				for(EnhancedHost host : sector.getSectorHostList()) {
					if(host.getNumberOfFreePes() >= minNeededPes)
					{
						minPesSectorId = sector.getSectorId();
						minPes = SectorList.getFreePes(sectorList,sector.getSectorId());
						break;
					}
						
				}
			}
		}
		return minPesSectorId;
	}
}
