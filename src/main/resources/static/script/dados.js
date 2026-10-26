/* Persistência do protótipo. Substituir este adaptador pela API Spring Boot. */
(() => {
  'use strict';
  const KEY = 'mef_front_v2';
  const id = () => crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(36).slice(2)}`;
  const norm = value => String(value || '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toUpperCase().trim();
  const parse = (key, fallback) => {
    const raw = localStorage.getItem(key);
    if (!raw) return fallback;
    try { return JSON.parse(raw); } catch { throw new Error(`Os dados locais de ${key} estão inválidos. Exporte uma cópia antes de limpar o navegador.`); }
  };
  function load() {
    const existing = parse(KEY, null);
    if (existing) return existing;
    const students = [];
    for (let i = 0; i < localStorage.length; i++) {
      const key = localStorage.key(i);
      if (!key.startsWith('alunos_turma_')) continue;
      const turma = key.slice(13).replace(/_/g, ' ');
      for (const s of parse(key, [])) students.push({ ...s, id: s.id || id(), turma, serie: s.serie || s.sub || '', diagnostico: s.diagnostico || '', contato: s.contato || '', medicamento: s.medicamento || '', acompanhamento: s.acompanhamento || '', legacyKey: key });
    }
    if (!students.length) ['Cauê Campos', 'Lucas Bonetti', 'Levi Viana', 'Heitor Oliveira'].forEach(nome => students.push({ id: id(), nome, turma: 'Turma 2 - Ensino Médio', serie: 'Ensino Médio', diagnostico: '', hiperfoco: '', foco: '', obs: '', contato: '', medicamento: '', acompanhamento: '' }));
    const tasks = parse('mente_em_foco_tarefas', []).map(t => ({ ...t, id: String(t.id || id()), tipo: norm(t.tipo).includes('QUIZ') ? 'quiz' : t.tipo, alunos: (t.alunos || []).flatMap(a => a === 'Geral' ? students.map(s => s.id) : students.filter(s => s.nome === a || s.id === a).map(s => s.id)), perguntas: t.perguntas || [], status: t.status === 'Rascunho' ? 'Rascunho' : 'Ativa', legacyStatus: t.status }));
    // Exemplos identificados: dados demonstrativos, sem diagnóstico fictício.
    if (!tasks.length) {
      const prazo = new Date(Date.now() + 7 * 86400000).toISOString().slice(0, 10);
      const base = { prazo, status: 'Ativa', alunos: students.map(s => s.id), demonstracao: true, instrucoes: 'Leia as instruções e responda no seu ritmo.' };
      tasks.push(
        { ...base, id: id(), titulo: 'Quiz: classes de palavras', tipo: 'quiz', perguntas: [{ pergunta: 'Qual palavra é um substantivo?', opcoes: ['Casa', 'Correr', 'Azul'], correta: 0 }, { pergunta: 'Qual palavra indica uma ação?', opcoes: ['Feliz', 'Ler', 'Livro'], correta: 1 }] },
        { ...base, id: id(), titulo: 'Memória: palavras e significados', tipo: 'memoria', pares: [{ termo: 'Substantivo', significado: 'Nomeia seres, objetos ou ideias' }, { termo: 'Verbo', significado: 'Expressa ação, estado ou fenômeno' }, { termo: 'Adjetivo', significado: 'Caracteriza um substantivo' }] },
        { ...base, id: id(), titulo: 'Palavras cruzadas: vocabulário', tipo: 'cruzadas', palavras: [{ palavra: 'CASA', dica: 'Lugar onde podemos morar' }, { palavra: 'SAPO', dica: 'Anfíbio que costuma saltar' }, { palavra: 'AMOR', dica: 'Sentimento de afeto' }] },
        { ...base, id: id(), titulo: 'Leitura guiada: a biblioteca', tipo: 'leitura', trechos: ['Ana visitou a biblioteca da escola. Ela procurava um livro de aventuras.', 'A bibliotecária indicou uma história sobre uma viagem. Ana levou o livro para casa e começou a ler.'], perguntas: [{ pergunta: 'Onde Ana procurou um livro?', opcoes: ['Na biblioteca', 'No mercado', 'No parque'], correta: 0 }, { pergunta: 'Que tipo de livro Ana procurava?', opcoes: ['Receitas', 'Aventuras', 'Dicionário'], correta: 1 }] }
      );
    }
    const db = { version: 2, students, tasks, reports: [], notifications: [], preferences: {} };
    localStorage.setItem(KEY, JSON.stringify(db));
    return db;
  }
  function transaction(change) {
    const db = load();
    const result = change(db);
    // Uma única escrita evita concluir a atividade sem salvar seu relatório.
    localStorage.setItem(KEY, JSON.stringify(db));
    return result;
  }
  const role = () => location.pathname.includes('/psicopedagogo/') ? 'psicopedagogo' : location.pathname.includes('/professor/') ? 'professor' : 'aluno';
  const studentId = () => {
    const students = load().students;
    const saved = localStorage.getItem('mef_aluno_atual');
    return students.some(s => s.id === saved) ? saved : students[0]?.id;
  };
  const audience = (r = role()) => r === 'aluno' ? `aluno:${studentId()}` : r;
  const notify = (db, who, title, message, link) => {
    const unique = [...new Set(who)];
    unique.forEach(target => {
      if (db.preferences[target] === false) return;
      db.notifications.unshift({ id: id(), target, title, message, link, read: false, date: new Date().toISOString() });
    });
  };
  const assigned = (task, sid = studentId()) => task.status === 'Ativa' && task.alunos.includes(sid);
  const attempts = (taskId, sid = studentId()) => load().reports.filter(r => r.kind === 'automatico' && r.taskId === taskId && r.studentId === sid);
  window.MEF = { KEY, id, norm, load, transaction, role, studentId, audience, notify, assigned, attempts };
})();
