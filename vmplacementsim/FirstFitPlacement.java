package vmplacementsim;

public class FirstFitPlacement implements VMPlacementAlgorithm
{
	public String getName()
	{
		return "First Fit";
	}

	//we encourage anybody implementing this method to first check if pm has the correct conf, i.e., if(pm.correctConfiguration(pc)) as the first thing this method does
	public void place(PlacementConfiguration pc, Request r, PlacementModule pm)
	{
		try{
		if(!pm.correctConfiguration(pc))
			throw new IllegalArgumentException("The given placement configuration does not concide with the placement module's placement configuration");
	
		int[] requestedVMs = r.getValues();

		if (requestedVMs.length != pc.getMatrix()[0].length) //if the number on the requests is different from the VM configurations	
			throw new IllegalArgumentException("The given request "+r+"does not match the VM placement configuration dimensions "+pc.getMatrix()[0].length);

		for(int j = 0; j < requestedVMs.length; j++)
			for(int q = 0;  q < requestedVMs[j]; q++)
			{
				CloudInfrastructure hosts = pc.getCloudInfrastructure();
				VMConfiguration vms = pc.getVMConfiguration();
				for(int i = 0; i < hosts.size(); i++)
				{
					Host h = (Host)hosts.get(i);
					if (h.canAllocate((VM)vms.get(j)))
					{
						if(pm.place(j,i))
						{
							//internal consistency
							h.allocate((VM)vms.get(j));
							pc.incrementAtIndex(i,j);
						}
						else
							throw new Exception("Unexpected allocation problem. Placement module failure");	
						return;
					}
				}

			}
		}catch(Exception E)
		{
			System.out.println(E);
		}
	}

} 
