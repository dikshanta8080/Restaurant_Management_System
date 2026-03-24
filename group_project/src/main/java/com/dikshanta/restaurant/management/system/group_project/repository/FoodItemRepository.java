package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.model.entities.FoodItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    List<FoodItem> findByRestaurantId(Long restaurantId);
    
    Page<FoodItem> findByCategoryId(Long categoryId, Pageable pageable);

    @Query("""
            SELECT f FROM FoodItem f
            WHERE (:categoryId IS NULL OR f.category.id = :categoryId)
              AND (:search IS NULL OR LOWER(CAST(f.name AS string)) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
              AND (:minPrice IS NULL OR f.price >= :minPrice)
              AND (:maxPrice IS NULL OR f.price <= :maxPrice)
            """)
    Page<FoodItem> findByFilters(@Param("categoryId") Long categoryId,
                                 @Param("search") String search,
                                 @Param("minPrice") java.math.BigDecimal minPrice,
                                 @Param("maxPrice") java.math.BigDecimal maxPrice,
                                 Pageable pageable);
}
