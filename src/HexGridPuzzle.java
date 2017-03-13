
import java.util.*;

/**
 * 
 * @author NadavNV
 */
public class HexGridPuzzle {
    // How recursive steps it took to solve the riddle. Used to compare 
    // different solving algorithms.
    private static int recursionSteps;
    // The maximum absolute value a coordinate can have in any axis
    private final int maxRadius;
    // Default maximum radius
    private static final int DEFAULT_MAX_RADIUS = 4;
    // A collection of the remaining values to place and the nodes in which they can be placed
    private HashMap<Integer, HashSet<CubeHex>> possibleNodes;
    // The actual grid
    private HashMap<CubeHex,Integer> grid;
    // zero indicates an empty hex
    private static int EMPTY_HEX = 0;
    // The initial state of the grid, as given in the riddle by Intel
    private static final HashMap<CubeHex, Integer> INITIAL_STATE;
    static {
        INITIAL_STATE = new HashMap<>();
        INITIAL_STATE.put(new CubeHex(-2, 1, 1), 1);
        INITIAL_STATE.put(new CubeHex(-3, 2, 1), 2);
        INITIAL_STATE.put(new CubeHex(-1, 2, -1), 5);
        INITIAL_STATE.put(new CubeHex(-1, 3, -2), 7);
        INITIAL_STATE.put(new CubeHex(-4, 1, 3), 16);
        INITIAL_STATE.put(new CubeHex(-3, 0, 3), 17);
        INITIAL_STATE.put(new CubeHex(-1, -1, 2), 20);
        INITIAL_STATE.put(new CubeHex(-1, 1, 0), 23);
        INITIAL_STATE.put(new CubeHex(0, 2, -2), 26);
        INITIAL_STATE.put(new CubeHex(1, 1, -2), 31);
        INITIAL_STATE.put(new CubeHex(2, 0, -2), 33);
        INITIAL_STATE.put(new CubeHex(3, -1, -2), 39);
        INITIAL_STATE.put(new CubeHex(3, -2, -1), 40);
        INITIAL_STATE.put(new CubeHex(3, -3, 0), 43);
        INITIAL_STATE.put(new CubeHex(1, -4, 3), 46);
        INITIAL_STATE.put(new CubeHex(1, -3, 2), 47);
        INITIAL_STATE.put(new CubeHex(-1, -2, 3), 58);
        INITIAL_STATE.put(new CubeHex(-4, 0, 4), 61);
    }
    // The values that haven't been placed in the grid yet
    private SortedSet<Integer> remainingValues;
    public enum MenuOption {
        SOLVE_DFS, SOLVE_PATH
    }
    
    
    public HexGridPuzzle() {
        this(DEFAULT_MAX_RADIUS);
    }
    
    public HexGridPuzzle(int maxRadius) throws IllegalArgumentException {
        if (maxRadius < 0) {
            throw new IllegalArgumentException("Radius must be unsigned integer");
        }
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
        for (CubeHex hex: INITIAL_STATE.keySet()) {
            value = INITIAL_STATE.get(hex);
            remainingValues.remove(value);
            grid.put(hex, value);
        }
        // printGrid();
        
    }
    
    private void printGrid() {
        SortedSet<CubeHex> hexes = new TreeSet<>(grid.keySet());
        String separator = "  ";
        // How many nodes to print in the current line
        int nodesToPrint = maxRadius + 1;
        // How mcuh whitespace to leave before the first node
        int spaces = maxRadius;
        // print lines until the middle line
        Iterator it = hexes.iterator();
        for (; nodesToPrint < 2*maxRadius+1; nodesToPrint++, spaces--) {
            for (int i = 0; i < spaces; i++) {
                System.out.print(separator);
            }
            System.out.format("%2d", grid.get(it.next()));
            for (int i = 1; i < nodesToPrint; i++) {
                System.out.format(separator + "%2d", grid.get(it.next()));
            }
            System.out.println("");
        }
        // print remaining lines
        for (/*nodesToPrint = 2*maxRadius, spaces = 1*/; nodesToPrint >= maxRadius+1; nodesToPrint--, spaces++) {
            for (int i = 0; i < spaces; i++) {
                System.out.print(separator);
            }
            System.out.format("%2d", grid.get(it.next()));
            for (int i = 1; i < nodesToPrint; i++) {
                System.out.format(separator + "%2d", grid.get(it.next()));
            }
            System.out.println("");
        }
    }
    
    // Finds the coordinate in the grid of the given value
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
    
    
    public void solvePathfinding() {
        
        
        // I know the first and last values are already placed, so I will
        // not check for those corner cases. If necessary I can add them later
        recursionSteps = 0;
        boolean solved = false;
        PathSolver solver = new PathSolver();
        int nextValue = Collections.min(remainingValues);
        if (nextValue == 1) { // No definitive starting position to search from
            solved = initialzeSolution(solver);
        } else {
            solved = solver.solve(nextValue);
        }
        
    }
    
    private class PathSolver implements Solver {
        
        @Override
        public boolean solve(int nextValue) {
            /*
            Find next remaining value
            Find its parent
            Find next-highest value already placed in the grid
            Find a path from parent to next-highest placed value
            If no such value exists, try each of the parent's neighbors in DFS
            Finally check validity of solution
            */
            System.err.println("Path solving for " + nextValue);
            CubeHex parent = getPosition(nextValue - 1);
            CubeHex target = null;
            for (int value : new TreeSet<>(grid.values())) {
                if (value > nextValue) {
                    target = getPosition(value);
                    break;
                }
            }
            if (target == null) {
                System.err.println("Going DFS");
                // No higher values exist, so we switch to a simple DFS solution
                DFSSolver solver = new DFSSolver();
                solver.solve(nextValue);
            }
            recursionSteps++;
            // Try possible nodes according to how close they are to the target.
            SortedSet<CubeHex> candidates = new TreeSet<>(new CubeHex.DistanceComparator(target));
            candidates.addAll(parent.getNeighbors());
            for (CubeHex candidate: candidates) {
                if (grid.containsKey(candidate) &&
                        grid.get(candidate) == EMPTY_HEX) {
                    remainingValues.remove(nextValue);
                    grid.put(candidate, nextValue);
                    if (remainingValues.isEmpty()) {
                        return checkSolution();
                    } else if (solve(Collections.min(remainingValues))) {
                        return true;
                    } else {
                        // Undo previous step
                        grid.put(candidate, EMPTY_HEX);
                        remainingValues.add(nextValue);
                    }
                }
            }
            
            return false;
        }
    }
    
    
    
    // In the case that 1 isn't already on the grid, we need to attempt to
    // place it, and then attemp to solve from there.
    private boolean initialzeSolution(Solver solver) {
        recursionSteps = 0;
        HashSet<Integer> placedValues = new HashSet<>(grid.values());
        placedValues.remove(EMPTY_HEX);
        int lowestPlacedValue = Collections.min(placedValues);
        if (lowestPlacedValue != 1) {
            CubeHex target = getPosition(lowestPlacedValue);
            // Attempt to start from all the hexes that are at most 
            // lowerPlacedValue distance from the next placed value. If the 
            // distance is greater than that then the path will never reach
            // that value in time.
            HashSet<CubeHex> candidates = getNodesWithinDistance(target, lowestPlacedValue-1);
            for (CubeHex candidate : candidates) {
                if (grid.get(candidate) == EMPTY_HEX) {
                    // Attempt to place 1 at this position
                    remainingValues.remove(1);
                    grid.put(candidate, 1);
                    if (solver.solve(Collections.min(remainingValues))) {
                        return true;
                    } else {
                        // Undo previous step
                        grid.put(candidate, EMPTY_HEX);
                        remainingValues.add(1);
                    }
                }
            }            
        }
        
        return solver.solve(Collections.min(remainingValues));
    }
    
    // Find all nodes within a certain distance from the given node (exclusing
    // the node itself).
    private HashSet<CubeHex> getNodesWithinDistance(CubeHex node, int distance) {
        HashSet<CubeHex> alreadyChecked = new HashSet<>();
        alreadyChecked.add(node);
        HashSet<CubeHex> result = new HashSet<>();
        HashSet<CubeHex> toCheck = new HashSet<>();
        toCheck.addAll(node.getNeighbors());
        while (!toCheck.isEmpty()) {
            for (CubeHex hex: toCheck) {
                alreadyChecked.add(hex);
                toCheck.remove(hex);
                if (grid.containsKey(hex)) {
                    if (hex.distanceTo(node) <= distance) {
                        result.add(hex);
                    } else if (hex.distanceTo(node) < distance) {
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
        @Override
        public boolean solve(int currentValue) {
            /*
            Find next remaining value
            Find its parent
            Try to place in each of the parent's neightbors via DFS
            Finally check validity of solution
            */
            
            // System.err.println("Trying to place " + currentValue);
            // If the next value is already placed, then the current value must be
            // placed next to it.
            recursionSteps++;
            boolean nextValueExists = !remainingValues.contains(currentValue + 1);
            CubeHex parent = getPosition(currentValue - 1);
            if (nextValueExists) {
                for (CubeHex neighbor: getEmptyNeighbors(parent)) {
                    // Placing currentValue here will make it adjacent to both its
                    // successor and predecessor.
                    if (getAdjacentValues(neighbor).contains(currentValue + 1)) {
                        // Nodes that are part of the initial problem
                        // declaration should not be changed
                        assert !INITIAL_STATE.keySet().contains(neighbor);
                        remainingValues.remove(currentValue);
                        grid.put(neighbor, currentValue);
                        // System.err.println("Placing " + currentValue + " at " + neighbor.toString());
                        if (remainingValues.isEmpty()) {
                            /*
                             * No more values to place, so we check if the current
                             * grid represents a good solution, and cascade the 
                             * answer back up the recursion.
                             */                        
                            return checkSolution();
                        } else if (solve(Collections.min(remainingValues))) {
                            // Found a solution, so we cascade it up the recursion.
                            return true;
                        } else {
                            // undo previous step before checking the next neighbor.
                            // System.err.println("Could not place " + currentValue + " at " + neighbor.toString());
                            grid.put(neighbor, EMPTY_HEX);
                            remainingValues.add(currentValue);
                        }
                    }
                }
            } else {
                for (CubeHex neighbor: getEmptyNeighbors(parent)) {
                    // System.err.println("Placing " + currentValue + " at " + neighbor.toString());

                    // Nodes that are part of the initial problem
                    // declaration should not be changed
                    assert !INITIAL_STATE.keySet().contains(neighbor);
                    remainingValues.remove(currentValue);
                    grid.put(neighbor, currentValue);
                    if (remainingValues.isEmpty()) {
                        /*
                             * No more values to place, so we check if the current
                             * grid represents a good solution, and cascade the 
                             * answer back up the recursion.
                             */                        
                            return checkSolution();
                    } else if (solve(currentValue + 1)) { // Attempt to place the next value
                        // Found a solution, so we cascade it up the recursion.
                        return true;
                    } else {
                        // undo previous step before checking the next neighbor.
                        // System.err.println("Could not place " + currentValue + " at " + neighbor.toString());
                        grid.put(neighbor, EMPTY_HEX);
                        remainingValues.add(currentValue);
                    }
                }
            }
            // Couldn't place currentValue in any viable neighbor
            return false;
        }
    }

    private void solve(MenuOption algorithm) throws IllegalArgumentException {
        Solver solver;
        System.out.println("Initial state:");
        printGrid();
        switch (algorithm) {
            case SOLVE_DFS:
                solver = new DFSSolver();
                break;
            case SOLVE_PATH:
                solver = new PathSolver();
                break;
            default:
                throw new IllegalArgumentException("Invalid algorithm: " + algorithm);
        }
        if (initialzeSolution(solver)) {
            System.out.println("Riddle solved successfully with " + 
                    recursionSteps + " recursive calls.");
        } else {
            System.out.println("Could not find solution within " + 
                    recursionSteps + " steps. Solution does not exist?");
        }
        printGrid();
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
        System.out.println("Boo");
        Scanner input = new Scanner(System.in);
        HexGridPuzzle puzzle = null;
        try {
            puzzle = new HexGridPuzzle();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        // Indicate that the game loop is still running
        boolean running = true;
        while (running) {
            System.out.println("What would you like to do?");
            System.out.println("1) Solve using a simple DFS algorithm.");
            System.out.println("2) Solve through pathfinding. (WIP)");
            try {
                int selection = input.nextInt();
                switch (selection) {
                    case 1:
                        puzzle.solve(MenuOption.SOLVE_DFS);
                        running = false;
                        break;
                    case 2:
                        puzzle.solve(MenuOption.SOLVE_PATH);
                        running = false;
                        break;
                    default:
                        System.out.println("That is not a valid option.\n");
                        break;
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter an integer number.\n");
                input.nextLine(); // remove the faulty line from the stream
            }
            
        }
    }   
}
