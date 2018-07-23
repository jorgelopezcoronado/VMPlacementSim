import java.util.LinkedList;
import vmplacementsim.*;

public class Experiments
{
	/**************************************************************************
	Create the cloud entivonment
	*****************************************************************************/

	public static PlacementConfiguration env4()
	{
		CloudInfrastructure CI = new CloudInfrastructure();
		for(int i = 1; i <= 3; i++)
		{
			CI.add(new Host(62,96));		
			CI.add(new Host(96,96));		
			CI.add(new Host(32,32));		
		}
	
		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));

		PowerConsumptionProfile CP = new PowerConsumptionProfile();

		for(int i = 1; i <= 3; i++)
		{
			CP.add(new PowerConsumption(10,15,5));
			CP.add(new PowerConsumption(10,5,15));
			CP.add(new PowerConsumption(10,5,15));
		}

		return new PlacementConfiguration(CI, VC, CP);
	}

	/**************************************************************************
	Create power consumption test cases for the cloud environment
	*****************************************************************************/

	public static LinkedList<VMPlacementTestCase> PCTestCases()
	{
		LinkedList<VMPlacementTestCase> testcases = new LinkedList();
		testcases.add(VMPlacementTester.getPowerConsumptionTC(env4()));
		return testcases;
	}

	/**************************************************************************
	Create occupation est cases for the cloud environment
	*****************************************************************************/

	public static LinkedList<VMPlacementTestCase> RUTestCases()
	{	
		LinkedList<VMPlacementTestCase> testcases = new LinkedList();
		testcases.add(VMPlacementTester.getResourceUtilizationTC(env4()));
		for (VMPlacementTestCase tc: testcases)	
			System.out.println("\n\n\n\t\tTHIS IS A TC: \n"+tc.getReqSeq()+"\n\n\n\n\n");
		return testcases;
	}

	/**************************************************************************
	Simulate the test cases for the infrastructure for different algorithms
	*****************************************************************************/

	public static void main(String args[])
	{
		LinkedList<VMPlacementTestCase> pctestcases = PCTestCases();
		LinkedList<VMPlacementTestCase> rutestcases = RUTestCases();
		VMPlacementTester tester_100_pc = new VMPlacementTester(false, 100, pctestcases);
		VMPlacementTester tester_100_ru = new VMPlacementTester(false, 100, rutestcases);
		AvailableRandomPlacement arp = new AvailableRandomPlacement();	
		FirstFitPlacement ffp = new FirstFitPlacement();
		tester_100_ru.test(arp, VMPlacementTester::occupation, "Occupation");
		System.out.println();
		tester_100_ru.test(ffp, VMPlacementTester::occupation, "Occupation");
		System.out.println("\n\n");
		tester_100_pc.test(arp, VMPlacementTester::powerConsumption, "Power Consumption");
		System.out.println();
		tester_100_pc.test(ffp, VMPlacementTester::powerConsumption, "Power Consumption");

	}

}
