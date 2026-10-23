# Rebuild notes

The remaining project folder contained five PDFs, five HTML pages, four CSS files, images and Eclipse settings. The Java source and compiled-code folders were empty. No database export or Azure configuration was found.

The original files are left unchanged. This repository uses made-up customer details. The private PDFs, portrait, student numbers and old test passwords are excluded.

## What was used

- SRS report (2024): roles, use cases and diagrams.
- Use Case Description (June 2024): account creation, frame deletion and account updates.
- Enterprise Programming proposal (December 2024): database tables and interface screenshots.
- STD (2025): inputs, expected results and order procedures.
- STR (July 2025): test results and recorded problems.
- HTML/CSS prototype: colours, images, navigation and page fields.

## What changed

The backend was written again using Java 17 and Spring Boot. H2 replaces the unavailable database for local use. The owner confirms that the original group used Oracle and Microsoft Azure; the earlier SRS mentions MySQL, which is a mismatch in the old documents.

Staff and customers share an accounts table with different roles. Frames and lenses share a products table. This avoids the circular relationships in the proposal. Phone and IC values are strings to keep leading zeros. Prices use BigDecimal.

The later test instructions are followed for customer lookup and delivery. Eye power is required in customer profiles; prescription type and astigmatism are optional. Both are required for lens catalogue entries.

Stock checks, order history, saved prices, edit-version checks and automated tests were added. Product deletion hides the product rather than removing its database row. The old report's validation and lens-listing problems were fixed rather than copied.

The colours and branding are retained, but spacing, headings and smaller-screen layouts differ. Some screens had to be designed from written descriptions. This version follows the available workflows; it cannot reproduce the exact missing code, database or final 2025 screens.
