package org.cloudbus.cloudsim.lists;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Aisle;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Rack;
import org.cloudbus.cloudsim.Sector;

public class AisleList {
	
	public static<T extends Aisle> T getAisleById(List<T> aisleList,int aisleId) 
	{
		for(T Aisle : aisleList) 
		{
			
			if(Aisle.getAisleId() == aisleId) {
				return Aisle;
			}
		}
		return null;
	}
	
	
	public static<T extends Aisle> T getAisleByName(List<T> aisleList,String aisleName) 
	{
		for(T Aisle : aisleList) 
		{
			
			if(Aisle.getName() == aisleName) {
				return Aisle;
			}
		}
		return null;
	}
	
	
	public static<T extends Aisle> List<EnhancedHost> getHostsByAisle(List<T> aisleList,int aisleId)
	{
	
		List <EnhancedHost> aislehosts = new ArrayList<EnhancedHost>();
		
		
		
				for(Aisle aisle  : aisleList)
				{
					if(aisle.getAisleId()== aisleId)
					{
					
						for(Rack rack : aisle.getAisleRackList())
						{
							aislehosts.addAll( rack.getRackHostList());
				
						}
					}
				}
			
		
		return aislehosts;
	}
	
	public static<T extends Aisle> int getFreePes(List<T> aisleList,int aisleId)
	{
		for(Aisle aisle : aisleList)
		{
			if(aisle.getAisleId() == aisleId)
			{
				return aisle.freePesPerAisle();
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
