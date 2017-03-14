
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.*;

/**
 * Given a grid of hexagonal tiles partially-filled with natural numbers,</br>
 * the goal of this type of puzzle is to completely fill the grid with</br>
 * numbers such that any number N is adjacent to both its predecessor (N-1)</br>
 * and its successor (N+1).</br>
 * </br> 
 * As a reference for working with a representation of a hexagonal grid I</br>
 * used 
 * <a href="http://www.redblobgames.com/grids/hexagons/">this excellent guide</a>
 * by Amit Patel.
 * 
 * @author NadavNV
 */
public class HexGridPuzzle {
    // Used for debugging
    private File logFile;
    private static final String LOG_PATH = "log.txt";
    private PrintStream logWriteStream;
    // How many recursive steps it took to solve the puzzle. Used to compare 
    // different solving algorithms.
    private static int recursionSteps;
    // The maximum absolute value a coordinate can have in any axis
    private final int maxRadius;
    // Default maximum radius
    // The actual grid
    private HashMap<CubeHex,Integer> grid;
    // The values that haven't been placed in the grid yet
    private SortedSet<Integer> remainingValues;
    private static final int DEFAULT_MAX_RADIUS = 4;
    // zero indicates an empty hex
    private static int EMPTY_HEX = 0;
    // The initial state of the grid, as given in the puzzle by Intel
    private static final HashMap<CubeHex, Integer> DEFAULT_INITIAL_STATE;
    static {
        DEFAULT_INITIAL_STATE = new HashMap<>();
        DEFAULT_INITIAL_STATE.put(new CubeHex(-2, 1, 1), 1);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-3, 2, 1), 2);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-1, 2, -1), 5);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-1, 3, -2), 7);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-4, 1, 3), 16);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-3, 0, 3), 17);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-1, -1, 2), 20);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-1, 1, 0), 23);
        DEFAULT_INITIAL_STATE.put(new CubeHex(0, 2, -2), 26);
        DEFAULT_INITIAL_STATE.put(new CubeHex(1, 1, -2), 31);
        DEFAULT_INITIAL_STATE.put(new CubeHex(2, 0, -2), 33);
        DEFAULT_INITIAL_STATE.put(new CubeHex(3, -1, -2), 39);
        DEFAULT_INITIAL_STATE.put(new CubeHex(3, -2, -1), 40);
        DEFAULT_INITIAL_STATE.put(new CubeHex(3, -3, 0), 43);
        DEFAULT_INITIAL_STATE.put(new CubeHex(1, -4, 3), 46);
        DEFAULT_INITIAL_STATE.put(new CubeHex(1, -3, 2), 47);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-1, -2, 3), 58);
        DEFAULT_INITIAL_STATE.put(new CubeHex(-4, 0, 4), 61);
    }
    // Creates an instance of the puzzle with the default state given above.
    public HexGridPuzzle() throws IOException{
        this(DEFAULT_MAX_RADIUS);
    }
    
    
    // Creates an instance of this puzzle on a grid with the given radius.
    // Will be adapted to take an initial state from the user, instead of
    // the default state.
    public HexGridPuzzle(int maxRadius) throws IllegalArgumentException, IOException {
        if (maxRadius < 0) {
            throw new IllegalArgumentException("Radius must be anunsigned integer");
        }
        logFile = new File(LOG_PATH);
        logWriteStream = new PrintStream(logFile);
        //System.err.println(maxRadius);
        this.maxRadius = maxRadius;
        
        grid = new HashMap<>();
        CubeHex root = new CubeHex(0, 0, 0);
        grid.put(root, EMPTY_HEX);
        addNeighbors(root);
        remainingValues = new TreeSet<>();
        for (int i = 1; i <= grid.size(); i++) {
            remainingValues.add(i);
        }
        int value;
        for (CubeHex hex: DEFAULT_INITIAL_STATE.keySet()) {
            value = DEFAULT_INITIAL_STATE.get(hex);
            remainingValues.remove(value);
            grid.put(hex, value);
        }
        // printGrid();
        
    }
    
    // Currently supports the case where numbers are at most 2 digits.
    // Will be adapted to support longer numbers.
    private void printGrid(PrintStream output) {
        SortedSet<CubeHex> hexes = new TreeSet<>(grid.keySet());
        String separator = "  ";
        // How many nodes to print in the current line
        int nodesToPrint = maxRadius + 1;
        // How much whitespace to leave before the first node
        int spaces = maxRadius;
        // print lines up to the middle line
        Iterator<CubeHex> it = hexes.iterator();
        for (; nodesToPrint < 2*maxRadius+1; nodesToPrint++, spaces--) {
            for (int i = 0; i < spaces; i++) {
                output.print(separator);
            }
            output.format("%2d", grid.get(it.next()));
            for (int i = 1; i < nodesToPrint; i++) {
                output.format(separator + "%2d", grid.get(it.next()));
            }
            output.println();
        }
        // print remaining lines
        for (; nodesToPrint >= maxRadius+1; nodesToPrint--, spaces++) {
            for (int i = 0; i < spaces; i++) {
                output.print(separator);
            }
            output.format("%2d", grid.get(it.next()));
            for (int i = 1; i < nodesToPrint; i++) {
                output.format(separator + "%2d", grid.get(it.next()));
            }
            output.println();
        }
        output.println();
    }
    
    // Finds the coordinate in the grid of the given value.
    // The inverse of searching the map by key. Returns null
    // if the value doesn't exist on the grid.
    private CubeHex getPosition(int value) throws IllegalArgumentException {
        // System.err.println("Finding position of " + value);
        for (Map.Entry<CubeHex, Integer> entry: grid.entrySet()) {
            if (value == entry.getValue()) {
                // System.err.println("Position is " + entry.getKey());
                return entry.getKey();
            }
            // System.err.println("Position is NOT " + entry.getKey());
        }
        return null;
    }
    
    
    /*
     * Finding a solution to this puzzle is equivalent to finding a path
     * that covers the whole grid and does not repeat itself. 
     * A possible way to solve this is to find partial paths to close the gaps
     * between the numbers that are already on the grid, trying to find the
     * shortest path each time.
     */
    private class PathSolver implements Solver {
        private static final String NAME = "Pathfinding";
        @Override
        public boolean solve(int currentValue) {
            /*
            Find next remaining value
            Find its parent
            Find next-highest value already placed in the grid
            Find a path from parent to next-highest placed value
            If no such value exists, try each of the parent's neighbors in DFS
            Finally check validity of solution
            */
//            logWriteStream.println("Path solving for " + currentValue);
            CubeHex parent = getPosition(currentValue - 1);
//            logWriteStream.println("Parent " + (currentValue - 1) +
//                    " is at " + parent);
            CubeHex target = null;
            for (int value : new TreeSet<>(grid.values())) {
                if (value > currentValue) {
                    target = getPosition(value);
                    break;
                }
            }
            if (target == null) {
                logWriteStream.println("Going DFS");
                // No higher values exist, so we switch to a simple DFS solution
                DFSSolver solver = new DFSSolver();
                return solver.solve(currentValue);
            }
//            logWriteStream.println("Target " + grid.get(target) + " is at " + target);
            recursionSteps++;
            // Try possible nodes according to how close they are to the target.
            ArrayList<CubeHex> candidates = new ArrayList<>();
            candidates.sort(new CubeHex.DistanceComparator(target));
            candidates.addAll(getEmptyNeighbors(parent));
            for (CubeHex candidate: candidates) {
                // If the distance is larger then we can't reach
                // target in time.
                if (candidate.distanceTo(target) <=
                        grid.get(target) - currentValue) {
                    logWriteStream.println("Placing " + currentValue + " at " + candidate);
                    remainingValues.remove(currentValue);
                    grid.put(candidate, currentValue);
                    printGrid(logWriteStream);
                    if (remainingValues.isEmpty()) {
                        return checkSolution();
                    } else if (solve(Collections.min(remainingValues))) {
                        return true;
                    } else {
                        // Undo previous step
                        logWriteStream.println("Could not place " + currentValue + " at " + candidate);
                        grid.put(candidate, EMPTY_HEX);
                        remainingValues.add(currentValue);
                        printGrid(logWriteStream);
                    }
                }
            }
            
            return false;
        }

        @Override
        public String getName() {
            return NAME;
        }
    }
    
    
    
    // In the case that 1 isn't already on the grid, we need to attempt to
    // place it, and then attemp to solve from there.
    private boolean initializeSolution(Solver solver) {
        recursionSteps = 0;
        HashSet<Integer> placedValues = new HashSet<>(grid.values());
        placedValues.remove(EMPTY_HEX);
        int lowestPlacedValue = Collections.min(placedValues);
        if (lowestPlacedValue != 1) {
            logWriteStream.println("Lowest placed value is: " + lowestPlacedValue);
            CubeHex target = getPosition(lowestPlacedValue);
            // Attempt to start from all the hexes that are at most 
            // lowerPlacedValue distance from the next placed value. If the 
            // distance is greater than that then the path will never reach
            // that value in time.
            ArrayList<CubeHex> candidates = new ArrayList<>(getNodesWithinDistance(target, lowestPlacedValue-1));
            // As the smallest number initially on the board is higher,
            // even if it's as low as 5, randomly trying to place the initial
            // causes the solution to be very slow, even when using the faster
            // solvers. Merely sorting the possible starting points by their
            // distance from the smallest value on the board reduces running
            // time considerably.
            candidates.sort(new CubeHex.DistanceComparator(target));
            logWriteStream.println("Candidates for starting position:");
            logWriteStream.println(candidates);
            for (CubeHex candidate : candidates) {
                if (grid.get(candidate) == EMPTY_HEX) {
                    // Attempt to place 1 at this position
                    logWriteStream.println("Placing 1 at " + candidate);
                    remainingValues.remove(1);
                    grid.put(candidate, 1);
                    printGrid(logWriteStream);
                    if (solver.solve(Collections.min(remainingValues))) {
                        return true;
                    } else {
                        // Undo previous step
                        logWriteStream.println("Could not place 1 at " + candidate);
                        grid.put(candidate, EMPTY_HEX);
                        remainingValues.add(1);
                        printGrid(logWriteStream);
                    }
                }
            }
            logWriteStream.println("Could not solve from any starting position.");
            return false;
        } else {
            return solver.solve(Collections.min(remainingValues));
        }
    }
    
    // Find all nodes within a certain distance from the given node (exclusing
    // the node itself). Assuming distance is greater than zero.
    private HashSet<CubeHex> getNodesWithinDistance(CubeHex root, int distance) {
        HashSet<CubeHex> alreadyChecked = new HashSet<>();
        alreadyChecked.add(root);
        HashSet<CubeHex> result = new HashSet<>();
        HashSet<CubeHex> toCheck = new HashSet<>();
        toCheck.addAll(root.getNeighbors());
        while (!toCheck.isEmpty()) {
            HashSet<CubeHex> toCheckClone = new HashSet<>(toCheck);
            for (CubeHex hex: toCheckClone) {
                alreadyChecked.add(hex);
                toCheck.remove(hex);
                if (grid.containsKey(hex)) {
                    if (hex.distanceTo(root) <= distance) {
                        result.add(hex);
                    }
                    if (hex.distanceTo(root) < distance) {
                        HashSet<CubeHex> neighbors = hex.getNeighbors();
                        neighbors.removeAll(alreadyChecked);
                        toCheck.addAll(neighbors);
                    }
                }
            }
        } 
        return result;
    }
    
    private class DFSSolver implements Solver {
        private static final String NAME = "DFS";
        @Override
        public boolean solve(int currentValue) {
            /*
            Find next remaining value
            Find its parent
            Try to place in each of the parent's neightbors via DFS
            Finally check validity of solution
            */
            
            // If the next value is already placed, then the current value must be
            // placed next to it.
            recursionSteps++;
            boolean nextValueExists = !remainingValues.contains(currentValue + 1);
            CubeHex parent = getPosition(currentValue - 1);
            for (CubeHex candidate: getEmptyNeighbors(parent)) {
                if (remainingValues.contains(currentValue + 1) ||
                    getAdjacentValues(candidate).contains(currentValue + 1) || 
                    currentValue > Collections.max(grid.values())) {
                    // Nodes that are part of the initial problem
                    // declaration should not be changed
                    assert !DEFAULT_INITIAL_STATE.keySet().contains(candidate);
                    logWriteStream.println("Placing " + currentValue + " at " + candidate);
                    remainingValues.remove(currentValue);
                    grid.put(candidate, currentValue);
                    printGrid(logWriteStream);
                    if (remainingValues.isEmpty()) {
                        /*
                         * No more values to place, so we check if the current
                         * grid represents a good solution, and cascade the 
                         * answer back up the recursion.
                         */                        
                        return checkSolution();
                    } else if (solve(Collections.min(remainingValues))) { // Attempt to place the next value
                        // Found a solution, so we cascade it up the recursion.
                        return true;
                    } else {
                        // undo previous step before checking the next neighbor.
                        logWriteStream.println("Could not place " + currentValue + " at " + candidate);
                        grid.put(candidate, EMPTY_HEX);
                        remainingValues.add(currentValue);
                        printGrid(logWriteStream);
                    }
                }
            }
            // Couldn't place currentValue in any viable neighbor
            return false;
        }

        @Override
        public String getName() {
            return NAME;
        }
    }

    private void solve(Solver solver) {
        System.out.println("Initial state:");
        printGrid(System.out);
        logWriteStream.println("Attempting to solve with " + solver.getName());
        if (initializeSolution(solver)) {
            System.out.println("Puzzle solved successfully with " + 
                    recursionSteps + " recursive calls.");
        } else {
            System.out.println("Could not find solution within " + 
                    recursionSteps + " steps. Solution does not exist?");
        }
        printGrid(System.out);
    }
    
    
    
    /**
     * Adds the neighbors of the given node to the grid representation,
     * if they are valid and aren't in the grid already.
     * 
     * @param node 
     */
    private void addNeighbors(CubeHex node) {
        //System.err.println("Adding neighbors of: " + node.toString());
        for (CubeHex neighbor: node.getNeighbors()) {
            if (isValidCoordinate(neighbor) && !grid.containsKey(neighbor)) {
                //System.err.println(node.toString());
                grid.put(neighbor, EMPTY_HEX);
                addNeighbors(neighbor);
            }
        }
    }
    
    private HashSet<CubeHex> getEmptyNeighbors(CubeHex hex) {
        HashSet<CubeHex> neighbors = new HashSet();
        for (CubeHex neighbor: hex.getNeighbors()) {
            if (grid.containsKey(neighbor) && grid.get(neighbor) == EMPTY_HEX) {
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }
    
    private HashSet<Integer> getAdjacentValues(CubeHex hex) {
        HashSet<Integer> neighbors = new HashSet();
        for (CubeHex neighbor: hex.getNeighbors()) {
            if (grid.containsKey(neighbor) && grid.get(neighbor) != EMPTY_HEX) {
                neighbors.add(grid.get(neighbor));
            }
        }
        return neighbors;
    }
    
    private boolean legalPosition(int value) {
        CubeHex position = getPosition(value);
        // The values that need to be adjacent to this one
        HashSet<Integer> expectedNeighbors = new HashSet<>();
        if (value > 1) {
            expectedNeighbors.add(value - 1);
        }
        if (value < grid.size()) {
            expectedNeighbors.add(value + 1);
        }
        
        return getAdjacentValues(position).containsAll(expectedNeighbors);
    }
    
    private boolean checkSolution() {
        for (int i = 1; i <= grid.size(); i++) {
            if (!legalPosition(i)) {
                return false;
            }
        }
        return true;
    }
    
    // The number of hexes in the grid
    private int getGridSize() {
        int size = 1;
        for (int i = 1; i <= maxRadius; i++) {
            size += 6*i;
        }
        return size;
    }
    
    private boolean isValidCoordinate (CubeHex point) {
        return point.getX() + point.getY() + point.getZ() == 0 &&
               Math.abs(point.getX()) <= maxRadius &&
               Math.abs(point.getY()) <= maxRadius &&
               Math.abs(point.getZ()) <= maxRadius;
    }
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        HexGridPuzzle puzzle = null;
        try {
            puzzle = new HexGridPuzzle();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        // Indicate that the game loop is still running
        boolean running = true;
        while (running) {
            System.out.println("What would you like to do?");
            System.out.println("1) Solve using a simple DFS algorithm.");
            System.out.println("2) Solve through pathfinding.");
            try {
                int selection = input.nextInt();
                switch (selection) {
                    case 1:
                        puzzle.solve(puzzle.new DFSSolver());
                        running = false;
                        break;
                    case 2:
                        puzzle.solve(puzzle.new PathSolver());
                        running = false;
                        break;
                    default:
                        System.out.println("That is not a valid option.\n");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter an integer number.\n");
                input.nextLine(); // remove the faulty line from the stream
            } finally {
                try {
                    puzzle.logWriteStream.close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    System.err.println(e.getStackTrace());
                }
            }
            
        }
    }   
}
