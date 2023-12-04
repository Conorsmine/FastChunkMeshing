package com.conorsmine.net.fastchunkmeshing.meshing;

public class Cuboid {

    private final byte x1, z1, x2, z2;
    private final short y1, y2;

    private final Plane[] planes;

    protected Cuboid(byte x1, short y1, byte z1, byte x2, short y2, byte z2) {
        this.x1 = (byte) Math.min(x1, x2);
        this.y1 = (short) Math.min(y1, y2);
        this.z1 = (byte) Math.min(z1, z2);
        this.x2 = (byte) Math.max(x1, x2);
        this.y2 = (short) Math.max(y1, y2);
        this.z2 = (byte) Math.max(z1, z2);

        planes = createPlanes();
    }

    private Plane[] createPlanes() {
        /*
                     2
                     ||
                     V
                o--------o
               /|  1    /|
        5 ->  / |      / |
             o--------o  | <- 3
             |  o-----|--o
             | /   0  | /
             |/       |/
             o--------o
                 ^
                ||
                4
         */

        Plane[] planes = new Plane[6];
        planes[0] = new Plane(x1, y1, z1, x2, y1, z2);  // Bottom   ✓
        planes[1] = new Plane(x1, y2, z1, x2, y2, z2);  // Top      ✓
        planes[2] = new Plane(x1, y1, z2, x2, y2, z2);  // Back     ✓
        planes[3] = new Plane(x2, y1, z1, x2, y2, z2);  // Right    ✓
        planes[4] = new Plane(x1, y1, z1, x2, y2, z1);  // Front    ✓
        planes[5] = new Plane(x1, y1, z1, x1, y2, z2);  // Left     ✓
        return planes;
    }

    public Plane[] toPlanes() {
        return planes;
    }

    @Override
    public String toString() {
        return String.format("Cuboid{bottom=[%d, %d, %d], top=[%d, %d, %d]}", x1, y1, z1, x2, y2, z2);
    }
}
