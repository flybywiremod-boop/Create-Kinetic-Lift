package net.flybywire.createkineticlift.Jet;

public class JetFanFuelTank {

    // 2,500 kg = 2,500,000 g
    public static final long MAX_FUEL_G = 2_500_000;

    private long fuelG;

    public long getFuelG() {
        return fuelG;
    }

    public void addFuel(long grams) {
        fuelG = Math.min(fuelG + grams, MAX_FUEL_G);
    }

    public boolean consumeFuel(long grams) {
        if (fuelG < grams) return false;
        fuelG -= grams;
        return true;
    }

    public boolean isFull() {
        return fuelG >= MAX_FUEL_G;
    }
}
