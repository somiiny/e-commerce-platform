package com.sparta.camp.java.FinalProject.domain.product.service;


import com.sparta.camp.java.FinalProject.common.exception.ServiceException;
import com.sparta.camp.java.FinalProject.common.exception.ServiceExceptionCode;
import com.sparta.camp.java.FinalProject.domain.product.dto.ProductImageResponse;
import com.sparta.camp.java.FinalProject.domain.product.entity.Product;
import com.sparta.camp.java.FinalProject.domain.product.entity.ProductImage;
import com.sparta.camp.java.FinalProject.domain.product.mapper.ProductImageMapper;
import com.sparta.camp.java.FinalProject.domain.product.repository.ProductImageRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageService {

  private final ProductImageRepository productImageRepository;
  private final ProductImageMapper productImageMapper;

  @Value("${file.dir}")
  private String fileDir;

  public void createProductImageList(Product product, List<MultipartFile> productImageList)
      throws IOException {
    List<ProductImage> newImageList = this.setProductImage(productImageList, product);
    productImageRepository.saveAll(newImageList);
  }

  public List<ProductImageResponse> updateProductImageList (Product product, List<MultipartFile> productImageList
  ) throws IOException {

    productImageRepository.softDeleteByProductId(product.getId(), LocalDateTime.now());

    List<ProductImage> newImageList = this.setProductImage(productImageList, product);
    productImageRepository.saveAll(newImageList);

    return newImageList.stream()
        .map(productImageMapper::toResponse)
        .collect(Collectors.toList());
  }

  private List<ProductImage> setProductImage (List<MultipartFile> productImageList, Product product)
      throws IOException {

    List<ProductImage> newImageList = new ArrayList<>();

    for (int i = 0; i < productImageList.size(); i++) {
      String originalFileName = productImageList.get(i).getOriginalFilename();
      String storedFileName = this.createStoreFileName(originalFileName);
      Path path = Paths.get(fileDir, storedFileName);
      productImageList.get(i).transferTo(path.toFile());

      ProductImage productImage = ProductImage.builder()
          .product(product)
          .originalName(originalFileName)
          .storedName(storedFileName)
          .uploadPath(path.toString())
          .sortOrder(i)
          .isMain(i == 0)
          .build();

      newImageList.add(productImage);
    }
    return newImageList;
  }

  private String createStoreFileName(String originalFilename) {
    String ext = extractExt(originalFilename);
    String uuid = UUID.randomUUID().toString();

    return uuid + "." + ext;
  }

  private String extractExt(String originalFilename) {
    if (originalFilename == null || originalFilename.isEmpty()) {
      throw new ServiceException(ServiceExceptionCode.NOT_FOUND_FILE);
    }
    int pos = originalFilename.lastIndexOf(".");
    return (pos == -1) ? "" : originalFilename.substring(pos + 1);
  }
}
