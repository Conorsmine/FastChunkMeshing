package com.conorsmine.net.gpufastraytrace.meshing;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChunkMesh {

    private final List<Cuboid> chunkCuboids;

    private final int x, z;

    protected ChunkMesh(int x, int z, final Collection<Cuboid> chunkCuboids) {
        super();
        this.x = x;
        this.z = z;

        this.chunkCuboids = List.copyOf(chunkCuboids);
    }

    public List<Cuboid> getChunkCuboids() {
        return chunkCuboids;
    }

    public List<Plane> getPlanes() {
        return chunkCuboids.stream()
                .map(Cuboid::toPlanes)
                .flatMap(Stream::of)
                .collect(Collectors.toList());
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }
}
