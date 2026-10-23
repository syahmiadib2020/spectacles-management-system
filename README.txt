Spectacles Management System

This is a degree group project for Meteor Eyewear. It lets staff manage frames, lenses, customer spectacle profiles and orders. Customers can view the catalogue, update their account and check their own orders.

The original project was done by the Pokémon group in 2024–2025 using Oracle and Microsoft Azure. Access to those accounts was lost. This version was rebuilt in 2026 from the remaining documents, HTML pages and images. It uses sample data, not the old customer records.

Screenshots

These 36 screenshots cover the main staff and customer pages. All customer details are fictional. Click an image to view it at full size.

Login and project information

| Login | Customer registration |
| --- | --- |
| Login: docs/screenshots/login.jpg | Customer registration: docs/screenshots/signup.jpg |

| About the project |
| --- |
| About the project: docs/screenshots/about.jpg |

Staff overview and account

| Staff dashboard | Staff account |
| --- | --- |
| Staff dashboard: docs/screenshots/staff-dashboard.jpg | Staff account: docs/screenshots/staff-account.jpg |

| Update staff account |
| --- |
| Update staff account: docs/screenshots/edit-staff-account.jpg |

Frames and lenses

| Frame catalogue | Lens catalogue |
| --- | --- |
| Frame catalogue: docs/screenshots/frame-catalogue.jpg | Lens catalogue: docs/screenshots/lens-catalogue.jpg |

| Frame details | Lens details |
| --- | --- |
| Frame details: docs/screenshots/frame-details.jpg | Lens details: docs/screenshots/lens-details.jpg |

| Add frame | Add lens |
| --- | --- |
| Add frame: docs/screenshots/add-frame.jpg | Add lens: docs/screenshots/add-lens.jpg |

| Edit frame | Edit lens |
| --- | --- |
| Edit frame: docs/screenshots/edit-frame.jpg | Edit lens: docs/screenshots/edit-lens.jpg |

Customer management and spectacle profiles

| Find a customer | Customer details |
| --- | --- |
| Find a customer: docs/screenshots/customer-search.jpg | Customer details: docs/screenshots/customer-details.jpg |

| Update customer account | Customer spectacle profile |
| --- | --- |
| Update customer account: docs/screenshots/edit-customer.jpg | Customer spectacle profile: docs/screenshots/spectacle-profile.jpg |

| Create spectacle profile | Update spectacle profile |
| --- | --- |
| Create spectacle profile: docs/screenshots/new-spectacle-profile.jpg | Update spectacle profile: docs/screenshots/edit-spectacle-profile.jpg |

Orders and reports

| Order list | Order details and status history |
| --- | --- |
| Order list: docs/screenshots/staff-orders.jpg | Order details and status history: docs/screenshots/order-details.jpg |

| Create order | Customer delivery and order total |
| --- | --- |
| Create order: docs/screenshots/new-order.jpg | Customer delivery and order total: docs/screenshots/new-order-delivery.jpg |

| Update order status | Stock and order reports |
| --- | --- |
| Update order status: docs/screenshots/edit-order.jpg | Stock and order reports: docs/screenshots/reports.jpg |

Customer screens

| Customer dashboard | Customer account |
| --- | --- |
| Customer dashboard: docs/screenshots/customer-dashboard.jpg | Customer account: docs/screenshots/customer-account.jpg |

| Update own account | View own spectacle profile |
| --- | --- |
| Update own account: docs/screenshots/edit-customer-account.jpg | View own spectacle profile: docs/screenshots/customer-spectacle-profile.jpg |

| My orders | View own order |
| --- | --- |
| My orders: docs/screenshots/customer-orders.jpg | View own order: docs/screenshots/customer-order-details.jpg |

| Browse frames | Browse lenses |
| --- | --- |
| Browse frames: docs/screenshots/customer-frame-catalogue.jpg | Browse lenses: docs/screenshots/customer-lens-catalogue.jpg |

| View frame details | View lens details |
| --- | --- |
| View frame details: docs/screenshots/customer-frame-details.jpg | View lens details: docs/screenshots/customer-lens-details.jpg |

Run the project

You need Java 17 or newer. Building from source needs a JDK and an internet connection the first time.

On Windows:

1. Double-click Start-Demo.cmd.
2. Leave the window open.
3. Open http://localhost:8080 (http://localhost:8080).
4. Use the Staff or Customer button to fill in a demo account, then click Log in.
5. Press Ctrl+C in the application window when finished.

The launcher builds the project if the JAR file is missing. To build and test it yourself:

.\mvnw.cmd verify
java -jar target\meteor-eyewear-1.0.0.jar

On macOS or Linux, use sh ./mvnw verify and java -jar target/meteor-eyewear-1.0.0.jar.

Demo accounts

| Role | Email | Password |
| --- | --- | --- |
| Staff | staff@meteor.example | StaffDemo2026! |
| Customer | customer@meteor.example | CustomerDemo2026! |

All demo customer details are made up. Customer IC 000000000002 already has a spectacle profile. IC 000000000003 has no profile yet.

Main functions

- Register, view and update accounts.
- Search for customers by IC number, name or email.
- Add, view, edit and remove frames and lenses.
- Record left and right eye power, prescription type and astigmatism.
- Create an order with a frame and lens.
- Arrange delivery to the Merlimau outlet or a customer address.
- Update order status and view its history.
- View stock and order reports.

Staff manage products, customer profiles and orders. Customers can only access their own account, profile and orders.

Tools used

- Java 17 and Spring Boot
- Thymeleaf for the HTML pages
- Spring Security for login and access checks
- Spring Data JPA for database queries
- H2 for the local database
- JUnit and MockMvc for tests

H2 saves data in the data folder, so changes remain after restarting. Oracle compatibility mode is enabled, but H2 is still a different database. No Oracle or Azure account is needed to run this demo.

Trying the main flow

Log in as staff. Open Customer and search for IC 000000000002. Check the spectacle profile, create an order, then update its status. Log out and use the customer account to view that order.

QA & Testing

All 64 automated tests passed locally and in GitHub Actions. The tests check:

- Valid and invalid logins, required fields and duplicate accounts.
- Staff permissions and customers being limited to their own records.
- Frame, lens and spectacle-profile changes.
- Order totals, delivery details, status changes and cancellation.
- Two orders competing for the last frame: only one should succeed.

Browser checks covered staff/customer login, customer search, order forms, live totals and saved data after restarting. Tests use a separate database, so they do not change the local demo records.

Run mvnw.cmd verify on Windows or sh ./mvnw verify on macOS/Linux. See test notes (docs/VERIFICATION.md) and the GitHub checks (https://github.com/syahmiadib2020/spectacles-management-system/actions/workflows/verify.yml).

Oracle and Azure deployment have not been tested.

Notes about this version

The original documents and personal data are kept out of this repository. The group is credited below. The code was rebuilt; it is not the recovered original source. Differences are listed in rebuild notes (docs/RECONSTRUCTION.md).

Google/Facebook login, notifications, payments, image uploads, password recovery and staff account setup are not included. The Oracle profile and Dockerfile are starting points and have not been tested on Oracle or Azure. See database notes (docs/ORACLE-AND-AZURE.md).

My contribution

My documented work in the original group project covered:

- The Update Account use-case description.
- The use-case diagram.
- Part of the overall project description in the SRS report.

These contributions are recorded in the original project documents. The original project was group work; the team members are listed below. This repository also contains the rebuilt 2026 version, with differences explained in the rebuild notes.

Original group

- Ammar Yusuf bin Moh Bakery
- Adib Syahmi bin Azman
- Nurul Afrina binti Abdul Rahim
- Wan Nur Syahira binti Che Wan Abd Aziz
- Nur Farah Izzati binti Mohd Azami

This copy is prepared for Adib Syahmi's portfolio. Image sources and credits are in ATTRIBUTION.md (docs/ATTRIBUTION.md).
