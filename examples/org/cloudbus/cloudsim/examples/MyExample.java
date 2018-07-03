package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.List;
import org.cloudbus.cloudsim.EnhancedHost;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.lists.EnhancedHostList;
import org.cloudbus.cloudsim.power.models.PowerModelCubic;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class MyExample {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		try {
			List<EnhancedHost> hostList = new ArrayList<EnhancedHost>();
			List<Pe> peList = new ArrayList<Pe>();
			
			int mips = 1000;

			// 3. Create PEs and add these into a list.
			peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
			
			int hostId = 0;
			int ram = 2048; // host memory (MB)
			long storage = 1000000; // host storage
			int bw = 10000;
			int rack = 0;
			int aisle = 1;
			double maxPower = 100000;
			double staticPowerPercent = 56.43;
			hostList.add(new EnhancedHost(hostId,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList,
					new VmSchedulerTimeShared(peList),
					new PowerModelCubic(maxPower,staticPowerPercent),
					rack,
					aisle
					)
			);
			EnhancedHost a = EnhancedHostList.getById(hostList, hostId);
			System.out.println("This is the number of PEs" + a.getNumberOfPes());
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("An error occured");
		}
	}
	
}
