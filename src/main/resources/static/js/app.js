// Fill a demo account. The user still clicks Log in.
const demoButtons = document.querySelectorAll('[data-demo]');
for (const button of demoButtons) {
    button.addEventListener('click', function () {
        if (this.dataset.demo === 'staff') {
            document.getElementById('email').value = 'staff@meteor.example';
            document.getElementById('password').value = 'StaffDemo2026!';
        } else {
            document.getElementById('email').value = 'customer@meteor.example';
            document.getElementById('password').value = 'CustomerDemo2026!';
        }
        document.querySelector('button[type="submit"]').focus();
    });
}

const deleteForms = document.querySelectorAll('[data-confirm]');
for (const form of deleteForms) {
    form.addEventListener('submit', function (event) {
        if (!window.confirm(this.dataset.confirm)) {
            event.preventDefault();
        }
    });
}

const printButtons = document.querySelectorAll('[data-print]');
for (const button of printButtons) {
    button.addEventListener('click', function () {
        window.print();
    });
}

// Only submit the fields for the selected product type.
const productType = document.querySelector('[data-product-type]');
function updateProductFields() {
    const fieldsets = document.querySelectorAll('[data-type-fields]');
    for (const fieldset of fieldsets) {
        const isSelected = fieldset.dataset.typeFields === productType.value;
        fieldset.hidden = !isSelected;
        fieldset.disabled = !isSelected;
    }
}
if (productType) {
    productType.addEventListener('change', updateProductFields);
    updateProductFields();
}

const delivery = document.querySelector('[data-delivery]');
function updateDeliveryFields() {
    const sections = document.querySelectorAll('[data-delivery-fields]');
    for (const section of sections) {
        const isSelected = section.dataset.deliveryFields === delivery.value;
        section.hidden = !isSelected;
        const inputs = section.querySelectorAll('[data-conditional-required]');
        for (const input of inputs) {
            input.required = isSelected;
            input.disabled = !isSelected;
        }
    }
}
if (delivery) {
    delivery.addEventListener('change', updateDeliveryFields);
    updateDeliveryFields();
}

// This total is a preview. Java checks the current prices when saving the order.
function updateOrderTotal() {
    const output = document.getElementById('order-estimate');
    if (!output) {
        return;
    }

    const productSelects = document.querySelectorAll('[data-price-select]');
    const quantity = document.querySelector('[data-quantity]');
    let totalCents = 0;

    for (const select of productSelects) {
        const option = select.options[select.selectedIndex];
        if (!option || !option.dataset.price || !quantity.value) {
            output.textContent = 'Select your items';
            return;
        }
        totalCents = totalCents + Math.round(Number(option.dataset.price) * 100);
    }

    const total = totalCents * Number(quantity.value) / 100;
    output.textContent = new Intl.NumberFormat('en-MY', {
        style: 'currency',
        currency: 'MYR'
    }).format(total);
}

const orderInputs = document.querySelectorAll('[data-price-select], [data-quantity]');
for (const input of orderInputs) {
    input.addEventListener('input', updateOrderTotal);
}
updateOrderTotal();
