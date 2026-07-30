package com.example;

public record RecordWithCompactConstructor(int quantity) {
  public RecordWithCompactConstructor {
    if (quantity <= 0) {
      System.out.println("mutate me");
      throw new IllegalArgumentException("quantity must be positive");
    }
  }

  @Override
  public int quantity() {
    if (quantity > 100) {
      return 100;
    }
    return quantity;
  }
}
