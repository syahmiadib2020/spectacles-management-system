package com.meteor.eyewear;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

// Add sample records only when the database is empty.
@Component
@ConditionalOnProperty(name = "meteor.demo.enabled", havingValue = "true")
public class DemoData implements CommandLineRunner {
    private final MeteorService service;
    private final PasswordEncoder passwords;
    private final Accounts accounts;

    public DemoData(MeteorService service, PasswordEncoder passwords, Accounts accounts) {
        this.service = service;
        this.passwords = passwords;
        this.accounts = accounts;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (accounts.count() != 0) return;
        Account staff =
                createAccount(
                        "Demo Staff",
                        "staff@meteor.example",
                        "STAFF",
                        "000000000001",
                        "StaffDemo2026!");
        Account customer =
                createAccount(
                        "Alex Demo",
                        "customer@meteor.example",
                        "CUSTOMER",
                        "000000000002",
                        "CustomerDemo2026!");
        createAccount(
                "Jamie Sample",
                "jamie@meteor.example",
                "CUSTOMER",
                "000000000003",
                "CustomerDemo2026!");
        ProfileForm profileForm = new ProfileForm();
        profileForm.setLeftPower(new BigDecimal("-1.50"));
        profileForm.setRightPower(new BigDecimal("-1.25"));
        profileForm.setPrescription("Short-sightedness");
        profileForm.setAstigmatism("No");
        profileForm.setNotes("Fictional demonstration profile.");
        service.saveProfile(staff, customer.getId(), profileForm);
        Product frame =
                product(
                        staff,
                        "FRAME",
                        "Classic Round",
                        "Meteor",
                        "189.00",
                        18,
                        "Round",
                        "Tortoiseshell",
                        null,
                        null);
        product(
                staff,
                "FRAME",
                "Everyday Square",
                "Meteor",
                "149.00",
                24,
                "Square",
                "Black",
                null,
                null);
        product(
                staff,
                "FRAME",
                "Lightweight Oval",
                "Meteor",
                "169.00",
                4,
                "Oval",
                "Gold",
                null,
                null);
        product(
                staff,
                "FRAME",
                "Weekend Sun",
                "Meteor",
                "229.00",
                12,
                "Aviator",
                "Black",
                null,
                null);
        Product lens =
                product(
                        staff,
                        "LENS",
                        "Clear Everyday",
                        "Meteor",
                        "110.00",
                        32,
                        null,
                        null,
                        "Short-sightedness",
                        "No");
        product(
                staff,
                "LENS",
                "Photochromic Lens",
                "Meteor",
                "250.00",
                8,
                null,
                null,
                "Short-sightedness",
                "Yes");
        product(
                staff,
                "LENS",
                "Multifocal Comfort",
                "Meteor",
                "390.00",
                3,
                null,
                null,
                "Multifocal",
                "Yes");
        OrderForm orderForm = new OrderForm();
        orderForm.setFrameId(frame.getId());
        orderForm.setLensId(lens.getId());
        orderForm.setDelivery("OUTLET");
        orderForm.setOutlet("Merlimau");
        CustomerOrder order = service.createOrder(staff, customer.getId(), orderForm);
        OrderUpdateForm update = new OrderUpdateForm();
        update.setVersion(order.getVersion());
        update.setShippingAddress(order.getShippingAddress());
        update.setStatus("In progress");
        service.updateOrder(staff, order.getId(), update);
    }

    private Account createAccount(
            String name, String email, String role, String ic, String password) {
        Account account = new Account();
        account.setName(name);
        account.setEmail(email);
        account.setRole(role);
        account.setIc(ic);
        account.setPhone("0100000000");
        account.setAddress("Demo address, Merlimau, Melaka");
        account.setNickname(name.split(" ")[0]);
        account.setCreatedAt(Instant.now());
        account.setPasswordHash(passwords.encode(password));
        return accounts.saveAndFlush(account);
    }

    private Product product(
            Account staff,
            String type,
            String model,
            String brand,
            String price,
            int quantity,
            String shape,
            String colour,
            String prescription,
            String astigmatism) {
        ProductForm form = new ProductForm();
        form.setType(type);
        form.setModel(model);
        form.setBrand(brand);
        form.setPrice(new BigDecimal(price));
        form.setQuantity(quantity);
        form.setFrameShape(shape);
        form.setFrameColour(colour);
        form.setPrescription(prescription);
        form.setAstigmatism(astigmatism);
        return service.saveProduct(staff, null, form);
    }
}
