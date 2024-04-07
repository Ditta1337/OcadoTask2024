# ocado internship task 2024
#### Artur Dwornik

## Task:
Create a java library that will split the user basket into the smallest subset of avaible delivery types. The correct output is the one that has the smallest number of delivery types and contains delivery type with largest number of products delivered.

#### Example:
config.json:
```json
{
    "Carrots (1kg)": ["Express Delivery", "Click&Collect"],
    "Cold Beer (330ml)": ["Express Delivery"],
    "Steak (300g)": ["Express Delivery", "Click&Collect"],
    "AA Battery (4 Pcs.)": ["Express Delivery", "Courier"],
    "Espresso Machine": ["Courier", "Click&Collect"],
    "Garden Chair": ["Courier"]
}
```

listOfBasketItems (list of strings):
```json
[ 
    "Steak (300g)",
    "Carrots (1kg)",
    "AA Battery (4 Pcs.)",
    "Cold Beer (330ml)",
    "Espresso Machine",
    "Garden Chair"
]
```

result:
```json
{
    "Courier": ["Garden Chair", "Espresso Machine"],
    "Express Delivery": ["Cold Beer (330ml)", "Steak (300g)", "AA Battery (4 Pcs.), Carrots (1kg)"],
}
```

## Usage:
1. Make sure you have Java 17 installed.
2. In your gradle project add the following lines to your build.gradle file:
```gradle
repositories {
    mavenCentral()
    flatDir {
        dirs '/path/to/directory/where/BasketSplitter-0.1.0/jar/is/located'
    }
}

dependencies {
    // other dependencies
    implementation name: 'BasketSplitter-0.1.0'
}
```
3. import library:
```java
import com.ocado.basket.BasketSplitter;
```
4. instantiate BasketSplitter class with absolute path to config.json file:
```java
BasketSplitter bs = new BasketSplitter("/path/to/config.json");
```
5. call bs.split() method with list of basket items (strings) as an argument:
```java
Map<String, List<String>> result = bs.split(listOfBasketItems);
```

## Other info:
- The library was deveoped on Java 17.
- The library is using `com.googlecode.json-simple:json-simple:1.1.1` library to parse JSON files and `org.junit.jupiter:junit-jupiter:5.8.1` library for testing.
- The library was built using Gradle and fat-jar was created using `shadowJar` plugin.
