package org.cloudbus.cloudsim.examples.power.planetlab;

import org.cloudbus.cloudsim.examples.power.planetlab.EnhancedRunner;

public class SectorAllocationSimpleExample
{
	public static void main(String[] args)
	{
		try {
		String input = new String("/home/manish/cloudsim-3.0.3ext/examples/workload/LANL/LANL-CM5-short.swf");
		new EnhancedRunner(input,"output","/home/manish/cloudsim-3.0.3ext/examples/workload/planetlab/test");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}