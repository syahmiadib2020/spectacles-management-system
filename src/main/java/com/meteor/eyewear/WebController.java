package com.meteor.eyewear;

import jakarta.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.*;

@Controller
public class WebController {
    private final MeteorService service;
    private final boolean demo;

    public WebController(MeteorService service, @Value("${meteor.demo.enabled}") boolean demo) {
        this.service = service;
        this.demo = demo;
    }

    private Account getCurrentUser(Principal principal) {
        return service.current(principal.getName());
    }

    @ModelAttribute
    void shared(Model model, Principal principal) {
        if (principal != null) {
            Account currentUser = getCurrentUser(principal);
            model.addAttribute("viewer", currentUser);
            model.addAttribute("isStaff", service.staff(currentUser));
        }
        model.addAttribute("demo", demo);
        model.addAttribute("prescriptions", MeteorService.PRESCRIPTIONS);
    }

    @GetMapping("/")
    String home(Principal principal) {
        return principal == null ? "redirect:/login" : "redirect:/dashboard";
    }

    @GetMapping("/login")
    String login() {
        return "login";
    }

    @GetMapping("/about")
    String about() {
        return "about";
    }

    @GetMapping("/signup")
    String signup(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "signup";
    }

    @PostMapping("/signup")
    String signup(
            @Valid @ModelAttribute("form") RegistrationForm form,
            BindingResult errors,
            Model model,
            RedirectAttributes flash) {
        if (!errors.hasErrors()) {
            try {
                service.register(form);
                flash.addFlashAttribute("success", "Account created. You can now log in.");
                return "redirect:/login";
            } catch (BusinessException
                    | DataIntegrityViolationException
                    | OptimisticLockingFailureException exception) {
                addFormError(errors, exception);
            }
        }
        return "signup";
    }

    @GetMapping("/dashboard")
    String dashboard(Principal principal, Model model) {
        Account currentUser = getCurrentUser(principal);
        List<CustomerOrder> orders = service.visibleOrders(currentUser);
        List<Product> products = service.catalog(null, null);
        List<CustomerOrder> recentOrders = new ArrayList<>();
        int activeOrderCount = 0;
        for (CustomerOrder order : orders) {
            if (recentOrders.size() < 5) {
                recentOrders.add(order);
            }
            if (!"Completed".equals(order.getStatus()) && !"Cancelled".equals(order.getStatus())) {
                activeOrderCount++;
            }
        }
        List<Product> lowStock = new ArrayList<>();
        for (Product product : products) {
            if (product.getQuantity() <= 5) {
                lowStock.add(product);
            }
        }
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("activeOrders", activeOrderCount);
        model.addAttribute("productCount", products.size());
        model.addAttribute(
                "customerCount",
                service.staff(currentUser) ? service.customers(currentUser, "").size() : 0);
        model.addAttribute("lowStock", lowStock);
        return "dashboard";
    }

    @GetMapping("/catalog")
    String catalog(
            @RequestParam(defaultValue = "FRAME") String type,
            @RequestParam(defaultValue = "") String q,
            Model model) {
        String selected = "LENS".equals(type) ? "LENS" : "FRAME";
        model.addAttribute("type", selected);
        model.addAttribute("q", q);
        model.addAttribute("products", service.catalog(selected, q));
        return "catalog";
    }

    @GetMapping("/catalog/{id}")
    String productDetail(@PathVariable Long id, Model model) {
        model.addAttribute("product", service.product(id));
        return "product-detail";
    }

    @GetMapping("/staff/products/new")
    String newProduct(@RequestParam(defaultValue = "FRAME") String type, Model model) {
        ProductForm form = new ProductForm();
        form.setType("LENS".equals(type) ? "LENS" : "FRAME");
        model.addAttribute("form", form);
        model.addAttribute("productId", null);
        return "product-form";
    }

    @GetMapping("/staff/products/{id}/edit")
    String editProduct(@PathVariable Long id, Model model) {
        ProductForm form = new ProductForm();
        BeanUtils.copyProperties(service.product(id), form);
        model.addAttribute("form", form);
        model.addAttribute("productId", id);
        return "product-form";
    }

    @PostMapping({"/staff/products/new", "/staff/products/{id}/edit"})
    String saveProduct(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") ProductForm form,
            BindingResult errors,
            Principal principal,
            Model model,
            RedirectAttributes flash) {
        model.addAttribute("productId", id);
        if (!errors.hasErrors()) {
            try {
                service.saveProduct(getCurrentUser(principal), id, form);
                flash.addFlashAttribute(
                        "success",
                        id == null
                                ? "Product added successfully."
                                : "Product updated successfully.");
                return "redirect:/catalog?type=" + form.getType();
            } catch (BusinessException
                    | DataIntegrityViolationException
                    | OptimisticLockingFailureException exception) {
                addFormError(errors, exception);
            }
        }
        return "product-form";
    }

    @PostMapping("/staff/products/{id}/delete")
    String deleteProduct(
            @PathVariable Long id,
            @RequestParam Long version,
            Principal principal,
            RedirectAttributes flash) {
        Product product = service.product(id);
        service.deleteProduct(getCurrentUser(principal), id, version);
        flash.addFlashAttribute(
                "success",
                "Product removed from the catalogue. Existing order records have been preserved.");
        return "redirect:/catalog?type=" + product.getType();
    }

    @GetMapping("/account")
    String myAccount(Principal principal, Model model) {
        model.addAttribute("account", getCurrentUser(principal));
        model.addAttribute("self", true);
        return "account";
    }

    @GetMapping("/account/edit")
    String editAccount(Principal principal, Model model) {
        return accountForm(getCurrentUser(principal).getId(), getCurrentUser(principal), model);
    }

    @GetMapping("/staff/customers/{id}/edit")
    String editCustomer(@PathVariable Long id, Principal principal, Model model) {
        return accountForm(id, getCurrentUser(principal), model);
    }

    private String accountForm(Long id, Account actor, Model model) {
        Account currentUser =
                actor.getId().equals(id) ? actor : service.accessibleCustomer(actor, id);
        AccountForm form = new AccountForm();
        BeanUtils.copyProperties(currentUser, form);
        model.addAttribute("form", form);
        model.addAttribute("accountId", id);
        model.addAttribute("self", actor.getId().equals(id));
        return "account-form";
    }

    @PostMapping({"/account/edit", "/staff/customers/{id}/edit"})
    String saveAccount(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("form") AccountForm form,
            BindingResult errors,
            Principal principal,
            Model model,
            RedirectAttributes flash,
            jakarta.servlet.http.HttpServletRequest request) {
        Account currentUser = getCurrentUser(principal);
        Long target = id == null ? currentUser.getId() : id;
        boolean emailChanged =
                currentUser.getId().equals(target)
                        && !currentUser
                                .getEmail()
                                .equalsIgnoreCase(
                                        form.getEmail() == null ? "" : form.getEmail().trim());
        if (!currentUser.getId().equals(target)) service.accessibleCustomer(currentUser, target);
        model.addAttribute("accountId", target);
        model.addAttribute("self", currentUser.getId().equals(target));
        if (!errors.hasErrors()) {
            try {
                service.updateAccount(currentUser, target, form);
                if (emailChanged) {
                    org.springframework.security.core.context.SecurityContextHolder.clearContext();
                    if (request.getSession(false) != null) request.getSession(false).invalidate();
                    return "redirect:/login?updated";
                }
                flash.addFlashAttribute("success", "Account successfully updated.");
                return "redirect:"
                        + (currentUser.getId().equals(target)
                                ? "/account"
                                : "/staff/customers/" + target);
            } catch (BusinessException
                    | DataIntegrityViolationException
                    | OptimisticLockingFailureException exception) {
                addFormError(errors, exception);
            }
        }
        return "account-form";
    }

    @GetMapping("/staff/customers")
    String customers(@RequestParam(defaultValue = "") String q, Principal principal, Model model) {
        model.addAttribute("q", q);
        model.addAttribute("customers", service.customers(getCurrentUser(principal), q));
        return "customers";
    }

    @GetMapping("/staff/customers/{id}")
    String customer(@PathVariable Long id, Principal principal, Model model) {
        Account currentUser = getCurrentUser(principal);
        model.addAttribute("customer", service.accessibleCustomer(currentUser, id));
        model.addAttribute("profile", service.profile(currentUser, id));
        model.addAttribute("customerOrders", service.customerOrders(currentUser, id));
        return "customer";
    }

    @GetMapping("/spectacle-profile")
    String myProfile(Principal principal, Model model) {
        model.addAttribute("customer", getCurrentUser(principal));
        model.addAttribute(
                "profile",
                service.profile(getCurrentUser(principal), getCurrentUser(principal).getId()));
        return "profile";
    }

    @GetMapping("/staff/customers/{id}/profile")
    String viewProfile(@PathVariable Long id, Principal principal, Model model) {
        model.addAttribute("customer", service.accessibleCustomer(getCurrentUser(principal), id));
        model.addAttribute("profile", service.profile(getCurrentUser(principal), id));
        return "profile";
    }

    @GetMapping("/staff/customers/{id}/profile/edit")
    String editProfile(@PathVariable Long id, Principal principal, Model model) {
        Account currentUser = getCurrentUser(principal);
        SpectacleProfile profile = service.profile(currentUser, id);
        ProfileForm form = new ProfileForm();
        if (profile != null) BeanUtils.copyProperties(profile, form);
        model.addAttribute("customer", service.accessibleCustomer(currentUser, id));
        model.addAttribute("form", form);
        return "profile-form";
    }

    @PostMapping("/staff/customers/{id}/profile/edit")
    String saveProfile(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") ProfileForm form,
            BindingResult errors,
            Principal principal,
            Model model,
            RedirectAttributes flash) {
        Account currentUser = getCurrentUser(principal);
        model.addAttribute("customer", service.accessibleCustomer(currentUser, id));
        if (!errors.hasErrors()) {
            try {
                service.saveProfile(currentUser, id, form);
                flash.addFlashAttribute("success", "Your changes have been saved!");
                return "redirect:/staff/customers/" + id + "/profile";
            } catch (BusinessException
                    | DataIntegrityViolationException
                    | OptimisticLockingFailureException exception) {
                addFormError(errors, exception);
            }
        }
        return "profile-form";
    }

    @GetMapping("/orders")
    String orders(
            @RequestParam(defaultValue = "") String status, Principal principal, Model model) {
        List<CustomerOrder> matchingOrders = new ArrayList<>();
        for (CustomerOrder order : service.visibleOrders(getCurrentUser(principal))) {
            if (status.isEmpty() || status.equals(order.getStatus())) {
                matchingOrders.add(order);
            }
        }
        model.addAttribute("orders", matchingOrders);
        model.addAttribute("status", status);
        model.addAttribute("statuses", MeteorService.STATUSES);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    String order(@PathVariable Long id, Principal principal, Model model) {
        Account currentUser = getCurrentUser(principal);
        CustomerOrder order = service.order(currentUser, id);
        model.addAttribute("order", order);
        model.addAttribute(
                "customer", service.accessibleCustomer(currentUser, order.getCustomerId()));
        model.addAttribute("history", service.history(currentUser, id));
        return "order-detail";
    }

    private void orderOptions(Long customerId, Account currentUser, Model model) {
        model.addAttribute("customer", service.accessibleCustomer(currentUser, customerId));
        model.addAttribute("profile", service.profile(currentUser, customerId));
        model.addAttribute("frames", service.catalog("FRAME", null));
        model.addAttribute("lenses", service.catalog("LENS", null));
    }

    @GetMapping("/staff/customers/{id}/orders/new")
    String newOrder(@PathVariable Long id, Principal principal, Model model) {
        Account currentUser = getCurrentUser(principal);
        orderOptions(id, currentUser, model);
        OrderForm form = new OrderForm();
        form.setShippingAddress(service.account(id).getAddress());
        model.addAttribute("form", form);
        return "order-form";
    }

    @PostMapping("/staff/customers/{id}/orders/new")
    String createOrder(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") OrderForm form,
            BindingResult errors,
            Principal principal,
            Model model,
            RedirectAttributes flash) {
        Account currentUser = getCurrentUser(principal);
        orderOptions(id, currentUser, model);
        if (!errors.hasErrors()) {
            try {
                CustomerOrder order = service.createOrder(currentUser, id, form);
                flash.addFlashAttribute("success", "Order created.");
                return "redirect:/orders/" + order.getId();
            } catch (BusinessException
                    | DataIntegrityViolationException
                    | OptimisticLockingFailureException exception) {
                addFormError(errors, exception);
            }
        }
        return "order-form";
    }

    @GetMapping("/staff/orders/{id}/edit")
    String editOrder(@PathVariable Long id, Principal principal, Model model) {
        CustomerOrder order = service.order(getCurrentUser(principal), id);
        OrderUpdateForm form = new OrderUpdateForm();
        BeanUtils.copyProperties(order, form);
        model.addAttribute("form", form);
        model.addAttribute("order", order);
        model.addAttribute("statuses", service.nextStatuses(order));
        return "order-edit";
    }

    @PostMapping("/staff/orders/{id}/edit")
    String saveOrder(
            @PathVariable Long id,
            @Valid @ModelAttribute("form") OrderUpdateForm form,
            BindingResult errors,
            Principal principal,
            Model model,
            RedirectAttributes flash) {
        Account currentUser = getCurrentUser(principal);
        CustomerOrder order = service.order(currentUser, id);
        model.addAttribute("order", order);
        model.addAttribute("statuses", service.nextStatuses(order));
        if (!errors.hasErrors()) {
            try {
                service.updateOrder(currentUser, id, form);
                flash.addFlashAttribute("success", "Order successfully updated.");
                return "redirect:/orders/" + id;
            } catch (BusinessException
                    | DataIntegrityViolationException
                    | OptimisticLockingFailureException exception) {
                addFormError(errors, exception);
            }
        }
        return "order-edit";
    }

    @GetMapping("/staff/reports")
    String reports(Principal principal, Model model) {
        List<CustomerOrder> orders = service.visibleOrders(getCurrentUser(principal));
        List<Product> products = service.catalog(null, null);
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String status : MeteorService.STATUSES) {
            counts.put(status, 0L);
        }

        BigDecimal orderValue = BigDecimal.ZERO;
        for (CustomerOrder order : orders) {
            String status = order.getStatus();
            counts.put(status, counts.get(status) + 1);
            if (!"Cancelled".equals(status)) {
                orderValue = orderValue.add(order.getTotal());
            }
        }

        BigDecimal inventoryValue = BigDecimal.ZERO;
        List<Product> lowStock = new ArrayList<>();
        for (Product product : products) {
            BigDecimal stockValue =
                    product.getPrice().multiply(BigDecimal.valueOf(product.getQuantity()));
            inventoryValue = inventoryValue.add(stockValue);
            if (product.getQuantity() <= 5) {
                lowStock.add(product);
            }
        }

        model.addAttribute("statusCounts", counts);
        model.addAttribute("orders", orders);
        model.addAttribute("products", products);
        model.addAttribute("orderValue", orderValue);
        model.addAttribute("inventoryValue", inventoryValue);
        model.addAttribute("lowStock", lowStock);
        return "reports";
    }

    private void addFormError(BindingResult errors, RuntimeException exception) {
        if (exception instanceof BusinessException) {
            errors.reject("business", exception.getMessage());
        } else if (exception instanceof DataIntegrityViolationException) {
            errors.reject(
                    "duplicate",
                    "A matching record already exists. Reload and check your details.");
        } else {
            errors.reject(
                    "conflict", "This record changed in another session. Reload before saving.");
        }
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String businessError(BusinessException e, Model model) {
        model.addAttribute("message", e.getMessage());
        return "error";
    }
}
