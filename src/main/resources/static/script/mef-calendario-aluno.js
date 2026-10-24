/* Integração do calendário do aluno com os dados persistidos do Mente em Foco. */
(() => {
    'use strict';

    const $ = (selector, root = document) => root.querySelector(selector);
    const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
    const monthNames = [
        'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
        'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
    ];

    const calendarData = $('#mef-eventos');
    const calendarBody = $('#calendar-body');
    const eventList = $('#events-stack-list');
    const modalNewEvent = $('#modal-new-event');
    const modalViewEvent = $('#modal-view-event');
    const logoutModal = $('#logout-modal');

    if (!calendarData || !calendarBody || !eventList) return;

    const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[char]);

    const normalize = value => String(value ?? '')
        .normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase();

    const parseDate = value => {
        if (!value) return null;
        const parts = String(value).slice(0, 10).split('-').map(Number);
        if (parts.length !== 3 || parts.some(number => !Number.isFinite(number))) return null;
        const [year, month, day] = parts;
        if (!year || month < 1 || month > 12 || day < 1 || day > 31) return null;
        return { year, month: month - 1, day };
    };

    const priorityInfo = value => {
        const priority = normalize(value);
        if (priority === 'ALTA' || priority === 'HIGH') {
            return { value: 'ALTA', css: 'high', pill: 'orange', label: 'Alta Prioridade' };
        }
        if (priority === 'BAIXA' || priority === 'LOW') {
            return { value: 'BAIXA', css: 'low', pill: 'blue', label: 'Baixa Prioridade' };
        }
        return { value: 'MEDIA', css: 'medium', pill: 'dark', label: 'Média Prioridade' };
    };

    function normalizeSourceItem(element) {
        const date = parseDate(element.dataset.date);
        if (!date || !element.dataset.title) return null;

        const kind = element.dataset.kind || 'evento';
        const priority = priorityInfo(element.dataset.priority);
        const status = normalize(element.dataset.status);
        const time = (element.dataset.time || '').slice(0, 5);
        const completed = status.includes('CONCLUID');
        const overdue = !completed && new Date(date.year, date.month, date.day, 23, 59, 59) < new Date();

        let labelPill = element.dataset.title;
        let pillClass = priority.pill;
        if (kind === 'feriado') {
            labelPill = 'Feriado';
            pillClass = 'holiday';
        } else if (kind === 'atividade') {
            labelPill = completed ? 'Concluída' : (overdue ? 'Atrasada' : 'Atividade');
            // A atividade não tem prioridade própria no modelo; não atribuímos uma prioridade fictícia.
            pillClass = 'dark';
        }

        return {
            id: element.dataset.id || `${kind}-${date.year}-${date.month}-${date.day}-${element.dataset.title}`,
            kind,
            year: date.year,
            month: date.month,
            day: date.day,
            title: element.dataset.title,
            desc: element.dataset.description || (kind === 'feriado' ? 'Feriado nacional.' : 'Sem descrição informada.'),
            time,
            priority: priority.value,
            priorityClass: kind === 'evento' ? priority.css : 'medium',
            pillClass,
            labelPill,
            isHoliday: kind === 'feriado',
            isCompleted: completed,
            isOverdue: overdue,
            activityType: element.dataset.type || ''
        };
    }

    let databaseEvents = $$('#mef-eventos [data-kind]')
        .map(normalizeSourceItem)
        .filter(Boolean);

    const now = new Date();
    let currentYear = now.getFullYear();
    let currentMonth = now.getMonth();
    const loadedHolidayYears = new Set([currentYear]);
    const pendingHolidayYears = new Set();

    async function ensureHolidayYear(year) {
        if (loadedHolidayYears.has(year) || pendingHolidayYears.has(year)) return;
        pendingHolidayYears.add(year);
        try {
            const response = await fetch(`/aluno/api/calendario/feriados/${year}`, {
                method: 'GET',
                headers: { 'Accept': 'application/json' },
                credentials: 'same-origin'
            });
            if (!response.ok) throw new Error('Não foi possível carregar os feriados deste ano.');
            const feriados = await response.json();
            databaseEvents = databaseEvents.filter(event => !(event.kind === 'feriado' && event.year === year));
            for (const feriado of feriados) {
                const parsed = parseDate(feriado.data || feriado.dataFeriado);
                const title = feriado.titulo || feriado.nome;
                if (!parsed || !title) continue;
                databaseEvents.push({
                    id: `feriado-${parsed.year}-${parsed.month}-${parsed.day}-${title}`,
                    kind: 'feriado', year: parsed.year, month: parsed.month, day: parsed.day,
                    title, desc: 'Feriado nacional.', time: '', priority: 'BAIXA',
                    priorityClass: 'low', pillClass: 'holiday', labelPill: 'Feriado',
                    isHoliday: true, isCompleted: false, isOverdue: false
                });
            }
            loadedHolidayYears.add(year);
            if (year === currentYear) renderCalendar(currentYear, currentMonth);
        } catch (error) {
            // A falha ao consultar a API de feriados não impede a navegação no calendário.
            loadedHolidayYears.add(year);
        } finally {
            pendingHolidayYears.delete(year);
        }
    }

    function formatDate(evento) {
        const date = `${String(evento.day).padStart(2, '0')} de ${monthNames[evento.month].toLowerCase()} de ${evento.year}`;
        return evento.time ? `${date} • ${evento.time}` : date;
    }

    function eventsOnDay(year, month, day) {
        return databaseEvents.filter(event => event.year === year && event.month === month && event.day === day);
    }

    function renderCalendar(year, month) {
        ensureHolidayYear(year);
        $('#calendar-month-year').textContent = `${monthNames[month]} ${year}`;
        $('#agenda-month-label').textContent = monthNames[month];

        const firstDay = new Date(year, month, 1).getDay();
        const totalDays = new Date(year, month + 1, 0).getDate();
        const previousMonthLastDay = new Date(year, month, 0).getDate();
        const rows = Math.ceil((firstDay + totalDays) / 7);
        const totalCells = rows * 7;
        const today = new Date();
        let html = '';

        for (let cell = 0; cell < totalCells; cell += 1) {
            if (cell % 7 === 0) html += '<tr>';

            if (cell < firstDay) {
                const day = previousMonthLastDay - firstDay + cell + 1;
                html += `<td class="muted-day"><span class="num">${day}</span></td>`;
            } else if (cell >= firstDay + totalDays) {
                const day = cell - (firstDay + totalDays) + 1;
                html += `<td class="muted-day"><span class="num">${day}</span></td>`;
            } else {
                const day = cell - firstDay + 1;
                const dayEvents = eventsOnDay(year, month, day);
                const isToday = day === today.getDate() && month === today.getMonth() && year === today.getFullYear();
                const classes = [];
                if (isToday) classes.push('active-today');
                if (dayEvents.length) classes.push('clickable-day');

                const pills = dayEvents.slice(0, 2).map(event =>
                    `<div class="event-pill-item ${escapeHtml(event.pillClass)}">${escapeHtml(event.labelPill)}</div>`
                ).join('');
                const more = dayEvents.length > 2 ? `<div class="event-more-pill">+${dayEvents.length - 2}</div>` : '';
                const todayTag = isToday ? '<span class="day-tag">Hoje</span>' : '';

                html += `<td class="${classes.join(' ')}" data-year="${year}" data-month="${month}" data-day="${day}">
                    <span class="num">${day}</span>${todayTag}${pills}${more}
                </td>`;
            }

            if (cell % 7 === 6) html += '</tr>';
        }

        calendarBody.innerHTML = html;
        $$('#calendar-body .clickable-day').forEach(cell => {
            cell.addEventListener('click', () => {
                const selected = eventsOnDay(Number(cell.dataset.year), Number(cell.dataset.month), Number(cell.dataset.day));
                if (selected.length) openEventDetails(selected[0]);
            });
        });

        updateStatistics(year, month);
        renderAgendaList(year, month);
        window.lucide?.createIcons();
    }

    function updateStatistics(year, month) {
        const activities = databaseEvents.filter(event => event.kind === 'atividade' && event.year === year && event.month === month);
        const pendingActivities = activities.filter(event => !event.isCompleted);
        const highPriorityEvents = databaseEvents.filter(event =>
            event.kind === 'evento' && event.year === year && event.month === month && event.priority === 'ALTA'
        );

        $('#stat-aulas').textContent = String(activities.length);
        $('#stat-corrigir').textContent = String(pendingActivities.length);
        $('#stat-pendentes').textContent = String(highPriorityEvents.length);
    }

    function renderAgendaList(year, month) {
        const monthEvents = databaseEvents
            .filter(event => event.year === year && event.month === month)
            .sort((a, b) => a.day - b.day || a.time.localeCompare(b.time));

        if (!monthEvents.length) {
            eventList.innerHTML = '<p style="font-size:0.85rem;color:#8fa5b4;text-align:center;padding:20px;">Nenhum compromisso agendado para este mês.</p>';
            return;
        }

        eventList.innerHTML = monthEvents.map(event => {
            const dateLabel = `${event.day} ${monthNames[month].substring(0, 3).toUpperCase()} • ${event.time || (event.kind === 'atividade' ? 'Prazo' : 'Dia todo')}`;
            if (event.isHoliday) {
                return `<div class="event-item-card is-holiday-card" role="button" tabindex="0" data-id="${escapeHtml(event.id)}" data-kind="${escapeHtml(event.kind)}">
                    <div class="event-card-header"><span class="badge-holiday">Feriado Nacional</span><span class="event-date-time">${escapeHtml(dateLabel)}</span></div>
                    <h4>${escapeHtml(event.title)}</h4><p>${escapeHtml(event.desc)}</p>
                </div>`;
            }

            let badgeText = event.priorityClass === 'high' ? 'Alta Prioridade' : event.priorityClass === 'low' ? 'Baixa Prioridade' : 'Média Prioridade';
            if (event.kind === 'atividade') {
                badgeText = event.isCompleted ? 'Atividade concluída' : (event.isOverdue ? 'Atividade atrasada' : 'Atividade');
            }

            return `<div class="event-item-card priority-${escapeHtml(event.priorityClass)}" role="button" tabindex="0" data-id="${escapeHtml(event.id)}" data-kind="${escapeHtml(event.kind)}">
                <div class="event-card-header"><span class="badge-priority ${escapeHtml(event.priorityClass)}">${escapeHtml(badgeText)}</span><span class="event-date-time">${escapeHtml(dateLabel)}</span></div>
                <h4>${escapeHtml(event.title)}</h4><p>${escapeHtml(event.desc)}</p>
            </div>`;
        }).join('');

        $$('.event-item-card', eventList).forEach(card => {
            const open = () => {
                const selected = databaseEvents.find(event => String(event.id) === card.dataset.id && event.kind === card.dataset.kind);
                if (selected) openEventDetails(selected);
            };
            card.addEventListener('click', open);
            card.addEventListener('keydown', event => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    open();
                }
            });
        });
    }

    function openEventDetails(evento) {
        $('#view-title').textContent = evento.title;
        $('#view-description').textContent = evento.desc || 'Sem descrição informada.';
        $('#view-date').textContent = formatDate(evento);
        modalViewEvent?.classList.add('active');
    }

    function notify(message) {
        if (window.MEFPopups?.alert) window.MEFPopups.alert(message);
        else window.alert(message);
    }

    function closeModal(modal) {
        modal?.classList.remove('active');
    }

    $('#btn-prev-month')?.addEventListener('click', () => {
        currentMonth -= 1;
        if (currentMonth < 0) { currentMonth = 11; currentYear -= 1; }
        renderCalendar(currentYear, currentMonth);
    });

    $('#btn-next-month')?.addEventListener('click', () => {
        currentMonth += 1;
        if (currentMonth > 11) { currentMonth = 0; currentYear += 1; }
        renderCalendar(currentYear, currentMonth);
    });

    $('#btn-open-new-event')?.addEventListener('click', () => modalNewEvent?.classList.add('active'));
    $('#btn-cancel-new-event')?.addEventListener('click', () => closeModal(modalNewEvent));
    $('#btn-close-view-event')?.addEventListener('click', () => closeModal(modalViewEvent));

    [modalNewEvent, modalViewEvent, logoutModal].filter(Boolean).forEach(modal => {
        modal.addEventListener('click', event => {
            if (event.target === modal) closeModal(modal);
        });
    });

    $('#btn-open-logout')?.addEventListener('click', event => {
        event.preventDefault();
        logoutModal?.classList.add('active');
    });
    $('#btn-cancel-logout')?.addEventListener('click', () => closeModal(logoutModal));
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') [modalNewEvent, modalViewEvent, logoutModal].forEach(closeModal);
    });
    $('#toggle-btn')?.addEventListener('click', () => $('#sidebar')?.classList.toggle('expanded'));

    $('#btn-save-new-event')?.addEventListener('click', async () => {
        const titleInput = $('#new-event-title');
        const dateInput = $('#new-event-date');
        const timeInput = $('#new-event-time');
        const priorityInput = $('#new-event-priority');
        const descInput = $('#new-event-desc');
        const title = titleInput.value.trim();
        const date = dateInput.value;
        const time = timeInput.value || '14:00';
        const priority = priorityInput.value;
        const description = descInput.value.trim();

        if (!title || !date) {
            notify('Por favor, preencha o título e a data do evento.');
            return;
        }

        const csrfToken = $('meta[name="_csrf"]')?.content;
        const csrfHeader = $('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
        const saveButton = $('#btn-save-new-event');
        saveButton.disabled = true;

        try {
            const headers = { 'Content-Type': 'application/json', 'Accept': 'application/json' };
            if (csrfToken) headers[csrfHeader] = csrfToken;
            const response = await fetch('/aluno/api/calendario/eventos', {
                method: 'POST',
                headers,
                credentials: 'same-origin',
                body: JSON.stringify({ titulo: title, descricao: description, data: date, horario: time, prioridade: priority })
            });

            let payload = {};
            const responseText = await response.text();
            if (responseText) {
                try { payload = JSON.parse(responseText); }
                catch { payload = { message: responseText }; }
            }
            if (!response.ok) {
                throw new Error(payload.message || 'Não foi possível salvar o evento. Tente novamente.');
            }

            const parsed = parseDate(payload.dataInicio || date);
            if (!parsed) throw new Error('O servidor retornou uma data inválida para o evento.');
            const savedPriority = priorityInfo(payload.prioridade || priority);
            databaseEvents.push({
                id: payload.id,
                kind: 'evento',
                year: parsed.year,
                month: parsed.month,
                day: parsed.day,
                title: payload.nome || title,
                desc: payload.descricao || description || 'Sem descrição informada.',
                time: String(payload.horario || time).slice(0, 5),
                priority: savedPriority.value,
                priorityClass: savedPriority.css,
                pillClass: savedPriority.pill,
                labelPill: payload.nome || title,
                isHoliday: false,
                isCompleted: false,
                isOverdue: false
            });

            titleInput.value = '';
            dateInput.value = '';
            descInput.value = '';
            timeInput.value = '14:00';
            priorityInput.value = 'medium';
            closeModal(modalNewEvent);
            currentYear = parsed.year;
            currentMonth = parsed.month;
            renderCalendar(currentYear, currentMonth);
            notify('Evento salvo com sucesso.');
        } catch (error) {
            notify(error.message || 'Ocorreu um erro ao salvar o evento.');
        } finally {
            saveButton.disabled = false;
        }
    });

    // O calendário inicia no mês atual, não em uma data fixa de demonstração.
    renderCalendar(currentYear, currentMonth);
    window.lucide?.createIcons();
})();
