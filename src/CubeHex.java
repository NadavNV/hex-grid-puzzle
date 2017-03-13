/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author NadavNV
 */

import java.util.*;

class CubeHex implements Comparable<CubeHex> {

    // Possible directions for moving in CubeHex coordinates
    private static final ArrayList<CubeHex> DIRECTIONS = new ArrayList() {
        {
            add(new CubeHex(1, -1, 0));
            add(new CubeHex(1, 0, -1));
            add(new CubeHex(0, 1, -1));
            add(new CubeHex(-1, 1, 0));
            add(new CubeHex(-1, 0, 1));
            add(new CubeHex(0, -1, 1));
        }
    };
    
    private final int x;
    private final int y;
    private final int z;

    public CubeHex(int x, int y, int z) {
        if (x + y + z != 0) {
            throw new IllegalArgumentException("Sum of coordinates must be zero.");
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            CubeHex other = (CubeHex) obj;
            if (this.x == other.x
                    && this.y == other.y
                    && this.z == other.z) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return x + (y * 10) + (z * 100);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public CubeHex move(CubeHex direction) {
        return new CubeHex(x + direction.x, y + direction.y, z + direction.z);
    }

    public AxialHex toAxialHex() {
        return new AxialHex(x, z);
    }

    public int distanceTo(CubeHex other) {
        return Math.max(Math.max(Math.abs(x - other.x), Math.abs(y - other.y)), Math.abs(z - other.z));
    }
    
    public HashSet<CubeHex> getNeighbors() {
        HashSet<CubeHex> neighbors = new HashSet<>();
        CubeHex neighbor;
        for (CubeHex direction: DIRECTIONS) {
            neighbor = move(direction);
            neighbors.add(neighbor);
        }
        return neighbors;
    }

    // To print the hex grid, we want to print top to bottom, left to right.
    // the z coordinate increases from top to bottom and the x coordinate
    // increases from left to right, so this comparison will allow us
    // to sort the grid into printing order
    @Override
    public int compareTo(CubeHex o) {
        if (z == o.z) {
            return Integer.compare(x, o.x);
        } else {
            return Integer.compare(z, o.z);
        }
    }
    
    public static class DistanceComparator implements Comparator<CubeHex> {
        // Compare by distance to this coordinate.
        private CubeHex root;
        
        public DistanceComparator(CubeHex root) {
            this.root = root;
        }
        
        @Override
        public int compare(CubeHex o1, CubeHex o2) {
            return Integer.compare(o1.distanceTo(root), o2.distanceTo(root));
        }
        
    }
}
