package com.morrow.commerce.config;

import com.morrow.commerce.customer.Customer;
import com.morrow.commerce.customer.CustomerRepository;
import com.morrow.commerce.inventory.Product;
import com.morrow.commerce.inventory.ProductRepository;
import com.morrow.commerce.order.CustomerOrder;
import com.morrow.commerce.order.OrderRepository;
import com.morrow.commerce.payment.Payment;
import com.morrow.commerce.payment.PaymentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DemoData {
    @Bean
    CommandLineRunner seedDemoData(CustomerRepository customers, ProductRepository products,
                                   OrderRepository orders, PaymentRepository payments) {
        return args -> {
            if (customers.count() > 0) return;
            customers.saveAll(List.of(
                    new Customer("Aarav Mehta", "aarav@example.test", "Mumbai", "UPI", 1845000, 12, true),
                    new Customer("Isha Kapoor", "isha@example.test", "Delhi", "CARD", 928000, 8, false),
                    new Customer("Rohan Shah", "rohan@example.test", "Ahmedabad", "UPI", 674000, 5, false),
                    new Customer("Ananya Iyer", "ananya@example.test", "Chennai", "UPI", 2189000, 18, true),
                    new Customer("Kabir Khanna", "kabir@example.test", "Pune", "COD", 340000, 3, false)
            ));
            products.saveAll(List.of(
                    new Product("Everyday Linen Shirt", "Apparel", "Sand / M", "Mumbai", "IN", "INR", 249900, 142, 0),
                    new Product("Cloud Knit Sneakers", "Footwear", "White / 8", "Bengaluru", "IN", "INR", 459900, 8, 0),
                    new Product("Sculpt Ceramic Mug", "Home", "Chalk", "Delhi", "IN", "INR", 89900, 86, 0),
                    new Product("Weekend Carryall", "Accessories", "Olive", "Mumbai", "IN", "INR", 329900, 4, 0),
                    new Product("Ribbed Lounge Set", "Apparel", "Rose / S", "Hyderabad", "IN", "INR", 389900, 57, 0)
            ));
            var seededOrders = orders.saveAll(List.of(
                    new CustomerOrder("Aarav Mehta", "IN", "INR", "UPI · PhonePe", "Processing", 489900, 2),
                    new CustomerOrder("Isha Kapoor", "IN", "INR", "Card · Visa", "Shipped", 1245000, 1),
                    new CustomerOrder("Rohan Shah", "IN", "INR", "UPI · GPay", "Processing", 219900, 3),
                    new CustomerOrder("Ananya Iyer", "IN", "INR", "COD", "Delivered", 89900, 1),
                    new CustomerOrder("Kabir Khanna", "IN", "INR", "Card · Mastercard", "Refunded", 674000, 2)
            ));
            payments.saveAll(List.of(
                    new Payment("ORD-" + seededOrders.get(0).getId(), "Aarav Mehta", "IN", "INR", "UPI · PhonePe", "Captured", 489900),
                    new Payment("ORD-" + seededOrders.get(1).getId(), "Isha Kapoor", "IN", "INR", "Card · Visa", "Captured", 1245000),
                    new Payment("ORD-" + seededOrders.get(2).getId(), "Rohan Shah", "IN", "INR", "UPI · GPay", "Pending", 219900),
                    new Payment("ORD-" + seededOrders.get(3).getId(), "Ananya Iyer", "IN", "INR", "Cash on delivery", "Captured", 89900),
                    new Payment("ORD-" + seededOrders.get(4).getId(), "Kabir Khanna", "IN", "INR", "Card · Mastercard", "Refunded", 674000)
            ));
        };
    }
}
