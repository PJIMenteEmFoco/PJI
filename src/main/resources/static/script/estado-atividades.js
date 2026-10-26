/* Estado de atrasadas a partir das atividades realmente atribuídas ao aluno. */
(() => {
  'use strict';
  const dateKey=(date=new Date())=>`${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`;
  function empty(hasTasks=true){
    return `<div class="mef-overdue-empty" role="status"><span class="mef-overdue-empty-icon" aria-hidden="true"><i data-lucide="circle-check"></i></span><div><h3>Tudo em dia!</h3><p>${hasTasks?'Você não tem atividades atrasadas. Continue acompanhando os prazos das próximas entregas.':'Você não tem atividades atrasadas. As atividades que passarem do prazo aparecerão aqui.'}</p></div></div>`;
  }
  const file=location.pathname.split('/').at(-1);
  function update(){
    if(file!=='dashboard_aluno.html')return;
    const grids=[...document.querySelectorAll('.task-grid-4')];if(grids.length<2)return;
    const data=MEF.load(),sid=MEF.studentId(),today=dateKey();
    const tasks=data.tasks.filter(task=>MEF.assigned(task,sid)&&!MEF.attempts(task.id,sid).length).sort((a,b)=>(a.prazo||'9999').localeCompare(b.prazo||'9999'));
    const names={quiz:'Quiz',memoria:'Jogo da Memória',cruzadas:'Palavras Cruzadas',leitura:'Leitura Guiada'},esc=MEFUI.esc;
    grids.forEach((grid,index)=>{
      const list=tasks.filter(task=>index?task.prazo&&task.prazo<today:!task.prazo||task.prazo>=today);
      grid.innerHTML=list.slice(0,4).map(task=>`<a href="executar_atividade.html?id=${encodeURIComponent(task.id)}" class="student-task-card" data-task-id="${esc(task.id)}"><div class="task-card-header ${index?'urgent':''}"><span>${index?'ATRASADA':'ENTREGA'}</span><span>${task.prazo?new Date(task.prazo+'T12:00:00').toLocaleDateString('pt-BR').slice(0,5):'Sem prazo'}</span></div><div class="task-card-body">${MEFActivityArt.image(task.tipo)}<h4 class="task-title">${esc(task.titulo)}</h4><span class="task-tag">${esc(names[task.tipo]||task.tipo)}</span></div></a>`).join('')||(index?empty(tasks.length>0):'<p class="original-empty">Nenhuma atividade pendente no momento.</p>');
    });
    const greeting=document.querySelector('.greeting-title'),student=data.students.find(s=>s.id===sid);
    if(greeting&&student)greeting.textContent='Bem vindo(a), '+student.nome.split(' ')[0]+'!';
    window.lucide?.createIcons();
  }
  window.MEFTaskState={dateKey,empty};
  if(file==='dashboard_aluno.html'){update();window.addEventListener('storage',e=>{if(e.key===MEF.KEY||e.key==='mef_aluno_atual')update();});window.addEventListener('pageshow',update);}
})();
