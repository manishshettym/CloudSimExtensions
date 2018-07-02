package org.cloudbus.cloudsim.lists;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Aisle;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Rack;
import org.cloudbus.cloudsim.Sector;

public class RackList 
{
	public static<T extends Rack> T getRackById(List<T> rackList,int rackId) 
	{
		for(T Rack : rackList) 
		{
			
			if(Rack.getRackId() == rackId) {
				return Rack;
			}
		}
		return null;
	}
	
	
	public static<T extends Rack> T getRackByName(List<T> rackList,String rackName) 
	{
		for(T Rack : rackList) 
		{
			
			if(Rack.getName() == rackName) {
				return Rack;
			}
		}
		return null;
	}
	
	
	public static<T extends Rack> List<EnhancedHost> getHostsByRack(List<T> rackList,int rackId)
	{
	
		List <EnhancedHost> rackhosts = new ArrayList<EnhancedHost>();
		
				
		for(Rack rack : rackList)
		{
			if(rack.getRackId()== rackId)
			{
				rackhosts.addAll( rack.getRackHostList());
				
			}

		}
					
				
		return rackhosts;
	}
	
	public static<T extends Rack> int getFreePes(List<T> rackList,int rackId)
	{
		for(Rack rack : rackList)
		{
			if(rack.getRackId() == rackId)
			{
				return rack.freePesPerRack();
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
