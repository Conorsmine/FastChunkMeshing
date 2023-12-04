package com.conorsmine.net.fastchunkmeshing.meshing;

import com.conorsmine.net.fastchunkmeshing.util.MathUtil;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>Creates a collection of Meshes from a chunk.</p>
 * <p>Hereby it only considers if the block is air or not.</p>
 */
public final class MeshBuilder {

    private static final int BUILDER_THREAD_COUNT = 5;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(BUILDER_THREAD_COUNT);

    private final Queue<Chunk> parseChunks = new ConcurrentLinkedQueue<>();

    public MeshBuilder addChunk(final Chunk chunk) {
        parseChunks.add(chunk);
        return this;
    }

    public MeshBuilder addChunk(final World world, int chunkX, int chunkZ) {
        parseChunks.add(world.getChunkAt(chunkX, chunkZ));
        return this;
    }

    /**
     * "endX" and "endZ" are inclusive
     */
    public MeshBuilder addChunkArea(final World world, int startX, int startZ, int endX, int endZ) {
        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                addChunk(world, x, z);
            }
        }
        return this;
    }

    public CompletableFuture<Collection<ChunkMesh>> build() {
        final CompletableFuture<Collection<ChunkMesh>> future = new CompletableFuture<>();
        final Collection<ChunkMesh> chunkMeshes = new ConcurrentLinkedQueue<>();

        final int toComplete = parseChunks.size();
        final AtomicInteger finished = new AtomicInteger(0);

        if (toComplete == 0) return CompletableFuture.completedFuture(chunkMeshes);

        for (Chunk parseChunk : parseChunks) {
            final ChunkSnapshot chunkSnapshot = parseChunk.getChunkSnapshot(true, false, false);
            final int yMinWorld = parseChunk.getWorld().getMinHeight();
            final int yMax = parseChunk.getWorld().getMaxHeight();

            EXECUTOR.submit(() -> {
                final int yMin = getHighestYValue(chunkSnapshot, yMinWorld);
                chunkMeshes.add(meshChunk(chunkSnapshot, yMin, yMax));

                if (finished.incrementAndGet() == toComplete)
                    future.complete(chunkMeshes);
            });
        }

        return future;
    }

    /////////////////////////////////////////////////
    // Greedy meshing algorithm
    /////////////////////////////////////////////////

    private static ChunkMesh meshChunk(final ChunkSnapshot chunk, int yMin, int yMax) {
        final List<Cuboid> cuboids = calculateCuboids(createChunkBitLayers(chunk, yMin, yMax), yMin);
        final ChunkMesh chunkMesh = new ChunkMesh(chunk.getX(), chunk.getZ(), cuboids);
        return chunkMesh;
    }

    private static List<Cuboid> calculateCuboids(short[][] chunkBitLayers, int yMin) {
        final List<Cuboid> cuboids = new LinkedList<>();

        for (int i = 0; i < chunkBitLayers.length; i++) {
            final short[] bitLayer = chunkBitLayers[i];
            int width = -1, length = -1, height = -1;

            for (int i1 = 0; i1 < bitLayer.length; i1++) {
                final short[] points = getLayerMaskPoints(bitLayer[i1]);

                for (short point : points) {
                    final short[] cuboidLayerMask = getPlanarLayerMask(bitLayer, point, i1);

                    width = (Integer.bitCount(cuboidLayerMask[0]) > 16) ? Integer.bitCount(cuboidLayerMask[0]) - 16 : Integer.bitCount(cuboidLayerMask[0]);
                    length = cuboidLayerMask.length;
                    height = getCuboidHeight(chunkBitLayers, cuboidLayerMask, i1, i);

                    for (int j = i; j < height + i; j++) removeLayerMask(chunkBitLayers[j], cuboidLayerMask, i1);

                    final Cuboid e = new Cuboid(
                            (byte) i1,
                            (short) (yMin + i),
                            (byte) getFirstOne(cuboidLayerMask[0]),
                            (byte) (i1 + length),
                            (short) (yMin + i + height),
                            (byte) (getFirstOne(cuboidLayerMask[0]) + width)
                    );
                    cuboids.add(e);
                }
            }
        }

        return cuboids;
    }

    private static short[][] createChunkBitLayers(final ChunkSnapshot chunk, int yMin, int yMax) {
        final short[][] chunkLayers = new short[yMax - yMin][16];

        for (int y = yMin; y < yMax; y++) {
            chunkLayers[y + Math.abs(yMin)] = createBitLayer(chunk, y);
        }

        return chunkLayers;
    }

    private static short[] createBitLayer(final ChunkSnapshot chunk, int yLevel) {
        final short[] blockLayer = new short[16];

        for (byte x = 1; x <= 16; x++) {
            for (byte z = 1; z <= 16; z++) {
                boolean passable = chunk.getBlockType(x - 1, yLevel, z - 1).isAir();
                if(passable) continue;

                short bitMask = (short) Math.pow(2, (z - 1));
                blockLayer[x - 1] |= bitMask;
            }
        }

        return blockLayer;
    }

    // rowMask: 0000000000000011
    // bitLayer:     vv
    // 1011110001110011
    // 1011110001110011
    // 1000000001110011
    //               VV
    //               ||
    // => [          VV
    // 0000000000000011
    // 0000000000000011
    // 0000000000000011
    // ]
    private static short[] getPlanarLayerMask(short[] bitLayer, short rowMask, int layerStartIndex) {
        final List<Short> mask = new LinkedList<>();

        for (int i = layerStartIndex; i < bitLayer.length; i++) {
            int result = (bitLayer[i] & rowMask);
            if (result == rowMask) mask.add(rowMask);
            else break;
        }

        return listTooArr(mask);
    }

    // line: 1011110001110010 ->
    // [
    //  1000000000000000
    //  0011110000000000
    //  0000000001110000
    //  0000000000000010
    // ]
    private static short[] getLayerMaskPoints(short line) {
        final List<Short> startingPoints = new LinkedList<>();
        short linePoints = 0;

        for (int i = 0; i < 16; i++) {
            short pointer = (short) MathUtil.twosPower(i);
            short point = (short) (pointer & line);
            if (point == 0) {
                if (linePoints != 0) startingPoints.add(linePoints);
                linePoints = 0;
            }

            linePoints |= point;
            if (i == 15 && linePoints != 0) startingPoints.add(linePoints);
        }

        return listTooArr(startingPoints);
    }

    private static int getCuboidHeight(final short[][] chunkBitLayers, final short[] layerMask, int layerStartIndex, int heightStartIndex) {
        int height = 1;
        for (int y = ++heightStartIndex; y < chunkBitLayers.length - 1; y++) {
            if (!layerMatchesMask(chunkBitLayers[y], layerMask, layerStartIndex)) break;
            height++;
        }

        return height;
    }

    // Layer    Mask    Result
    // 110      010
    // 010      010     true
    //
    // 110   -> 110
    // 010      110     false
    private static boolean layerMatchesMask(final short[] bitLayer, final short[] layerMask, int layerStartIndex) {
        for (int i = layerStartIndex; i < layerMask.length + layerStartIndex; i++) {
            final short row = bitLayer[i];
            final short rowMask = layerMask[i - layerStartIndex];

            if ((rowMask & row) != rowMask) return false;
        }

        return true;
    }

    // bitLayer:    layerMask:
    // 11001        11000
    // 11000        11000
    //
    // new bitLayer =>
    // 00001
    // 00000
    private static void removeLayerMask(short[] bitLayer, final short[] layerMask, int layerStartIndex) {
        for (int i = layerStartIndex; i < layerStartIndex + layerMask.length; i++) {
            short rowMask = layerMask[i - layerStartIndex];

            bitLayer[i] &= (short) ~rowMask;
        }
    }

    private static byte getFirstOne(final short bits) {
        if (bits == 0) return 0;
        return (byte) Integer.numberOfTrailingZeros(bits);
    }

    private static int getHighestYValue(ChunkSnapshot chunkSnapshot, int yMin) {
        int val = yMin;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                final int yAt = chunkSnapshot.getHighestBlockYAt(x, z);
                if (yAt > val) val = yAt;
            }
        }

        return val;
    }

    private static short[] listTooArr(final List<Short> l) {
        short[] returnArr = new short[l.size()];
        for (int i = 0; i < l.size(); i++) returnArr[i] = l.get(i);
        return returnArr;
    }
}
