package vmplacementsim;

import java.util.LinkedList;
public class VMPlacementTestCase
{
	//a test case is composed of a request sequence and possible outputs, i.e., matrices
	private RequestSequence reqSeq = null;
	private PlacementConfiguration init = null;
	private LinkedList<PlacementConfiguration> expectedOutputs = null;

	public VMPlacementTestCase(PlacementConfiguration init, RequestSequence rs, LinkedList<PlacementConfiguration> exout)
	{
		this.init = init;
		this.reqSeq = rs;		
		this.expectedOutputs = exout;
	}
	
	public PlacementConfiguration getInit()
	{
		return this.init;
	}

	public void setInit(PlacementConfiguration init)
	{
		this.init = init;//no naming convention, I know, coding fast
	}
	
	public RequestSequence getReqSeq()
	{
		return reqSeq;
	}
	
	public void setReqSeq(RequestSequence rs)
	{
		this.reqSeq = rs;
	}
	
	public LinkedList<PlacementConfiguration> getExpectedOutputs()
	{
		return this.expectedOutputs;
	}

	public void setExpectedOutputs(LinkedList<PlacementConfiguration> exout)
	{
		this.expectedOutputs = exout;
	}
}
