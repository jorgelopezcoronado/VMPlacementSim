package vmplacementsim;

import java.util.Random;
import java.util.LinkedList;
public class AvailableRandomPlacement implements VMPlacementAlgorithm
{
	public String getName()
	{
		return "Available Random";
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
				LinkedList<Integer> electable = new LinkedList<Integer>();
				CloudInfrastructure hosts = pc.getCloudInfrastructure();
				VMConfiguration vms = pc.getVMConfiguration();
				for(int i = 0; i < hosts.size(); i++)
				{
					Host h = (Host)hosts.get(i);
					if (h.canAllocate((VM)vms.get(j)))
						electable.add(new Integer(i));
				}

				if(electable.size() == 0)
					continue; //sad, but I have no more space to allocate, what to do...

				Random chooser = new Random();
				int position = chooser.nextInt(electable.size()); //uniformely distributed random choice of electable hosts
				int atHost = electable.get(position);
				if(pm.place(j,atHost))
				{	
					Host h = (Host)hosts.get(atHost);
					h.allocate((VM)vms.get(j));//keep track of allocated vms
					//me, I do not need to update my pc matrix, but I'll do just because
					pc.incrementAtIndex(atHost, j);
				}//if not, someting went terribly wrong.... terribly wrong
				else
					throw new Exception("Unexpected allocation problem. Placement module failure");			
			}
		}catch(Exception E)
		{
			System.out.println(E);
		}
	}
}
