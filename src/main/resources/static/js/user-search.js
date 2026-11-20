(() => {
    const search = document.querySelector('#user-search');
    if (!search) return;
    const rows = [...document.querySelectorAll('[data-user-row]')];
    const empty = document.querySelector('#search-empty');
    const status = document.querySelector('#user-search-status');
    const normalize = value => value.normalize('NFKD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replace(/\s+/g, ' ').trim();
    // Search only the visible user details, excluding action labels and hidden tokens.
    const entries = rows.map(row => ({row, text: normalize([...row.cells].slice(0, 3).map(cell => cell.textContent).join(' '))}));
    function filter() {
        const terms = normalize(search.value).split(' ').filter(Boolean);
        let matches = 0;
        entries.forEach(({row, text}) => {
            row.hidden = !terms.every(term => text.includes(term));
            if (!row.hidden) matches++;
        });
        if (empty) empty.hidden = matches > 0 || terms.length === 0;
        status.textContent = terms.length ? `${matches} of ${rows.length} users match your search` : '';
    }
    search.addEventListener('input', filter);
    search.addEventListener('search', filter);
})();
