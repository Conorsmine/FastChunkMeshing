package com.conorsmine.net.gpufastraytrace.casting;

import com.conorsmine.net.gpufastraytrace.util.MathUtil;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.joml.Vector3d;

import java.util.concurrent.CompletableFuture;

public abstract class RayCastableArea {
    private final ChunkMeshProvider chunkMeshProvider;
    private final RayCaster rayCaster;

    public RayCastableArea(@NonNull ChunkMeshProvider chunkMeshProvider, @NonNull RayCaster rayCaster) {
        this.chunkMeshProvider = chunkMeshProvider;
        this.rayCaster = rayCaster;
    }

    public CompletableFuture<CastingResult> cast(double x, double y, double z, Vector3d dir) {
        final CompletableFuture<CastingResult> future = new CompletableFuture<>();
        runCast(new CastingRequest(x, y, z, dir), future);
        return future;
    }

    public CompletableFuture<CastingResult> cast(Location dirLoc) {
        final CompletableFuture<CastingResult> future = new CompletableFuture<>();
        runCast(new CastingRequest(dirLoc.getX(), dirLoc.getY(), dirLoc.getZ(), dirLoc.getDirection().toVector3d()), future);
        return future;
    }

    private void runCast(CastingRequest castData, CompletableFuture<CastingResult> future) {
        future.complete(
                rayCaster.cast(chunkMeshProvider, castData)
        );
    }

    private int[] getChunkPosition(double coordX, double coordZ) {
        return new int[] {
                MathUtil.floor(coordX) >> 4,
                MathUtil.floor(coordZ) >> 4
        };
    }
}
