import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.google.protobuf.Duration;

import java.util.logging.Logger;

/** VRPTW. */
public class VrpTimeWindows {
    private static final Logger logger = Logger.getLogger(VrpTimeWindows.class.getName());


    /// @brief Print the solution.
    static void printSolution(
            DataModel data, RoutingModel routing, RoutingIndexManager manager, Assignment solution) {
        // Solution cost.
        System.out.println("Objective : " + solution.objectiveValue());
        // Inspect solution.
        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        long totalTime = 0;
        for (int i = 0; i < data.vehicleNumber; ++i) {
            long index = routing.start(i);
            System.out.println("Route for Vehicle " + i + ":");
            String route = "";
            while (!routing.isEnd(index)) {
                IntVar timeVar = timeDimension.cumulVar(index);
                route += manager.indexToNode(index) + " Time(" + solution.min(timeVar) + ","
                        + solution.max(timeVar) + ") -> ";
                index = solution.value(routing.nextVar(index));
            }
            IntVar timeVar = timeDimension.cumulVar(index);
            route += manager.indexToNode(index) + " Time(" + solution.min(timeVar) + ","
                    + solution.max(timeVar) + ")";
            System.out.println(route);
            System.out.println("Time of the route: " + solution.min(timeVar) + "min");
            System.out.println();
            totalTime += solution.min(timeVar);
        }
        System.out.println("Total time of all routes: " + totalTime + "min");
    }

    public static void main(String[] args) throws Exception {
        Loader.loadNativeLibraries();

        Time.initialize(8,0);

        AuftragSupplier auftragSupplier = new AuftragSupplier();
        auftragSupplier.generate(5);
        auftragSupplier.draw();
        auftragSupplier.calcTimeMatrix();
        auftragSupplier.calcTimeWindows(-15, 15);
//        auftragSupplier.printTimeMatrix();
//        auftragSupplier.printTimeWindows();

        // Instantiate the data problem.
        DataModel data = new DataModel();
        data.timeMatrix = auftragSupplier.timeMatrix;
        data.timeWindows = auftragSupplier.timeWindows;

        data.vehicleNumber = 2;

        // Create Routing Index Manager
        RoutingIndexManager manager =
                new RoutingIndexManager(data.timeMatrix.length, data.vehicleNumber, data.depot);

        // Create Routing Model.
        RoutingModel routing = new RoutingModel(manager);

        // Create and register a transit callback.
        final int transitCallbackIndex =
                routing.registerTransitCallback((long fromIndex, long toIndex) -> {
                    // Convert from routing variable Index to user NodeIndex.
                    int fromNode = manager.indexToNode(fromIndex);
                    int toNode = manager.indexToNode(toIndex);
                    return data.timeMatrix[fromNode][toNode];
                });

        // Define cost of each arc.
        routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

        // Add Time constraint.
        routing.addDimension(transitCallbackIndex, // transit callback
                99999, // allow waiting time
                99999, // vehicle maximum capacities
                false, // start cumul to zero
                "Time");
        RoutingDimension timeDimension = routing.getMutableDimension("Time");
        // Add time window constraints for each location except depot.
        for (int i = 1; i < data.timeWindows.length; ++i) {
            long index = manager.nodeToIndex(i);
            timeDimension.cumulVar(index).setRange(data.timeWindows[i][0], data.timeWindows[i][1]);
        }
        // Add time window constraints for each vehicle start node.
        for (int i = 0; i < data.vehicleNumber; ++i) {
            long index = routing.start(i);
            timeDimension.cumulVar(index).setRange(data.timeWindows[0][0], data.timeWindows[0][1]);
        }

        // Instantiate route start and end times to produce feasible times.
        for (int i = 0; i < data.vehicleNumber; ++i) {
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.start(i)));
            routing.addVariableMinimizedByFinalizer(timeDimension.cumulVar(routing.end(i)));
        }

        // Setting first solution heuristic.
        RoutingSearchParameters searchParameters =
                main.defaultRoutingSearchParameters()
                        .toBuilder()
                        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.AUTOMATIC)
                        .setLocalSearchMetaheuristic(LocalSearchMetaheuristic.Value.GUIDED_LOCAL_SEARCH)
                        .setTimeLimit(Duration.newBuilder().setSeconds(10).build())
                        .build();

        // Solve the problem.
        Assignment solution = routing.solveWithParameters(searchParameters);

        // Print solution on console.
        printSolution(data, routing, manager, solution);
    }
}