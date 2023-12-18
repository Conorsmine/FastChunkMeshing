package com.conorsmine.net.fastchunkmeshing.meshing;

import com.conorsmine.net.fastchunkmeshing.util.BitUtil;

public class Plane {
    private final long data;

    protected Plane(byte x1, short y1, byte z1, byte x2, short y2, byte z2) {
        this.data = BitUtil.compressCoordsToLong(x1, y1, z1, x2, y2, z2);
    }

    private int[] getCoords() {
        return BitUtil.uncompressChunkCoords(data);
    }

    public byte getXOne() {
        return (byte) getCoords()[0];
    }

    public byte getXTwo() {
        return (byte) getCoords()[3];
    }

    public short getYOne() {
        return (short) getCoords()[1];
    }

    public short getYTwo() {
        return (short) getCoords()[4];
    }

    public byte getZOne() {
        return (byte) getCoords()[2];
    }

    public byte getZTwo() {
        return (byte) getCoords()[5];
    }

    @Override
    public String toString() {
        final int[] coords = getCoords();
        return String.format("Plane{One=[%d, %d, %d], Two=[%d, %d, %d]}", coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
    }
}
