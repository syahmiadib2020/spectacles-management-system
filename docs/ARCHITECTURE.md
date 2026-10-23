# How the code is organised

The project has a Java backend and HTML pages. It runs as one application.

## Files to start with

| File | What it does |
| --- | --- |
| MeteorApplication.java | Starts Spring Boot |
| WebController.java | Receives page requests and form submissions |
| MeteorService.java | Checks the rules and saves changes |
| Repositories.java | Contains the database query interfaces |
| SecurityConfig.java | Sets up login, passwords and access rules |
| DemoData.java | Adds sample records to an empty database |
| Account.java, Product.java, SpectacleProfile.java, CustomerOrder.java, OrderEvent.java | Describe the database records |
| Classes ending in Form | Hold submitted values and validation rules |
| resources/templates | Contains the Thymeleaf HTML pages |
| resources/static | Contains CSS, JavaScript and images |

Java files are in `src/main/java/com/meteor/eyewear`. Tests are in `src/test/java/com/meteor/eyewear`.

## Following a form submission

Start with the form's `th:action` in its HTML page. Find that path in WebController. The controller checks the form and calls MeteorService. The service checks permissions and business rules, then uses a repository to read or save records. If there is a problem, the controller shows the form again with an error message.

`@Valid` checks form values, such as required fields and prices. `@Transactional` makes related database changes succeed or fail together. `@Version` helps detect an edit made from an outdated page. The Spring Security configuration uses callbacks because that is how its configuration API works.

## Database tables

- accounts: login details, role and contact details.
- products: frame and lens details, prices and quantities.
- spectacle_profiles: a customer's current eye-power details.
- customer_orders: selected products, prices, delivery details and status.
- order_events: the status changes for each order.

Foreign keys connect these tables. Each customer has at most one current spectacle profile.

## Important rules

An order needs one frame, one lens and a customer profile. Creating it reduces stock. Both products are locked in ID order so two staff members cannot use the last item at the same time.

The order saves a copy of the price and eye-power values. Later product or profile edits do not change those copies. Removing a product hides it from the catalogue but keeps old orders readable.

Cancelling an eligible order returns its stock once. Completed and cancelled orders cannot be edited. Outlet orders go from Pending to In progress to Ready for collection to Completed. Delivery orders use Shipped instead of Ready for collection.

The app is intended for a small demo dataset. Lists are not paginated, and H2 updates the local schema automatically. A real deployment would need a separate setup for migrations, backups, account recovery and monitoring.
