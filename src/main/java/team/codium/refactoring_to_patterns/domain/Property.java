package team.codium.refactoring_to_patterns.domain;

public class Property {
    private final int id;
    private final String description;
    private final String postalCode;
    private final int price;
    private final int numberOfRooms;
    private final int squareMeters;
    private final int ownerId;

    public Property(int id, String description, String postalCode, int price, int numberOfRooms, int squareMeters, int ownerId) {
        this.id = id;
        this.description = description;
        this.postalCode = postalCode;
        this.price = price;
        this.numberOfRooms = numberOfRooms;
        this.squareMeters = squareMeters;
        this.ownerId = ownerId;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public int getPrice() {
        return price;
    }

    public int getNumberOfRooms() {
        return numberOfRooms;
    }

    public int getSquareMeters() {
        return squareMeters;
    }

    public int getOwnerId() {
        return ownerId;
    }
}
