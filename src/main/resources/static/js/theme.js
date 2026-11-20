(() => {
    const choices = [...document.querySelectorAll('[data-theme-choice]')];
    const themes = new Set(choices.map(button => button.dataset.themeChoice));
    const picker = document.querySelector('.skin-picker');
    const trigger = picker?.querySelector('summary');
    const label = document.querySelector('[data-skin-name]');
    const storageKey = 'user-registry-theme';
    function apply(theme) {
        if (!themes.has(theme)) theme = 'navy';
        document.documentElement.dataset.theme = theme;
        if (label) label.textContent = choices.find(button => button.dataset.themeChoice === theme)?.dataset.skinLabel || 'Navy';
        choices.forEach(button => {
            button.setAttribute('aria-pressed', String(button.dataset.themeChoice === theme));
        });
    }
    let saved = 'navy';
    try { saved = localStorage.getItem(storageKey) || 'navy'; } catch { /* Storage can be unavailable. */ }
    apply(saved);
    choices.forEach(button => {
        button.addEventListener('click', () => {
            apply(button.dataset.themeChoice);
            if (picker) picker.open = false;
            trigger?.focus();
            try { localStorage.setItem(storageKey, button.dataset.themeChoice); } catch { /* Theme still works without storage. */ }
        });
    });
    document.addEventListener('click', event => {
        if (picker?.open && !picker.contains(event.target)) picker.open = false;
    });
    picker?.addEventListener('keydown', event => {
        if (event.key === 'Escape') {
            picker.open = false;
            trigger?.focus();
            event.preventDefault();
        }
        if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
            event.preventDefault();
            picker.open = true;
            const index = choices.indexOf(document.activeElement);
            const next = index < 0 ? (event.key === 'ArrowDown' ? 0 : choices.length - 1)
                : (index + (event.key === 'ArrowDown' ? 1 : -1) + choices.length) % choices.length;
            choices[next].focus();
        }
    });
    window.addEventListener('storage' , event => {
        if (event.key === storageKey || event.key === null) apply(event.newValue || 'navy');
    });
})();
