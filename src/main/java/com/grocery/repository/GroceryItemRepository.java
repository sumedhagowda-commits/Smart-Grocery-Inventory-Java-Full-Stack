package com.grocery.repository;

import com.grocery.model.AppUser;
import com.grocery.model.GroceryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GroceryItemRepository extends JpaRepository<GroceryItem, Long> {
    List<GroceryItem> findByOwnerOrderByItemNameAsc(AppUser owner);
    Optional<GroceryItem> findByIdAndOwner(Long id, AppUser owner);
}
