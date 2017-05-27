public class PlacementModule
{
	private PlacementConfiguration configuration;
	
	public PlacementModule (PlacementConfiguration configuration)
	{
		this.configuration = configuration;
	}	

	public boolean place(int vmtype, int athost)
	{

		VMConfiguration VC = this.configuration.getVMConfiguration();
		CloudInfrastructure CI = this.configuration.getCloudInfrastructure();
		if (vmtype >= VC.size() || athost >= CI.size())
			return false; //can't place an unexisting vm type or host
		//try to place it in the corresponding host if it has space, if not return false
		Object hst = CI.get(athost); //should fix this all...
		Object virtM = VC.get(vmtype);
		if(!(hst instanceof Host) || !(virtM instanceof VM))
			return false;
		Host h = (Host)hst;
		VM vm = (VM)virtM;
		if (!h.canAllocate(vm))
			return false;
		//modify the configuration pc matrix to reflect the change if so, return true
		h.allocate(vm);
		this.configuration.incrementAtIndex(athost, vmtype);
		return true;
	}

	//returns if the placement configuration corresponds to the stored values in the placement module
	public boolean correctConfiguration(PlacementConfiguration tocheck)
	{
		return tocheck.equals(this.configuration);
	}
}
