public interface VMPlacementAlgorithm
{
	public String getName();
	//we encourage anybody implementing this method to first check if pm has the correct conf, i.e., if(pm.correctConfiguration(pc)) as the first thing this method does
	public void place(PlacementConfiguration pc, Request r, PlacementModule pm);
}
