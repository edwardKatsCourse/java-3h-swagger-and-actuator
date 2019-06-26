package com.telran.controller;

import com.telran.entity.Product;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RestController
public class ProductController {

    private static Set<Product> products = new HashSet<>();

    private static AtomicLong productIds = new AtomicLong(1);


    @PostMapping("/products")
    public Product saveProduct(@RequestBody Product product) {
        product.setId(productIds.getAndIncrement());
        products.add(product);

        return product;
    }

    @PutMapping("/products/{id}")
    public Product updateProduct(
            @PathVariable("id") Long id,
            @RequestParam(value = "seller", required = false) String sellerName,
            @RequestParam(value = "product", required = false) String productName,
            @RequestParam(value = "price", required = false) Double price) {

        Product product = products.stream()
                .filter(x -> x.getId().equals(id))
                .findAny()
                .orElse(null);

        if (product == null) {
            return null;
        }

        if (sellerName != null && !sellerName.isEmpty()) {
            product.setSellerName(sellerName);
        }

        if (productName != null && !productName.isEmpty()) {
            product.setProductName(productName);
        }

        if (price != null && price > 0) {
            product.setPrice(price);
        }

        return product;
    }


    @ApiOperation(value = "Get all products by seller name (case sensitive)", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully found list", response = List.class),
            @ApiResponse(code = 404, message = "Seller not found")
    })
    @GetMapping("/products/{seller}")
    public List<Product> getProductsBySeller(@PathVariable("seller") String seller) {
        List<Product> foundProducts = products.stream()
                .filter(x -> seller.equals(x.getSellerName()))
                .collect(Collectors.toList());

        if (foundProducts.isEmpty()) {
            throw new SellerNotFoundException(seller);
        }

        return foundProducts;
    }

    @DeleteMapping("/products") // ?seller=
    public void deleteAllSellerProducts(@RequestParam("seller") String seller) {
        products = products.stream()
                .filter(x -> !seller.equals(x.getSellerName()))
                .collect(Collectors.toSet());
    }

    @GetMapping("/my-health-check")
    public Status healthCheck() {
        return Status.UP;
    }

    @GetMapping("/my-health-check-2")
    public String healthCheck2() {
        return "OK";
    }
}


@ResponseStatus(value = HttpStatus.NOT_FOUND)
class SellerNotFoundException extends RuntimeException {

    private static final String DEFAULT_MESSAGE_TEMPLATE = "Seller with name %s does not have any products";
    public SellerNotFoundException(String seller) {
        super(String.format(DEFAULT_MESSAGE_TEMPLATE, seller));
    }
}