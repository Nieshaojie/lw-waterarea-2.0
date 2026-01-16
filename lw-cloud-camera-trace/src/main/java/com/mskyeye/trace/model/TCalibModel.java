package com.mskyeye.trace.model;

public class TCalibModel {

    public double A;
    public double B;
    public double C;

    public double compute(double azimuthDeg) {
        double rad = Math.toRadians(azimuthDeg);
        return A * Math.cos(rad)
             + B * Math.sin(rad)
             + C;
    }
}
