/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author NadavBC
 */
class AxialHex {

    private final int q;
    private final int r;

    public AxialHex(int q, int r) {
        this.q = q;
        this.r = r;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj.getClass() == this.getClass()) {
            AxialHex other = (AxialHex) obj;
            if (this.q == other.q
                    && this.r == other.r) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return q + (r * 10);
    }

    @Override
    public String toString() {
        return "(" + q + ", " + r + ")";
    }

    public int getQ() {
        return q;
    }

    public int getR() {
        return r;
    }

    /*
        public AxialHex move(CubeHex direction) {
            return new AxialHex(q + direction.x, y + direction.y, z + direction.z);
        }
     */
    public CubeHex toCubeHex() {
        return new CubeHex(q, -q - r, r);
    }

    public int distanceTo(AxialHex other) {
        CubeHex otherCubed = other.toCubeHex();
        return otherCubed.distanceTo(this.toCubeHex());
    }
}
