package app.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
public class GuideTotalPriceDTO {
    private String name;
    private String email;
    private int tripCount;
    private int totalPrice;
}
