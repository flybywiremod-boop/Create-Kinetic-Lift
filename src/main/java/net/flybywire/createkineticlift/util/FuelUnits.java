package net.flybywire.createkineticlift.util;

public class FuelUnits {

    public static final long G_PER_KG = 1_000;
    public static final long KG_PER_BUCKET = 1_000;
    public static final long G_PER_BUCKET = G_PER_KG * KG_PER_BUCKET; // 1,000,000

    private FuelUnits() {}

    public static long toGrams(double value, String unit) {
        return switch (unit) {
            case "g"  -> (long) value;
            case "kg" -> (long) (value * G_PER_KG);
            case "B"  -> (long) (value * G_PER_BUCKET);
            default -> throw new IllegalArgumentException("Unknown fuel unit: " + unit);
        };
    }

    public static double toKg(long grams) {
        return (double) grams / G_PER_KG;
    }

    public static double toBuckets(long grams) {
        return (double) grams / G_PER_BUCKET;
    }
}
