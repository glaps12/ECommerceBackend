package com.glaps12.ecommerce.dao;

import com.glaps12.ecommerce.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

@RepositoryRestResource(collectionResourceRel = "productCategory",path = "product-category")
@CrossOrigin("http://localhost:4200")
public interface ProductCategoryRepository  extends JpaRepository<ProductCategory, Long> {
}
