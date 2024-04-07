package com.ocado.basket;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class BasketSplitterTest {

    @Test
    void testInitSuccess() {
        String absolutePath = "src/test/resources/test_config.json";
        BasketSplitter bs = new BasketSplitter(absolutePath);
        assertNotNull(bs);
    }

    @Test
    void testInitFailInvalidPath() {
        String absolutePath = "/some/fake/path/to/config.json";
        try {
            BasketSplitter bs = new BasketSplitter(absolutePath);
            fail("Exception not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid path to config file", e.getMessage());
        }
    }

    @Test
    void testInitFailInvalidJson() {
        String absolutePath = "src/test/resources/test_config_invalid.json";
        try {
            BasketSplitter bs = new BasketSplitter(absolutePath);
            fail("Exception not thrown");
        } catch (IllegalArgumentException e) {
            assertEquals("Config file must be a valid JSON file", e.getMessage());
        }
    }

    @Test
    void testSplit() {
        String absolutePath = "src/test/resources/test_config.json";
        BasketSplitter bs = new BasketSplitter(absolutePath);
        List<String> list = List.of(
                "Steak (300g)",
                "Carrots (1kg)",
                "AA Battery (4 Pcs.)",
                "Cold Beer (330ml)",
                "Espresso Machine",
                "Garden Chair"
        );
        Map<String, List<String>> basket = bs.split(list);

        Map<String, List<String>> correctBasket = Map.of(
                "Express Delivery", List.of("Steak (300g)", "Carrots (1kg)", "AA Battery (4 Pcs.)", "Cold Beer (330ml)"),
                "Courier", List.of("Espresso Machine", "Garden Chair")
        );

        assertTrue(basket.keySet().containsAll(correctBasket.keySet()));
        assertTrue(basket.get("Express Delivery").containsAll(correctBasket.get("Express Delivery")));
        assertTrue(basket.get("Courier").containsAll(correctBasket.get("Courier")));
    }
}