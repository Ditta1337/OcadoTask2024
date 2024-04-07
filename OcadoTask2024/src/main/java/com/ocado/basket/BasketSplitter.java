package com.ocado.basket;

import java.util.*;
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static java.lang.Math.pow;

public class BasketSplitter {
    private final HashMap<String, List<String>> config;

    public BasketSplitter(String absolutePathToConfigFile) {
        JSONParser parser = new JSONParser();
        try {
            // parse JSON to HashMap
            config = new HashMap<>((JSONObject) parser.parse(new FileReader(absolutePathToConfigFile)));
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid path to config file");
        } catch (ParseException e) {
            throw new IllegalArgumentException("Config file must be a valid JSON file");
        }
    }

    public Map<String, List<String>> split(List<String> items) {
        // smart brute force checking all combinations of 1 to n delivers,
        // where n is the number of all possible delivers
        // returning early if optimal basket is found

        // create delivery -> items map
        HashMap<String, List<String>> deliveryToItemsMap = getDeliveryToItemsMap(items);
        // create delivery -> items map copy we will keep modifying later
        HashMap<String, List<String>> deliveryToItemsMapCopy = new HashMap<>(deliveryToItemsMap);
        // initialize final basket
        Map<String, List<String>> returnBasket = new HashMap<>();
        // number of all possible delivers
        int allDeliveries = deliveryToItemsMap.size();
        // size of biggest set of items delivered by one deliver in returning basket
        int biggestItemCount = -1;
        // initialize format for binary representation
        String format = "%" + allDeliveries + "s";
        for (int i = 1; i <= allDeliveries; i++) {
            for (int j = 1; j < pow(2, allDeliveries); j++) {
                // create bit mask
                String binary = Integer.toBinaryString(j);
                // first check if all items can be delivered by one, two, three... delivers
                if (countOnes(binary) == i) {
                    // fill the binary with zeros so its length matches the number of all delivers
                    String zeroFilledBinary = String.
                            format(format, binary).
                            replace(' ', '0');
                    // get list of chosen delivers
                    List<String> chosenDeliveries = parseBinaryToDeliveries(
                            zeroFilledBinary,
                            deliveryToItemsMapCopy
                    );
                    // check if all items can be delivered by chosen delivers
                    if (checkIfAllItemsAreDelivered(chosenDeliveries, items, deliveryToItemsMapCopy)) {
                        // if so create basket
                        Map<String, List<String>> basket = createBasket(
                                chosenDeliveries,
                                items.size(),
                                deliveryToItemsMapCopy
                        );
                        // get number of most items delivered by one deliver in this basket
                        int basketCount = getItemCount(basket);
                        // if it's bigger than the last ones, we rather take this basket
                        if (basketCount > biggestItemCount) {
                            biggestItemCount = basketCount;
                            returnBasket = basket;
                        }
                        // create copy of the map for next iteration, since we modified it
                        deliveryToItemsMapCopy = new HashMap<>(deliveryToItemsMap);
                    }
                }
            }
            // if we found optimal basket with m delivers,
            // none of the baskets with m + i (i > 0) delivers will be better
            if (!returnBasket.isEmpty()) {
                return returnBasket;
            }
        }
        // if we didn't find any basket, return null
        return null;
    }

    private int countOnes(String binary) {
        int count = 0;
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                count += 1;
            }
        }
        return count;
    }

    private Map<String, List<String>> createBasket(
            List<String> chosenDeliveries,
            int itemsToProcess, HashMap<String,
            List<String>> deliveryToItemsMap
    ) {
        // initialize basket
        Map<String, List<String>> basket = new HashMap<>();
        // repeat until all items are processed
        while (itemsToProcess > 0) {
            // get delivery with most items from chosen delivers
            String deliveryWithMostItems = getDeliveryWithMostItems(
                    chosenDeliveries,
                    deliveryToItemsMap,
                    basket.keySet()
            );
            // add deliver with most items to the basket
            List<String> itemsToDeliver = List.copyOf(deliveryToItemsMap.get(deliveryWithMostItems));
            basket.put(deliveryWithMostItems, itemsToDeliver);
            // remove deliver and all of its items from the map, since they are already processed
            itemsToProcess -= removeDeliversItemsFromMap(
                    deliveryWithMostItems,
                    itemsToDeliver,
                    deliveryToItemsMap
            );
        }
        return basket;
    }

    private int getItemCount(Map<String, List<String>> basket) {
        // get size of biggest set of items delivered by one deliver in basket
        int biggestItemCount = 0;
        for (List<String> items : basket.values()) {
            if (items.size() > biggestItemCount) {
                biggestItemCount = items.size();
            }
        }
        return biggestItemCount;
    }

    private int removeDeliversItemsFromMap(
            String deliver,
            List<String> items,
            HashMap<String, List<String>> deliveryToItemsMap
    ) {
        // removes given delivers items from deliver -> items map
        // also removes this deliver from the map, since all its items are processed
        int processed = 0;
        // remove processed items from the map
        for (String item : items) {
            for (String deliveryName : config.get(item)) {
                deliveryToItemsMap.get(deliveryName).remove(item);
            }
            processed += 1;
        }
        // remove deliver from the map
        deliveryToItemsMap.remove(deliver);

        return processed;
    }

    private String getDeliveryWithMostItems(
            List<String> chosenDeliveries,
            Map<String, List<String>> deliveryToItemsMap,
            Set<String> processedDeliveries
    ) {
        // finds and returns deliver with the most items from chosen delivers
        // that has not been processed yet
        String deliveryWithMostItems = null;
        int mostItems = 0;
        for (String delivery : chosenDeliveries) {
            // check if delivery has been processed yet
            if (!processedDeliveries.contains(delivery)) {
                int itemsCount = deliveryToItemsMap.get(delivery).size();
                if (itemsCount > mostItems) {
                    mostItems = itemsCount;
                    deliveryWithMostItems = delivery;
                }
            }
        }
        return deliveryWithMostItems;
    }

    private boolean checkIfAllItemsAreDelivered(
            List<String> chosenDeliveries,
            List<String> items,
            Map<String, List<String>> deliveryToItemsMap
    ) {
        // check if chosen delivers can deliver all items
        Set<String> itemsDelivered = new HashSet<>();
        for (String delivery : chosenDeliveries) {
            itemsDelivered.addAll(deliveryToItemsMap.get(delivery));
        }
        return itemsDelivered.containsAll(items);
    }

    private List<String> parseBinaryToDeliveries(
            String binary,
            HashMap<String, List<String>> deliveryToItemsMap
    ) {
        // get list of chosen delivers from binary representation
        // where 1 means deliver is chosen
        List<String> chosenDeliveries = new ArrayList<>();
        List<String> deliveries = new ArrayList<>(deliveryToItemsMap.keySet());
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '1') {
                chosenDeliveries.add(deliveries.get(i));
            }
        }
        return chosenDeliveries;
    }

    private HashMap<String, List<String>> getDeliveryToItemsMap(List<String> items) {
        // remap the config to delivery -> items map
        // take into consideration only items that are in the initial item list (initial basket)!
        HashMap<String, List<String>> deliveryToItemsMap = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : config.entrySet()) {
            String item = entry.getKey();
            List<String> deliveries = entry.getValue();

            // if item is in the initial list (initial basket)
            if (items.contains(item)) {
                for (String delivery : deliveries) {
                    List<String> itemsDelivered = deliveryToItemsMap.get(delivery);
                    // if delivery already exists in the map, add the item to the list
                    if (itemsDelivered != null) {
                        itemsDelivered.add(item);
                        // if delivery does not exist in the map, create new entry with the item
                    } else {
                        List<String> newItemsDelivered = new ArrayList<>();
                        newItemsDelivered.add(item);
                        deliveryToItemsMap.put(delivery, newItemsDelivered);
                    }
                }
            }
        }

        return deliveryToItemsMap;
    }
}