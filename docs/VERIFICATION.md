# Test notes

The project has 64 automated test cases. Run them with `mvnw.cmd verify` on Windows or `sh ./mvnw verify` on macOS/Linux.

SystemIntegrationTest checks page rendering, login, permissions, validation, accounts, products, spectacle profiles and orders. StockConcurrencyTest places two orders at the same time when only one frame remains. Only one order should succeed.

Tests use separate in-memory H2 databases. They do not change the demo records saved in the data folder.

Browser checks covered staff/customer login, customer IC search, lens listing, order selection, price totals, delivery fields, order creation and customer profile viewing. Saved orders were still present after restarting the application.

GitHub Actions also passed all 64 tests on 23 October 2026. Oracle, Azure and the Docker container have not been tested. Google/Facebook login, notifications, payments and image uploads are not implemented.

The readability update passed all 64 tests on 23 October 2026. Browser checks also confirmed demo login, order totals and delivery fields.
