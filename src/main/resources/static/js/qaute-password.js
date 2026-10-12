(() => {
    'use strict';
    const eye = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z"/><circle cx="12" cy="12" r="3"/></svg>';
    const eyeOff = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" aria-hidden="true"><path d="m3 3 18 18M9.5 5.3A12 12 0 0 1 12 5c6.5 0 10 7 10 7a18 18 0 0 1-3.1 4M6.1 6.1A19 19 0 0 0 2 12s3.5 7 10 7a12 12 0 0 0 5.9-1.5M10 10a3 3 0 0 0 4 4"/></svg>';
    function initialize() {
        document.querySelectorAll('input[type="password"]').forEach((input, index) => {
            if (input.dataset.passwordToggle) return;
            input.dataset.passwordToggle = 'true';
            if (!input.id) {
                let id = 'qaute-password-' + index;
                while (document.getElementById(id)) id += '-field';
                input.id = id;
            }
            let group = input.parentElement;
            if (!group.classList.contains('input-group')) {
                group = document.createElement('div');
                group.className = 'input-group' + (input.classList.contains('form-control-sm') ? ' input-group-sm' : '');
                input.before(group);
                group.append(input);
            }
            const toggle = document.createElement('button');
            toggle.type = 'button';
            toggle.className = 'btn btn-outline-secondary qaute-password-toggle';
            toggle.setAttribute('aria-controls', input.id);
            function update() {
                const visible = input.type === 'text';
                const label = visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu';
                toggle.setAttribute('aria-label', label);
                toggle.setAttribute('aria-pressed', String(visible));
                toggle.title = label;
                toggle.innerHTML = visible ? eyeOff : eye;
            }
            toggle.addEventListener('click', () => {
                const start = input.selectionStart;
                const end = input.selectionEnd;
                input.type = input.type === 'password' ? 'text' : 'password';
                update();
                input.focus({preventScroll: true});
                if (start !== null && end !== null) input.setSelectionRange(start, end);
            });
            if (input.form) input.form.addEventListener('reset', () => { input.type = 'password'; update(); });
            group.append(toggle);
            update();
        });
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
    else initialize();
})();
