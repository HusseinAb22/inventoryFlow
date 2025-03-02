package com.Store.inventoryflow;
enum ItemCategory {
    ELECTRONICS,
    CLOTHING,
    GROCERY,
    FURNITURE,
    SPORTS,
    BEAUTY,
    TOYS,
    AUTOMOTIVE,
    BOOKS,
    MEDICAL
}
public class Item {
    private String id, name;
    private ItemCategory category;
    private float price;
    private Integer stockCount;

    public Item() {
    }

    public Item(String id, String name, ItemCategory category, float price, Integer stockCount) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stockCount = stockCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ItemCategory getCategory() {
        return category;
    }

    public void setCategory(ItemCategory category) {
        this.category = category;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", stockCount=" + stockCount +
                '}';
    }
}
