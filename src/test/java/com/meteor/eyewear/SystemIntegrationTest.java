package com.meteor.eyewear;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.validation.Validator;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@SpringBootTest(
        properties = {
            "spring.datasource.url=jdbc:h2:mem:meteor-tests;MODE=Oracle;DB_CLOSE_DELAY=-1",
            "spring.jpa.hibernate.ddl-auto=create-drop"
        })
@AutoConfigureMockMvc
@Transactional
class SystemIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired MeteorService service;
    @Autowired Accounts accounts;
    @Autowired Products products;
    @Autowired Orders orders;
    @Autowired Validator validator;
    @Autowired PasswordEncoder passwords;
    Account staff, customer, other;

    @BeforeEach
    void users() {
        staff = service.current("staff@meteor.example");
        customer = service.current("customer@meteor.example");
        other = service.current("jamie@meteor.example");
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/login",
                "/signup",
                "/about",
                "/css/app.css",
                "/js/app.js",
                "/images/frame.svg",
                "/images/lens.svg"
            })
    void publicPagesRender(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/dashboard",
                "/catalog?type=FRAME",
                "/catalog?type=LENS",
                "/account",
                "/account/edit",
                "/spectacle-profile",
                "/orders"
            })
    void customerPagesRender(String path) throws Exception {
        mvc.perform(get(path).with(user(customer.getEmail()).roles("CUSTOMER")))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "/dashboard",
                "/catalog?type=FRAME",
                "/catalog?type=LENS",
                "/staff/customers",
                "/staff/products/new",
                "/staff/products/new?type=LENS",
                "/staff/reports",
                "/orders",
                "/account",
                "/account/edit"
            })
    void staffPagesRender(String path) throws Exception {
        mvc.perform(get(path).with(user(staff.getEmail()).roles("STAFF")))
                .andExpect(status().isOk());
    }

    @Test
    void customerAndOrderDetailPagesRender() throws Exception {
        long id = customer.getId();
        long orderId = service.visibleOrders(staff).get(0).getId();
        String[] paths = {
            "/staff/customers/" + id,
            "/staff/customers/" + id + "/edit",
            "/staff/customers/" + id + "/profile",
            "/staff/customers/" + id + "/profile/edit",
            "/staff/customers/" + id + "/orders/new",
            "/orders/" + orderId,
            "/staff/orders/" + orderId + "/edit",
            "/catalog/" + frame().getId(),
            "/staff/products/" + frame().getId() + "/edit"
        };
        for (String path : paths)
            mvc.perform(get(path).with(user(staff.getEmail()).roles("STAFF")))
                    .andExpect(status().isOk());
    }

    @Test
    void realLoginAcceptsHashedPassword() throws Exception {
        mvc.perform(formLogin("/login").user("email", staff.getEmail()).password("StaffDemo2026!"))
                .andExpect(authenticated().withRoles("STAFF"));
        assertThat(staff.getPasswordHash()).doesNotContain("StaffDemo2026!");
        assertThat(passwords.matches("StaffDemo2026!", staff.getPasswordHash())).isTrue();
    }

    @Test
    void incorrectPasswordIsRejected() throws Exception {
        mvc.perform(
                        formLogin("/login")
                                .user("email", customer.getEmail())
                                .password("incorrect-password"))
                .andExpect(unauthenticated());
    }

    @Test
    void anonymousCannotReadOrders() throws Exception {
        mvc.perform(get("/orders")).andExpect(status().is3xxRedirection());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/staff/customers", "/staff/reports", "/staff/products/new"})
    void customersCannotOpenStaffScreens(String path) throws Exception {
        mvc.perform(get(path).with(user(customer.getEmail()).roles("CUSTOMER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void csrfRequiredForMutations() throws Exception {
        mvc.perform(post("/staff/products/new").with(user(staff.getEmail()).roles("STAFF")))
                .andExpect(status().isForbidden());
        mvc.perform(post("/signup")).andExpect(status().isForbidden());
    }

    @Test
    void customerCannotReadAnotherCustomersOrder() throws Exception {
        long id = service.visibleOrders(staff).get(0).getId();
        mvc.perform(get("/orders/" + id).with(user(other.getEmail()).roles("CUSTOMER")))
                .andExpect(status().isForbidden());
        assertThatThrownBy(() -> service.profile(other, customer.getId()))
                .isInstanceOf(AccessDeniedException.class);
        assertThat(service.visibleOrders(other)).isEmpty();
    }

    @Test
    void ownOrderIsVisible() throws Exception {
        long id = service.visibleOrders(customer).get(0).getId();
        mvc.perform(get("/orders/" + id).with(user(customer.getEmail()).roles("CUSTOMER")))
                .andExpect(status().isOk());
    }

    @Test
    void nonexistentProductReturns404() throws Exception {
        mvc.perform(get("/catalog/999999").with(user(customer.getEmail()).roles("CUSTOMER")))
                .andExpect(status().isNotFound());
    }

    @Test
    void registrationPersistsOnlyCustomerRole() {
        RegistrationForm form = registration();
        Account account = service.register(form);
        assertThat(account.getRole()).isEqualTo("CUSTOMER");
        assertThat(account.getEmail()).isEqualTo("new@meteor.example");
        assertThat(passwords.matches(form.getPassword(), account.getPasswordHash())).isTrue();
    }

    @Test
    void duplicateEmailAndIcAreRejected() {
        RegistrationForm form = registration();
        form.setEmail(customer.getEmail());
        assertThatThrownBy(() -> service.register(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("email");
        form.setEmail("new@meteor.example");
        form.setIc(customer.getIc());
        assertThatThrownBy(() -> service.register(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("IC");
    }

    @Test
    void invalidRegistrationRendersErrorsAndDoesNotSave() throws Exception {
        long before = accounts.count();
        mvc.perform(
                        post("/signup")
                                .with(csrf())
                                .param("name", "New Demo")
                                .param("email", "invalid")
                                .param("phone", "abc")
                                .param("ic", "123")
                                .param("address", "")
                                .param("password", "short"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attributeHasFieldErrors(
                                        "form", "email", "phone", "ic", "address", "password"));
        assertThat(accounts.count()).isEqualTo(before);
    }

    @Test
    void overlongUtf8PasswordIsRejectedBeforeBcrypt() {
        RegistrationForm form = registration();
        form.setPassword("界".repeat(30));
        assertThatThrownBy(() -> service.register(form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("UTF-8");
    }

    @Test
    void accountUpdatesPersistAndDoNotChangeRole() {
        AccountForm form = new AccountForm();
        BeanUtils.copyProperties(customer, form);
        form.setPhone("0123456789");
        form.setAddress("Fictional updated address");
        Account account = service.updateAccount(customer, customer.getId(), form);
        assertThat(account.getPhone()).isEqualTo("0123456789");
        assertThat(account.getAddress()).isEqualTo("Fictional updated address");
        assertThat(account.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void accountCannotBeEditedByAnotherCustomer() {
        AccountForm form = new AccountForm();
        BeanUtils.copyProperties(customer, form);
        assertThatThrownBy(() -> service.updateAccount(other, customer.getId(), form))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void emailChangeEndsCurrentSession() throws Exception {
        mvc.perform(
                        post("/account/edit")
                                .with(user(customer.getEmail()).roles("CUSTOMER"))
                                .with(csrf())
                                .param("name", customer.getName())
                                .param("email", "changed@meteor.example")
                                .param("ic", customer.getIc())
                                .param("phone", customer.getPhone())
                                .param("address", customer.getAddress())
                                .param("version", String.valueOf(customer.getVersion())))
                .andExpect(redirectedUrl("/login?updated"));
    }

    @Test
    void staffCanFindCustomerByIc() {
        assertThat(service.customers(staff, customer.getIc()))
                .extracting(Account::getId)
                .containsExactly(customer.getId());
    }

    @Test
    void frameCrudAndHistoricalOrdersArePreserved() {
        Product product = service.saveProduct(staff, null, frameForm());
        ProductForm edit = frameForm();
        edit.setVersion(product.getVersion());
        edit.setFrameColour("Green");
        Product saved = service.saveProduct(staff, product.getId(), edit);
        assertThat(saved.getFrameColour()).isEqualTo("Green");
        service.deleteProduct(staff, product.getId(), saved.getVersion());
        assertThat(service.catalog("FRAME", null))
                .extracting(Product::getId)
                .doesNotContain(product.getId());
        assertThat(products.findById(product.getId())).isPresent();
    }

    @Test
    void lensCanBeCreatedViewedUpdatedAndDeleted() {
        ProductForm form = frameForm();
        form.setType("LENS");
        form.setPrescription("Multifocal");
        form.setAstigmatism("Yes");
        Product product = service.saveProduct(staff, null, form);
        assertThat(service.catalog("LENS", product.getModel()))
                .extracting(Product::getId)
                .contains(product.getId());
        form.setVersion(product.getVersion());
        form.setPrice(new BigDecimal("310.00"));
        Product saved = service.saveProduct(staff, product.getId(), form);
        assertThat(saved.getPrice()).isEqualByComparingTo("310.00");
        service.deleteProduct(staff, product.getId(), saved.getVersion());
        assertThat(service.catalog("LENS", null))
                .extracting(Product::getId)
                .doesNotContain(product.getId());
    }

    @Test
    void productValidationRejectsHistoricallyInvalidInputs() {
        ProductForm form = frameForm();
        assertThat(validator.validate(form)).isEmpty();
        form.setModel("LEE COOPER C?M500");
        form.setBrand("?");
        form.setPrice(new BigDecimal("-1"));
        form.setQuantity(-1);
        assertThat(validator.validate(form))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("model", "brand", "price", "quantity");
    }

    @Test
    void lensRequiresPrescriptionAndAstigmatism() {
        ProductForm form = frameForm();
        form.setType("LENS");
        assertThatThrownBy(() -> service.saveProduct(staff, null, form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("prescription");
        form.setPrescription("Short-sightedness");
        assertThatThrownBy(() -> service.saveProduct(staff, null, form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("astigmatism");
    }

    @Test
    void missingFrameFieldsRejectedOnServer() throws Exception {
        mvc.perform(
                        post("/staff/products/new")
                                .with(user(staff.getEmail()).roles("STAFF"))
                                .with(csrf())
                                .param("type", "FRAME")
                                .param("model", "Valid Model")
                                .param("brand", "Meteor")
                                .param("price", "100")
                                .param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasErrors("form"));
    }

    @Test
    void staleProductUpdateRejected() {
        Product product = frame();
        ProductForm form = frameForm();
        form.setVersion(product.getVersion() + 1);
        assertThatThrownBy(() -> service.saveProduct(staff, product.getId(), form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("another session");
    }

    @Test
    void customerCannotWriteProductsEvenThroughService() {
        assertThatThrownBy(() -> service.saveProduct(customer, null, frameForm()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void spectacleProfileSupportsCreateViewAndUpdate() {
        ProfileForm form = new ProfileForm();
        form.setLeftPower(new BigDecimal("-2.00"));
        form.setRightPower(new BigDecimal("-1.75"));
        SpectacleProfile profile = service.saveProfile(staff, other.getId(), form);
        assertThat(service.profile(other, other.getId()).getLeftPower()).isEqualByComparingTo("-2");
        form.setVersion(profile.getVersion());
        form.setRightPower(new BigDecimal("-2.25"));
        assertThat(service.saveProfile(staff, other.getId(), form).getRightPower())
                .isEqualByComparingTo("-2.25");
    }

    @Test
    void spectaclePowerRequiredAndOptionalFieldsMayBeBlank() {
        ProfileForm form = new ProfileForm();
        assertThat(validator.validate(form)).hasSize(2);
        form.setLeftPower(BigDecimal.ZERO);
        form.setRightPower(BigDecimal.ZERO);
        assertThat(validator.validate(form)).isEmpty();
        assertThat(service.saveProfile(staff, other.getId(), form).getId()).isNotNull();
    }

    @Test
    void orderCreationReservesStockAndSnapshotsPrice() {
        Product frame = frame(), lens = lens();
        int oldFrame = frame.getQuantity(), oldLensQuantity = lens.getQuantity();
        CustomerOrder order = service.createOrder(staff, customer.getId(), orderForm("OUTLET"));
        assertThat(frame.getQuantity()).isEqualTo(oldFrame - 1);
        assertThat(lens.getQuantity()).isEqualTo(oldLensQuantity - 1);
        BigDecimal price = order.getTotal();
        frame.setPrice(new BigDecimal("999.99"));
        products.saveAndFlush(frame);
        assertThat(service.order(customer, order.getId()).getTotal()).isEqualByComparingTo(price);
        assertThat(service.history(customer, order.getId())).hasSize(1);
    }

    @Test
    void orderNeedsProfile() {
        assertThatThrownBy(() -> service.createOrder(staff, other.getId(), orderForm("OUTLET")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("profile");
    }

    @Test
    void orderRequiresCorrectFrameAndLensTypes() {
        OrderForm form = orderForm("OUTLET");
        form.setFrameId(lens().getId());
        assertThatThrownBy(() -> service.createOrder(staff, customer.getId(), form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("one frame and one lens");
    }

    @Test
    void insufficientStockDoesNotCreateOrder() {
        OrderForm form = orderForm("OUTLET");
        form.setQuantity(100);
        long before = orders.count();
        int stock = frame().getQuantity();
        assertThatThrownBy(() -> service.createOrder(staff, customer.getId(), form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("stock");
        assertThat(orders.count()).isEqualTo(before);
        assertThat(frame().getQuantity()).isEqualTo(stock);
    }

    @Test
    void customerDeliveryRequiresAddressAndOutletRequiresSelection() {
        OrderForm form = orderForm("CUSTOMER");
        form.setShippingAddress("");
        assertThatThrownBy(() -> service.createOrder(staff, customer.getId(), form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("address");
        form.setDelivery("OUTLET");
        form.setOutlet("");
        assertThatThrownBy(() -> service.createOrder(staff, customer.getId(), form))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("outlet");
    }

    @Test
    void cancellationRestoresStockOnceAndClosesOrder() {
        CustomerOrder order = service.createOrder(staff, customer.getId(), orderForm("CUSTOMER"));
        int reserved = frame().getQuantity();
        OrderUpdateForm form = update(order, "Cancelled");
        service.updateOrder(staff, order.getId(), form);
        assertThat(frame().getQuantity()).isEqualTo(reserved + 1);
        form.setVersion(order.getVersion());
        assertThatThrownBy(() -> service.updateOrder(staff, order.getId(), form))
                .isInstanceOf(BusinessException.class);
        assertThat(frame().getQuantity()).isEqualTo(reserved + 1);
    }

    @Test
    void invalidOrderTransitionRejected() {
        CustomerOrder order = service.createOrder(staff, customer.getId(), orderForm("OUTLET"));
        assertThatThrownBy(
                        () -> service.updateOrder(staff, order.getId(), update(order, "Completed")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status");
    }

    @Test
    void deliveryOrderCanProgressToCompletion() {
        CustomerOrder order = service.createOrder(staff, customer.getId(), orderForm("CUSTOMER"));
        for (String status : new String[] {"In progress", "Shipped", "Completed"})
            order = service.updateOrder(staff, order.getId(), update(order, status));
        assertThat(order.getStatus()).isEqualTo("Completed");
        assertThat(service.history(customer, order.getId())).hasSize(4);
    }

    @Test
    void outletOrderCanProgressToCompletion() {
        CustomerOrder order = service.createOrder(staff, customer.getId(), orderForm("OUTLET"));
        for (String status : new String[] {"In progress", "Ready for collection", "Completed"})
            order = service.updateOrder(staff, order.getId(), update(order, status));
        assertThat(order.getStatus()).isEqualTo("Completed");
    }

    @Test
    void deliveryAddressCanBeUpdatedBeforeShipping() {
        CustomerOrder order = service.createOrder(staff, customer.getId(), orderForm("CUSTOMER"));
        OrderUpdateForm form = update(order, "Pending");
        form.setShippingAddress("Fictional changed address");
        assertThat(service.updateOrder(staff, order.getId(), form).getShippingAddress())
                .isEqualTo("Fictional changed address");
    }

    @Test
    void ordersRemainReadableAfterProductDeletion() {
        CustomerOrder order = service.visibleOrders(customer).get(0);
        Product product = products.findById(order.getFrameId()).orElseThrow();
        service.deleteProduct(staff, product.getId(), product.getVersion());
        assertThat(service.order(customer, order.getId()).getFrameName()).isNotBlank();
    }

    private Product frame() {
        return service.catalog("FRAME", null).get(0);
    }

    private Product lens() {
        return service.catalog("LENS", null).get(0);
    }

    private ProductForm frameForm() {
        ProductForm form = new ProductForm();
        form.setModel("Test Frame 123");
        form.setBrand("Meteor");
        form.setType("FRAME");
        form.setPrice(new BigDecimal("150.00"));
        form.setQuantity(10);
        form.setFrameShape("Round");
        form.setFrameColour("Black");
        return form;
    }

    private RegistrationForm registration() {
        RegistrationForm form = new RegistrationForm();
        form.setName("New Demo");
        form.setEmail("NEW@meteor.example");
        form.setPhone("0100000000");
        form.setIc("000000000099");
        form.setAddress("Fictional address");
        form.setPassword("ExamplePassword2026!");
        return form;
    }

    private OrderForm orderForm(String delivery) {
        OrderForm form = new OrderForm();
        form.setFrameId(frame().getId());
        form.setLensId(lens().getId());
        form.setDelivery(delivery);
        form.setOutlet("Merlimau");
        form.setShippingAddress("Fictional customer address");
        return form;
    }

    private OrderUpdateForm update(CustomerOrder order, String status) {
        OrderUpdateForm form = new OrderUpdateForm();
        form.setStatus(status);
        form.setVersion(order.getVersion());
        form.setShippingAddress(order.getShippingAddress());
        return form;
    }
}
