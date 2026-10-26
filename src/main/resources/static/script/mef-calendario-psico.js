(() => {
    'use strict';

    const body = document.body;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';
    const createEventUrl = document.querySelector('meta[name="mef-event-create-url"]')?.content
        || '/psicopedagogo/calendario/eventos';

    if (localStorage.getItem('theme') === 'escuro') body.classList.add('dark-mode');
    if (window.lucide && typeof window.lucide.createIcons === 'function') window.lucide.createIcons();

    const sidebar = document.getElementById('sidebar');
    const toggleButton = document.getElementById('toggle-btn');
    const logoutModal = document.getElementById('logout-modal');
    const newEventModal = document.getElementById('modal-new-event');
    const viewEventModal = document.getElementById('modal-view-event');
    const logoutForm = document.getElementById('logoutForm');

    function toggleSidebar() {
        sidebar?.classList.toggle('expanded');
    }
    toggleButton?.addEventListener('click', toggleSidebar);
    toggleButton?.addEventListener('keydown', event => {
        if (event.key === 'Enter' || event.key === ' ') {
            event.preventDefault();
            toggleSidebar();
        }
    });

    function avisoCalendario(mensagem) {
        if (window.MEFPopups && typeof window.MEFPopups.alert === 'function') {
            window.MEFPopups.alert(mensagem);
        } else {
            window.alert(mensagem);
        }
    }

    function escaparHTML(valor) {
        return String(valor ?? '').replace(/[&<>"']/g, caractere => ({
            '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
        })[caractere]);
    }

    function normalizarPrioridade(valor) {
        const texto = String(valor || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase();
        if (texto === 'ALTA' || texto === 'HIGH') return 'high';
        if (texto === 'BAIXA' || texto === 'LOW') return 'low';
        return 'medium';
    }

    function lerDataEvento(valor) {
        const match = String(valor || '').match(/^(\d{4})-(\d{2})-(\d{2})(?:[T ](\d{2}):(\d{2}))?/);
        if (!match) return null;
        const year = Number(match[1]);
        const month = Number(match[2]);
        const day = Number(match[3]);
        const date = new Date(year, month - 1, day);
        if (date.getFullYear() !== year || date.getMonth() !== month - 1 || date.getDate() !== day) return null;
        return { year, month: month - 1, day, time: match[4] ? `${match[4]}:${match[5]}` : null };
    }

    function normalizarHorario(valor) {
        if (!valor) return null;
        const match = String(valor).match(/^(\d{2}:\d{2})/);
        return match ? match[1] : null;
    }

    function lerEventosDoModel() {
        const eventos = [];
        document.querySelectorAll('#mef-eventos [data-date]').forEach(elemento => {
            const data = lerDataEvento(elemento.dataset.date);
            if (!data) return;
            const prioridade = normalizarPrioridade(elemento.dataset.priority);
            const titulo = elemento.dataset.title || 'Compromisso';
            eventos.push({
                ...data,
                id: elemento.dataset.id || null,
                title: titulo,
                desc: elemento.dataset.description || 'Sem descrição informada.',
                priority: prioridade,
                pillClass: { high: 'orange', medium: 'dark', low: 'blue' }[prioridade],
                labelPill: titulo,
                time: normalizarHorario(elemento.dataset.time) || data.time || 'Dia Todo',
                isHoliday: false
            });
        });

        document.querySelectorAll('#mef-feriados [data-date]').forEach(elemento => {
            const data = lerDataEvento(elemento.dataset.date);
            if (!data) return;
            eventos.push({
                ...data,
                title: elemento.dataset.title || 'Feriado nacional',
                desc: 'Feriado Nacional.',
                isHoliday: true,
                pillClass: 'holiday',
                labelPill: 'Feriado',
                time: 'Dia Todo'
            });
        });

        return eventos.sort((a, b) => a.year - b.year || a.month - b.month || a.day - b.day
            || String(a.time).localeCompare(String(b.time)));
    }

    let databaseEvents = lerEventosDoModel();
    const monthNames = [
        'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
        'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
    ];
    const hoje = new Date();
    let currentYear = hoje.getFullYear();
    let currentMonth = hoje.getMonth();

    const calendarBody = document.getElementById('calendar-body');
    const calendarHeader = document.getElementById('calendar-month-year');
    const agendaMonthLabel = document.getElementById('agenda-month-label');
    const eventsStackList = document.getElementById('events-stack-list');

    function formatarDataCompleta(day, month, year) {
        return `${day} de ${monthNames[month].toLowerCase()} de ${year}`;
    }

    function renderCalendar(year, month) {
        calendarBody.innerHTML = '';
        eventsStackList.innerHTML = '';
        calendarHeader.textContent = `${monthNames[month]} ${year}`;
        agendaMonthLabel.textContent = monthNames[month];

        const firstDayIndex = new Date(year, month, 1).getDay();
        const totalDaysInMonth = new Date(year, month + 1, 0).getDate();
        const previousMonthLastDay = new Date(year, month, 0).getDate();
        let dayCounter = 1;
        let nextMonthDayCounter = 1;
        let sessionsThisMonth = 0;
        let highPriorityCount = 0;
        let daysHtml = '';

        for (let row = 0; row < 6; row++) {
            let rowHtml = '<tr>';
            for (let col = 0; col < 7; col++) {
                const cellIndex = row * 7 + col;
                if (cellIndex < firstDayIndex) {
                    const previousDay = previousMonthLastDay - (firstDayIndex - cellIndex - 1);
                    rowHtml += `<td class="muted-day"><span class="num">${previousDay}</span></td>`;
                } else if (dayCounter > totalDaysInMonth) {
                    rowHtml += `<td class="muted-day"><span class="num">${nextMonthDayCounter}</span></td>`;
                    nextMonthDayCounter++;
                } else {
                    const day = dayCounter;
                    const isToday = day === hoje.getDate() && month === hoje.getMonth() && year === hoje.getFullYear();
                    const dayEvents = databaseEvents.filter(evento => evento.year === year && evento.month === month && evento.day === day);
                    const classes = [isToday ? 'active-today' : '', dayEvents.length ? 'clickable-day' : ''].filter(Boolean).join(' ');
                    const todayTag = isToday ? '<span class="day-tag">Hoje</span>' : '';
                    let pills = '';
                    dayEvents.forEach(evento => {
                        pills += `<div class="event-pill-item ${evento.pillClass}">${escaparHTML(evento.labelPill || evento.title)}</div>`;
                        if (!evento.isHoliday) sessionsThisMonth++;
                        if (evento.priority === 'high') highPriorityCount++;
                    });
                    rowHtml += `<td class="${classes}" data-calendar-day="${day}" data-calendar-year="${year}" data-calendar-month="${month}"><span class="num">${day}</span>${todayTag}${pills}</td>`;
                    dayCounter++;
                }
            }
            rowHtml += '</tr>';
            daysHtml += rowHtml;
            if (dayCounter > totalDaysInMonth && (row + 1) * 7 >= firstDayIndex + totalDaysInMonth) break;
        }

        calendarBody.innerHTML = daysHtml;
        calendarBody.querySelectorAll('[data-calendar-day]').forEach(cell => {
            cell.addEventListener('click', () => {
                const day = Number(cell.dataset.calendarDay);
                const events = databaseEvents.filter(evento => evento.year === year && evento.month === month && evento.day === day);
                if (events.length) openEventDetails(events[0], day, month, year);
            });
        });
        document.getElementById('stat-aulas').textContent = String(sessionsThisMonth);
        document.getElementById('stat-pendentes').textContent = String(highPriorityCount);
        renderAgendaList(year, month);
        if (window.lucide && typeof window.lucide.createIcons === 'function') window.lucide.createIcons();
    }

    function renderAgendaList(year, month) {
        const monthEvents = databaseEvents.filter(evento => evento.year === year && evento.month === month);
        if (!monthEvents.length) {
            eventsStackList.innerHTML = '<p style="font-size:0.85rem;color:#8fa5b4;text-align:center;padding:20px;">Nenhum compromisso agendado para este mês.</p>';
            return;
        }

        const html = monthEvents.map(evento => {
            const index = databaseEvents.indexOf(evento);
            const title = escaparHTML(evento.title);
            const description = escaparHTML(evento.desc);
            const date = `${evento.day} ${monthNames[month].substring(0, 3).toUpperCase()} • ${escaparHTML(evento.time || 'Dia Todo')}`;
            if (evento.isHoliday) {
                return `<div class="event-item-card is-holiday-card" data-event-index="${index}"><div class="event-card-header"><span class="badge-holiday">Feriado Nacional</span><span class="event-date-time">${date}</span></div><h4>${title}</h4><p>${description}</p></div>`;
            }
            const priority = evento.priority || 'medium';
            const priorityText = priority === 'high' ? 'Alta Prioridade' : priority === 'low' ? 'Baixa Prioridade' : 'Média Prioridade';
            return `<div class="event-item-card priority-${priority}" data-event-index="${index}"><div class="event-card-header"><span class="badge-priority ${priority}">${priorityText}</span><span class="event-date-time">${date}</span></div><h4>${title}</h4><p>${description}</p></div>`;
        }).join('');

        eventsStackList.innerHTML = html;
        eventsStackList.querySelectorAll('[data-event-index]').forEach(card => {
            card.addEventListener('click', () => {
                const event = databaseEvents[Number(card.dataset.eventIndex)];
                if (event) openEventDetails(event, event.day, event.month, event.year);
            });
        });
    }

    function openEventDetails(evento, day, month, year) {
        document.getElementById('view-title').textContent = evento.title;
        document.getElementById('view-description').textContent = evento.desc;
        document.getElementById('view-date').textContent = `${formatarDataCompleta(day, month, year)} • ${evento.time || 'Dia Todo'}`;
        viewEventModal.classList.add('active');
    }

    document.getElementById('btn-prev-month').addEventListener('click', () => {
        currentMonth--;
        if (currentMonth < 0) { currentMonth = 11; currentYear--; }
        renderCalendar(currentYear, currentMonth);
    });
    document.getElementById('btn-next-month').addEventListener('click', () => {
        currentMonth++;
        if (currentMonth > 11) { currentMonth = 0; currentYear++; }
        renderCalendar(currentYear, currentMonth);
    });

    document.getElementById('btn-open-new-event').addEventListener('click', () => newEventModal.classList.add('active'));
    document.getElementById('btn-cancel-new-event').addEventListener('click', () => newEventModal.classList.remove('active'));
    document.getElementById('btn-close-view-event').addEventListener('click', () => viewEventModal.classList.remove('active'));
    document.getElementById('btn-open-logout').addEventListener('click', event => {
        event.preventDefault();
        logoutModal.classList.add('active');
    });
    document.getElementById('btn-cancel-logout').addEventListener('click', () => logoutModal.classList.remove('active'));
    document.getElementById('btn-confirm-logout').addEventListener('click', event => {
        event.preventDefault();
        if (logoutForm) logoutForm.submit();
    });

    [newEventModal, viewEventModal, logoutModal].forEach(modal => {
        modal.addEventListener('click', event => {
            if (event.target === modal) modal.classList.remove('active');
        });
    });
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') [newEventModal, viewEventModal, logoutModal].forEach(modal => modal.classList.remove('active'));
    });

    document.getElementById('btn-save-new-event').addEventListener('click', async () => {
        const button = document.getElementById('btn-save-new-event');
        const title = document.getElementById('new-event-title').value.trim();
        const dateValue = document.getElementById('new-event-date').value;
        const timeValue = document.getElementById('new-event-time').value || '10:00';
        const priorityValue = document.getElementById('new-event-priority').value;
        const descriptionValue = document.getElementById('new-event-desc').value.trim();

        if (!title || !dateValue) {
            avisoCalendario('Por favor, preencha o título e a data do evento.');
            return;
        }

        const [year, month, day] = dateValue.split('-').map(Number);
        const priorityEnum = { high: 'ALTA', medium: 'MEDIA', low: 'BAIXA' }[priorityValue] || 'MEDIA';
        const payload = {
            titulo: title,
            descricao: descriptionValue || 'Sem descrição informada.',
            dataEvento: dateValue,
            horario: timeValue,
            prioridade: priorityEnum
        };

        button.disabled = true;
        try {
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
            const response = await fetch(createEventUrl, {
                method: 'POST', credentials: 'same-origin', headers, body: JSON.stringify(payload)
            });
            if (!response.ok || (response.redirected && /\/login(?:[?\/]|$)/.test(new URL(response.url).pathname))) {
                let message = 'Não foi possível salvar o evento. Verifique os dados e tente novamente.';
                try {
                    const result = await response.json();
                    if (result.erro) message = result.erro;
                } catch (_) { /* resposta pode não ser JSON */ }
                throw new Error(message);
            }
            let saved = {};
            try { saved = await response.json(); } catch (_) { /* o ID não é necessário para desenhar */ }

            const priority = normalizarPrioridade(priorityEnum);
            databaseEvents.push({
                id: saved.id || null,
                year,
                month: month - 1,
                day,
                title,
                desc: descriptionValue || 'Sem descrição informada.',
                priority,
                pillClass: { high: 'orange', medium: 'dark', low: 'blue' }[priority],
                labelPill: title,
                time: timeValue,
                isHoliday: false
            });
            databaseEvents.sort((a, b) => a.year - b.year || a.month - b.month || a.day - b.day
                || String(a.time).localeCompare(String(b.time)));

            document.getElementById('new-event-title').value = '';
            document.getElementById('new-event-date').value = '';
            document.getElementById('new-event-time').value = '10:00';
            document.getElementById('new-event-priority').value = 'medium';
            document.getElementById('new-event-desc').value = '';
            newEventModal.classList.remove('active');
            currentYear = year;
            currentMonth = month - 1;
            renderCalendar(currentYear, currentMonth);
        } catch (error) {
            console.error(error);
            avisoCalendario(error.message || 'Não foi possível salvar o evento. Verifique a conexão e tente novamente.');
        } finally {
            button.disabled = false;
        }
    });

    renderCalendar(currentYear, currentMonth);
})();
