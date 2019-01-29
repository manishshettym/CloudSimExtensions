package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.examples.power.EnhancedRunner;

public class SectorAllocationSimpleExample
{
	public static void main(String[] args)
	{
		try {
		
		new EnhancedRunner("/home/manish/CloudSimExtension/examples/workload/planetlab/20110303","output","/home/manish/CloudSimExtension/examples/workload/planetlab/test/");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}