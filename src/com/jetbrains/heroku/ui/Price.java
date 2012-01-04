package com.jetbrains.heroku.ui;

/**
* @author mh
* @since 04.01.12
*/
public class Price implements Comparable<Price> {
    int cents;
    String unit;
    private final String toString;

    public Price(int cents, String unit) {
        this.cents = cents;
        this.unit = unit;
        this.toString = String.format("%.2f USD%s", cents / 100f, unit == null ? "" : "/" + unit);
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public int compareTo(Price o) {
        if (this.cents == o.cents) return 0;
        if (this.cents < o.cents) return -1;
        return 1;
    }
}
