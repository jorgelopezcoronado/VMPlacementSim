# VMPlacementSim: A Java API for Test Generation and Simulation of Virtual Machine Placement Algorithms

This repository contains a Java API for boundary test case generation
for Virtual Machine (VM) placement modules. Also, the API contains
facilities for simulating VM placement algorithms under the generated
test cases.

Installation / Software Requirements
====================================

This API is provided as Java source code, under the BSD 3-clause
license. To install, simply download the source code, and update the
Java CLASSPATH correspondingly in your system. The code *requires Java
8* due to the constructs used. Further, as the generation of the test
suites[@ICTSS17] rely on the solution of an Integer Linear
Programming (ILP) problem, the Gurobi [@gurobi] optimization tool is
also required; recent versions (7 and 8) of the tool are known to work,
version 7 is preferred for stability reasons.

To check your Java version, issue the following command in your
operating system's terminal:

    java -version

To check your Gurobi version, issue the following command in your
operating system's terminal:

    gurobi_cl --version

Make sure your Gurobi license is up-to-date. Check the the location of
your Gurobi license file by issuing the following command in your
operating system's terminal:

    gurobi_cl --license

The Gurobi license file should contain something similar:

``` {.bash language="bash"}
$ cat ~/gurobi.lic 
# DO NOT EDIT THIS FILE except as noted
#
# License ID XXXXXX
# Gurobi license for Telecom SudParis
ORGANIZATION=Telecom SudParis
TYPE=ACADEMIC
VERSION=7
HOSTNAME=hostname.domain.tld
HOSTID=YYYYZZYY
USERNAME=username
EXPIRATION=2019-06-22
KEY=AAAAAAAA
CKEY=AAAAAAAA
```

A very simple way to check that your Gurobi installation works is by
compiling and executing an example file provided by Gurobi, for example
the file `Mip1.java`. The code of this file is the following:

``` {language="java"}
/* Copyright 2018, Gurobi Optimization, LLC */

/* This example formulates and solves the following simple MIP model:

     maximize    x +   y + 2 z
     subject to  x + 2 y + 3 z <= 4
                 x +   y       >= 1
     x, y, z binary
*/

import gurobi.*;

public class Mip1 {
  public static void main(String[] args) {
    try {
      GRBEnv    env   = new GRBEnv("mip1.log");
      GRBModel  model = new GRBModel(env);

      // Create variables

      GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x");
      GRBVar y = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");
      GRBVar z = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z");

      // Set objective: maximize x + y + 2 z

      GRBLinExpr expr = new GRBLinExpr();
      expr.addTerm(1.0, x); expr.addTerm(1.0, y); expr.addTerm(2.0, z);
      model.setObjective(expr, GRB.MAXIMIZE);

      // Add constraint: x + 2 y + 3 z <= 4

      expr = new GRBLinExpr();
      expr.addTerm(1.0, x); expr.addTerm(2.0, y); expr.addTerm(3.0, z);
      model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");

      // Add constraint: x + y >= 1

      expr = new GRBLinExpr();
      expr.addTerm(1.0, x); expr.addTerm(1.0, y);
      model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "c1");

      // Optimize model

      model.optimize();

      System.out.println(x.get(GRB.StringAttr.VarName)
                         + " " +x.get(GRB.DoubleAttr.X));
      System.out.println(y.get(GRB.StringAttr.VarName)
                         + " " +y.get(GRB.DoubleAttr.X));
      System.out.println(z.get(GRB.StringAttr.VarName)
                         + " " +z.get(GRB.DoubleAttr.X));

      System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

      // Dispose of model and environment

      model.dispose();
      env.dispose();

    } catch (GRBException e) {
      System.out.println("Error code: " + e.getErrorCode() + ". " +
                         e.getMessage());
    }
  }
}
```

To compile the file and executing, issue the following command:

``` {.bash language="bash"}
javac Mip1.java && java Mip1
```

The output of the previous command should be:

    Academic license - for non-commercial use only
    Optimize a model with 2 rows, 3 columns and 5 nonzeros
    Variable types: 0 continuous, 3 integer (3 binary)
    Coefficient statistics:
      Matrix range     [1e+00, 3e+00]
      Objective range  [1e+00, 2e+00]
      Bounds range     [1e+00, 1e+00]
      RHS range        [1e+00, 4e+00]
    Found heuristic solution: objective 2.0000000
    Presolve removed 2 rows and 3 columns
    Presolve time: 0.01s
    Presolve: All rows and columns removed

    Explored 0 nodes (0 simplex iterations) in 0.01 seconds
    Thread count was 1 (of 4 available processors)

    Solution count 2: 3 2 

    Optimal solution found (tolerance 1.00e-04)
    Best objective 3.000000000000e+00, best bound 3.000000000000e+00, gap 0.0000%
    x 1.0
    y 0.0
    z 1.0
    Obj: 3.0

If the Gurobi software is running correctly, you can proceed to compile
the classes found in this repository.

Using the Java Classes
----------------------

The easiest way to use the API is to put in your `CLASSPATH` environment
variable the jar file provided in this repository, VMPS.jar. For
example, for a typical \*-nix environment issuing the following command
in your operating system's terminal will set the variable:

``` {.bash language="bash"}
export CLASSPATH=$CLASSPATH:/path/to/the/jar/file/VMPS.jar
```

However, if you wish to modify the existing classes or to extend the
pre-existing ones, for instance to add another Virtual Machine (VM)
placement algorithm read continue reading, if not, you can skip to the
next section.

The source files can be found in the `vmplacementsim` directory of this
repository. If you add new classes they must have the correct access
(`public, private, protected`). Additionally, the new classes must
belong to the Java package `vmplacementsim`. To compile the java
classes, a Makefile is provided in this repository. To create a new jar
file, simple issue the `make` command.

Using the Java API
==================

This API contains two main functionalities. The first functionality is
to generate boundary test suites for a given cloud environment; the
second is to simulate the test suites for a given VM placement
algorithm(s). Therefore, three important groups of methods and objects
exist. The first group is related to the specification of a cloud
environment; the second is related to the test case generation; and,
finally, the third is the simulation of the test suites. Each of those
groups is explained below.

Creating the Cloud Environment Objects
--------------------------------------

Cloud environment objects are hosts (correspondingly the list of hosts,
i.e., the cloud infrastructure) , VM configurations (composed of VMs),
the power consumption (and correspondingly the list of power consumption
of all the hosts, i.e., the power consumption profile), and finally the
placement configuration, which holds the information regarding which VMs
are being executed at which hosts.

To create a host, the object `Host` can be used. There are different
constructors for this object. Perhaps the simplest of such constructors
is to use the constructor with *varargs* `int` parameters, where each
int represents the capacity of a host resource, e.g.:
`Host h = new Host(32, 64, 32)`. The Host class found under the
`vmplacementsim/Host.java` file contains all given constructors.

To create a cloud infrastructure (the collection of hosts) the object
`CloudInfrastructure` can be used with the non-parametrized constructor.
Later, hosts can be added though the `add` method.

Similarly to hosts, VMs can be created through the `VM` object, also
with the *varargs* `int` parameters constructor. Likewise, the VM
configurations can be created through the `VMConfiguration` object and
VMs can be adeed via the `add` method.

Again similarly to hosts and VMs, power consumption objects
(`PowerConsumption`) and power consumption profile
(`PowerConsumptionProfile`, collection of power consumption objects) can
be created in the same manner. **An important notice**: The number of
parameters of a power consumption object must be one more than the
associated host. The reason is that the first parameter is considered as
the power consumption of a host when the host is idle, and not hosting
any VM.

Finally, a placement configuration (`PlacementConfiguration`) works with
a `CloudInfrastructure`, a `VMConfiguration`, and a
`PowerConsumptionProfile` (this parameter may be `null` for calculating
test cases and simulations non-related to power consumption). A typical
creation of a placement configuration with all the required objects can
be implemented as follows:

``` {language="java"}
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
```

Generating Test Cases for Cloud Environments
--------------------------------------------

Once the placement configuration and all related objects is created,
boundary test case generation can be performed over the given cloud
environment. To generate such test cases (`VMPlacementTestCase`) is
pretty straightforward. To generate a power consumption test case given
a placement configuration can be done through the `static` method
`VMPlacementTester.getPowerConsumptionTC` which receives as a parameter
a `PlacementConfiguration` object. Likewise, to generate a resource
utilization test case given a placement configuration can be done
through the `static` method `VMPlacementTester.getResourceUtilizationTC`
which receives as a parameter a `PlacementConfiguration` object.

A test case is composed of a request sequence and a list of expected
outputs (due to the non-determinism nature of the placement problem). A
common operation is to get the request sequence of the test case. To do
so, the unparameterized method `getReqSeq` can be used. Further, request
sequences (`RequestSequence` objects) have a pre-defined `toString`
method, and they can be printed directly.

Assume the placement configuration returned by the previously described
`env4` method. To generate and display the request sequence the
following code can be used:

``` {language="java"}
VMPlacementTestCase tc = VMPlacementTester.getResourceUtilizationTC(env4());
System.out.println("Boundary (resource utilization) Test Case for Environment:\n"+tc.getReqSeq());
```

The corresponding display is the following:

``` {language="java" breaklines="true"}
Boundary (resource utilization) Test Case for Environment:
((0, 0, 0, 1), (0, 0, 0, 1), (0, 0, 0, 1), (0, 0, 0, 1), (0, 0, 0, 1), (0, 0, 0, 1), (0, 0, 0, 1), (0, 0, 0, 1), (0, 0, 0, 1), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (0, 1, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0), (1, 0, 0, 0))
```

The representation of a request is a tuple where each of its elements
represents the number of requested VMs of the configuration of the i-th
element. For example, the tuple (1, 0, 0, 0) represents a request for a
single VM of the configuration 1 in the VM configurations (where there
are 4).

Simulating VM Placement Requests for VM Placement Algorithms
------------------------------------------------------------

As mentioned before, the Java API also includes an interface for
simulation of request sequences on "different" algorithms. To simulate a
request sequence (or a list of those) an object of the
`VMPlacementTester` (tester for short) class can be used. The tester
object can be constructed with three parameters: (i) the verbosity (a
`Boolean` parameter); (ii) the number of simulations (an `int`
parameter); and finally (iii) a list (`LinkedList`) of request sequences
to simulate. It is important to note that the number of simulations is
mostly useful for non-deterministic or random placement algorithms. To
test a given algorithm, a `VMPlacementAlgorithm` (a Java interface)
object is necessary. In the current repository two VM placement
algorithms are implemented: (i) the First Fit (FF) algorithm, which
places a requested VM at the first host that can fit it; and (ii) the
Available Random (AR) algorithm, which places a requested VM on an
equality distributed random host which is capable of allocate it. Other
placement algorithms can be created by implementing the
`VMPlacementAlgorithm` interface. The tester reports the distance
compared to the optimal allocation; likewise, it reports its ratio. As
an example, given the `env4` cloud environment and the test case `tc`,
to test a FF algorithm with a boundary test case, the following code can
be executed:

        LinkedList<VMPlacementTestCase> rutestcases = new LinkedList();
        rutestcases.add(tc);
        VMPlacementTester tester_100_ru = new VMPlacementTester(false, 100, rutestcases);
        FirstFitPlacement ffp = new FirstFitPlacement();
        tester_100_ru.test(arp, VMPlacementTester::occupation, "Occupation");

Correspondingly the output is the following:

    Testing First Fit, criterion: Occupation
    Test case #1 Sequence: ...(omitted, the same sequence shown before)
        Occupation Distance: 145.0   Ratio: 0.8824006488240044
        Time: 2.33ms

Learn by Example
----------------

An easy way to learn how the API works is to look at a complete example.
An example is included in this repository, in the file `Example.java`

References
----------
[1] Jorge López, Natalia Kushik, and Djamal Zeghlache. Quality estimation of virtual machine placement in cloud infrastructures. In Testing Software and Systems - 29th IFIP WG 6.1 International Conference, ICTSS 2017, St. Petersburg, Russia, October 9-11, 2017, Proceedings, pages 213-229, 2017.

[2] Gurobi Optimization et al. Gurobi optimizer reference manual. URL: http://www.gurobi. com, 2012.
