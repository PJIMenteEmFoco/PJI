/* Telas e fluxos do front-end do Mente em Foco. */
(() => {
  'use strict';
  const $ = (s, root = document) => root.querySelector(s);
  const $$ = (s, root = document) => [...root.querySelectorAll(s)];
  const esc = v => String(v ?? '').replace(/[&<>"']/g, c => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' }[c]));
  const query = new URLSearchParams(location.search);
  const role = MEF.role();
  const suffix = { professor:'prof', psicopedagogo:'psico', aluno:'aluno' }[role];
  const names = { quiz:'Quiz', memoria:'Jogo da memória', cruzadas:'Palavras cruzadas', leitura:'Leitura guiada' };
  const db = () => MEF.load();
  const original = document.body.dataset.original === 'true';
  const root = $('#mef-root') || (original ? $('main') : null);
  const date = v => v ? new Date(v.length === 10 ? `${v}T12:00:00` : v).toLocaleString('pt-BR', v.length === 10 ? { dateStyle:'short' } : { dateStyle:'short', timeStyle:'short' }) : 'Não informado';
  const duration = n => `${Math.floor(n / 60)} min ${n % 60} s`;
  const student = sid => db().students.find(s => s.id === sid);
  const option = (value, label, selected = false) => `<option value="${esc(value)}" ${selected ? 'selected' : ''}>${esc(label)}</option>`;
  const link = (href, text, cls = '') => `<a class="mef-btn ${cls}" href="${esc(href)}">${esc(text)}</a>`;
  const button = (id, text, cls = '', type = 'button') => `<button type="${type}" class="mef-btn ${cls}" id="${id}">${esc(text)}</button>`;
  const field = (id, label, value = '', type = 'text', required = false) => `<label class="mef-field" for="${id}">${esc(label)}<input id="${id}" name="${id}" type="${type}" value="${esc(value)}" ${required ? 'required' : ''} maxlength="${type === 'text' ? 300 : 100}" ></label>`;
  const textarea = (id, label, value = '', required = false) => `<label class="mef-field" for="${id}">${esc(label)}<textarea id="${id}" name="${id}" ${required ? 'required' : ''} maxlength="20000">${esc(value)}</textarea></label>`;
  const empty = text => `<p class="mef-empty">${esc(text)}</p>`;
  const header = (title, description = '', actions = '') => `<header class="mef-header"><div><h1>${esc(title)}</h1><p class="mef-sub">${esc(description)}</p></div><div class="mef-actions">${actions}</div></header>`;
  const taskHref = () => `tarefas_${suffix}.html`;
  const reportsHref = sid => `relatorios.html${sid ? '?aluno=' + encodeURIComponent(sid) : ''}`;
  const message = (error, form = root) => {
    if(original && $('#warning-modal') && form === root) { $('#warning-modal-text').textContent = error instanceof Error ? error.message : error; $('#warning-modal').classList.add('active'); return; }
    let box = $('.mef-error', form);
    if (!box) { box = document.createElement('p'); box.className = 'mef-error'; box.setAttribute('role','alert'); form.prepend(box); }
    box.textContent = error instanceof Error ? error.message : error;
    if (box.textContent) box.scrollIntoView({ block:'nearest' });
  };
  function toast(text) {
    $('.mef-toast')?.remove();
    const el = document.createElement('div'); el.className = 'mef-toast'; el.setAttribute('role','status'); el.textContent = text; document.body.append(el);
    setTimeout(() => el.remove(), 5000);
  }
  function dialog(title, content, onReady) {
    const el = document.createElement('dialog'); el.className = 'mef-dialog';
    el.innerHTML = `<h2>${esc(title)}</h2>${content}`;
    document.body.append(el); window.MEFPopups?.prepare(el); el.showModal();
    el.addEventListener('close', () => el.remove());
    el.addEventListener('click', e => { if (e.target === el) { const r = el.getBoundingClientRect(); if (e.clientX < r.left || e.clientX > r.right || e.clientY < r.top || e.clientY > r.bottom) el.close(); } });
    $('[data-close]',el)?.addEventListener('click', () => el.close());
    onReady?.(el); return el;
  }
  function confirmAction(title, text, action) {
    dialog(title, `<p>${esc(text)}</p><div class="mef-actions">${button('mef-cancel','Cancelar')}${button('mef-confirm','Confirmar','primary')}</div>`, el => {
      $('#mef-cancel',el).onclick = () => el.close();
      $('#mef-confirm',el).onclick = () => { try { action(); el.close(); } catch(e) { message(e,el); } };
    });
  }
  function setupCommon() {
    if (localStorage.getItem('theme') === 'escuro') document.body.classList.add('dark-mode');
    if (root) {
      $('#toggle-btn')?.addEventListener('click', () => $('#sidebar').classList.toggle('expanded'));
      $('#btn-open-logout')?.addEventListener('click', () => confirmAction('Sair do protótipo?', 'Você será direcionado para a tela de login.', () => location.href = '../login/login.html'));
    }
    window.lucide?.createIcons();
    // Persistência da opção geral de notificações nas telas de ajustes existentes.
    const toggles = $$('.btn-toggle-status');
    if (toggles[0]) {
      const first = toggles[0], target = MEF.audience();
      const apply = () => { const enabled = db().preferences[target] !== false; first.textContent = enabled ? 'Ativado' : 'Desativado'; first.classList.toggle('disabled', !enabled); first.setAttribute('aria-pressed',String(enabled)); };
      first.removeAttribute('onclick'); apply();
      first.addEventListener('click', () => { try { MEF.transaction(d => d.preferences[target] = d.preferences[target] === false); apply(); } catch(e) { toast(e.message); } });
    }
  }

  function studentChooser() {
    return `<label class="mef-field" for="current-student">Aluno no protótipo<select id="current-student">${db().students.map(s => option(s.id,s.nome,s.id===MEF.studentId())).join('')}</select></label>`;
  }
  function chooseStudent() {
    $('#current-student')?.addEventListener('change', e => { localStorage.setItem('mef_aluno_atual', e.target.value); location.reload(); });
  }
  function studentsPage() {
    const classOnly = document.body.dataset.page === 'turma';
    const className = classOnly ? localStorage.getItem('turmaSelecionadaNome') || 'Turma 2 - Ensino Médio' : '';
    root.innerHTML = header(classOnly ? className : 'Alunos', 'Ficha individual e acompanhamento escolar.', button('add-student','Adicionar aluno','primary')) +
      `<div class="mef-filters">${field('search','Buscar por nome ou turma')}<label class="mef-field" for="class-filter">Turma<select id="class-filter">${option('','Todas')}${[...new Set([...db().students.map(s=>s.turma), ...(className ? [className] : [])])].map(t=>option(t,t,t===className)).join('')}</select></label></div><div id="student-list" class="mef-grid"></div>`;
    if (classOnly) { $('#class-filter').disabled = true; root.prepend(Object.assign(document.createElement('div'), {innerHTML: link(role === 'professor' ? 'turmas-prof.html' : 'turmas_psico.html','Voltar às turmas')})); }
    function render() {
      const q = MEF.norm($('#search').value), turma = $('#class-filter').value;
      const list = db().students.filter(s => (!turma || s.turma === turma) && MEF.norm(s.nome+' '+s.turma).includes(q));
      $('#student-list').innerHTML = list.map(s => `<article class="mef-card"><span class="mef-tag">${esc(s.turma)}</span><h2>${esc(s.nome)}</h2><p class="mef-muted">${esc(s.serie || 'Série não informada')}</p><div class="mef-actions">${link('ficha-aluno.html?id='+encodeURIComponent(s.id),'Ver ficha','primary')}${link(reportsHref(s.id),'Relatórios')}</div></article>`).join('') || empty('Nenhum aluno encontrado nesta turma.');
    }
    $('#search').oninput = render; $('#class-filter').onchange = render;
    $('#add-student').onclick = () => editStudent(null, className || $('#class-filter').value, studentsPage);
    render();
  }
  function editStudent(s, turma = '', after = studentPage) {
    dialog(s ? 'Editar dados do aluno' : 'Adicionar aluno', `<form id="student-form"><div class="mef-error" role="alert"></div><div class="mef-fields">${field('nome','Nome completo',s?.nome,'text',true)}${field('serie','Série',s?.serie,'text',true)}${field('turma','Turma',s?.turma || turma,'text',true)}${field('contato','Contato do responsável',s?.contato)}${field('hiperfoco','Interesses do aluno',s?.hiperfoco)}${field('foco','Preferências de tempo e pausas',s?.foco)}</div>${textarea('diagnostico','Diagnóstico informado (opcional)',s?.diagnostico)}${textarea('medicamento','Medicamento informado (opcional)',s?.medicamento)}${textarea('acompanhamento','Acompanhamento informado (opcional)',s?.acompanhamento)}${textarea('obs','Observações pedagógicas',s?.obs)}<div class="mef-actions">${button('cancel-student','Cancelar')}${button('save-student','Salvar dados','primary','submit')}</div></form>`, el => {
      $('#cancel-student',el).onclick=()=>el.close();
      $('#student-form',el).onsubmit = e => {
        e.preventDefault();
        try {
          const values = Object.fromEntries(new FormData(e.target).entries());
          Object.keys(values).forEach(k=>values[k]=values[k].trim());
          if (!values.nome || !values.serie || !values.turma) throw new Error('Preencha nome, série e turma.');
          MEF.transaction(d => { const existing=d.students.find(v=>v.id===s?.id); if (existing) Object.assign(existing,values,{sub:values.serie+(values.diagnostico?' • '+values.diagnostico:'')}); else d.students.push({...values,id:MEF.id()}); });
          el.close(); after(); toast('Dados do aluno salvos.');
        } catch(error) { message(error,el); }
      };
    });
  }
  function studentPage() {
    const s=student(query.get('id'));
    if(!s) { root.innerHTML=header('Ficha não encontrada')+link('alunos.html','Voltar aos alunos');return; }
    const reports=db().reports.filter(r=>r.studentId===s.id);
    root.innerHTML=header(s.nome,s.turma,button('edit-student','Editar dados','primary')+link('escrever-relatorio.html?aluno='+encodeURIComponent(s.id),'Escrever relatório'))+
      `<article class="mef-card"><h2>Ficha do aluno</h2><dl class="mef-detail">${[['Série',s.serie],['Contato do responsável',s.contato],['Interesses',s.hiperfoco],['Tempo e pausas',s.foco],['Diagnóstico informado',s.diagnostico],['Medicamento informado',s.medicamento],['Acompanhamento',s.acompanhamento],['Observações pedagógicas',s.obs]].map(([label,v])=>`<div><dt>${esc(label)}</dt><dd>${esc(v||'Não informado')}</dd></div>`).join('')}</dl></article><div class="mef-stats"><div class="mef-stat">Relatórios<strong>${reports.length}</strong></div><div class="mef-stat">Atividades atribuídas<strong>${db().tasks.filter(t=>t.alunos.includes(s.id)&&t.status==='Ativa').length}</strong></div></div><div class="mef-actions">${link(reportsHref(s.id),'Visualizar relatórios')}${link('alunos.html','Voltar aos alunos')}</div>`;
    $('#edit-student').onclick=()=>editStudent(s);
  }
  function taskList() {
    root.innerHTML=header(role==='aluno' ? 'Minhas atividades' : 'Atividades', 'Quatro formatos de atividades de Língua Portuguesa.',role==='aluno'?'':link('criar-nova-tarefa.html','Criar atividade','primary'))+
      (role==='aluno'?studentChooser():`<div class="mef-actions">${Object.entries(names).map(([key,title])=>link('criar-nova-tarefa.html?tipo='+key,'+ '+title)).join('')}</div>`)+
      `<div class="mef-filters">${field('search','Buscar título')}<label class="mef-field" for="type-filter">Formato<select id="type-filter">${option('','Todos')}${Object.entries(names).map(([k,v])=>option(k,v)).join('')}</select></label><label class="mef-field" for="status-filter">Situação<select id="status-filter">${option('','Todas')}${option('pending','Pendentes')}${option('done','Concluídas')}${role!=='aluno'?option('draft','Rascunhos'):''}</select></label></div><div id="task-list" class="mef-grid"></div>`;
    const render=()=>{
      const q=MEF.norm($('#search').value), type=$('#type-filter').value, status=$('#status-filter').value;
      const list=db().tasks.filter(t=>role!=='aluno'||MEF.assigned(t)).filter(t=>MEF.norm(t.titulo).includes(q)&&(!type||t.tipo===type));
      const cards=list.map(t=>{
        const completedIds=new Set(db().reports.filter(r=>r.kind==='automatico'&&r.taskId===t.id).map(r=>r.studentId));
        const done=role==='aluno'?completedIds.has(MEF.studentId()):t.alunos.length>0&&t.alunos.every(sid=>completedIds.has(sid));
        if(status==='draft'&&t.status!=='Rascunho'||status==='done'&&!done||status==='pending'&&(done||t.status==='Rascunho'))return '';
        const overdue=t.prazo&&t.prazo<new Date().toLocaleDateString('en-CA');
        const label=t.status==='Rascunho'?'Rascunho':done?'Concluída':overdue?'Prazo encerrado':'Pendente';
        const report=MEF.attempts(t.id).at(-1);
        return `<article class="mef-card"><span class="mef-tag">${esc(names[t.tipo]||t.tipo)}</span><span class="mef-tag orange">${label}</span>${t.demonstracao?'<span class="mef-tag">Exemplo</span>':''}<h2>${esc(t.titulo)}</h2><p class="mef-muted">Prazo: ${date(t.prazo)}</p><p class="mef-muted">${role==='aluno'?esc(t.instrucoes):`${t.alunos.length} aluno(s) • ${completedIds.size} concluíram`}</p><div class="mef-actions">${role==='aluno'?(done?link('relatorio_desempenho.html?id='+encodeURIComponent(report.id),'Ver resultado','primary'):link('executar_atividade.html?id='+encodeURIComponent(t.id),'Iniciar atividade','primary')):link('editar-atividade.html?id='+encodeURIComponent(t.id),'Editar','primary')+`<button class="mef-btn danger" data-delete="${esc(t.id)}">Excluir</button>`}</div></article>`;
      }).join('');
      $('#task-list').innerHTML=cards||empty('Nenhuma atividade encontrada.');
      $$('[data-delete]').forEach(b=>b.onclick=()=>confirmAction('Excluir atividade?', 'Os relatórios já gerados serão preservados.',()=>{
        MEF.transaction(d=>{const task=d.tasks.find(t=>t.id===b.dataset.delete); d.tasks=d.tasks.filter(t=>t.id!==task.id);MEF.notify(d,task.alunos.map(sid=>'aluno:'+sid),'Atividade excluída',task.titulo,'tarefas_aluno.html');});render();toast('Atividade excluída.');
      }));
    };
    $('#search').oninput=render;$('#type-filter').onchange=render;$('#status-filter').onchange=render;chooseStudent();render();
  }
  const defaultQuestion=()=>({pergunta:'',opcoes:['',''],correta:0});
  function editor() {
    const existing=query.get('id')?db().tasks.find(t=>t.id===query.get('id')):null;
    if(query.get('id')&&!existing){root.innerHTML=header('Atividade não encontrada')+link(taskHref(),'Voltar às atividades');return;}
    let model=existing?structuredClone(existing):{id:MEF.id(),titulo:'',instrucoes:'',tipo:names[query.get('tipo')]?query.get('tipo'):'quiz',prazo:'',alunos:[],perguntas:[defaultQuestion()],pares:[{termo:'',significado:''},{termo:'',significado:''}],palavras:[{palavra:'',dica:''},{palavra:'',dica:''}],trechos:['']};
    model.perguntas ||= [defaultQuestion()];model.pares ||= [{termo:'',significado:''},{termo:'',significado:''}];model.palavras ||= [{palavra:'',dica:''},{palavra:'',dica:''}];model.trechos ||= [''];
    root.innerHTML=header(existing?'Editar atividade':'Criar atividade','Defina conteúdo, destinatários e prazo.',link(taskHref(),'Voltar'))+
      `<form id="task-form" novalidate><div class="mef-error" role="alert"></div><section class="mef-card"><h2>1. Informações</h2><div class="mef-fields">${field('task-title','Título',model.titulo,'text',true)}<label class="mef-field" for="task-type">Formato<select id="task-type">${Object.entries(names).map(([k,v])=>option(k,v,k===model.tipo)).join('')}</select></label></div>${textarea('task-instructions','Instruções ao aluno',model.instrucoes)}</section><section class="mef-card"><div class="mef-header"><h2>2. Conteúdo da atividade</h2>${button('use-example','Preencher com exemplo')}</div><div id="content-editor"></div></section><section class="mef-card"><h2>3. Atribuição e prazo</h2>${field('task-deadline','Prazo',model.prazo,'date')}<p class="mef-muted">Selecione os alunos que receberão a atividade.</p><div class="mef-checklist">${db().students.map(s=>`<label class="mef-check"><input class="student-check" type="checkbox" value="${esc(s.id)}" ${model.alunos.includes(s.id)?'checked':''}>${esc(s.nome)} (${esc(s.turma)})</label>`).join('')}</div></section><div class="mef-actions">${button('preview-task','Pré-visualizar')}${button('save-draft','Salvar rascunho')}${button('publish-task',existing?'Salvar alterações':'Publicar atividade','primary','submit')}</div></form>`;
    const content=$('#content-editor');
    function questionsHTML() {
      return `<h3>Perguntas de múltipla escolha</h3>${model.perguntas.map((q,i)=>`<fieldset class="mef-question" data-question="${i}"><legend>Pergunta ${i+1}</legend>${field('question-'+i,'Enunciado',q.pergunta)}${q.opcoes.map((o,j)=>`<div class="mef-option"><label><input type="radio" name="correct-${i}" value="${j}" ${q.correta===j?'checked':''} aria-label="Alternativa ${j+1} correta da pergunta ${i+1}"></label><label class="mef-field" for="option-${i}-${j}">Alternativa ${j+1}<input id="option-${i}-${j}" class="option-input" value="${esc(o)}" maxlength="1000"></label><button type="button" class="mef-btn" data-remove-option="${i}:${j}" aria-label="Remover alternativa ${j+1}">×</button></div>`).join('')}<div class="mef-actions">${button('add-option-'+i,'Adicionar alternativa')}<button class="mef-btn danger" type="button" data-remove-question="${i}">Remover pergunta</button></div></fieldset>`).join('')}${button('add-question','Adicionar pergunta')}`;
    }
    function capture() {
      model.titulo=$('#task-title').value.trim();model.instrucoes=$('#task-instructions').value.trim();model.prazo=$('#task-deadline').value;model.alunos=$$('.student-check:checked').map(c=>c.value);
      if(model.tipo==='quiz'||model.tipo==='leitura') model.perguntas=$$('[data-question]',content).map((box,i)=>({pergunta:$('#question-'+i,box).value.trim(),opcoes:$$('.option-input',box).map(v=>v.value.trim()),correta:Number($('input[type=radio]:checked',box)?.value??-1)}));
      if(model.tipo==='leitura')model.trechos=$('#reading-text').value.split(/\n\s*\n/).map(v=>v.trim()).filter(Boolean);
      if(model.tipo==='memoria') model.pares=$$('[data-pair]').map((box,i)=>({termo:$('#term-'+i,box).value.trim(),significado:$('#meaning-'+i,box).value.trim()}));
      if(model.tipo==='cruzadas') model.palavras=$$('[data-word]').map((box,i)=>({palavra:MEF.norm($('#word-'+i,box).value),dica:$('#clue-'+i,box).value.trim()}));
    }
    function renderContent() {
      if(model.tipo==='quiz')content.innerHTML=questionsHTML();
      if(model.tipo==='leitura')content.innerHTML=textarea('reading-text','Texto (separe os trechos com uma linha em branco)',model.trechos.join('\n\n'))+'<p class="mef-muted">O aluno verá um trecho por vez, seguido das perguntas de compreensão.</p>'+questionsHTML();
      if(model.tipo==='memoria')content.innerHTML='<p class="mef-muted">Crie de 2 a 8 pares de palavras e significados.</p>'+model.pares.map((p,i)=>`<div class="mef-question" data-pair="${i}"><div class="mef-fields">${field('term-'+i,'Palavra / conceito',p.termo)}${field('meaning-'+i,'Significado correspondente',p.significado)}</div><button type="button" class="mef-btn danger" data-remove-pair="${i}">Remover par</button></div>`).join('')+button('add-pair','Adicionar par');
      if(model.tipo==='cruzadas')content.innerHTML='<p class="mef-muted">Crie de 2 a 10 palavras, com 2 a 16 letras cada. As palavras devem permitir cruzamentos. Acentos são desconsiderados na grade.</p>'+model.palavras.map((p,i)=>`<div class="mef-question" data-word="${i}"><div class="mef-fields">${field('word-'+i,'Palavra',p.palavra)}${field('clue-'+i,'Dica',p.dica)}</div><button type="button" class="mef-btn danger" data-remove-word="${i}">Remover palavra</button></div>`).join('')+button('add-word','Adicionar palavra');
      $('#add-question')?.addEventListener('click',()=>{capture();model.perguntas.push(defaultQuestion());renderContent();});
      $$('[data-remove-question]').forEach(b=>b.onclick=()=>{capture();if(model.perguntas.length<=1){toast('Mantenha pelo menos uma pergunta.');return;}model.perguntas.splice(Number(b.dataset.removeQuestion),1);renderContent();});
      model.perguntas.forEach((q,i)=>$('#add-option-'+i)?.addEventListener('click',()=>{capture();model.perguntas[i].opcoes.push('');renderContent();}));
      $$('[data-remove-option]').forEach(b=>b.onclick=()=>{capture();const[i,j]=b.dataset.removeOption.split(':').map(Number),q=model.perguntas[i];if(q.opcoes.length<=2){toast('Mantenha pelo menos duas alternativas.');return;}q.opcoes.splice(j,1);q.correta=q.correta===j?-1:q.correta>j?q.correta-1:q.correta;renderContent();});
      for(const [kind,key,blank]of[['pair','pares',{termo:'',significado:''}],['word','palavras',{palavra:'',dica:''}]]){
        $('#add-'+kind)?.addEventListener('click',()=>{capture();model[key].push({...blank});renderContent();});
        $$('[data-remove-'+kind+']').forEach(b=>b.onclick=()=>{capture();if(model[key].length<=2){toast('Mantenha pelo menos dois itens.');return;}model[key].splice(Number(b.dataset[kind==='pair'?'removePair':'removeWord']),1);renderContent();});
      }
    }
    $('#task-type').onchange=e=>{capture();model.tipo=e.target.value;renderContent();};
    $('#use-example').onclick=()=>{const sample=db().tasks.find(t=>t.demonstracao&&t.tipo===model.tipo);if(!sample){toast('Nenhum exemplo disponível para este formato.');return;}capture();for(const key of ['perguntas','pares','palavras','trechos'])if(sample[key])model[key]=structuredClone(sample[key]);renderContent();};
    $('#preview-task').onclick=()=>{try{capture();validateContent(model);dialog('Prévia do conteúdo',previewContent(model)+'<div class="mef-actions"><button type="button" class="mef-btn" data-close>Fechar prévia</button></div>');message('');}catch(e){message(e);}};
    function save(status){
      try{
        capture();if(!model.titulo)throw new Error('Preencha o título da atividade.');
        if(status==='Ativa'){if(!model.prazo)throw new Error('Informe um prazo.');if(!model.alunos.length)throw new Error('Selecione pelo menos um aluno.');validateContent(model);}
        MEF.transaction(d=>{
          const idx=d.tasks.findIndex(t=>t.id===model.id),previous=idx>=0?d.tasks[idx]:null;
          const task={...model,status,demonstracao:false,updatedAt:new Date().toISOString(),createdBy:role};
          if(idx<0)d.tasks.push(task);else d.tasks[idx]=task;
          if(status==='Ativa')MEF.notify(d,task.alunos.map(sid=>'aluno:'+sid),previous?'Atividade atualizada':'Nova atividade',task.titulo,'tarefas_aluno.html');
        });
        location.href=taskHref();
      }catch(e){message(e);}
    }
    $('#save-draft').onclick=()=>save('Rascunho');$('#task-form').onsubmit=e=>{e.preventDefault();save('Ativa');};renderContent();
  }
  function validateContent(t) {
    if(!names[t.tipo])throw new Error('Formato de atividade não suportado. Edite o conteúdo.');
    if(t.tipo==='quiz'||t.tipo==='leitura'){
      if(!t.perguntas?.length)throw new Error('Adicione pelo menos uma pergunta.');
      t.perguntas.forEach((q,i)=>{if(!q.pergunta?.trim()||q.opcoes.length<2||q.opcoes.some(o=>!o.trim())||!Number.isInteger(q.correta)||q.correta<0||q.correta>=q.opcoes.length)throw new Error(`Complete a pergunta ${i+1}, as alternativas e marque uma resposta correta.`);});
    }
    if(t.tipo==='leitura'&&!MEFDocuments.valid(t.pdf)&&(!t.trechos?.length||t.trechos.some(v=>!v.trim())))throw new Error('Informe o texto da leitura guiada.');
    if(t.tipo==='memoria'){
      if(!t.pares||t.pares.length<2||t.pares.length>8||t.pares.some(p=>!p.termo.trim()||!p.significado.trim()))throw new Error('Complete de 2 a 8 pares de palavras e significados.');
      if(new Set(t.pares.map(p=>MEF.norm(p.termo))).size!==t.pares.length||new Set(t.pares.map(p=>MEF.norm(p.significado))).size!==t.pares.length)throw new Error('Use palavras e significados distintos para evitar pares ambíguos.');
    }
    if(t.tipo==='cruzadas'){
      if(!t.palavras||t.palavras.length<2||t.palavras.length>10||t.palavras.some(p=>!p.dica.trim()||! /^[A-Z]{2,16}$/.test(MEF.norm(p.palavra))))throw new Error('Informe de 2 a 10 palavras de 2 a 16 letras, cada uma com sua dica.');
      if(new Set(t.palavras.map(p=>MEF.norm(p.palavra))).size!==t.palavras.length)throw new Error('As palavras cruzadas não podem repetir palavras.');
      MEFCrossword.build(t.palavras);
    }
  }
  function previewContent(t) {
    let html=`<h3>${esc(t.titulo||names[t.tipo])}</h3><p>${esc(t.instrucoes)}</p>`;
    if(t.tipo==='leitura'&&MEFDocuments.valid(t.pdf))html+=MEFDocuments.view(t.pdf);
    if(t.tipo==='leitura')html+=(t.trechos||[]).map((v,i)=>`<h3>Trecho ${i+1}</h3><p class="mef-prose">${esc(v)}</p>`).join('');
    if(t.tipo==='quiz'||t.tipo==='leitura')html+=t.perguntas.map(q=>`<div class="mef-question"><h3>${esc(q.pergunta)}</h3>${q.opcoes.map((o,j)=>`<p>${esc(o)}${q.correta===j?' ✓ (correta)':''}</p>`).join('')}</div>`).join('');
    if(t.tipo==='memoria')html+=t.pares.map(p=>`<p><strong>${esc(p.termo)}</strong> — ${esc(p.significado)}</p>`).join('');
    if(t.tipo==='cruzadas')html+=crosswordHTML(MEFCrossword.build(t.palavras),true);
    return html;
  }
  function crosswordHTML(layout, preview = false) {
    const map=new Map(layout.cells.map(c=>[`${c.row},${c.col}`,c]));
    let cells='';
    for(let r=0;r<layout.rows;r++)for(let c=0;c<layout.cols;c++){
      const cell=map.get(`${r},${c}`),starts=layout.words.filter(w=>w.row===r&&w.col===c),num=starts[0]?.number;
      cells+=`<div class="mef-cell">${cell?`${num?'<span>'+num+'</span>':''}<input data-cell="${r},${c}" aria-label="Linha ${r+1}, coluna ${c+1}${num?', início da palavra '+num:''}" maxlength="1" autocomplete="off" spellcheck="false" ${preview?'value="'+cell.letter+'" readonly':''}>`:''}</div>`;
    }
    return `<div class="mef-cross-wrap"><div class="mef-cross" style="grid-template-columns:repeat(${layout.cols},38px)">${cells}</div></div>${['H','V'].map(dir=>`<h3>${dir==='H'?'Horizontais':'Verticais'}</h3><ul class="mef-clues">${layout.words.filter(w=>w.dir===dir).map(w=>`<li><strong>${w.number}.</strong> ${esc(w.dica)} (${w.word.length} letras)</li>`).join('')}</ul>`).join('')}`;
  }
  function execution() {
    const task=db().tasks.find(t=>t.id===query.get('id'));
    if(!task||!MEF.assigned(task)){root.innerHTML=header('Atividade indisponível','Escolha uma atividade atribuída ao aluno atual.')+link('tarefas_aluno.html','Voltar às atividades');return;}
    try{validateContent(task);}catch(e){root.innerHTML=header('Conteúdo precisa de revisão')+empty(e.message)+link('tarefas_aluno.html','Voltar');return;}
    const previous=MEF.attempts(task.id);
    if(previous.length&&query.get('repetir')!=='1'){location.replace('relatorio_desempenho.html?id='+encodeURIComponent(previous.at(-1).id));return;}
    const sid=MEF.studentId(), started=Date.now(), token=MEF.id();
    let timer, pauseStarted=0, pausedTime=0, paused=false, finishing=false;
    const seconds=()=>Math.floor(((paused?pauseStarted:Date.now())-started-pausedTime)/1000);
    if (!original) {
      root.innerHTML=header(task.titulo,names[task.tipo],link('tarefas_aluno.html','Voltar'))+
        `<div class="mef-card"><p>${esc(task.instrucoes||'Responda no seu ritmo.')}</p><div class="mef-actions"><span id="timer">00:00</span>${button('pause','Pausar')}</div></div><div class="mef-error" role="alert"></div><div id="activity-content"></div><div class="mef-actions">${button('finish','Concluir atividade','primary')}</div>`;
    } else {
      $('#task-display-title').textContent=task.titulo;
      $('#task-display-instructions').textContent=task.instrucoes || 'Responda às questões abaixo com atenção.';
      $('#warning-modal-text').setAttribute('role','alert');
      $('#btn-warning-ok').onclick=()=>$('#warning-modal').classList.remove('active');
      const awardLabel=$('.activity-info-group .stat-badge:nth-child(2) span');
      if(awardLabel)awardLabel.textContent=previous.length?'Prática sem XP extra':'Até 50 XP';
      const pause=document.createElement('button');pause.id='pause';pause.className='stat-badge';pause.type='button';pause.textContent='Pausar';$('.activity-info-group').append(pause);
    }
    const content = original ? $('#quiz-dynamic-container') : $('#activity-content');
    const finish = original ? $('#btn-finish-quiz') : $('#finish');
    const timerEl = original ? $('#timer-display') : $('#timer');
    timer=setInterval(()=>{const n=seconds();timerEl.textContent=String(Math.floor(n/60)).padStart(2,'0')+':'+String(n%60).padStart(2,'0');},1000);
    $('#pause').onclick=()=>{
      paused=!paused;
      if(paused){pauseStarted=Date.now();content.inert=true;content.hidden=true;finish.disabled=true;$('#pause').textContent='Continuar';}
      else{pausedTime+=Date.now()-pauseStarted;content.inert=false;content.hidden=false;finish.disabled=false;$('#pause').textContent='Pausar';}
    };
    window.addEventListener('pagehide',()=>clearInterval(timer),{once:true});
    let evaluate, beforeComplete=()=>{}, memoryBusy=false;
    function renderQuestions() {
      content.innerHTML=task.perguntas.map((q,i)=> original ? `<div class="quiz-question-card" id="question-card-${i}"><div class="question-title"><span class="question-number">${i+1}</span><h3>${esc(q.pergunta)}</h3></div><div class="options-group">${q.opcoes.map((o,j)=>`<label class="quiz-option"><input type="radio" name="answer-${i}" value="${j}"><span class="option-custom"></span><span class="option-text">${esc(o)}</span></label>`).join('')}</div></div>` : `<fieldset class="mef-question"><legend>Pergunta ${i+1}</legend><h3>${esc(q.pergunta)}</h3>${q.opcoes.map((o,j)=>`<label class="mef-option"><input type="radio" name="answer-${i}" value="${j}"><span>${esc(o)}</span></label>`).join('')}</fieldset>`).join('');
    }
    function gradeQuestions() {
      const details=task.perguntas.map((q,i)=>{
        const chosen=$(`input[name="answer-${i}"]:checked`,content);
        if(!chosen)throw new Error(`Responda a pergunta ${i+1} antes de concluir.`);
        const answer=Number(chosen.value);return{question:q.pergunta,answer:q.opcoes[answer],expected:q.opcoes[q.correta],correct:answer===q.correta};
      });
      return {details,correct:details.filter(v=>v.correct).length,errors:details.filter(v=>!v.correct).length,total:details.length,score:Math.round(details.filter(v=>v.correct).length/details.length*100)};
    }
    if(task.tipo==='quiz'){renderQuestions();evaluate=gradeQuestions;}
    if(task.tipo==='leitura'){
      let step=0;finish.hidden=true;
      const renderReading=()=>{
        content.innerHTML=`<article class="mef-card"><p class="mef-muted">Trecho ${step+1} de ${task.trechos.length}</p><progress class="mef-progress" value="${step+1}" max="${task.trechos.length}" aria-label="Progresso da leitura"></progress><p class="mef-prose">${esc(task.trechos[step])}</p><div class="mef-actions">${step>0?button('previous-reading','Trecho anterior'):''}${button('next-reading',step===task.trechos.length-1?'Responder perguntas':'Li este trecho','primary')}</div></article>`;
        $('#previous-reading')?.addEventListener('click',()=>{step--;renderReading();});
        $('#next-reading').onclick=()=>{if(step<task.trechos.length-1){step++;renderReading();}else{renderQuestions();finish.hidden=false;}};
      };
      if(MEFDocuments.valid(task.pdf)){
        content.innerHTML=MEFDocuments.view(task.pdf)+'<div class="mef-actions">'+button('next-reading','Concluir leitura do PDF e continuar','primary')+'</div>';
        $('#next-reading').onclick=()=>{if(task.trechos?.length)renderReading();else{renderQuestions();finish.hidden=false;}};
      }else renderReading();
      evaluate=()=>({...gradeQuestions(),readingSegments:task.trechos?.length||0,readingPDF:!!task.pdf});
    }
    if(task.tipo==='cruzadas'){
      const layout=MEFCrossword.build(task.palavras);content.innerHTML=`<article class="mef-card"><p>Digite para avançar automaticamente na palavra. Use as setas para mudar a direção e Backspace para voltar.</p>${crosswordHTML(layout)}</article>`;
      MEFCrosswordNavigation.mount(content,layout);
      evaluate=()=>{
        if($$('[data-cell]',content).some(i=>!i.value))throw new Error('Preencha todas as casas das palavras cruzadas.');
        const details=layout.words.map(w=>{
          let answer='';for(let n=0;n<w.word.length;n++)answer+=$(`[data-cell="${w.row+(w.dir==='V'?n:0)},${w.col+(w.dir==='H'?n:0)}"]`,content).value;
          return{question:`${w.number}. ${w.dica}`,answer,expected:w.word,correct:answer===w.word};
        });
        const correct=details.filter(d=>d.correct).length;
        return{details,correct,errors:details.length-correct,total:details.length,score:Math.round(correct/details.length*100)};
      };
    }
    if(task.tipo==='memoria'){
      let first=null, matched=0, mistakes=0, moves=0, pendingTimer;
      const attemptBadge=original?$('.activity-info-group .stat-badge:nth-child(2)'):null;
      if(attemptBadge)attemptBadge.innerHTML='<span id="memory-attempts" role="status">0 tentativas</span>';
      const details=[];
      const cards=task.pares.flatMap((p,i)=>[{pair:i,text:p.termo},{pair:i,text:p.significado}]);
      // Fisher–Yates: embaralhamento sem viés de comparação aleatória.
      for(let i=cards.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[cards[i],cards[j]]=[cards[j],cards[i]];}
      content.innerHTML=`<p class="mef-muted">Encontre cada palavra e o seu significado. Use Tab e Enter para virar as cartas.</p><p id="memory-progress" role="status">0 de ${task.pares.length} pares encontrados</p><div class="mef-memory">${cards.map((c,i)=>`<button type="button" data-card="${i}" aria-label="Virar carta ${i+1}" aria-pressed="false">?</button>`).join('')}</div>`;
      $$('[data-card]',content).forEach(b=>b.onclick=()=>{
        if(paused||memoryBusy||b.disabled||b===first)return;
        const card=cards[Number(b.dataset.card)];b.textContent=card.text;b.classList.add('flipped');b.setAttribute('aria-label',card.text);b.setAttribute('aria-pressed','true');
        if(!first){first=b;return;}
        const other=first, c=cards[Number(other.dataset.card)];first=null;moves++;if($('#memory-attempts'))$('#memory-attempts').textContent=moves+' '+(moves===1?'tentativa':'tentativas');
        const correct=card.pair===c.pair;
        details.push({question:`Tentativa ${moves}`,answer:`${c.text} ↔ ${card.text}`,expected:correct?'Par correspondente':'Palavra e significado do mesmo par',correct});
        if(correct){matched++;[other,b].forEach(v=>{v.disabled=true;v.classList.add('matched');});$('#memory-progress').textContent=`${matched} de ${task.pares.length} pares encontrados`;}
        else{mistakes++;memoryBusy=true;pendingTimer=setTimeout(()=>{[other,b].forEach(v=>{v.textContent='?';v.classList.remove('flipped');v.setAttribute('aria-label','Virar carta '+(Number(v.dataset.card)+1));v.setAttribute('aria-pressed','false');});memoryBusy=false;},900);}
      });
      evaluate=()=>{if(matched!==task.pares.length)throw new Error('Encontre todos os pares antes de concluir.');return{details,correct:matched,errors:mistakes,total:moves,score:Math.round(matched/moves*100),pairs:task.pares.length};};
      beforeComplete=()=>clearTimeout(pendingTimer);
    }
    finish.onclick=()=>{
      if(finishing||paused)return;
      try{
        const result=evaluate();finishing=true;finish.disabled=true;
        const report=MEF.transaction(d=>{
          const current=d.tasks.find(t=>t.id===task.id);
          if(!current||!MEF.assigned(current,sid))throw new Error('Esta atividade foi removida ou deixou de ser atribuída ao aluno.');
          const duplicate=d.reports.find(r=>r.attemptToken===token);if(duplicate)return duplicate;
          const already=d.reports.some(r=>r.taskId===task.id&&r.studentId===sid&&r.kind==='automatico');
          const report={id:MEF.id(),kind:'automatico',attemptToken:token,studentId:sid,studentName:student(sid)?.nome,taskId:task.id,title:task.titulo,type:task.tipo,snapshot:structuredClone(task),finishedAt:new Date().toISOString(),seconds:seconds(),xp:already?0:Math.round(result.score/2),...result};
          d.reports.push(report);
          MEF.notify(d,['professor','psicopedagogo'],'Novo relatório disponível',`${report.studentName} concluiu ${report.title}.`,'relatorios.html');
          MEF.notify(d,['aluno:'+sid],'Atividade concluída',report.title,'relatorio_desempenho.html?id='+encodeURIComponent(report.id));
          return report;
        });
        beforeComplete();clearInterval(timer);location.href='relatorio_desempenho.html?id='+encodeURIComponent(report.id);
      }catch(e){finishing=false;finish.disabled=false;message(e);}
    };
  }
  function reportContent(r) {
    const author=r.author==='professor'?'Professor':'Psicopedagogo';
    if(r.kind==='manual')return `<article class="mef-card"><span class="mef-tag">Relatório manual • ${author}</span><h2>${esc(r.title)}</h2><p class="mef-muted">${esc(student(r.studentId)?.nome||r.studentName)} • ${date(r.finishedAt)}${r.updatedAt?' • Editado em '+date(r.updatedAt):''}</p>${[['Observações',r.observations],['Estratégias utilizadas',r.strategies],['Próximos passos',r.nextSteps]].map(([label,value])=>`<h3>${label}</h3><p class="mef-prose">${esc(value||'Não informado')}</p>`).join('')}</article>`;
    return `<article class="mef-card"><span class="mef-tag">Relatório automático</span><h2>${esc(r.title)}</h2><p class="mef-muted">${esc(r.studentName)} • ${esc(names[r.type])} • Concluída em ${date(r.finishedAt)}</p><div class="mef-stats">${(r.type==='memoria'?[['Pares encontrados',r.correct],['Tentativas',r.total],['Combinações incorretas',r.errors],['Tempo de execução',duration(r.seconds)],['XP desta tentativa',r.xp]]:[['Acertos',r.correct],['Erros',r.errors],['Aproveitamento',r.score+'%'],['Tempo de execução',duration(r.seconds)],['XP desta tentativa',r.xp]]).map(([label,v])=>`<div class="mef-stat">${label}<strong>${esc(v)}</strong></div>`).join('')}</div>${r.type==='memoria'?'<p class="mef-muted">Na memória, o aproveitamento é a proporção de pares corretos sobre as tentativas de combinação.</p>':''}<div class="mef-table-wrap"><table class="mef-table"><caption>Respostas registradas nesta tentativa</caption><thead><tr><th>Pergunta / tentativa</th><th>Resposta</th><th>Esperado</th><th>Resultado</th></tr></thead><tbody>${r.details.map(v=>`<tr><td>${esc(v.question)}</td><td>${esc(v.answer)}</td><td>${esc(v.expected)}</td><td>${v.correct?'Acerto':'Erro'}</td></tr>`).join('')}</tbody></table></div></article>`;
  }
  function resultPage() {
    const r=db().reports.find(r=>r.id===query.get('id')&&r.kind==='automatico'&&r.studentId===MEF.studentId());
    if(!r){root.innerHTML=header('Resultado não encontrado')+link('tarefas_aluno.html','Voltar às atividades');return;}
    root.innerHTML=header('Atividade concluída','Veja as respostas e converse com o professor quando precisar.')+reportContent(r)+`<div class="mef-actions">${link('tarefas_aluno.html','Voltar às atividades','primary')}${db().tasks.some(t=>t.id===r.taskId&&MEF.assigned(t))?button('retry','Tentar novamente'):''}</div>`;
    $('#retry')?.addEventListener('click',()=>confirmAction('Repetir atividade?', 'Uma nova tentativa será registrada. O relatório anterior será preservado.',()=>location.href='executar_atividade.html?id='+encodeURIComponent(r.taskId)+'&repetir=1'));
  }
  function reportsPage() {
    root.innerHTML=header('Relatórios dos alunos','Consulte resultados automáticos e registros pedagógicos.',link('escrever-relatorio.html','Escrever relatório','primary'))+
      `<div class="mef-filters">${field('search','Buscar título ou aluno')}<label class="mef-field" for="student-filter">Aluno<select id="student-filter">${option('','Todos')}${db().students.map(s=>option(s.id,s.nome,s.id===query.get('aluno'))).join('')}</select></label><label class="mef-field" for="kind-filter">Tipo<select id="kind-filter">${option('','Todos')}${option('automatico','Automático')}${option('manual','Manual')}</select></label>${field('date-filter','Data da conclusão','','date')}</div><div id="report-list"></div>`;
    const render=()=>{
      const q=MEF.norm($('#search').value),sid=$('#student-filter').value,kind=$('#kind-filter').value,day=$('#date-filter').value;
      const list=db().reports.filter(r=>(!query.get('tarefa')||r.taskId===query.get('tarefa'))&&(!sid||r.studentId===sid)&&(!kind||r.kind===kind)&&(!day||new Date(r.finishedAt).toLocaleDateString('en-CA')===day)&&MEF.norm(r.title+' '+(student(r.studentId)?.nome||r.studentName)).includes(q)).sort((a,b)=>b.finishedAt.localeCompare(a.finishedAt));
      $('#report-list').innerHTML=list.map(r=>`<article class="mef-card"><span class="mef-tag">${r.kind==='automatico'?'Automático':'Manual'}</span><h2>${esc(r.title)}</h2><p>${esc(student(r.studentId)?.nome||r.studentName)} • ${date(r.finishedAt)}</p>${r.kind==='automatico'?`<p>${r.correct} acertos • ${r.errors} erros • ${r.score}% • ${duration(r.seconds)}</p>`:''}<div class="mef-actions"><button class="mef-btn primary" data-report="${esc(r.id)}">Visualizar relatório</button>${r.kind==='manual'?link('escrever-relatorio.html?id='+encodeURIComponent(r.id),'Editar relatório'):''}${link('ficha-aluno.html?id='+encodeURIComponent(r.studentId),'Ficha do aluno')}</div></article>`).join('')||empty('Nenhum relatório encontrado. Conclua uma atividade ou escreva um relatório manual.');
      $$('[data-report]').forEach(b=>b.onclick=()=>{const r=db().reports.find(v=>v.id===b.dataset.report);dialog('Relatório do aluno',reportContent(r)+'<div class="mef-actions"><button class="mef-btn" data-close>Fechar</button></div>');});
    };
    $('#search').oninput=render;$('#student-filter').onchange=render;$('#kind-filter').onchange=render;$('#date-filter').onchange=render;render();
  }
  function manualReport() {
    const r=query.get('id')?db().reports.find(v=>v.id===query.get('id')&&v.kind==='manual'):null;
    if(query.get('id')&&!r){root.innerHTML=header('Relatório não encontrado')+link('relatorios.html','Voltar');return;}
    root.innerHTML=header(r?'Editar relatório manual':'Escrever relatório manual','Registre observações pedagógicas e estratégias de acompanhamento.',link('relatorios.html','Voltar'))+
      `<form id="manual-form" class="mef-card"><div class="mef-error" role="alert"></div><label class="mef-field" for="report-student">Aluno<select name="studentId" id="report-student" required>${option('','Selecione um aluno')}${db().students.map(s=>option(s.id,s.nome,s.id===(r?.studentId||query.get('aluno')))).join('')}</select></label>${field('report-title','Título',r?.title,'text',true)}${textarea('observations','Observações pedagógicas',r?.observations,true)}${textarea('strategies','Estratégias utilizadas',r?.strategies)}${textarea('next-steps','Próximos passos',r?.nextSteps)}${button('save-manual','Salvar relatório','primary','submit')}</form>`;
    $('#manual-form').onsubmit=e=>{
      e.preventDefault();try{
        const sid=$('#report-student').value,title=$('#report-title').value.trim(),observations=$('#observations').value.trim();
        if(!student(sid)||!title||!observations)throw new Error('Selecione o aluno e preencha título e observações.');
        MEF.transaction(d=>{
          const now=new Date().toISOString(),data={id:r?.id||MEF.id(),kind:'manual',studentId:sid,studentName:student(sid).nome,title,observations,strategies:$('#strategies').value.trim(),nextSteps:$('#next-steps').value.trim(),author:role,finishedAt:r?.finishedAt||now,updatedAt:r?now:null};
          const idx=d.reports.findIndex(v=>v.id===data.id);if(idx>=0)d.reports[idx]=data;else d.reports.push(data);
          MEF.notify(d,['professor','psicopedagogo'],'Relatório manual salvo',title,'relatorios.html');
        });location.href=reportsHref(sid);
      }catch(error){message(error);}
    };
  }
  function notificationsPage() {
    const target=MEF.audience();
    root.innerHTML=header('Notificações','Acompanhe atividades e relatórios do seu perfil.',button('mark-all','Marcar todas como lidas'))+
      (role==='aluno'?studentChooser():'')+`<div class="mef-card"><label class="mef-check"><input id="notifications-enabled" type="checkbox" ${db().preferences[target]!==false?'checked':''}>Receber novas notificações</label><p class="mef-muted">Desativar impede novos avisos neste perfil de demonstração. O histórico continua disponível.</p></div><div class="mef-filters"><label class="mef-field" for="read-filter">Exibir<select id="read-filter">${option('','Todas')}${option('unread','Não lidas')}${option('read','Lidas')}</select></label></div><div id="notification-list"></div>`;
    const render=()=>{
      const filter=$('#read-filter').value,list=db().notifications.filter(n=>n.target===target&&(!filter||(filter==='read'?n.read:!n.read)));
      $('#notification-list').innerHTML=list.map(n=>`<article class="mef-card ${n.read?'':'mef-unread'}"><span class="mef-tag">${n.read?'Lida':'Não lida'}</span><h2>${esc(n.title)}</h2><p>${esc(n.message)}</p><p class="mef-muted">${date(n.date)}</p><div class="mef-actions"><a class="mef-btn primary" href="${esc(n.link)}" data-open-notification="${esc(n.id)}">Abrir</a>${n.read?'':`<button type="button" class="mef-btn" data-read="${esc(n.id)}">Marcar como lida</button>`}</div></article>`).join('')||empty('Nenhuma notificação nesta seleção.');
      $$('[data-read]').forEach(b=>b.onclick=()=>{try{MEF.transaction(d=>{d.notifications.find(n=>n.id===b.dataset.read&&n.target===target).read=true;});render();}catch(e){message(e);}});
      $$('[data-open-notification]').forEach(a=>a.onclick=e=>{try{MEF.transaction(d=>{d.notifications.find(n=>n.id===a.dataset.openNotification&&n.target===target).read=true;});}catch(error){e.preventDefault();message(error);}});
    };
    $('#mark-all').onclick=()=>{try{MEF.transaction(d=>d.notifications.filter(n=>n.target===target).forEach(n=>n.read=true));render();}catch(e){message(e);}};
    $('#notifications-enabled').onchange=e=>{try{MEF.transaction(d=>d.preferences[target]=e.target.checked);toast(e.target.checked?'Notificações ativadas.':'Notificações desativadas.');}catch(error){e.target.checked=!e.target.checked;message(error);}};
    $('#read-filter').onchange=render;chooseStudent();render();
  }
  window.MEFUI = {esc, dialog, confirmAction, toast, validateContent, reportContent, editStudent, message, execution};
  if (document.body.dataset.original === 'true') return;
  try {
    setupCommon();
    if(root){
      const routes={alunos:studentsPage,turma:studentsPage,ficha:studentPage,tarefas:taskList,editor,executar:execution,resultado:resultPage,relatorios:reportsPage,manual:manualReport,notificacoes:notificationsPage};
      routes[document.body.dataset.page]?.();
    }
  } catch(e) { if(root)root.innerHTML=header('Não foi possível carregar os dados')+`<p class="mef-error" role="alert">${esc(e.message)}</p>`;else toast(e.message); }
})();
