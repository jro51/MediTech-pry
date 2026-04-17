package com.medictech.product_service.service;

import com.medictech.product_service.dto.ProductRequest;
import com.medictech.product_service.dto.ProductResponse;
import com.medictech.product_service.entity.Product;
import com.medictech.product_service.exception.ProductNotFoundException;
import com.medictech.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService{
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        log.info("Obteniendo todos los productos");
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        log.info("Buscando producto con id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        log.info("Creando producto: {}", request.getName());

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .description(request.getDescription())
                .imageSrc(request.getImageSrc())
                .build();

        Product saved = productRepository.save(product);
        log.info("Producto creado con id: {}", saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        log.info("Actualizando producto con id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());
        product.setImageSrc(request.getImageSrc());

        Product updated = productRepository.save(product);
        log.info("Producto actualizado con id: {}", updated.getId());
        return toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Eliminando producto con id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
        log.info("Producto eliminado con id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return product.getStock() >= quantity;
    }

    @Override
    @Transactional
    public void reduceStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStock() < quantity) {
            throw new IllegalArgumentException(
                    "Stock insuficiente para el producto: " + product.getName()
            );
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        log.info("Stock reducido en {} unidades para producto id: {}", quantity, productId);
    }

    // Metodo privado que convierte entidad a DTO de respuesta
    // Patrón Mapper manual — evita dependencias externas como MapStruct
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .description(product.getDescription())
                .imageSrc(product.getImageSrc())
                .iaRecommendation(product.getIaRecommendation())
                .build();
    }
}
