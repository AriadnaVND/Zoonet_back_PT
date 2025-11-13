package com.tecsup.pe.back_zonet.util;

// 💡 CLASE DE UTILIDAD PARA CALCULAR DISTANCIA EN KM (Fórmula Haversine)
public class DistanceCalculator {

    private static final int EARTH_RADIUS_KM = 6371; // Radio de la Tierra en kilómetros

    /**
     * Calcula la distancia en kilómetros entre dos puntos de latitud y longitud.
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convertir grados a radianes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Fórmula de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distancia en kilómetros
        return EARTH_RADIUS_KM * c;
    }
}