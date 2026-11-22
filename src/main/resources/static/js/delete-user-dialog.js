(() => {
    const dialog = document.querySelector('#delete-user-dialog');
    if (!dialog) return;
    const confirm = dialog.querySelector('[data-confirm-delete]');
    let pendingForm = null;
    let trigger = null;
    let confirmed = false;

    document.querySelector('[data-user-grid]').addEventListener('submit', event => {
        const form = event.target;
        if (!form.matches('[data-delete-user]')) return;
        if (confirmed && pendingForm === form) return;
        event.preventDefault();
        pendingForm = form;
        trigger = event.submitter || form.querySelector('button');
        const name = form.dataset.userName.trim().replace(/\s+/g, ' ');
        // Use textContent to keep names and contact information as plain text.
        dialog.querySelector('[data-delete-name]').textContent = name;
        dialog.querySelector('[data-delete-id]').textContent = form.dataset.userId;
        dialog.querySelector('[data-delete-phone]').textContent = form.dataset.userPhone;
        dialog.querySelector('[data-delete-location]').textContent = form.dataset.userLocation;
        dialog.querySelector('[data-delete-avatar]').textContent = name.split(' ').filter(Boolean)
            .filter((_, i, words) => i === 0 || i === words.length - 1)
            .map(word => Array.from(word)[0]).join('').toUpperCase();
        dialog.showModal();
    });

    dialog.querySelectorAll('[data-dismiss-delete]').forEach(button => {
        button.addEventListener('click', () => dialog.close());
    });
    dialog.addEventListener('click', event => {
        const bounds = dialog.getBoundingClientRect();
        if (event.target === dialog && (event.clientX < bounds.left || event.clientX > bounds.right
            || event.clientY < bounds.top || event.clientY > bounds.bottom)) dialog.close();
    });
    dialog.addEventListener('close', () => {
        pendingForm = null;
        confirmed = false;
        confirm.disabled = false;
        confirm.textContent = 'Yes, delete user';
        trigger?.focus();
        trigger = null;
    });
    confirm.addEventListener('click', () => {
        if (!pendingForm || confirmed) return;
        confirmed = true;
        confirm.disabled = true;
        confirm.textContent = 'Deleting…';
        pendingForm.requestSubmit();
    });
})();
