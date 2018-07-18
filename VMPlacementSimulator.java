package vmplacementsim;

public class VMPlacementSimulator
{
	public static void simulate(PlacementConfiguration configuration, RequestSequence seq,  VMPlacementAlgorithm A)
	{
		PlacementModule pm = new PlacementModule(configuration);
		for (int l = 0; l < seq.size(); l++)
		{
			PlacementConfiguration tempPC = configuration.clone(); //just making sure the algorithm does not modify the conf directly but through the placement module
			if(!(seq.get(l) instanceof Request))
				continue;
			Request r = (Request)seq.get(l);
			A.place(tempPC, r, pm);
		}
	}
}
