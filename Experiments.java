import java.util.LinkedList;

public class Experiments
{
	/**************************************************************************
	Particular tests
	*****************************************************************************/

	public static PlacementConfiguration tc1()
	{
		CloudInfrastructure CI = new CloudInfrastructure();
		CI.add(new Host(6,3));		
		CI.add(new Host(3,3));		

		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));

		PowerConsumptionProfile CP = new PowerConsumptionProfile();
		CP.add(new PowerConsumption(10,15,5));
		CP.add(new PowerConsumption(10,5,15));

		return new PlacementConfiguration(CI, VC, CP);
	}

	public static PlacementConfiguration tc2()
	{
		CloudInfrastructure CI = new CloudInfrastructure();
		CI.add(new Host(6,3));		
		CI.add(new Host(3,3));		
		CI.add(new Host(6,3));		
	
		VMConfiguration VC = new VMConfiguration();
		VC.add(new VM(3,2));
		VC.add(new VM(2,3));
		VC.add(new VM(2,2));
		VC.add(new VM(1,1));

		PowerConsumptionProfile CP = new PowerConsumptionProfile();
		CP.add(new PowerConsumption(10,15,5));
		CP.add(new PowerConsumption(10,5,15));
		CP.add(new PowerConsumption(10,5,15));

		return new PlacementConfiguration(CI, VC, CP);
	}


	public static LinkedList<VMPlacementTestCase> TestCases()
	{
		LinkedList<VMPlacementTestCase> testcases = new LinkedList();
		testcases.add(VMPlacementTester.getPowerConsumptionTC(tc1()));
		testcases.add(VMPlacementTester.getPowerConsumptionTC(tc2()));
		return testcases;
	}

	public static void main(String args[])
	{
		//LinkedList<VMPlacementTestCase> testcases = createTestCases();

		LinkedList<VMPlacementTestCase> testcases = TestCases();
		VMPlacementTester tester_100 = new VMPlacementTester(false, 100, testcases);
		VMPlacementTester tester_1 = new VMPlacementTester(false, 1, testcases);
		AvailableRandomPlacement arp = new AvailableRandomPlacement();	
		FirstFitPlacement ffp = new FirstFitPlacement();
		//tester.test(arp, VMPlacementTester::occupation, "Occupation");
		//System.out.println();
		//tester.test(ffp, VMPlacementTester::occupation, "Occupation");
		//System.out.println("\n\n");
		tester_100.test(arp, VMPlacementTester::powerConsumption, "Power Consumption");
		System.out.println();
		tester_1.test(ffp, VMPlacementTester::powerConsumption, "Power Consumption");

	}

}
