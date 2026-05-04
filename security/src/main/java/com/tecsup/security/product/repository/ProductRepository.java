package com.tecsup.security.product.repository;

import com.tecsup.security.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByRegion(String region);
    List<Product> findByOwner_Id(Long ownerId);
}
