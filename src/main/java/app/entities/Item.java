package app.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Item {
    private String name;
    private int weightInGrams;
    private int quantity;
    private String description;
    private String category;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BuyingOptions> buyingOptions;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuyingOptions{
        private String shopName;
        private String shopUrl;
        private int price;
    }
}

