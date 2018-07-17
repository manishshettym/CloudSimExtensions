package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.examples.power.EnhancedRunner;

public class SectorAllocationSimpleExample
{
	public static void main(String[] args)
	{
		try {
		
		new EnhancedRunner("/home/manish/cloudsim-3.0.3ext/examples/workload/planetlab/20110303","output","/home/manish/cloudsim-3.0.3ext/examples/workload/planetlab/test");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}