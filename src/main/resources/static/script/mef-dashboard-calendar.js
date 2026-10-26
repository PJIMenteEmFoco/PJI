/* Calendário compacto do dashboard do aluno. Não substitui a agenda completa. */
(() => {
    'use strict';

    const calendarBody = document.getElementById('calendar-body');
    const monthSelect = document.getElementById('cal-month-select');
    const yearSelect = document.getElementById('cal-year-select');
    if (!calendarBody || !monthSelect || !yearSelect) return;

    const monthNames = [
        'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
        'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
    ];
    const today = new Date();
    const currentMonth = today.getMonth();
    const currentYear = today.getFullYear();

    monthSelect.replaceChildren(...monthNames.map((name, month) =>
        new Option(name, String(month), month === currentMonth, month === currentMonth)
    ));
    const yearOptions = [];
    for (let year = currentYear - 2; year <= currentYear + 2; year += 1) {
        yearOptions.push(new Option(String(year), String(year), year === currentYear, year === currentYear));
    }
    yearSelect.replaceChildren(...yearOptions);

    function generateCalendar(month, year) {
        calendarBody.replaceChildren();

        const firstDayIndex = new Date(year, month, 1).getDay();
        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const daysInPreviousMonth = new Date(year, month, 0).getDate();
        const rowCount = Math.ceil((firstDayIndex + daysInMonth) / 7);
        let day = 1;
        let nextMonthDay = 1;

        for (let rowIndex = 0; rowIndex < rowCount; rowIndex += 1) {
            const row = document.createElement('tr');

            for (let columnIndex = 0; columnIndex < 7; columnIndex += 1) {
                const cell = document.createElement('td');
                const cellIndex = rowIndex * 7 + columnIndex;

                if (cellIndex < firstDayIndex) {
                    cell.classList.add('muted');
                    cell.textContent = String(daysInPreviousMonth - firstDayIndex + cellIndex + 1);
                } else if (day <= daysInMonth) {
                    if (day === today.getDate() && month === currentMonth && year === currentYear) {
                        const badge = document.createElement('span');
                        badge.className = 'day-badge orange';
                        badge.setAttribute('aria-label', 'Hoje');
                        badge.textContent = String(day);
                        cell.appendChild(badge);
                    } else {
                        cell.textContent = String(day);
                    }
                    day += 1;
                } else {
                    cell.classList.add('muted');
                    cell.textContent = String(nextMonthDay);
                    nextMonthDay += 1;
                }
                row.appendChild(cell);
            }
            calendarBody.appendChild(row);
        }
    }

    const updateCalendar = () => generateCalendar(Number(monthSelect.value), Number(yearSelect.value));
    monthSelect.addEventListener('change', updateCalendar);
    yearSelect.addEventListener('change', updateCalendar);
    updateCalendar();
})();
