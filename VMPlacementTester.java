import java.util.LinkedList;

public class VMPlacementTester
{
	private final int repetitions;
	private final boolean verbose;
	private final LinkedList<VMPlacementTestCase> testcases;

	public VMPlacementTester(boolean verbose, int repetitions, LinkedList<VMPlacementTestCase> testcases)
	{
		this.verbose = verbose;
		this.repetitions = repetitions;
		this.testcases = testcases;
	}

	//2do

	private static int occupation(PlacementConfiguration pc)
	{
		//$\mathcal{U}=\sum_{i=1}^{m}\sum_{j=1}^{n}\left(pc_{ij}*\sum_{k=1}^{p}vm_{j_k}\right)$, where $vm_{j_k}$ represents the $k$-th element (resource limit) of the $j$-th virtual machine configuration type
		//also denoted as f in our paper... 
		VMConfiguration VC = pc.getVMConfiguration();
		CloudInfrastructure CI = pc.getCloudInfrastructure();
		int o = 0;	
		for(int i = 0; i < CI.size(); i++)
			for(int j = 0; j < VC.size(); j++)
			{
				int sum_of_vmjParams = 0;
				int[] vmjParams = ((VM)VC.get(j)).getValues();
				for(int k = 0; k < vmjParams.length; k++)
					sum_of_vmjParams += vmjParams[k];
				o += pc.getMatrix()[i][j] * sum_of_vmjParams;
			}
		
		return o;
	}
	public static int distance(PlacementConfiguration expected, PlacementConfiguration out)	
	{
		return Math.abs(occupation(expected) - occupation(out)) ;
	}

	public void test(VMPlacementAlgorithm A)
	{

		System.out.println("Testing "+A.getName());
		int avg_d = 0;
		for(int iter = 0; iter < this.repetitions; iter++)
		{
			for (VMPlacementTestCase testcase : testcases)
			{
				PlacementConfiguration pc = testcase.getInit().clone();
				RequestSequence alpha = testcase.getReqSeq();
				if(this.verbose)
				{
					System.out.println("Iteration: "+iter+"\nIntial placement configuration");
					System.out.println(pc);
				}
				VMPlacementSimulator.simulate(pc, alpha, A);
				if(this.verbose)
				{
					System.out.println("Final f = "+occupation(pc)+", Final placement configuration");
					System.out.println(pc);
					System.out.println("Expected \n\t\t==================\n");
				}
				LinkedList<PlacementConfiguration> expectedouts  = testcase.getExpectedOutputs();
				for (PlacementConfiguration expectedout : expectedouts)
				{	
					if(this.verbose)
					{
						System.out.println("f = "+occupation(expectedout));
						System.out.println(expectedout);
						System.out.println("\t\t==================\n");
					}
					int distance = distance(expectedout, pc);
					avg_d += distance;
					if(this.verbose)
						System.out.println("Distance: "+distance(expectedout, pc));
				}
			}
		}
		System.out.println("\tDistance: "+(double)avg_d/this.repetitions);
	
	}	

	/**************************************************************************
	Particular tests
	*****************************************************************************/
	
	public static VMPlacementTestCase testcase1()
	{
		CloudInfrastructure CI = new CloudInfrastructure();
		CI.add(new Host(3,3));		
		CI.add(new Host(3,2));		

		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));

		PlacementConfiguration pconf = new PlacementConfiguration(CI, VC);
	
		RequestSequence alpha = new RequestSequence();
		alpha.add(new Request(0,0,0,1));
		alpha.add(new Request(0,0,0,1));
		alpha.add(new Request(0,0,0,1));
		//alpha.add(new Request(0,0,1,0));
		alpha.add(new Request(1,0,0,0));

		PlacementConfiguration expected = pconf.clone();
		expected.setValAtIndex(1,1,0);
		//expected.setValAtIndex(1,1,2);
		expected.setValAtIndex(3,0,3);

		LinkedList<PlacementConfiguration> exouts = new LinkedList();
		exouts.add(expected);

		VMPlacementTestCase tc = new VMPlacementTestCase(pconf, alpha, exouts);
		return tc;
	}

	public static VMPlacementTestCase testcase2()
	{
		CloudInfrastructure CI = new CloudInfrastructure();
		CI.add(new Host(3,2));		
		CI.add(new Host(3,3));		
		CI.add(new Host(3,2));		

		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));

		PlacementConfiguration pconf = new PlacementConfiguration(CI, VC);
	
		RequestSequence alpha = new RequestSequence();
		alpha.add(new Request(0,0,0,1));
		alpha.add(new Request(0,0,0,1));
		alpha.add(new Request(0,0,0,1));
		//alpha.add(new Request(0,0,1,0));
		alpha.add(new Request(1,0,0,0));

		PlacementConfiguration expected = pconf.clone();
		expected.setValAtIndex(1,1,0);
		//expected.setValAtIndex(1,1,2);
		expected.setValAtIndex(3,0,3);

		LinkedList<PlacementConfiguration> exouts = new LinkedList();
		exouts.add(expected);

		VMPlacementTestCase tc = new VMPlacementTestCase(pconf, alpha, exouts);
		return tc;
	}


	public static LinkedList<VMPlacementTestCase> createTestCases()
	{
		LinkedList<VMPlacementTestCase> testcases = new LinkedList();
		testcases.add(testcase1());
		return testcases;
	}

	public static void main(String args[])
	{
		LinkedList<VMPlacementTestCase> testcases = createTestCases();
		VMPlacementTester tester = new VMPlacementTester(false, 100, testcases);
		AvailableRandomPlacement arp = new AvailableRandomPlacement();	
		FirstFitPlacement ffp = new FirstFitPlacement();
		tester.test(arp);
		tester.test(ffp);
	}
}
