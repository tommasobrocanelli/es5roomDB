package com.example.es5roomdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Product {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "product_name")
    public String productName;

    @ColumnInfo(name = "price")
    public double price;

    @ColumnInfo(name = "description")
    public String description;

    public String toString() {
        return id + " - " + productName + " (€" + String.format("%.2f", price) + ")";
    }
}