document.querySelectorAll('[data-report-export]').forEach(form => {
    const from = form.elements.namedItem('from');
    const to = form.elements.namedItem('to');
    if (!from || !to) return;
    const validate = () => {
        to.setCustomValidity(from.value && to.value && from.value > to.value
            ? 'Ngày kết thúc phải từ ngày bắt đầu trở đi.' : '');
    };
    from.addEventListener('input', validate);
    to.addEventListener('input', validate);
    form.addEventListener('submit', event => {
        validate();
        if (!form.reportValidity()) event.preventDefault();
    });
});
