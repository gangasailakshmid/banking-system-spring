(() => {
    const grid = document.querySelector('[data-user-grid]');
    if (!grid) return;
    const search = grid.querySelector('#user-search');
    const results = grid.querySelector('#user-grid-results');
    const count = grid.querySelector('[data-user-count]');
    const searchStatus = grid.querySelector('#user-search-status');
    const reload = grid.querySelector('[data-reload-users]');
    const reloadStatus = grid.querySelector('[data-reload-status]');
    const normalize = value => value.normalize('NFKD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/\s+/g, ' ').trim();
    let entries;

    function indexRows() {
        entries = [...results.querySelectorAll('[data-user-row]')].map(row => ({
            row,
            text: normalize([...row.cells].slice(0, 3).map(cell => cell.textContent).join(' '))
        }));
    }

    function filter() {
        const terms = normalize(search.value).split(' ').filter(Boolean);
        let matches = 0;
        entries.forEach(({row, text}) => {
            row.hidden = !terms.every(term => text.includes(term));
            if (!row.hidden) matches++;
        });
        const empty = results.querySelector('#search-empty');
        if (empty) empty.hidden = matches > 0 || terms.length === 0;
        searchStatus.textContent = terms.length ? `${matches} of ${entries.length} users match your search` : '';
    }

    reload.addEventListener('click', async () => {
        reload.disabled = true;
        results.setAttribute('aria-busy', 'true');
        reloadStatus.textContent = 'Reloading users…';
        try {
            const response = await fetch(reload.dataset.url, {cache: 'no-store'});
            if (!response.ok) throw new Error('Directory request failed');
            const page = new DOMParser().parseFromString(await response.text(), 'text/html');
            const updated = page.querySelector('#user-grid-results');
            const updatedCount = page.querySelector('[data-user-count]');
            if (!updated || !updatedCount) throw new Error('Directory unavailable');

            results.replaceChildren(...updated.childNodes);
            count.textContent = updatedCount.textContent;
            indexRows();
            filter();
            reloadStatus.textContent = 'Users reloaded.';
        } catch {
            reloadStatus.textContent = 'Could not reload users. If your session expired, sign in again.';
        } finally {
            results.removeAttribute('aria-busy');
            reload.disabled = false;
        }
    });

    indexRows();
    filter();
    search.addEventListener('input', filter);
    search.addEventListener('search', filter);
    reload.hidden = false;
})();
