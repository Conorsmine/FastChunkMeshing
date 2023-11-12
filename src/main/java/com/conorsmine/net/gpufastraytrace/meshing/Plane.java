package com.conorsmine.net.gpufastraytrace.meshing;

import com.conorsmine.net.gpufastraytrace.util.BitUtil;

public class Plane {
    private final long data;

    protected Plane(byte x1, short y1, byte z1, byte x2, short y2, byte z2) {
        this.data = BitUtil.compressCoordsToLong(x1, y1, z1, x2, y2, z2);
    }

    public int[] getCoords() {
        return BitUtil.uncompressChunkCoords(data);
    }

    @Override
    public String toString() {
        final int[] coords = getCoords();
        return String.format("Plane{One=[%d, %d, %d], Two=[%d, %d, %d]}", coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
    }
}
