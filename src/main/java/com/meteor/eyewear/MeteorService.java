package com.meteor.eyewear;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class MeteorService {
    static final List<String> PRESCRIPTIONS =
            List.of("Short-sightedness", "Long-sightedness", "Multifocal", "Non-prescription");
    static final List<String> STATUSES =
            List.of(
                    "Pending",
                    "In progress",
                    "Ready for collection",
                    "Shipped",
                    "Completed",
                    "Cancelled");
    private final Accounts accounts;
    private final Products products;
    private final Profiles profiles;
    private final Orders orders;
    private final Events events;
    private final PasswordEncoder passwords;

    public MeteorService(
            Accounts accounts,
            Products products,
            Profiles profiles,
            Orders orders,
            Events events,
            PasswordEncoder passwords) {
        this.accounts = accounts;
        this.products = products;
        this.profiles = profiles;
        this.orders = orders;
        this.events = events;
        this.passwords = passwords;
    }

    public Account current(String email) {
        Account account = accounts.findByEmailIgnoreCase(email).orElse(null);
        if (account == null) {
            throw new AccessDeniedException("Account unavailable.");
        }
        return account;
    }

    public Account account(Long id) {
        Account account = accounts.findById(id).orElse(null);
        if (account == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return account;
    }

    public boolean staff(Account currentUser) {
        return "STAFF".equals(currentUser.getRole());
    }

    void requireStaff(Account currentUser) {
        if (!staff(currentUser)) throw new AccessDeniedException("Staff access required.");
    }

    public Account accessibleCustomer(Account currentUser, Long id) {
        Account customer = account(id);
        if (!staff(currentUser) && !currentUser.getId().equals(id))
            throw new AccessDeniedException("This account is private.");
        if (!"CUSTOMER".equals(customer.getRole()))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return customer;
    }

    public List<Account> customers(Account currentUser, String query) {
        requireStaff(currentUser);
        String searchText = clean(query).toLowerCase(Locale.ROOT);
        List<Account> matches = new ArrayList<>();

        for (Account customer : accounts.findByRoleOrderByNameAsc("CUSTOMER")) {
            boolean nameMatches = customer.getName().toLowerCase(Locale.ROOT).contains(searchText);
            boolean emailMatches = customer.getEmail().contains(searchText);
            boolean icMatches = customer.getIc().equals(searchText);

            if (searchText.isEmpty() || nameMatches || emailMatches || icMatches) {
                matches.add(customer);
            }
        }
        return matches;
    }

    @Transactional
    public Account register(RegistrationForm form) {
        Account account = new Account();
        account.setRole("CUSTOMER");
        applyAccount(account, form);
        // BCrypt has a 72-byte limit. Check the byte length before saving.
        if (form.getPassword().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
            throw new BusinessException(
                    "Password must be at most 72 UTF-8 bytes. Choose a shorter password.");
        account.setPasswordHash(passwords.encode(form.getPassword()));
        return accounts.saveAndFlush(account);
    }

    @Transactional
    public Account updateAccount(Account currentUser, Long id, AccountForm form) {
        Account account = account(id);
        if (!currentUser.getId().equals(id)) accessibleCustomer(currentUser, id);
        checkVersion(form.getVersion(), account.getVersion());
        applyAccount(account, form);
        return accounts.saveAndFlush(account);
    }

    private void applyAccount(Account account, AccountForm form) {
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT), ic = form.getIc().trim();
        Account existingEmail = accounts.findByEmailIgnoreCase(email).orElse(null);
        if (existingEmail != null && !Objects.equals(existingEmail.getId(), account.getId())) {
            throw new BusinessException("An account already uses this email address.");
        }

        Account existingIc = accounts.findByIc(ic).orElse(null);
        if (existingIc != null && !Objects.equals(existingIc.getId(), account.getId())) {
            throw new BusinessException("An account already uses this IC number.");
        }
        account.setName(form.getName().trim());
        account.setEmail(email);
        account.setPhone(form.getPhone().trim());
        account.setIc(ic);
        account.setAddress(form.getAddress().trim());
        account.setNickname(clean(form.getNickname()));
    }

    public List<Product> catalog(String type, String query) {
        String searchText = clean(query).toLowerCase(Locale.ROOT);
        List<Product> matches = new ArrayList<>();

        for (Product product : products.findByActiveTrueOrderByIdAsc()) {
            boolean typeMatches = type == null || type.isBlank() || product.getType().equals(type);
            String productText =
                    product.getModel() + " " + product.getBrand() + " " + product.getId();
            boolean textMatches = productText.toLowerCase(Locale.ROOT).contains(searchText);

            if (typeMatches && (searchText.isEmpty() || textMatches)) {
                matches.add(product);
            }
        }
        return matches;
    }

    public Product product(Long id) {
        Product product = products.findById(id).orElse(null);
        if (product == null || !product.getActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return product;
    }

    @Transactional
    public Product saveProduct(Account currentUser, Long id, ProductForm form) {
        requireStaff(currentUser);
        Product product;
        if (id == null) {
            product = new Product();
        } else {
            product = products.locked(id).orElse(null);
            if (product == null || !product.getActive()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }
        if (id != null) {
            checkVersion(form.getVersion(), product.getVersion());
            if (!product.getType().equals(form.getType()))
                throw new BusinessException("A product's type cannot change after creation.");
        }
        if ("FRAME".equals(form.getType())) {
            if (clean(form.getFrameShape()).isEmpty() || clean(form.getFrameColour()).isEmpty())
                throw new BusinessException("Enter both frame shape and frame colour.");
            product.setFrameShape(form.getFrameShape().trim());
            product.setFrameColour(form.getFrameColour().trim());
            product.setPrescription(null);
            product.setAstigmatism(null);
        } else {
            validatePrescription(form.getPrescription(), true);
            validateAstigmatism(form.getAstigmatism(), true);
            product.setPrescription(form.getPrescription());
            product.setAstigmatism(form.getAstigmatism());
            product.setFrameShape(null);
            product.setFrameColour(null);
        }
        product.setType(form.getType());
        product.setModel(form.getModel().trim());
        product.setBrand(form.getBrand().trim());
        product.setPrice(form.getPrice());
        product.setQuantity(form.getQuantity());
        return products.saveAndFlush(product);
    }

    @Transactional
    public void deleteProduct(Account currentUser, Long id, Long version) {
        requireStaff(currentUser);
        Product product = products.locked(id).orElse(null);
        if (product == null || !product.getActive()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        checkVersion(version, product.getVersion());
        // Hide the product from the catalogue, but keep it for old orders.
        product.setActive(false);
        products.saveAndFlush(product);
    }

    public SpectacleProfile profile(Account currentUser, Long customerId) {
        accessibleCustomer(currentUser, customerId);
        return profiles.findByCustomerId(customerId).orElse(null);
    }

    @Transactional
    public SpectacleProfile saveProfile(Account currentUser, Long customerId, ProfileForm form) {
        requireStaff(currentUser);
        accessibleCustomer(currentUser, customerId);
        SpectacleProfile profile =
                profiles.findByCustomerId(customerId).orElse(new SpectacleProfile());
        if (profile.getId() != null) checkVersion(form.getVersion(), profile.getVersion());
        else if (form.getVersion() != null)
            throw new BusinessException("This profile changed. Reload and try again.");
        validatePrescription(form.getPrescription(), false);
        validateAstigmatism(form.getAstigmatism(), false);
        profile.setCustomerId(customerId);
        profile.setLeftPower(form.getLeftPower());
        profile.setRightPower(form.getRightPower());
        profile.setPrescription(clean(form.getPrescription()));
        profile.setAstigmatism(clean(form.getAstigmatism()));
        profile.setNotes(clean(form.getNotes()));
        profile.setUpdatedAt(Instant.now());
        return profiles.saveAndFlush(profile);
    }

    public List<CustomerOrder> visibleOrders(Account currentUser) {
        if (staff(currentUser)) {
            return orders.findAllByOrderByCreatedAtDesc();
        }
        return orders.findByCustomerIdOrderByCreatedAtDesc(currentUser.getId());
    }

    public List<CustomerOrder> customerOrders(Account currentUser, Long id) {
        accessibleCustomer(currentUser, id);
        return orders.findByCustomerIdOrderByCreatedAtDesc(id);
    }

    public CustomerOrder order(Account currentUser, Long id) {
        CustomerOrder order = orders.findById(id).orElse(null);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        accessibleCustomer(currentUser, order.getCustomerId());
        return order;
    }

    public List<OrderEvent> history(Account currentUser, Long id) {
        order(currentUser, id);
        return events.findByOrderIdOrderByCreatedAtAsc(id);
    }

    @Transactional
    public CustomerOrder createOrder(Account currentUser, Long customerId, OrderForm form) {
        requireStaff(currentUser);
        accessibleCustomer(currentUser, customerId);
        SpectacleProfile profile = profiles.findByCustomerId(customerId).orElse(null);
        if (profile == null) {
            throw new BusinessException(
                    "Create a spectacle profile for this customer before placing an order.");
        }
        if (!List.of("OUTLET", "CUSTOMER").contains(clean(form.getDelivery())))
            throw new BusinessException("Choose a delivery option.");
        String address;
        if ("OUTLET".equals(form.getDelivery())) {
            if (!"Merlimau".equals(form.getOutlet()))
                throw new BusinessException("Choose the Merlimau outlet.");
            address = "Meteor Eyewear — Merlimau outlet";
        } else {
            address = clean(form.getShippingAddress());
            if (address.isEmpty())
                throw new BusinessException("Enter a customer delivery address.");
        }
        // Lock the lower product ID first so two orders use the same lock order.
        Long[] productIds = {form.getFrameId(), form.getLensId()};
        Arrays.sort(productIds);
        Map<Long, Product> lockedProducts = new HashMap<>();
        for (Long productId : productIds) {
            Product product = products.locked(productId).orElse(null);
            if (product == null || !product.getActive()) {
                throw new BusinessException("One of the selected products is no longer available.");
            }
            lockedProducts.put(productId, product);
        }
        Product frame = lockedProducts.get(form.getFrameId()),
                lens = lockedProducts.get(form.getLensId());
        if (!"FRAME".equals(frame.getType()) || !"LENS".equals(lens.getType()))
            throw new BusinessException("Choose one frame and one lens.");
        if (frame.getQuantity() < form.getQuantity() || lens.getQuantity() < form.getQuantity())
            throw new BusinessException("There is not enough stock for this order.");
        CustomerOrder order = new CustomerOrder();
        order.setCustomerId(customerId);
        order.setStaffId(currentUser.getId());
        order.setFrameId(frame.getId());
        order.setLensId(lens.getId());
        order.setFrameName(frame.getModel());
        order.setLensName(lens.getModel());
        order.setQuantity(form.getQuantity());
        order.setUnitPrice(frame.getPrice().add(lens.getPrice()));
        order.setTotal(order.getUnitPrice().multiply(BigDecimal.valueOf(form.getQuantity())));
        order.setDelivery(form.getDelivery());
        order.setOutlet("OUTLET".equals(form.getDelivery()) ? form.getOutlet() : null);
        order.setShippingAddress(address);
        order.setLeftPower(profile.getLeftPower());
        order.setRightPower(profile.getRightPower());
        order.setPrescription(profile.getPrescription());
        order.setAstigmatism(profile.getAstigmatism());
        frame.setQuantity(frame.getQuantity() - form.getQuantity());
        lens.setQuantity(lens.getQuantity() - form.getQuantity());
        orders.saveAndFlush(order);
        recordEvent(order, currentUser);
        return order;
    }

    public List<String> nextStatuses(CustomerOrder order) {
        List<String> choices = new ArrayList<>();
        choices.add(order.getStatus());

        switch (order.getStatus()) {
            case "Pending":
                choices.add("In progress");
                choices.add("Cancelled");
                break;
            case "In progress":
                if ("OUTLET".equals(order.getDelivery())) {
                    choices.add("Ready for collection");
                } else {
                    choices.add("Shipped");
                }
                choices.add("Cancelled");
                break;
            case "Ready for collection":
                choices.add("Completed");
                choices.add("Cancelled");
                break;
            case "Shipped":
                choices.add("Completed");
                break;
            default:
                // Completed and cancelled orders have no next status.
                break;
        }
        return choices;
    }

    @Transactional
    public CustomerOrder updateOrder(Account currentUser, Long id, OrderUpdateForm form) {
        requireStaff(currentUser);
        CustomerOrder order = orders.locked(id).orElse(null);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        checkVersion(form.getVersion(), order.getVersion());
        if (!nextStatuses(order).contains(form.getStatus()))
            throw new BusinessException("That status change is not allowed.");
        if (List.of("Completed", "Cancelled").contains(order.getStatus()))
            throw new BusinessException("This order is closed and cannot be edited.");
        if (!Objects.equals(order.getShippingAddress(), form.getShippingAddress().trim())) {
            if (!"CUSTOMER".equals(order.getDelivery()) || "Shipped".equals(order.getStatus()))
                throw new BusinessException("This delivery address can no longer be changed.");
            order.setShippingAddress(form.getShippingAddress().trim());
        }
        if (!order.getStatus().equals(form.getStatus())) {
            if ("Cancelled".equals(form.getStatus())) {
                Long[] productIds = {order.getFrameId(), order.getLensId()};
                Arrays.sort(productIds);
                for (Long productId : productIds) {
                    Product product = products.locked(productId).orElseThrow();
                    product.setQuantity(product.getQuantity() + order.getQuantity());
                }
            }
            order.setStatus(form.getStatus());
            recordEvent(order, currentUser);
        }
        return orders.saveAndFlush(order);
    }

    private void recordEvent(CustomerOrder order, Account currentUser) {
        OrderEvent e = new OrderEvent();
        e.setOrderId(order.getId());
        e.setStaffId(currentUser.getId());
        e.setStatus(order.getStatus());
        events.save(e);
    }

    private void validatePrescription(String value, boolean required) {
        if ((!required && clean(value).isEmpty()) || PRESCRIPTIONS.contains(clean(value))) return;
        throw new BusinessException("Choose a valid prescription type.");
    }

    private void validateAstigmatism(String value, boolean required) {
        if ((!required && clean(value).isEmpty()) || List.of("Yes", "No").contains(clean(value)))
            return;
        throw new BusinessException("Choose Yes or No for astigmatism.");
    }

    static void checkVersion(Long provided, long current) {
        if (provided == null || provided != current)
            throw new BusinessException(
                    "This record changed in another session. Reload it before saving.");
    }

    static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
