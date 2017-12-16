import java.util.LinkedList;
import java.util.HashMap;

//Gurobi is required! 
import gurobi.*; 

@FunctionalInterface
interface ObjectiveFunction
{
	public int compute(PlacementConfiguration pc);
}

@FunctionalInterface
interface ObjctiveFunctionModel
{
	public GRBLinExpr get(PlacementConfiguration pc, HashMap<String,GRBVar> vars);
}

@FunctionalInterface
interface ConstaintsModel
{
	public void add(PlacementConfiguration pc, HashMap<String,GRBVar> vars, GRBModel model);
}

@FunctionalInterface
interface VMSizeFunction
{
	public int size(PlacementConfiguration pc, VM vm);
}

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

	private static int powerConsumption(PlacementConfiguration pc)
	{
		//ef=\sum_{i=1}^{m}\left(e_{i_0}*b_i+\sum_{j=1}^{n}\left(pc_{ij}*\sum_{k=1}^{p}e_{i_k}*vm_{j_k}\right)\right)		
		VMConfiguration VC = pc.getVMConfiguration();
 		CloudInfrastructure CI = pc.getCloudInfrastructure();
		PowerConsumptionProfile CP = pc.getPowerConsumptionProfile();
		int e = 0;
		
		for(int i = 0; i< CI.size(); i++)
		{
			int [] conso  = ((PowerConsumption)CP.get(i)).getValues();
			//e_{i_0}*b_i
			e+= conso[0]*(((Host)CI.get(i)).getBooted()?1:0);
			for(int j = 0; j < VC.size(); j++)
			{
				int sumOfPowerConsos = 0;
				int[] vmjParams = ((VM)VC.get(j)).getValues();
				for(int k=0; k < vmjParams.length; k++)
					sumOfPowerConsos += conso[k+1] * vmjParams[k];//e_{i_k}*vm_{j_k}
				e += pc.getMatrix()[i][j] * sumOfPowerConsos; //pc_{ij}*\sum_{k=1}^{p}
			}
		}
		return e;
	}

	public static int distance(PlacementConfiguration expected, PlacementConfiguration out, ObjectiveFunction function)	
	{
		return Math.abs(function.compute(expected) - function.compute(out));
	}

	public void test(VMPlacementAlgorithm A, ObjectiveFunction function, String name)
	{

		System.out.println("Testing "+A.getName()+", criterion: "+name);
		
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
					System.out.println("Final of = "+occupation(pc)+", Final placement configuration");
					System.out.println(pc);
					System.out.println("Expected \n\t\t==================\n");
				}
				for (PlacementConfiguration expectedout : expectedouts)
				{	
					if(this.verbose)
					{
						System.out.println("of = "+occupation(expectedout));
						System.out.println(expectedout);
						System.out.println("\t\t==================\n");
					}
					int distance = distance(expectedout, pc, function);
					avg_d += distance;
					if(this.verbose)
						System.out.println(name+" Distance: "+distance);
				}
			}
	
			System.out.println("\t"+name+" Distance: "+(double)avg_d/this.repetitions+"\n\tTime: "+(double)(System.currentTimeMillis() - init_time)/(1000));
		}
	}	

	public static GRBLinExpr occupationModel(PlacementConfiguration pc, HashMap<String,GRBVar> vars)
	{
		if(pc == null)
			return null;
		
		VMConfiguration VC = pc.getVMConfiguration();
		CloudInfrastructure CI = pc.getCloudInfrastructure();

		if(VC == null || CI == null || VC.isEmpty())
			return null;

		GRBLinExpr objective = new GRBLinExpr();
		
		for (int i = 1; i <= CI.size(); i++)
			for (int j = 1; j <= VC.size(); j++)
			{
				int sum_of_vmjParams = 0;
				int[] vmjParams = ((VM)VC.get(j-1)).getValues();
				for(int k = 0; k < vmjParams.length; k++)
					sum_of_vmjParams += vmjParams[k];
				objective.addTerm(sum_of_vmjParams, vars.get("pc_"+i+"_"+j));
			}

		return objective;
	
	}

	public static void addOccupationConstraints(PlacementConfiguration pc, HashMap<String,GRBVar> vars, GRBModel model)
	{
		VMConfiguration VC = pc.getVMConfiguration();
                CloudInfrastructure CI = pc.getCloudInfrastructure();

		if(VC == null || CI == null || CI.isEmpty() )
			return;

		int p = ((Host)CI.element()).getValues().length;

		for (int i = 1; i <= CI.size(); i++)
		{
			for (int k = 1; k <= p; k++)
			{
				GRBLinExpr constraint = new GRBLinExpr();

				for(int j = 1; j <= VC.size(); j++)
					constraint.addTerm(((VM)VC.get(j-1)).getValues()[k-1], vars.get("pc_"+i+"_"+j));

				try
				{
					model.addConstr(constraint, GRB.LESS_EQUAL, ((Host)CI.get(i-1)).getValues()[k-1], "h_"+i+"_r_"+k);
				}
				catch (GRBException e)
				{
					System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
				}
				
			}
		}
	}

	public static int occupationSize(PlacementConfiguration pc, VM vm)
	{

		if(pc == null || vm == null)
			return 0;

		int size = 0;
		
		VMConfiguration VC = pc.getVMConfiguration();
		if(VC == null || VC.isEmpty())
			return 0;

		int[] vmjParams = ((VM)VC.element()).getValues();
		for(int k = 0; k < vmjParams.length; k++)
			size += vmjParams[k];

		return size;
	}

	private static void merge(int[] array, int l, int m, int r, PlacementConfiguration pc, VMConfiguration VC, VMSizeFunction f)
	{
		if(VC == null | f == null || array == null)
			return; 
		
		int left[] = new int[m -l + 1]; 
		int right[] = new int[r - m];

		for(int i = l; i < m + 1; i++)
			left[i - l] = array[i];

		for(int i = m + 1; i < r + 1; i++)
			right[i -m - 1] = array[i];

		int j1 = 0, j2 = 0;
		while (j1 < left.length && j2 < right.length)
			if(f.size(pc, (VM)VC.get(left[j1] - 1)) < f.size(pc, (VM)VC.get(right[j2] - 1)))
				array[l + j1 + j2] = left[j1++];
			else
				array[l + j1 + j2] = right[j2++];

		while(j1 < left.length)	
			array[l + j1 + j2] = left[j1++];

		while(j2 < right.length)	
			array[l + j1 + j2] = right[j2++];
	}

	private static void mergeSort(int arr[], int l, int r, PlacementConfiguration pc, VMConfiguration VC, VMSizeFunction f)
	{
		if(arr == null || arr.length == 0 || pc == null || VC == null || f == null)
			return ;
		
		if(l < r)
		{
			int m = (l + r) / 2;
	
			mergeSort(arr, l, m, pc, VC, f);
			mergeSort(arr, m + 1, r, pc, VC, f);

			merge(arr,l, m, r, pc, VC, f);
		}
		
	}

	private static int[] order(PlacementConfiguration pc, VMConfiguration VC, VMSizeFunction f)
	{
		if(VC == null || VC.isEmpty())
			return new int[0];

		int order[] = new int[VC.size()];
	
		for(int k=1; k <= order.length; order[k-1]=k++);
		
		//could have used other sorts... I just love the divide and coquer princple xD
		mergeSort(order, 0, order.length - 1, pc, VC, f);

		return order;
			
	}
		
	public static RequestSequence boundaryTSGen(PlacementConfiguration pc, HashMap<String,GRBVar> vars, VMSizeFunction f) 
	{
			VMConfiguration VC = pc.getVMConfiguration();
			CloudInfrastructure CI = pc.getCloudInfrastructure();

			if(CI == null || VC == null || VC.isEmpty() || CI.isEmpty() || vars == null)
				return null;
			
			int order[] = order(pc,VC,f);
			
			RequestSequence alpha = new RequestSequence();//empty	
			//Add GRBVar vars in the hashmap

			for(int j = 1; j <= VC.size(); j++)
				for(int i = 1; i <= CI.size(); i++)
				{
					int requestValues[] = new int[VC.size()];//\mathbf{0}^n$ $n$-tuple of 0's
					int c = order[j-1];
					requestValues[c - 1] = 1;
					try
					{
						for(int j1 = 0; j1 < vars.get("pc_"+i+"_"+c).get(GRB.DoubleAttr.X); j1++)
							alpha.add(new Request(requestValues));
					}
					catch (GRBException e)
					{	
						System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
						return null;
					}
					
				}
			return alpha;

	}

	/* optGoal can be GRB.MAXIMIZE or GRB.MINIMIZE*/
	public static VMPlacementTestCase generateTestCase(PlacementConfiguration pc, String name, int optGoal, ObjctiveFunctionModel objective, ConstaintsModel constraints, VMSizeFunction fsize)
	{
		try
		{
			GRBEnv env = new GRBEnv("VMPlacementTCGen."+name+".log");
			GRBModel model = new GRBModel(env);
			
			HashMap  vars = new HashMap<String,GRBVar>();
			
			VMConfiguration VC = pc.getVMConfiguration();
			CloudInfrastructure CI = pc.getCloudInfrastructure();
			PowerConsumptionProfile CP = pc.getPowerConsumptionProfile();
		
			//Add GRBVar vars in the hashmap
			for(int i = 1; i <= CI.size(); i++)
				for(int j = 1; j <= VC.size(); j++)
					vars.put("pc_"+i+"_"+j, model.addVar(0, Double.MAX_VALUE, 0, GRB.INTEGER, "pc_"+i+"_"+j));

			for(int i = 1; i <= CI.size(); i++)
				vars.put("b_"+i, model.addVar(0, 1, 0, GRB.INTEGER, "b_"+i));

			//set objective boundary testing
			model.setObjective(objective.get(pc, vars), GRB.MAXIMIZE);
		
			//add set of contraints
			constraints.add(pc, vars, model);	

			//Optimize the model and check
			model.set("LogToConsole", "0");
			model.optimize();
			
			int optimstatus = model.get(GRB.IntAttr.Status);
	
			if (optimstatus != GRB.Status.OPTIMAL) 
			{
        			System.out.println("Error! Optimal solution not found for boundary test case");
				return null; 
			} 
			
			RequestSequence alpha = boundaryTSGen(pc, vars, fsize);
		
			//get request sequence -- algorithm in sqj
			
			
			//get optimal... call optimal with another set of bounds if optimal differs from max otherwise optimal from obtained bnoundary
			//from optimal, get expected vmtestcase
			//VMPlacementTestCase tc = new VMPlacementTestCase(pconf, alpha, exouts);
			PlacementConfiguration expected = pc.clone();
			//expected.setValAtIndex(1,1,0);//add the correspoinding optimal

			LinkedList<PlacementConfiguration> exouts = new LinkedList();
			exouts.add(expected);

			VMPlacementTestCase tc = new VMPlacementTestCase(pc, alpha, exouts);
			return tc;
	
		}
		catch (GRBException e)
		{
			System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
			return null;
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

		VMPlacementTestCase autogen = generateTestCase(pconf, "Occupation", GRB.MAXIMIZE, VMPlacementTester::occupationModel, VMPlacementTester::addOccupationConstraints, VMPlacementTester::occupationSize);

		RequestSequence autogenrs = autogen.getReqSeq();

		System.out.println(autogenrs+"\n"+alpha+"\nEqual? "+(autogenrs.toString().equals(alpha.toString())));


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

		VMPlacementTestCase autogen = generateTestCase(pconf, "Occupation", GRB.MAXIMIZE, VMPlacementTester::occupationModel, VMPlacementTester::addOccupationConstraints, VMPlacementTester::occupationSize);

		RequestSequence autogenrs = autogen.getReqSeq();

		System.out.println(autogenrs+"\n\n"+alpha+"\nEqual? "+(autogenrs.toString().equals(alpha.toString())));


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
		tester.test(arp, VMPlacementTester::occupation, "Occupation");
		System.out.println();
		tester.test(ffp, VMPlacementTester::occupation, "Occupation");
		System.out.println("\n\n");
		tester.test(arp, VMPlacementTester::powerConsumption, "Power Consumption");
		System.out.println();
		tester.test(ffp, VMPlacementTester::powerConsumption, "Power Consumption");

	}
}
