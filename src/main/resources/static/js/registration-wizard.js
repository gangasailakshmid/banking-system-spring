(() => {
    const form = document.querySelector('.user-form');
    if (!form) return;
    const phases = [...form.querySelectorAll('fieldset')];
    const progress = form.querySelector('.step-progress');
    const steps = [...progress.children];
    const caption = form.querySelector('.step-caption');
    const back = form.querySelector('[data-back]');
    const next = form.querySelector('[data-next]');
    const finish = form.querySelector('[data-finish]');
    let current = 0;
    // Validate manually so invalid controls in another phase can be revealed first.
    form.noValidate = true;
    progress.hidden = false;
    caption.hidden = false;

    function show(index, focus = true) {
        current = index;
        phases.forEach((phase, i) => { phase.hidden = i !== current; });
        steps.forEach((step, i) => {
            step.classList.toggle('complete', i < current);
            step.classList.toggle('current', i === current);
            if (i === current) step.setAttribute('aria-current', 'step');
            else step.removeAttribute('aria-current');
            step.querySelector('.step-number').textContent = i < current ? '✓' : String(i + 1);
            step.querySelector('.step-state').textContent = i < current ? 'Completed' : i === current ? 'Current' : 'Upcoming';
        });
        caption.textContent = `Step ${current + 1} of ${phases.length} · ${['Personal details', 'Address & contact', 'Bank details'][current]}`;
        back.hidden = current === 0;
        next.hidden = current === phases.length - 1;
        finish.hidden = current !== phases.length - 1;
        if (focus) phases[current].querySelector('input, select, textarea')?.focus();
    }

    function invalidControl(phase) {
        return [...phase.querySelectorAll('input, select, textarea')].find(control => !control.checkValidity());
    }

    function advance() {
        const invalid = invalidControl(phases[current]);
        if (invalid) { invalid.reportValidity(); return; }
        show(current + 1);
    }

    back.addEventListener('click', () => show(current - 1));
    next.addEventListener('click', advance);
    form.addEventListener('keydown', event => {
        if (event.key === 'Enter' && !event.isComposing && current < phases.length - 1
            && event.target.matches('input')) {
            event.preventDefault();
            advance();
        }
    });
    form.addEventListener('submit', event => {
        if (current < phases.length - 1) {
            event.preventDefault();
            advance();
            return;
        }
        for (let i = 0; i < phases.length; i++) {
            const invalid = invalidControl(phases[i]);
            if (invalid) {
                event.preventDefault();
                show(i, false);
                invalid.reportValidity();
                return;
            }
        }
        finish.disabled = true;
        finish.textContent = 'Saving…';
    });
    show(0, false);
})();
