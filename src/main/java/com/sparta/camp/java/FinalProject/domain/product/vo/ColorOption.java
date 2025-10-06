package com.sparta.camp.java.FinalProject.domain.product.vo;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ColorOption {

  String colorName;

  List<SizeOption> sizes;

}
