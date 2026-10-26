/* Ajustes solicitados na revisão do front-end, sem substituir as telas originais. */
(() => {
  'use strict';
  const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];
  const file=location.pathname.split('/').at(-1),base=new URL('../',document.currentScript.src);
  const role=window.MEF?.role(),suffix={professor:'prof',psicopedagogo:'psico',aluno:'aluno'}[role];
  document.body.dataset.appRole=role||'public';
  const names={quiz:'Quiz',memoria:'Jogo da Memória',cruzadas:'Palavras Cruzadas',leitura:'Leitura Guiada'};
  const esc=value=>String(value??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  function activate(element,handler){if(!element)return;element.tabIndex=0;if(element.tagName!=='BUTTON')element.setAttribute('role','button');element.onclick=handler;element.onkeydown=event=>{if(event.key==='Enter'||event.key===' '){event.preventDefault();handler();}};}
  // Voltar pertence às telas secundárias; os destinos do menu já têm navegação própria.
  const parentPage={
    'criar-nova-tarefa.html':'tarefas_'+suffix+'.html',
    'editar-atividade.html':'tarefas_'+suffix+'.html',
    'executar_atividade.html':'tarefas_aluno.html',
    'fazer_tarefa.html':'tarefas_aluno.html',
    'relatorio_desempenho.html':'tarefas_aluno.html',
    'detalhes-turma.html':'turmas-prof.html',
    'detalhes-turma-psico.html':'turmas_psico.html',
    'alunos.html':role==='professor'?'turmas-prof.html':'turmas_psico.html',
    'ficha-aluno.html':'alunos.html',
    'relatorios.html':'alunos.html',
    'escrever-relatorio.html':'relatorios.html',
    'notificacoes.html':'configuracoes_'+suffix+'.html',
    'cadastro.html':'login.html',
    'email-verificacao.html':'login.html',
    'codigo-verificacao.html':'email-verificacao.html',
    'nova-senha.html':'codigo-verificacao.html',
    'LEIA-ME.html':'index.html',
    '404.html':'index.html'
  }[file];
  // O histórico é limitado aos endereços da própria aplicação.
  const inApp=value=>{try{const url=new URL(value,location.href);return url.origin===base.origin&&url.pathname.startsWith(base.pathname);}catch{return false;}};
  function previous(){
    if(history.length>1&&document.referrer&&inApp(document.referrer)){history.back();return;}
    let paths=[];try{paths=JSON.parse(sessionStorage.getItem('mef-navigation')||'[]');}catch{}
    const target=[...paths].reverse().find(path=>path!==location.href&&inApp(path));
    location.href=target||new URL(parentPage||'index.html',location.href).href;
  }
  try{let paths=JSON.parse(sessionStorage.getItem('mef-navigation')||'[]');if(paths.at(-1)!==location.href)paths.push(location.href);sessionStorage.setItem('mef-navigation',JSON.stringify(paths.slice(-30)));}catch{}
  const existing=$('.btn-back,.btn-back-link,.btn-error-primary')||$$('main a,main button').find(element=>/^Voltar(?:\s|$)/.test(element.textContent.trim()));
  if(parentPage&&existing){existing.dataset.reviewBack='true';if(!existing.matches('a[href]'))existing.onclick=event=>{event.preventDefault();previous();};}
  else if(parentPage){
    const host=$('main')||$('.chat-sidebar')||$('.error-container')||document.body;if(host){const button=document.createElement('button');button.type='button';button.className='review-back';button.dataset.reviewBack='true';button.innerHTML='<i data-lucide="arrow-left"></i><span>Voltar</span>';button.onclick=previous;host.prepend(button);}
  }
  const sidebar=$('#sidebar'),toggle=$('#toggle-btn');
  if(sidebar&&toggle){
    function arrow(){const expanded=sidebar.classList.contains('expanded');toggle.innerHTML=`<i data-lucide="chevron-${expanded?'left':'right'}"></i>`;toggle.setAttribute('aria-expanded',String(expanded));toggle.setAttribute('aria-label',expanded?'Recolher menu':'Expandir menu');toggle.title=expanded?'Recolher menu':'Expandir menu';if(role==='psicopedagogo'&&matchMedia('(min-width:651px)').matches)try{sessionStorage.setItem('mef-menu:psicopedagogo',expanded?'expanded':'collapsed');}catch{}window.lucide?.createIcons();}
    toggle.setAttribute('role','button');toggle.tabIndex=0;toggle.setAttribute('aria-controls','sidebar');toggle.addEventListener('keydown',event=>{if(event.key==='Enter'||event.key===' '){event.preventDefault();toggle.click();}});
    new MutationObserver(arrow).observe(sidebar,{attributes:true,attributeFilter:['class']});arrow();
  }
  // Estado do botão de visualizar senha acompanha o campo, inclusive após voltar.
  for(const button of $$('.password-toggle')){
    const field=$('input',button.closest('.input-wrapper'));if(!field)continue;
    const sync=()=>{const shown=field.type==='text';button.classList.toggle('password-is-visible',shown);button.setAttribute('aria-pressed',String(shown));button.setAttribute('aria-label',shown?'Ocultar senha':'Mostrar senha');};
    new MutationObserver(sync).observe(field,{attributes:true,attributeFilter:['type']});sync();
  }
  // Mesmo acesso às notificações em todas as telas do psicopedagogo.
  if(role==='psicopedagogo'&&sidebar&&!$('.sidebar-nav a[href*="notificacoes"]')){
    if(file==='notificacoes.html')$$('.sidebar-nav .active').forEach(link=>link.classList.remove('active'));
    const link=document.createElement('a');link.href='notificacoes.html';link.className='nav-link'+(file==='notificacoes.html'?' active':'');link.title='Notificações';link.innerHTML='<i data-lucide="bell"></i><span class="nav-title">Notificações</span>';$('.sidebar-nav').append(link);
  }
  if(file==='dashboard_psico.html'){
    const students=MEF.load().students;
    $('.students-list').innerHTML=students.map(student=>`<div class="student-item-card"><div class="student-details"><div class="student-name">${esc(student.nome)}</div><div class="student-meta">${esc(student.turma)}</div></div><a class="btn-view-profile" href="relatorios.html?aluno=${encodeURIComponent(student.id)}">Ver Relatório</a></div>`).join('')||'<p class="original-empty">Nenhum aluno vinculado.</p>';
  }
  if(file==='dashboard_aluno.html'){
    const state=MEFProgress.forStudent(),streak=$('.streak-card');
    $('.streak-count').textContent=state.streak+' '+(state.streak===1?'Dia':'Dias');$('.streak-label').textContent=state.streak?'Ofensiva ativa!':'Vamos começar?';
    activate(streak,()=>{
      const week=Array.from({length:7},(_,i)=>{const day=new Date();day.setDate(day.getDate()-(6-i));const complete=state.days.has(MEFProgress.dateKey(day));return `<div class="streak-day ${complete?'done':''}" title="${day.toLocaleDateString('pt-BR')}${complete?' — atividade concluída':''}"><span>${day.toLocaleDateString('pt-BR',{weekday:'short'}).slice(0,3)}</span><strong>${day.getDate()}</strong>${complete?'<span aria-label="Concluído">✓</span>':'<span aria-label="Sem conclusão">—</span>'}</div>`;}).join('');
      MEFUI.dialog('Minha ofensiva',`<p>Você tem <strong>${state.streak} ${state.streak===1?'dia':'dias'}</strong> com atividades concluídas em sequência.</p><div class="streak-week">${week}</div><p>Conclua uma atividade por dia para manter sua sequência. Cada passo conta.</p><div class="mef-actions"><a class="mef-btn primary" href="tarefas_aluno.html">Ver atividades</a><button class="mef-btn" type="button" data-close>Fechar</button></div>`);
    });streak.title='Ver minha ofensiva';
    const group=document.createElement('div');group.className='student-progress-summary';streak.before(group);group.append(streak);
    const xp=document.createElement('button');xp.type='button';xp.id='student-xp';xp.className='streak-card xp-card';xp.innerHTML=`<span class="streak-icon-box"><i data-lucide="award"></i></span><span class="streak-info"><span class="streak-count">Nível ${state.level}</span><span class="streak-label">${state.xp} XP</span></span>`;group.append(xp);
    xp.onclick=()=>MEFUI.dialog('Meu progresso',`<div class="mef-stats"><div class="mef-stat">Nível<strong>${state.level}</strong></div><div class="mef-stat">XP acumulado<strong>${state.xp} XP</strong></div></div><label for="level-progress">${state.current} de ${MEFProgress.LEVEL_XP} XP neste nível</label><progress class="mef-progress" id="level-progress" value="${state.current}" max="${MEFProgress.LEVEL_XP}"></progress><p>Faltam ${state.remaining} XP para o nível ${state.level+1}. O XP é obtido ao concluir atividades.</p><div class="mef-actions"><a class="mef-btn primary" href="tarefas_aluno.html">Ver atividades</a><button class="mef-btn" type="button" data-close>Fechar</button></div>`);
    const monday=new Date();monday.setHours(0,0,0,0);monday.setDate(monday.getDate()-(monday.getDay()+6)%7);const end=new Date(monday);end.setDate(end.getDate()+7);
    const done=new Set(state.reports.filter(report=>new Date(report.finishedAt)>=monday&&new Date(report.finishedAt)<end).map(report=>report.taskId)).size;
    $('.goal-card').style.setProperty('--goal-progress',Math.min(100,done/10*100)+'%');
    $('.goal-count').textContent=done+'/10 Feitas';$('.progress-bar-fill').style.width=Math.min(100,done/10*100)+'%';$('.goal-subtext').textContent=done>=10?'Meta da semana alcançada. Continue no seu ritmo!':`Complete mais ${10-done} atividades para alcançar a meta da semana.`;
  }
  if(file==='dashboard_prof.html'){
    const data=MEF.load(),grids=$$('.cards-grid-3'),tasks=[...data.tasks].reverse().slice(0,2);
    $('.greeting-title').textContent='Bem vindo(a), '+(data.profiles?.professor?.name||'Professor').split(' ')[0]+'!';
    grids[0].classList.add('dashboard-activities');grids[0].innerHTML='<a href="criar-nova-tarefa.html" class="dashboard-create-card"><i data-lucide="plus-circle"></i><strong>Criar atividade</strong></a>'+tasks.map(task=>`<a href="editar-atividade.html?id=${encodeURIComponent(task.id)}" class="task-card dashboard-activity-card"><div class="task-card-header"><span class="task-type-tag">${esc(names[task.tipo])}</span><span class="task-status-dot ${task.status==='Rascunho'?'pending':'active'}" title="${esc(task.status)}"></span></div>${MEFActivityArt.image(task.tipo)}<h3 class="task-card-title">${esc(task.titulo)}</h3><div class="task-card-meta"><p>${task.alunos.length} aluno(s)</p><p>Prazo: ${task.prazo?new Date(task.prazo+'T12:00:00').toLocaleDateString('pt-BR'):'Sem prazo'}</p></div></a>`).join('');
    const classes=[...new Set(data.students.map(student=>student.turma))];grids[1].innerHTML=classes.slice(0,3).map(name=>`<a href="detalhes-turma.html" class="dashboard-class-card" data-class-name="${esc(name)}"><i data-lucide="users"></i><strong>${esc(name)}</strong><span>${data.students.filter(student=>student.turma===name).length} alunos</span></a>`).join('');
    $$('[data-class-name]').forEach(card=>card.onclick=()=>localStorage.setItem('turmaSelecionadaNome',card.dataset.className));
    const numbers=$$('.metric-value');numbers[0].textContent=data.students.length;numbers[1].textContent=data.tasks.filter(task=>task.status==='Ativa').length;numbers[2].textContent=data.reports.length;$$('.metric-label')[1].textContent='Atividades publicadas';
    const total=data.tasks.filter(task=>task.status==='Ativa').reduce((sum,task)=>sum+task.alunos.length,0),finished=data.tasks.filter(task=>task.status==='Ativa').reduce((sum,task)=>sum+new Set(data.reports.filter(report=>report.taskId===task.id&&report.kind==='automatico'&&task.alunos.includes(report.studentId)).map(report=>report.studentId)).size,0);$$('.summary-count').forEach((element,i)=>element.textContent=[finished,0,total-finished][i]);
    $$('.summary-label span')[1].textContent='Em andamento:';
  }
  window.lucide?.createIcons();
})();
