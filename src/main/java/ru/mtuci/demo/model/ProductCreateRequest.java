package ru.mtuci.demo.model;

import lombok.*;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {
    private String name;
    private Boolean isBlocked;
}
