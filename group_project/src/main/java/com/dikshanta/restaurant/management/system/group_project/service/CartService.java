package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.CartItemRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.CartItemResponse;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Cart;
import com.dikshanta.restaurant.management.system.group_project.model.entities.CartItem;
import com.dikshanta.restaurant.management.system.group_project.model.entities.FoodItem;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.CartItemRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.CartRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.FoodItemRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final UserRepository userRepository;
    private final SecurityAuditorAware securityAuditorAware;

    private Long getCurrentUserId() {
        return securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
    }

    private Cart getOrCreateUserCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }

    private CartItemResponse mapToResponse(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .foodItemId(cartItem.getFoodItem().getId())
                .foodItemName(cartItem.getFoodItem().getName())
                .price(cartItem.getFoodItem().getPrice())
                .quantity(cartItem.getQuantity())
                .restaurantId(cartItem.getFoodItem().getRestaurant().getId())
                .build();
    }

    @Transactional
    public CartItemResponse addToCart(CartItemRequest request) {
        Long userId = getCurrentUserId();
        Cart cart = getOrCreateUserCart(userId);

        FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                .orElseThrow(() -> new RuntimeException("Food item not found"));

        if (!foodItem.getAvailable()) {
            throw new RuntimeException("Food item is not available");
        }

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getFoodItem().getId().equals(foodItem.getId()))
                .findFirst().orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            return mapToResponse(cartItemRepository.save(existingItem));
        }

        CartItem newItem = CartItem.builder()
                .cart(cart)
                .foodItem(foodItem)
                .quantity(request.getQuantity())
                .build();

        cart.addItem(newItem);
        CartItem savedItem = cartItemRepository.save(newItem);
        return mapToResponse(savedItem);
    }

    @Transactional
    public CartItemResponse updateCartItem(Long cartItemId, Integer quantity) {
        Long userId = getCurrentUserId();
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to update this cart item");
        }

        if (quantity <= 0) {
            removeCartItem(cartItemId);
            return null;
        }

        cartItem.setQuantity(quantity);
        return mapToResponse(cartItemRepository.save(cartItem));
    }

    @Transactional
    public void removeCartItem(Long cartItemId) {
        Long userId = getCurrentUserId();
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new RuntimeException("You are not authorized to remove this cart item");
        }

        Cart cart = cartItem.getCart();
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
    }

    public List<CartItemResponse> getCartItemsForUser() {
        Long userId = getCurrentUserId();
        Cart cart = getOrCreateUserCart(userId);
        return cart.getItems().stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cart.getItems().clear();
            cartRepository.save(cart);
        }
    }
}
