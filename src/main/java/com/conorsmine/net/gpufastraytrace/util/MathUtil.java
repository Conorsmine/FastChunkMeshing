package com.conorsmine.net.gpufastraytrace.util;

import com.conorsmine.net.gpufastraytrace.meshing.Plane;
import org.joml.Vector3d;

import java.util.LinkedList;
import java.util.List;

public class MathUtil {

    // https://en.wikipedia.org/wiki/Midpoint_circle_algorithm#Jesko's_Method
    public static int[][] generatePixelCircle(int radius) {
        final List<Integer[]> values = new LinkedList<>();

        double minAngle = Math.acos(1 - (1.0d / radius));
        for(double angle = 0; angle < 360; angle += minAngle) {
            int x = (int) Math.round(radius * Math.cos(angle * Math.PI / 180));
            int y = (int) Math.round(radius * Math.sin(angle * Math.PI / 180));
            values.add(new Integer[] { x, y });
        }

        int[][] arr = new int[values.size()][2];
        for (int i = 0; i < values.size(); i++) arr[i] = new int[] { values.get(i)[0], values.get(i)[1] };
        return arr;
    }

    /**
     * Determines the point of intersection between a plane defined by a point and a normal vector and a line defined by a point and a direction vector.
     *
     * @param planePoint    A point on the plane.
     * @param planeNormal   The normal vector of the plane.
     * @param linePoint     A point on the line.
     * @param lineDirection The direction vector of the line.
     * @return The point of intersection between the line and the plane, null if the line is parallel to the plane.
     */
    // https://stackoverflow.com/questions/5666222/3d-line-plane-intersection
    public static Vector3d lineIntersection(final Vector3d planePoint, final Vector3d planeNormal, final Vector3d linePoint, final Vector3d lineDirection) {
        final Vector3d normLineDir = new Vector3d(lineDirection).normalize();
        if (planeNormal.dot(normLineDir) == 0) return null;

        double t = (planeNormal.dot(planePoint) - planeNormal.dot(linePoint)) / planeNormal.dot(normLineDir);
        return new Vector3d(linePoint)
                .add(new Vector3d(normLineDir).mul(t));
    }

    // https://www.spigotmc.org/threads/get-chunk-coordinate-from-location.456619/
    public static int floor(double num) {
        int floor = (int) num;
        return floor == num ? floor : floor - (int) (Double.doubleToRawLongBits(num) >>> 63);
    }

    /**
     * <p>Calculates:</p>
     * <pre>2 ^ pow</pre>
     * @param pow Power of 2; Must be <64
     * @return A power of two
     */
    public static long twosPower(int pow) {
        if (pow >= 64) throw new UnsupportedOperationException("Does not support powers larger than 63. Requested power was " + pow);
        return 1L << pow;
    }
}
