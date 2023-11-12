package com.conorsmine.net.gpufastraytrace.casting;

public interface RayCaster {

    CastingResult cast(ChunkMeshProvider meshProvider, CastingRequest request);
}
