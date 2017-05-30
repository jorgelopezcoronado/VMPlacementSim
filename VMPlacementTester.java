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
		
		int tc = 1; ///quick and dirty modif... should have changed the for and that is it

		for (VMPlacementTestCase testcase : testcases)
		{
			int avg_d = 0;
			long init_time = System.currentTimeMillis();
			RequestSequence alpha = testcase.getReqSeq();
			System.out.println("Test case #"+tc+++" Sequence: "+alpha);

			LinkedList<PlacementConfiguration> expectedouts  = testcase.getExpectedOutputs();

			for(int iter = 0; iter < this.repetitions; iter++)
			{
				PlacementConfiguration pc = testcase.getInit().clone();
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
						System.out.println("Distance: "+distance);
				}
			}
	
			System.out.println("\tDistance: "+(double)avg_d/this.repetitions+"\n\tTime: "+(double)(System.currentTimeMillis() - init_time)/(1000));
		}
	
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
		alpha.add(new Request(1,0,0,0));

		PlacementConfiguration expected = pconf.clone();
		expected.setValAtIndex(1,0,0);
		expected.setValAtIndex(3,1,3);
		expected.setValAtIndex(1,2,0);
		//expected.setValAtIndex(1,1,2);

		LinkedList<PlacementConfiguration> exouts = new LinkedList();
		exouts.add(expected);

		VMPlacementTestCase tc = new VMPlacementTestCase(pconf, alpha, exouts);
		return tc;
	}

	public static VMPlacementTestCase testcase3()
	{
		CloudInfrastructure CI = new CloudInfrastructure();
		CI.add(new Host(64,96));		
		CI.add(new Host(96,64));		
		CI.add(new Host(32,32));		

		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));

		PlacementConfiguration pconf = new PlacementConfiguration(CI, VC);
	
		RequestSequence alpha = new RequestSequence();

		alpha.add(new Request(0,0,0,1));
		alpha.add(new Request(0,0,0,1));//2 machines type 4
	
		for(int i = 0; i < 38; i++)
			alpha.add(new Request(0,1,0,0)); //38 machines type 2

		for(int i = 0; i < 38; i++)
			alpha.add(new Request(1,0,0,0)); //38 machines type 1

		PlacementConfiguration expected = pconf.clone();
		expected.setValAtIndex(32,0,1);//32 vms of type two placed at host 1
		expected.setValAtIndex(32,1,0); //32 vms type 1 placed at host 2
		expected.setValAtIndex(6,2,0); //etc
		expected.setValAtIndex(6,2,1);
		expected.setValAtIndex(2,2,3);

		LinkedList<PlacementConfiguration> exouts = new LinkedList();
		exouts.add(expected);

		VMPlacementTestCase tc = new VMPlacementTestCase(pconf, alpha, exouts);
		return tc;
	}

	public static VMPlacementTestCase testcase4()
	{
		CloudInfrastructure CI = new CloudInfrastructure();
		CI.add(new Host(64,96));		
		CI.add(new Host(96,64));		
		CI.add(new Host(32,32));		
		CI.add(new Host(64,96));		
		CI.add(new Host(96,64));		
		CI.add(new Host(32,32));		
		CI.add(new Host(64,96));		
		CI.add(new Host(96,64));		
		CI.add(new Host(32,32));		

		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));

		PlacementConfiguration pconf = new PlacementConfiguration(CI, VC);
	
		RequestSequence alpha = new RequestSequence();


		for(int i = 0; i < 66; i++)
			alpha.add(new Request(0,0,0,1));//66 machines type 4
	
		for(int i = 0; i < 102; i++)
			alpha.add(new Request(0,1,0,0)); //38 machines type 2

		for(int i = 0; i < 102; i++)
			alpha.add(new Request(1,0,0,0)); //38 machines type 1

		PlacementConfiguration expected = pconf.clone();
		expected.setValAtIndex(32,0,1); //32 vms type 2 placed at host 1
		expected.setValAtIndex(32,1,0); //32 vms type 1 placed at host 2
		expected.setValAtIndex(32,2,3); //32 vms type 4 placed at host 3
		expected.setValAtIndex(32,3,1); //32 vms type 2 placed at host 4
		expected.setValAtIndex(32,4,0); //32 vms type 1 placed at host 5
		expected.setValAtIndex(32,5,3); //32 vms type 4 placed at host 6
		expected.setValAtIndex(32,6,1); //32 vms type 2 placed at host 7
		expected.setValAtIndex(32,7,0); //32 vms type 1 placed at host 8
		expected.setValAtIndex(06,8,1); //06 vms type 2 placed at host 9
		expected.setValAtIndex(06,8,0); //06 vms type 1 placed at host 9
		expected.setValAtIndex(02,8,3); //06 vms type 4 placed at host 9

		LinkedList<PlacementConfiguration> exouts = new LinkedList();
		exouts.add(expected);

		VMPlacementTestCase tc = new VMPlacementTestCase(pconf, alpha, exouts);
		return tc;
	}

	public static LinkedList<VMPlacementTestCase> createTestCases()
	{
		LinkedList<VMPlacementTestCase> testcases = new LinkedList();
//		testcases.add(testcase1());
//		testcases.add(testcase2());
//		testcases.add(testcase3());
		testcases.add(testcase4());
		return testcases;
	}

	public static void main(String args[])
	{
		LinkedList<VMPlacementTestCase> testcases = createTestCases();
		VMPlacementTester tester = new VMPlacementTester(false, 100, testcases);
		AvailableRandomPlacement arp = new AvailableRandomPlacement();	
		FirstFitPlacement ffp = new FirstFitPlacement();
		tester.test(arp);
		System.out.println();
		tester.test(ffp);
	}
}
