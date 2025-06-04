package com.example.es5roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM product ORDER BY id ASC")
    List<Product> getAll();

    @Query("SELECT * FROM product WHERE id = :productId")
    Product getById(int productId);

    @Query("SELECT * FROM product WHERE product_name LIKE :name")
    List<Product> findByName(String name);

    @Insert
    void insertAll(Product... products);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);
}