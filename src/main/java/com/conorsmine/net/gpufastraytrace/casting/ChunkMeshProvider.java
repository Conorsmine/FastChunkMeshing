package com.conorsmine.net.gpufastraytrace.casting;

import com.conorsmine.net.gpufastraytrace.meshing.ChunkMesh;

public interface ChunkMeshProvider {

    ChunkMesh provide(int chunkX, int chunkZ);
}
