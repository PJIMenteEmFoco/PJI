/* Correções funcionais nos elementos e nas classes do layout original. */
(() => {
  'use strict';
  const $=(s,r=document)=>r.querySelector(s), $$=(s,r=document)=>[...r.querySelectorAll(s)];
  const ui=MEFUI, esc=ui.esc, role=MEF.role(), suffix={professor:'prof',psicopedagogo:'psico',aluno:'aluno'}[role];
  const query=new URLSearchParams(location.search), file=location.pathname.split('/').at(-1);
  const names={quiz:'Quiz',memoria:'Jogo da Memória',cruzadas:'Palavras Cruzadas',leitura:'Leitura Guiada'};
  const tags={quiz:'quiz',memoria:'memory',cruzadas:'memory',leitura:'comic'};
  const db=()=>MEF.load();
  const date=v=>v?new Date(v.length===10?v+'T12:00:00':v).toLocaleDateString('pt-BR'):'Sem prazo';
  const link=(url,text,cls='btn-card-action')=>`<a href="${esc(url)}" class="${cls}">${text}</a>`;
  const option=(v,t,selected=false)=>`<option value="${esc(v)}" ${selected?'selected':''}>${esc(t)}</option>`;
  function common(){
    if(localStorage.getItem('theme')==='escuro')document.body.classList.add('dark-mode');
    $('#toggle-btn')?.addEventListener('click',()=>$('#sidebar').classList.toggle('expanded'));
    $('#btn-open-logout')?.addEventListener('click',e=>{e.preventDefault();$('#logout-modal')?.classList.add('active');});
    $('#btn-cancel-logout')?.addEventListener('click',()=>$('#logout-modal')?.classList.remove('active'));
    $('#btn-confirm-logout')?.addEventListener('click',()=>location.href='../login/login.html');
    $$('.modal-overlay').forEach(el=>{el.setAttribute('role','dialog');el.setAttribute('aria-modal','true');el.addEventListener('click',e=>{if(e.target===el)el.classList.remove('active');});});
    document.addEventListener('keydown',e=>{if(e.key==='Escape')$$('.modal-overlay.active').forEach(el=>el.classList.remove('active'));});
    window.lucide?.createIcons();
  }
  function alertError(e){ui.message(e,$('main'));}
  function taskCards(){
    const psico=role==='psicopedagogo';
    const grid=psico?$('.previous-tasks-grid'):$('#tasks-grid');
    const input=psico?$('.search-box input'):$('#task-search-input');
    const pills=$$('.pill-btn');let filter='all';
    const render=()=>{
      const q=MEF.norm(input?.value),list=db().tasks.filter(t=>MEF.norm(t.titulo+' '+t.alunos.map(id=>db().students.find(s=>s.id===id)?.nome).join(' ')).includes(q));
      list.sort((a,b)=>($('#sort-select')?.value==='old'?1:-1)*(String(a.createdAt||a.id).localeCompare(String(b.createdAt||b.id))));
      grid.innerHTML=list.map(t=>{
        const reports=db().reports.filter(r=>r.taskId===t.id&&r.kind==='automatico'),completed=new Set(reports.map(r=>r.studentId));
        const pending=t.status==='Rascunho'||completed.size<t.alunos.length;
        if(filter==='pending'&&!pending)return '';
        const alumnos=t.alunos.map(id=>db().students.find(s=>s.id===id)?.nome).filter(Boolean).join(', ');
        return `<div class="task-card" data-task-id="${esc(t.id)}" data-status="${pending?'pending':'active'}"><div class="task-card-header"><span class="task-type-tag ${tags[t.tipo]||'quiz'}">${esc(names[t.tipo]||t.tipo)}</span><span class="task-status-dot ${pending?'pending':'active'}" title="${t.status==='Rascunho'?'Rascunho':pending?'Pendente':'Concluída'}"></span></div><h3 class="task-card-title">${esc(t.titulo)}</h3><div class="task-card-meta"><div class="task-meta-row"><i data-lucide="${t.alunos.length>1?'users':'user'}"></i><span>${esc(alumnos||'Rascunho sem destinatários')}</span></div><div class="task-meta-row"><i data-lucide="clock"></i><span>Prazo: ${date(t.prazo)}</span></div></div>${psico?`<button class="btn-view-report" data-reports="${esc(t.id)}">Ver Relatório do Aluno</button>`:''}</div>`;
      }).join('')||'<p class="empty-state-card">Nenhuma atividade encontrada.</p>';
      if(!psico)$$('[data-task-id]').forEach(card=>{card.tabIndex=0;card.setAttribute('role','link');card.title='Abrir e editar tarefa';const open=()=>location.href='editar-atividade.html?id='+encodeURIComponent(card.dataset.taskId);card.onclick=open;card.onkeydown=e=>{if(e.key==='Enter')open();};});
      else $$('[data-reports]').forEach(b=>b.onclick=()=>location.href='relatorios.html?tarefa='+encodeURIComponent(b.dataset.reports));
      window.lucide?.createIcons();
    };
    input?.addEventListener('input',render);$('#sort-select')?.addEventListener('change',render);
    pills.forEach((p,i)=>p.onclick=()=>{pills.forEach(v=>v.classList.remove('active'));p.classList.add('active');filter=p.dataset.filter||(i?'pending':'all');render();});
    if(psico){
      const stats=$$('.psico-stat-card h3'),reports=db().reports.filter(r=>r.kind==='automatico');
      if(stats[0])stats[0].textContent=reports.length+' Atividades';
      if(stats[1])stats[1].textContent=reports.length?Math.round(reports.reduce((n,r)=>n+r.seconds,0)/reports.length/60)+' min':'—';
      if(stats[2])stats[2].textContent='—';
      const actions=$('.controls-group');if(actions){const b=document.createElement('a');b.className='pill-btn';b.href='escrever-relatorio.html';b.textContent='Escrever Relatório';actions.append(b);}
    }
    render();
  }
  function studentTasks(){
    const sections=$$('.cards-grid-row'),delivered=$('#delivered-grid');
    const render=()=>{
      const tasks=db().tasks.filter(t=>MEF.assigned(t)),now=MEFTaskState.dateKey();
      const grids=[[],[],[]];
      tasks.forEach(t=>{
        const result=MEF.attempts(t.id).at(-1),kind=result?'delivered':t.prazo&&t.prazo<now?'overdue':'pending',index=result?2:kind==='overdue'?1:0;
        const url=result?'relatorio_desempenho.html?id='+encodeURIComponent(result.id):'executar_atividade.html?id='+encodeURIComponent(t.id);
        grids[index].push(`<a href="${url}" class="task-card-full ${kind}" data-task-id="${esc(t.id)}"><div class="card-header-bar"><span>${result?'CONCLUÍDA':kind==='overdue'?'ATRASADA':'ENTREGA'}</span><span>${date(result?.finishedAt||t.prazo).slice(0,5)}</span></div><div class="card-body-content">${MEFActivityArt.image(t.tipo)}<h4 class="task-title">${esc(t.titulo)}</h4><span class="task-tag">${esc(names[t.tipo]||t.tipo)}</span></div></a>`);
      });
      [sections[0],sections[1],delivered].forEach((grid,i)=>{grid.innerHTML=grids[i].join('')||(i===1?MEFTaskState.empty(tasks.length>0):'<p class="original-empty">Nenhuma tarefa nesta seção.</p>');});
    };
    $('#toggle-delivered-btn').onclick=()=>{$('#toggle-delivered-btn').classList.toggle('open');delivered.classList.toggle('open');};
    // Troca de aluno para testar o protótipo, acessível sem alterar a página de tarefas.
    render();window.lucide?.createIcons();
  }
  function editor(){
    const task=query.get('id')?db().tasks.find(t=>t.id===query.get('id')):null;
    if(query.get('id')&&!task){alertError('Atividade não encontrada.');return;}
    const aliases={'hq':'leitura','caca-palavras':'cruzadas'};
    const type=aliases[query.get('tipo')]||query.get('tipo');
    let model=task?structuredClone(task):{id:MEF.id(),titulo:'',instrucoes:'',prazo:$('#task-deadline').value,tipo:names[type]?type:'quiz',alunos:db().students.length?[db().students[0].id]:[],perguntas:[{pergunta:$('.question-input').value,opcoes:$$('.answer-input').map(i=>i.value),correta:0}],pares:[{termo:'',significado:''},{termo:'',significado:''}],palavras:[{palavra:'',dica:''},{palavra:'',dica:''}],trechos:['']};
    model.perguntas||=[{pergunta:'',opcoes:['',''],correta:0}];model.pares||=[{termo:'',significado:''},{termo:'',significado:''}];model.palavras||=[{palavra:'',dica:''},{palavra:'',dica:''}];model.trechos||=[''];
    $('#task-title').value=model.titulo;$('#task-instructions').value=model.instrucoes;$('#task-deadline').value=model.prazo||'';
    if(task){$('.page-title').textContent='Editar Tarefa';$('#btn-submit-task').textContent='Salvar Alterações';}
    const roster=$('.students-list-box');roster.innerHTML=db().students.map((s,i)=>`<label class="student-item"><input type="checkbox" class="chk-student" value="${esc(s.id)}" ${model.alunos.includes(s.id)?'checked':''}><span class="student-name">${esc(s.nome)}</span><span class="student-team">${esc(s.turma)}</span></label>`).join('');
    $('#search-student').oninput=e=>{$$('.student-item',roster).forEach(row=>row.hidden=!MEF.norm(row.textContent).includes(MEF.norm(e.target.value)));};
    const content=$('#questions-container'),typeCard=$('.type-selector-card');typeCard.tabIndex=0;typeCard.setAttribute('role','button');typeCard.setAttribute('aria-label','Escolher formato da atividade');
    const formField=(label,id,value='',area=false)=>`<div class="form-group"><label class="form-label" for="${id}">${esc(label)}</label>${area?`<textarea class="form-textarea" id="${id}">${esc(value)}</textarea>`:`<input class="form-input" id="${id}" value="${esc(value)}">`}</div>`;
    function capture(){
      model.titulo=$('#task-title').value.trim();model.instrucoes=$('#task-instructions').value.trim();model.prazo=$('#task-deadline').value;model.alunos=$$('.chk-student:checked').map(i=>i.value);
      if(model.tipo==='quiz'||model.tipo==='leitura')model.perguntas=$$('[data-qindex]',content).map((box,i)=>({pergunta:$('.question-input',box).value.trim(),opcoes:$$('.answer-input',box).map(input=>input.value.trim()),correta:Number($('input[type=radio]:checked',box)?.value??-1)}));
      if(model.tipo==='leitura')model.trechos=$('#reading-text').value.split(/\n\s*\n/).map(s=>s.trim()).filter(Boolean);
      if(model.tipo==='memoria')model.pares=$$('[data-pair]').map((box,i)=>({termo:$('#term-'+i,box).value.trim(),significado:$('#meaning-'+i,box).value.trim()}));
      if(model.tipo==='cruzadas')model.palavras=$$('[data-word]').map((box,i)=>({palavra:MEF.norm($('#word-'+i,box).value),dica:$('#clue-'+i,box).value.trim()}));
    }
    function questions(){return model.perguntas.map((q,i)=>`<div class="question-box" data-qindex="${i}"><div class="question-header"><i data-lucide="grip-vertical" style="color:#557D91;"></i><input type="text" class="question-input" value="${esc(q.pergunta)}" placeholder="Digite a pergunta aqui..."><button type="button" class="btn-trash" data-remove-question="${i}" title="Excluir Pergunta"><i data-lucide="trash-2"></i></button></div><div class="answers-list">${q.opcoes.map((o,j)=>`<div class="answer-item"><input type="radio" name="correct-${i}" value="${j}" ${q.correta===j?'checked':''} title="Marcar como alternativa correta"><input type="text" class="answer-input" value="${esc(o)}" placeholder="Texto da alternativa..."></div>`).join('')}</div><div class="builder-actions"><button type="button" class="btn-builder btn-builder-secondary" data-add-option="${i}"><i data-lucide="plus"></i> Adicionar Resposta</button><button type="button" class="btn-builder btn-builder-primary" data-add-question><i data-lucide="plus-circle"></i> Adicionar Nova Pergunta</button></div></div>`).join('');}
    function render(){
      typeCard.innerHTML=`<i data-lucide="${{quiz:'help-circle',memoria:'layers',cruzadas:'grid',leitura:'image'}[model.tipo]}"></i><span>${model.tipo==='quiz'?'Quiz Adaptado':names[model.tipo]}</span>`;
      if(model.tipo==='quiz')content.innerHTML=questions();
      if(model.tipo==='leitura')content.innerHTML=`<div class="question-box">${formField('Texto da leitura (separe os trechos com uma linha em branco)','reading-text',model.trechos.join('\n\n'),true)}</div>`+questions();
      if(model.tipo==='memoria')content.innerHTML=model.pares.map((p,i)=>`<div class="question-box" data-pair="${i}">${formField('Palavra / conceito','term-'+i,p.termo)}${formField('Significado correspondente','meaning-'+i,p.significado)}<div class="builder-actions"><button class="btn-builder btn-builder-secondary" type="button" data-remove-pair="${i}">Remover Par</button></div></div>`).join('')+'<button type="button" class="btn-builder btn-builder-primary" id="add-pair">Adicionar Par</button>';
      if(model.tipo==='cruzadas')content.innerHTML=model.palavras.map((p,i)=>`<div class="question-box" data-word="${i}">${formField('Palavra (2 a 16 letras)','word-'+i,p.palavra)}${formField('Dica','clue-'+i,p.dica)}<div class="builder-actions"><button class="btn-builder btn-builder-secondary" type="button" data-remove-word="${i}">Remover Palavra</button></div></div>`).join('')+'<button type="button" class="btn-builder btn-builder-primary" id="add-word">Adicionar Palavra</button>';
      $$('[data-add-option]').forEach(b=>b.onclick=()=>{capture();model.perguntas[Number(b.dataset.addOption)].opcoes.push('');render();});
      $$('[data-add-question]').forEach(b=>b.onclick=()=>{capture();model.perguntas.push({pergunta:'',opcoes:['',''],correta:0});render();});
      $$('[data-remove-question]').forEach(b=>b.onclick=()=>{capture();if(model.perguntas.length===1){ui.toast('A atividade precisa de pelo menos uma pergunta.');return;}model.perguntas.splice(Number(b.dataset.removeQuestion),1);render();});
      for(const[k,key,blank]of[['pair','pares',{termo:'',significado:''}],['word','palavras',{palavra:'',dica:''}]]){
        $('#add-'+k)?.addEventListener('click',()=>{capture();model[key].push({...blank});render();});
        $$('[data-remove-'+k+']').forEach(b=>b.onclick=()=>{capture();if(model[key].length<=2){ui.toast('Mantenha pelo menos dois itens.');return;}model[key].splice(Number(b.dataset[k==='pair'?'removePair':'removeWord']),1);render();});
      }
      MEFDocuments.mount(content,model,capture);
      window.lucide?.createIcons();
    }
    function chooseType(){
      capture();ui.dialog('Tipo de Atividade',`<label class="mef-field" for="choose-type">Formato<select id="choose-type">${Object.entries(names).map(([k,n])=>option(k,n,k===model.tipo)).join('')}</select></label><div class="mef-actions"><button class="mef-btn" id="type-example">Preencher com exemplo</button><button class="mef-btn primary" id="type-confirm">Confirmar</button></div>`,el=>{
        $('#type-confirm',el).onclick=()=>{model.tipo=$('#choose-type',el).value;el.close();render();};
        $('#type-example',el).onclick=()=>{const type=$('#choose-type',el).value,sample=db().tasks.find(t=>t.demonstracao&&t.tipo===type);if(sample){model.tipo=type;for(const k of ['perguntas','pares','palavras','trechos'])if(sample[k])model[k]=structuredClone(sample[k]);el.close();render();}else ui.toast('Não há exemplo disponível para este formato.');};
      });
    }
    typeCard.onclick=chooseType;typeCard.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();chooseType();}};
    function save(status){try{capture();if(!model.titulo)throw new Error('Preencha o título da tarefa.');if(status==='Ativa'){if(!model.alunos.length)throw new Error('Selecione pelo menos um aluno.');if(!model.prazo)throw new Error('Informe o prazo.');ui.validateContent(model);}MEF.transaction(d=>{const idx=d.tasks.findIndex(t=>t.id===model.id);const value={...model,status,demonstracao:false,updatedAt:new Date().toISOString(),createdAt:model.createdAt||new Date().toISOString()};if(idx>=0)d.tasks[idx]=value;else d.tasks.push(value);if(status==='Ativa')MEF.notify(d,value.alunos.map(id=>'aluno:'+id),idx>=0?'Atividade atualizada':'Nova atividade',value.titulo,'tarefas_aluno.html');});location.href='tarefas_'+suffix+'.html';}catch(e){alertError(e);}}
    $('#btn-save-draft').onclick=()=>save('Rascunho');$('#btn-submit-task').onclick=()=>save('Ativa');
    if(task){const b=document.createElement('button');b.className='btn-action btn-draft';b.type='button';b.textContent='Excluir Tarefa';b.onclick=()=>ui.confirmAction('Excluir Tarefa?', 'Os relatórios existentes serão preservados.',()=>{MEF.transaction(d=>{d.tasks=d.tasks.filter(t=>t.id!==task.id);MEF.notify(d,task.alunos.map(id=>'aluno:'+id),'Atividade excluída',task.titulo,'tarefas_aluno.html');});location.href='tarefas_'+suffix+'.html';});$('.footer-submit-bar').prepend(b);}
    render();
  }
  function result(){
    const r=db().reports.find(r=>r.id===query.get('id')&&r.kind==='automatico'&&r.studentId===MEF.studentId());
    if(!r){$('#res-greeting').textContent='Nenhum resultado disponível';$('#res-subtitle').textContent='Escolha e conclua uma tarefa para visualizar o resultado.';$('#res-score').textContent='—';$('#res-time').textContent='—';$('#res-xp').textContent='—';$('#btn-retry').disabled=true;return;}
    const first=(r.studentName||'').split(' ')[0];
    $('#res-greeting').textContent=r.score===100?`Desempenho Perfeito, ${first}!`:`Atividade Concluída, ${first}!`;
    $('#res-subtitle').textContent='Atividade: '+r.title;$('#res-score').textContent=r.type==='memoria'?String(r.total):r.score+'%';if(r.type==='memoria')$('#res-score').previousElementSibling.textContent='Tentativas';$('#res-time').textContent=String(Math.floor(r.seconds/60)).padStart(2,'0')+':'+String(r.seconds%60).padStart(2,'0');$('#res-xp').textContent='+'+r.xp+' XP';
    const score=$('#res-score');score.tabIndex=0;score.setAttribute('role','button');score.title=`${r.correct} acertos e ${r.errors} erros. Clique para ver as respostas.`;score.setAttribute('aria-label',score.title);
    const open=()=>ui.dialog('Relatório da Atividade',ui.reportContent(r)+'<button type="button" class="mef-btn" data-close>Fechar</button>');score.onclick=open;score.onkeydown=e=>{if(e.key==='Enter')open();};
    $('#btn-retry').onclick=()=>ui.confirmAction('Praticar Novamente?', 'Uma nova tentativa será salva sem apagar a anterior.',()=>location.href='executar_atividade.html?id='+encodeURIComponent(r.taskId)+'&repetir=1');
  }
  function roster(){
    const turma=localStorage.getItem('turmaSelecionadaNome')||'Turma 2 - Ensino Médio';
    $('#turma-titulo').textContent=turma;$('#turma-professores').textContent=`${localStorage.getItem('turmaSelecionadaProf')||'Prof. Levi Viana'} | ${localStorage.getItem('turmaSelecionadaPsico')||'Psicopedagogo Lucas Bonetti'}`;
    const grid=$('#students-grid'),reportModal=$('#relatorio-modal'),ficha=$('#ficha-modal');
    let current=null;
    function render(){
      const students=db().students.filter(s=>s.turma===turma),q=MEF.norm($('#search-student').value);
      $('#total-alunos-val').textContent=students.length;$('#total-atividades-val').textContent=db().tasks.filter(t=>t.alunos.some(id=>students.some(s=>s.id===id))).length;
      grid.innerHTML=students.filter(s=>MEF.norm(s.nome+' '+s.serie).includes(q)).map(s=>`<div class="student-card"><div class="student-profile-group"><div class="student-avatar">${esc(s.inicial||s.nome[0])}</div><div><h3 class="student-name">${esc(s.nome)}</h3><span class="student-sub">${esc(s.sub||s.serie)}</span></div></div><div class="student-info-pill"><span class="info-label">Hiperfoco</span><span class="info-value">${esc(s.hiperfoco||'Não informado')}</span></div><div class="student-info-pill"><span class="info-label">Tempo Médio de Foco</span><span class="info-value">${esc(s.foco||'Não informado')}</span></div><div class="student-actions-row"><button class="btn-card-action btn-view-report" data-student="${esc(s.id)}" title="Ver Relatório"><i data-lucide="file-text"></i> Relatório</button><button class="btn-card-action btn-view-ficha" data-student="${esc(s.id)}" title="Ver Ficha"><i data-lucide="clipboard-list"></i> Ficha</button><a href="calendario_${suffix}.html" class="btn-card-action" title="Ver Calendário"><i data-lucide="calendar"></i> Calendário</a></div></div>`).join('')||'<div class="empty-state-card"><i data-lucide="users" class="empty-icon"></i><h3>Nenhum aluno vinculado</h3><p>Esta turma ainda não possui alunos cadastrados. Clique em "Vincular Aluno" acima para adicionar o primeiro aluno.</p></div>';
      $$('.btn-view-report').forEach(b=>b.onclick=()=>{
        current=db().students.find(s=>s.id===b.dataset.student);$('#relatorio-aluno-nome').textContent='Relatório: '+current.nome;$('#relatorio-aluno-sub').textContent=current.sub||current.serie;$('#relatorio-hiperfoco').textContent=current.hiperfoco||'Não informado';$('#relatorio-foco').textContent=current.foco||'Não informado';$('#relatorio-obs-view').textContent=current.obs||'Sem observações psicopedagógicas inseridas.';$('#relatorio-obs-edit').classList.add('hidden');$('#relatorio-obs-view').classList.remove('hidden');$('#btn-editar-relatorio').classList.remove('hidden');$('#btn-salvar-relatorio').classList.add('hidden');$('#all-reports').href='relatorios.html?aluno='+encodeURIComponent(current.id);reportModal.classList.add('active');
      });
      $$('.btn-view-ficha').forEach(b=>b.onclick=()=>{current=db().students.find(s=>s.id===b.dataset.student);$('#ficha-aluno-nome').textContent='Ficha: '+current.nome;$('#ficha-aluno-sub').textContent=current.sub||current.serie;$('#ficha-diagnostico').textContent=current.diagnostico||current.sub?.split('•')[1]||'Não informado';$('#ficha-medicamento').textContent=current.medicamento||'Nenhum informado';$('#ficha-acompanhamento').textContent=current.acompanhamento||'Não informado';$('#ficha-contacto').textContent=current.contato||'Não informado';$('#full-profile').href='ficha-aluno.html?id='+encodeURIComponent(current.id);ficha.classList.add('active');});
      window.lucide?.createIcons();
    }
    const all=document.createElement('a');all.id='all-reports';all.className='btn-modal-confirm';all.textContent='Ver Todos os Relatórios';$('.modal-btn-row',reportModal).append(all);
    const full=document.createElement('a');full.id='full-profile';full.className='btn-modal-confirm';full.textContent='Abrir Ficha Completa';$('.modal-btn-row',ficha).append(full);
    const edit=document.createElement('button');edit.className='btn-modal-confirm';edit.type='button';edit.textContent='Editar Dados';edit.id='btn-edit-profile';$('.modal-btn-row',ficha).append(edit);edit.onclick=()=>{ficha.classList.remove('active');ui.editStudent(current,'',render);};
    $('#btn-fechar-relatorio').onclick=()=>reportModal.classList.remove('active');$('#btn-fechar-ficha').onclick=()=>ficha.classList.remove('active');
    $('#btn-editar-relatorio').onclick=()=>{$('#relatorio-obs-edit').value=current.obs||'';$('#relatorio-obs-edit').classList.remove('hidden');$('#relatorio-obs-view').classList.add('hidden');$('#btn-editar-relatorio').classList.add('hidden');$('#btn-salvar-relatorio').classList.remove('hidden');};
    $('#btn-salvar-relatorio').onclick=()=>{try{const text=$('#relatorio-obs-edit').value.trim();if(!text)throw new Error('Preencha as observações.');MEF.transaction(d=>{const s=d.students.find(v=>v.id===current.id);s.obs=text;const now=new Date().toISOString();const r={id:s.manualReportId||MEF.id(),kind:'manual',studentId:s.id,studentName:s.nome,title:'Relatório psicopedagógico',observations:text,strategies:'',nextSteps:'',author:role,finishedAt:now};s.manualReportId=r.id;const idx=d.reports.findIndex(v=>v.id===r.id);if(idx>=0)d.reports[idx]={...r,finishedAt:d.reports[idx].finishedAt,updatedAt:now};else d.reports.push(r);MEF.notify(d,['professor','psicopedagogo'],'Relatório manual salvo',s.nome,'relatorios.html');});$('#relatorio-obs-view').textContent=text;$('#relatorio-obs-view').classList.remove('hidden');$('#relatorio-obs-edit').classList.add('hidden');$('#btn-editar-relatorio').classList.remove('hidden');$('#btn-salvar-relatorio').classList.add('hidden');ui.toast('Relatório salvo.');}catch(e){ui.message(e,$('.vincular-inner-card',reportModal));}};
    const addModal=$('#vincular-aluno-modal');$('#btn-open-vincular-aluno').onclick=()=>addModal.classList.add('active');$('#btn-cancel-vincular').onclick=()=>addModal.classList.remove('active');
    $('#form-vincular-aluno').onsubmit=e=>{e.preventDefault();try{const nome=$('#nome-aluno').value.trim(),serie=$('#serie-aluno').value.trim();if(!nome||!serie)throw new Error('Informe nome e série.');MEF.transaction(d=>d.students.push({id:MEF.id(),nome,serie,sub:serie,turma,diagnostico:$('#diagnostico-aluno').value.trim(),hiperfoco:$('#hiperfoco-aluno').value.trim(),foco:$('#foco-aluno').value.trim(),obs:$('#obs-aluno').value.trim()}));e.target.reset();addModal.classList.remove('active');render();}catch(error){ui.message(error,$('.vincular-inner-card',addModal));}};
    $('#search-student').oninput=render;render();
  }
  function hooks(){
    if(file.startsWith('configuracoes_')){
      const first=$$('.btn-toggle-status')[0],target=MEF.audience();if(first){first.removeAttribute('onclick');const render=()=>{const on=db().preferences[target]!==false;first.textContent=on?'Ativado':'Desativado';first.classList.toggle('disabled',!on);first.setAttribute('aria-pressed',String(on));};render();first.onclick=()=>{try{MEF.transaction(d=>d.preferences[target]=d.preferences[target]===false);render();}catch(e){ui.toast(e.message);}};}
      const heading=$$('.section-title').find(h=>h.textContent==='Notificações');if(heading){heading.innerHTML='<a href="notificacoes.html" class="original-text-link">Notificações</a>';heading.title='Abrir todas as notificações';}

    }
    if(file.startsWith('turmas')){
      for(const turma of new Set(db().students.map(s=>s.turma)))localStorage.setItem('alunos_turma_'+turma.replace(/\s+/g,'_'),JSON.stringify(db().students.filter(s=>s.turma===turma)));
      if(typeof window.renderizarTurmas==='function')window.renderizarTurmas();
    }
    if(file==='dashboard_psico.html')$$('.btn-view-profile').forEach((b,i)=>b.onclick=()=>location.href='relatorios.html?aluno='+encodeURIComponent(db().students[i]?.id||''));
    if(file==='dashboard_prof.html'){
      const metrics=$$('.metric-card');if(metrics[0]){metrics[0].onclick=()=>location.href='alunos.html';metrics[0].style.cursor='pointer';}
      if(metrics[2]){metrics[2].onclick=()=>location.href='relatorios.html';metrics[2].style.cursor='pointer';}
    }
    if(file.startsWith('dashboard_'))$$('.alert-card').forEach(card=>{card.style.cursor='pointer';card.onclick=()=>location.href='notificacoes.html';});
  }
  try{
    const routes={'tarefas_prof.html':taskCards,'tarefas_psico.html':taskCards,'tarefas_aluno.html':studentTasks,'criar-nova-tarefa.html':editor,'editar-atividade.html':editor,'executar_atividade.html':ui.execution,'relatorio_desempenho.html':result,'detalhes-turma.html':roster,'detalhes-turma-psico.html':roster};
    if(routes[file]){common();routes[file]();}
    else hooks();
  }catch(e){alertError(e);}
})();
