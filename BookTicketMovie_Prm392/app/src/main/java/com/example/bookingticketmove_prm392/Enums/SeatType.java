package com.example.bookingticketmove_prm392.Enums;

public enum SeatType {
    REGULAR,
    PREMIUM,
    VIP;

    public static SeatType fromString(String text) {
        for (SeatType b : SeatType.values()) {
            if (b.name().equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}
