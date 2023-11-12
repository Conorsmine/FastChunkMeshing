package com.conorsmine.net.gpufastraytrace.casting;

import org.joml.Vector3d;

public record CastingRequest(double x, double y, double z, Vector3d dir) {
}
