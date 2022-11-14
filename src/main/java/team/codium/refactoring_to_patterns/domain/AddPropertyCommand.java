package team.codium.refactoring_to_patterns.domain;

public record AddPropertyCommand(int id, String description, String postalCode, int price, int numberOfRooms,
                                 int squareMeters, int ownerId) {
}