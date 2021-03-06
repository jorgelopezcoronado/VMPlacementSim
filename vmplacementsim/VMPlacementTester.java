package vmplacementsim;

import java.util.LinkedList;
import java.util.HashMap;
import java.io.*;

//Gurobi is required! 
import gurobi.*; 

@FunctionalInterface
interface ObjctiveFunctionModel
{
	GRBLinExpr get(PlacementConfiguration pc, HashMap<String,GRBVar> vars);
}

@FunctionalInterface
interface ConstaintsModel
{
	void add(PlacementConfiguration pc, HashMap<String,GRBVar> vars, GRBModel model);
}

@FunctionalInterface
interface VMSizeFunction
{
	int size(PlacementConfiguration pc, VM vm);
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

	/*
		The following function must be created for each new criterion:
		public static int <criterion>(PlacementConfiguration pc)
	*/

	public static int occupation(PlacementConfiguration pc)
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

	public static int powerConsumption(PlacementConfiguration pc)
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

	private static int distance(PlacementConfiguration expected, PlacementConfiguration out, ObjectiveFunction function)	
	{
		return Math.abs(function.compute(expected) - function.compute(out));
	}

	private static double ratio (PlacementConfiguration expected, PlacementConfiguration out, ObjectiveFunction function)	
	{
		return (double)function.compute(out)/function.compute(expected);
	}


	public void test(VMPlacementAlgorithm A, ObjectiveFunction function, String name)
	{

		System.out.println("Testing "+A.getName()+", criterion: "+name);
		if(testcases == null || testcases.isEmpty())
		{
			System.out.println("Error Empty set of test cases!");
			return;
		}
		
		int tc = 1; ///quick and dirty modif... should have changed the for and that is it

		for (VMPlacementTestCase testcase : testcases)
		{
			int avg_d = 0;
			double avg_r = 0;
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
					System.out.println("Final f = "+function.compute(pc)+", Final placement configuration");
					System.out.println(pc);
					System.out.println("Expected \n\t\t==================\n");
				}
				for (PlacementConfiguration expectedout : expectedouts)
				{	
					if(this.verbose)
					{
						System.out.println("f = "+function.compute(expectedout));
						System.out.println(expectedout);
						System.out.println("\t\t==================\n");
					}
					int distance = distance(expectedout, pc, function);
					double ratio = ratio(expectedout, pc, function);
					avg_d += distance;
					avg_r += ratio;
					if(this.verbose)
					{
						System.out.println(name+" Distance: "+distance);
						System.out.println(name+" Ratio: "+ratio);
					}
				}
			}
	
			System.out.println("\t"+name+" Distance: "+(double)avg_d/this.repetitions+"\t Ratio: "+(double)avg_r/this.repetitions+"\n\tTime: "+(double)(System.currentTimeMillis() - init_time)/(this.repetitions)+"ms");
		}
	}	

	/*
		Follwing 3 functions must be created for each new criterion:
		private static GRBLinExpr <crit>Model(PlacementConfiguration pc, HashMap<String,GRBVar> vars)
		private static void add<crit>Constarints(PlacementConfiguration pc, HashMap<String,GRBVar> vars, GRBModel model)
		private static int <crit_f'_size>(PlacementConfiguration pc, VM vm)
	*/

	/*
	//Power Consumption
	*/

	private static GRBLinExpr powerConsumptionModel(PlacementConfiguration pc, HashMap<String,GRBVar> vars)
	{
		if(pc == null)
			return null;

		VMConfiguration VC = pc.getVMConfiguration();
 		CloudInfrastructure CI = pc.getCloudInfrastructure();
		PowerConsumptionProfile CP = pc.getPowerConsumptionProfile();
	
		if(VC == null || CI == null || CP == null || VC.isEmpty() || CI.isEmpty() || CP.isEmpty())
			return null;
		
		GRBLinExpr objective = new GRBLinExpr();

		//ef=\sum_{i=1}^{m}\left(e_{i_0}*b_i+\sum_{j=1}^{n}\left(pc_{ij}*\sum_{k=1}^{p}e_{i_k}*vm_{j_k}\right)\right)		
		for(int i = 1; i <= CI.size(); i++)
		{
			int [] conso  = ((PowerConsumption)CP.get(i - 1)).getValues();
			//e_{i_0}*b_i
			objective.addTerm(conso[0], vars.get("b_"+i));
			for(int j = 1; j <= VC.size(); j++)
			{
				int sumOfPowerConsos = 0;
				int[] vmjParams = ((VM)VC.get(j - 1)).getValues();
				for(int k=1; k <= vmjParams.length; k++)
					sumOfPowerConsos += conso[k] * vmjParams[k - 1];//e_{i_k}*vm_{j_k}
				objective.addTerm(sumOfPowerConsos, vars.get("pc_"+i+"_"+j)); //pc_{ij}*\sum_{k=1}^{p}
			}
		}
		return objective;

	}

	private static void addPowerConsumptionConstraints(PlacementConfiguration pc, HashMap<String,GRBVar> vars, GRBModel model)
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

				constraint.addTerm (-((Host)CI.get(i-1)).getValues()[k-1],vars.get("b_"+i));
				try
				{
					model.addConstr(constraint, GRB.LESS_EQUAL, 0, "h_"+i+"_r_"+k);
				}
				catch (GRBException e)
				{
					System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
				}
				
			}
		}

		/* initial placement configuration? need to add a function that passes the test suite
	
		Request r = getRequestFromFile("pc.txt");
	
		for(int j = 1; j <= VC.size(); j++)
		{
			GRBLinExpr constraint = new GRBLinExpr();

			for(int i = 1; i <= CI.size(); i++)
				constraint.addTerm(1, vars.get("pc_"+i+"_"+j));

			try
			{
				model.addConstr(constraint, GRB.EQUAL, r.getValues()[j-1], "q_"+j);
			}
			catch (GRBException e)
			{
				System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
			}
		
		}
		
		*/

	}

	private static int powerConsumptionSize(PlacementConfiguration pc, VM vm)
	{

		if(pc == null || vm == null)
			return 0;

		int size = 0;
		
		VMConfiguration VC = pc.getVMConfiguration();
		PowerConsumptionProfile CP = pc.getPowerConsumptionProfile();

		if(VC == null || VC.isEmpty() || CP == null || CP.isEmpty())
			return 0;


		int[] vmjParams = ((VM)VC.element()).getValues();
		for(int k = 1; k <= vmjParams.length; k++)
		{
			int consoAvg = 0; 
			for(int i = 1; i <= CP.size(); i++)	
				consoAvg += ((PowerConsumption)CP.get(i - 1)).getValues()[k];
				
			size += consoAvg * vmjParams[k - 1];
		}
				
		size /= CP.size();

		return size;
	}

	private static Request getRequestFromFile(String fileName)
	{
		String line = null;
		Request r = new Request(4); 
		try 
		{
			FileReader fileReader = new FileReader(fileName);

            		BufferedReader bufferedReader = new BufferedReader(fileReader);

	            	if((line = bufferedReader.readLine()) != null) //single line file reading
			{
				int base = line.indexOf("(") + 1;
				while(base > 0)
				{
					line = line.substring(base);
					if(line.charAt(0) == '(')//we are at the begining
						line = line.substring(1);

					int t1 = Character.getNumericValue(line.charAt(0));
					int t2 = Character.getNumericValue(line.charAt(3));
					int t3 = Character.getNumericValue(line.charAt(6));
					int t4 = Character.getNumericValue(line.charAt(9));
			
					int r_vals[] = r.getValues();
					r_vals[0] += t1; 	
					r_vals[1] += t2; 	
					r_vals[2] += t3; 	
					r_vals[3] += t4; 	
			
					r.setValues(r_vals);
						
					base = line.indexOf("(") + 1;
				}
				 
			}

            		bufferedReader.close();         
            	}   
        	catch(Exception ex) 
		{
            		System.out.println("Unable with file '" +fileName + "'");                
        	}

		return r;

	} 

	/*
	// Occupation
	*/

	private static GRBLinExpr occupationModel(PlacementConfiguration pc, HashMap<String,GRBVar> vars)
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

	private static void addOccupationConstraints(PlacementConfiguration pc, HashMap<String,GRBVar> vars, GRBModel model)
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

		/*Initial placement? need to add a function to read the original requests / TS
		
		Request r = getRequestFromFile("ru.txt");

		for(int j = 1; j <= VC.size(); j++)
		{
			GRBLinExpr constraint = new GRBLinExpr();

			for(int i = 1; i <= CI.size(); i++)
				constraint.addTerm(1, vars.get("pc_"+i+"_"+j));

			try
			{
				model.addConstr(constraint, GRB.EQUAL, r.getValues()[j-1], "q_"+j);
			}
			catch (GRBException e)
			{
				System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
			}
		
		}
		*/

	}

	private static int occupationSize(PlacementConfiguration pc, VM vm)
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

	/***********************************
		TS Derivation part
	************************************
	*/

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
		
	private static RequestSequence boundaryTSGen(PlacementConfiguration pc, HashMap<String,GRBVar> vars, VMSizeFunction f) 
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
	private static VMPlacementTestCase generateTestCase(PlacementConfiguration pc, String name, int optGoal, ObjctiveFunctionModel objective, ConstaintsModel constraints, VMSizeFunction fsize)
	{
		try
		{

			long init_time = System.currentTimeMillis();
			GRBEnv env = new GRBEnv("VMPlacementTCGen."+name+".log");
			GRBModel model = new GRBModel(env);
			
			HashMap<String,GRBVar>  vars = new HashMap<String,GRBVar>();
			
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
			GRBLinExpr obj = objective.get(pc, vars);
			model.setObjective(obj, GRB.MAXIMIZE);
		
			//add set of contraints
			constraints.add(pc, vars, model);	

			//Optimize the model and check
			model.set("LogToConsole", "0");
			model.optimize();
	
			
			int optimstatus = model.get(GRB.IntAttr.Status);

			if(optimstatus == GRB.Status.INF_OR_UNBD)			
			{
				model.set(GRB.IntParam.Presolve, 0);
       	 			model.optimize();
        			optimstatus = model.get(GRB.IntAttr.Status);
			}

      			 if (optimstatus == GRB.Status.INFEASIBLE)
			{
				model.update();
				model.write("debug.lp");
			}


			if (optimstatus != GRB.Status.OPTIMAL) 
			{
        			System.out.println("Error! Optimal solution not found for boundary test case");
				return null; 
			} 
			
			//request sequence obtained as in submitted Algorithm 1 for the Springer's Software Quality Journal SI: Testing Software and Systems
			RequestSequence alpha = boundaryTSGen(pc, vars, fsize);

			//get optimal... call optimal with another set of bounds if optimal differs from max otherwise optimal from obtained bnoundary
			PlacementConfiguration expected = pc.clone();
			
			if(optGoal == GRB.MINIMIZE)
			{
				//add restrictions w.r.t. the requested resources.
				//\sum_{i=1}^{m}pc_{ij} = \sum_{a=1}^{l}\alpha_{a_j}; \forall j=1,\ldots,n
				for(int j = 1; j <= VC.size(); j++)
				{	
					GRBLinExpr allocConstraint = new GRBLinExpr();
					for(int i = 1; i <= CI.size(); i++)
						allocConstraint.addTerm(1,vars.get("pc_"+i+"_"+j));
					//allocConstraint holds sum_{i=1}^{m}pc_{ij}
				
					int VMjReqQuantity = 0;
					for(int a = 1; a <= alpha.size(); a++)	
						VMjReqQuantity += ((Request)alpha.get(a - 1)).getValues()[j - 1]; //\alpha_{a_j}				//VMjReqQuantity holds \sum_{a=1}^{l}\alpha_{a_j}
					
					model.addConstr(allocConstraint, GRB.EQUAL, VMjReqQuantity, "num_req_vm"+j);
				}
				
				//re-compute the optimal 
				model.setObjective(obj, GRB.MINIMIZE);

				//optimize
				model.optimize();

				optimstatus = model.get(GRB.IntAttr.Status);
	
				if (optimstatus != GRB.Status.OPTIMAL) 
				{
        				System.out.println("Error! Optimal solution not found for request sequence of boundary test suite!");
					return null; 
				} 

				//reverse alpha... order conjecture?
				/*RequestSequence aux = new RequestSequence();
				while(!alpha.isEmpty())
					aux.add(alpha.removeLast());
				
				alpha = null;
				
				alpha = aux;
			
				aux = null;*/
			}

			//expected is loaded in vars at this point
			for(int i = 1; i <= CI.size(); i++)
			{
				for(int j = 1; j <= VC.size(); j++)
					expected.setValAtIndex((int)vars.get("pc_"+i+"_"+j).get(GRB.DoubleAttr.X), i - 1, j - 1);
				//add also b_{i}'s
				((Host)CI.get(i - 1)).setBooted(((int)vars.get("b_"+i).get(GRB.DoubleAttr.X) == 1)?true:false);
				
			}

			LinkedList<PlacementConfiguration> exouts = new LinkedList();
			exouts.add(expected);

			VMPlacementTestCase tc = new VMPlacementTestCase(pc, alpha, exouts);

			System.out.println(name+"TC Generation time: "+(System.currentTimeMillis()-init_time)+"ms TS Length: "+alpha.size());
			return tc;
	
		}
		catch (GRBException e)
		{
			System.out.println("Error code: " + e.getErrorCode() + ". " +e.getMessage());
			return null;
		}
	}
		
	/*
		For each new criterion a function must be created:
		public static VMPlacementTestCase get<crit>TC (PlacementConfiguration pc)
	*/

	//Power consumption
	public static VMPlacementTestCase getPowerConsumptionTC (PlacementConfiguration pc)
	{
		return generateTestCase(pc, "Power Consumption", GRB.MINIMIZE, VMPlacementTester::powerConsumptionModel, VMPlacementTester::addPowerConsumptionConstraints, VMPlacementTester::powerConsumptionSize);
	}


	//Occupation	
	public static VMPlacementTestCase getResourceUtilizationTC (PlacementConfiguration pc)
	{
		return generateTestCase(pc, "Occupation", GRB.MAXIMIZE, VMPlacementTester::occupationModel, VMPlacementTester::addOccupationConstraints, VMPlacementTester::occupationSize);
	}

}
