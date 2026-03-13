package com.mskyeye.trace.model;

public class TCalibModel {

   /* public double A;
    public double B;
    public double C;

    public double compute(double azimuthDeg) {
        double rad = Math.toRadians(azimuthDeg);
        return A * Math.cos(rad)
             + B * Math.sin(rad)
             + C;
    }*/
   public double A;
    public double B;
    public double C;
    public double D;
    public double E;
    public double F;

    public double compute(double azimuthDeg, double distanceMeter) {

        double rad = Math.toRadians(azimuthDeg);

        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return A * cos
                + B * sin
                + C
                + D * distanceMeter
                + E * distanceMeter * cos
                + F * distanceMeter * sin;
    }
}
