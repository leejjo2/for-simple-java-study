package com.example.forsimplestudy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

class Shop {
    private final String name;
    private final Random random;

    public static void delay() {
        int delay = 1000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public Shop(String name) {
        this.name = name;
        random = new Random(name.charAt(0) * name.charAt(1) * name.charAt(2));
    }

    public double getPrice(String product) {
        return calculatePrice(product);
    }

    private double calculatePrice(String product) {
        delay();
        return random.nextDouble() * product.charAt(0) * product.charAt(1);
    }

    public String getName() {
        return name;
    }
}

class PriceFinder {
    private final List<Shop> shops = Arrays.asList(
            new Shop("Shop1"),
            new Shop("Shop2"),
            new Shop("Shop3"),
            new Shop("Shop4"),
            new Shop("Shop5"),
            new Shop("Shop6")
    );

    // 스트림 호출
    public List<String> findPrices1(String product) {
        return shops.stream()
                .map(shop -> String.format("\n %s 가격은 %.2f \n", shop.getName(), shop.getPrice(product)))
                .collect(Collectors.toList());
    }

    // 병렬 스트림 블로킹 호출
    public List<String> findPrices2(String product) {
        return shops.parallelStream()
                .map(shop -> String.format("\n %s 가격은 %.2f \n", shop.getName(), shop.getPrice(product)))
                .collect(Collectors.toList());
    }

    // CompletableFuture 호출
    public List<String> findPrices3(String product) {
        List<CompletableFuture<String>> priceFutures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() ->
                        String.format("\n %s 가격은 %.2f \n", shop.getName(), shop.getPrice(product))))
                .collect(Collectors.toList());
        return priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    // CompletableFuture 호출 (Executor 의 사용)
    private final Executor executor = Executors.newFixedThreadPool(Math.min(shops.size(), 100), (Runnable r) -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    public List<String> findPrices4(String product) {
        List<CompletableFuture<String>> priceFutures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() ->
                        String.format("\n %s 가격은 %.2f \n", shop.getName(), shop.getPrice(product)), executor))
                .collect(Collectors.toList());
        return priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public List<String> findPrices5(String product){
        return shops.stream()
                .map(shop -> String.format("%s:%.2f:GOLD", shop.getName(), shop.getPrice(product)))
                .map(Quote::parse)
                .map(Discount::applyDiscount)
                .collect(Collectors.toList());
    }

    public List<String> findPrices6(String product){
        List<CompletableFuture<String>> priceFutures = shops.stream()
                .map(shop -> CompletableFuture.supplyAsync(() ->
                        String.format("%s:%.2f:GOLD", shop.getName(), shop.getPrice(product)), executor))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> future.thenCompose(quote -> CompletableFuture.supplyAsync(() ->
                        Discount.applyDiscount(quote), executor)))
                .collect(Collectors.toList());

        return priceFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

}

class Discount {
    public static void delay() {
        int delay = 1000;
        try {
            Thread.sleep(delay);
        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }
    }

    public static Double format(Double d){return d;}

    public enum Code{
        NONE(0), SILVER(5), GOLD(10), PLATINUM(15), DIAMOND(20);

        private final int percentage;
        Code(int percentage){this.percentage = percentage;}
    }

    public static String applyDiscount(Quote quote){
        return quote.getShopName() + " price is " + Discount.apply(quote.getPrice(), quote.getDiscountCode());
    }

    private static double apply(double price, Code code){
        delay();
        return format(price * (100 - code.percentage) /100);
    }
}

class Quote{
    private final String shopName;
    private final double price;
    private final Discount.Code discountCode;

    public Quote(String shopName, double price, Discount.Code discountCode){
        this.shopName = shopName;
        this.price = price;
        this.discountCode = discountCode;
    }

    public static Quote parse(String s){
        String[] split = s.split(":");
        String shopName = split[0];
        double price = Double.parseDouble(split[1]);
        Discount.Code discountCode = Discount.Code.valueOf(split[2]);
        return new Quote(shopName, price, discountCode);
    }

    public String getShopName(){
        return shopName;
    }

    public double getPrice(){
        return price;
    }

    public Discount.Code getDiscountCode(){
        return discountCode;
    }

}


@SpringBootTest
class CompletableFutureTest {

    @Test
    void test1() {
        PriceFinder priceFinder = new PriceFinder();
        long start = System.nanoTime();
        System.out.println(priceFinder.findPrices1("Mac"));
        long duration = (System.nanoTime() - start)/1_000_000;
        System.out.println("완료 시간 : " + duration + " msecs");
    }
    @Test
    void test2() {
        PriceFinder priceFinder = new PriceFinder();
        long start = System.nanoTime();
        System.out.println(priceFinder.findPrices2("Mac"));
        long duration = (System.nanoTime() - start)/1_000_000;
        System.out.println("완료 시간 : " + duration + " msecs");
    }
    @Test
    void test3() {
        PriceFinder priceFinder = new PriceFinder();
        long start = System.nanoTime();
        System.out.println(priceFinder.findPrices3("Mac"));
        long duration = (System.nanoTime() - start)/1_000_000;
        System.out.println("완료 시간 : " + duration + " msecs");
    }
    @Test
    void test4() {
        PriceFinder priceFinder = new PriceFinder();
        long start = System.nanoTime();
        System.out.println(priceFinder.findPrices4("Mac"));
        long duration = (System.nanoTime() - start)/1_000_000;
        System.out.println("완료 시간 : " + duration + " msecs");
    }

    @Test
    void test5() {
        PriceFinder priceFinder = new PriceFinder();
        long start = System.nanoTime();
        System.out.println(priceFinder.findPrices5("Mac"));
        long duration = (System.nanoTime() - start)/1_000_000;
        System.out.println("완료 시간 : " + duration + " msecs");
    }
    @Test
    void test6() {
        PriceFinder priceFinder = new PriceFinder();
        long start = System.nanoTime();
        System.out.println(priceFinder.findPrices6("Mac"));
        long duration = (System.nanoTime() - start)/1_000_000;
        System.out.println("완료 시간 : " + duration + " msecs");
    }

}
