package ycheng.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by ycheng on 9/4/17.
 */
@Getter
@Setter
@AllArgsConstructor
public class BuyRecord {
    private int sessionId;
    private String timestamp;
    private int itemId;
    private int price;
    private int quantity;
}
