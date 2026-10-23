/* Lista atividades reais com o visual original; sem demonstrações do localStorage. */
(() => {
  'use strict';
  const rows=document.querySelectorAll('.task-section .cards-grid-row');
  const delivered=document.querySelector('#delivered-grid');
  if(rows.length<2||!delivered)return;
  const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const tipo={QUIZ:'Quiz',JOGO_DA_MEMORIA:'Jogo da Memória',CRUZADINHA:'Palavras Cruzadas',LEITURA_GUIADA:'Leitura Guiada'};
  const fmt=d=>{if(!d)return 'Sem prazo';const parts=d.split('-');return `${parts[2]}/${parts[1]}/${parts[0]}`;};
  function card(t,s){
    const status=s==='concluida'?'delivered':s==='atrasada'?'overdue':'pending';
    const header=s==='concluida'?'CONCLUÍDA':s==='atrasada'?'ATRASADA':'ENTREGA';
    const href=s==='concluida'?'':` href="/aluno/tarefas/${encodeURIComponent(t.id)}/executar"`;
    const tag=s==='concluida'?'div':'a';
    return `<${tag}${href} class="task-card-full ${status}" data-title="${esc(t.titulo)}" data-tag="${esc(tipo[t.tipo]||t.tipo)}"><div class="card-header-bar"><span>${header}</span><span>${esc(fmt(t.prazo))}</span></div><div class="card-body-content"><h4 class="task-title">${esc(t.titulo)}</h4><span class="task-tag">${esc(tipo[t.tipo]||t.tipo)}</span></div></${tag}>`;
  }
  fetch('/aluno/tarefas/dados',{credentials:'same-origin',headers:{'Accept':'application/json'}})
    .then(r=>{if(!r.ok)throw Error(`HTTP ${r.status}`);return r.json();})
    .then(tasks=>{
      const by=s=>tasks.filter(t=>t.situacao===s).map(t=>card(t,s)).join('')||'<p>Nenhuma atividade nesta categoria.</p>';
      rows[0].innerHTML=by('pendente');rows[1].innerHTML=by('atrasada');delivered.innerHTML=by('concluida');
    })
    .catch(()=>{rows[0].innerHTML='<p>Não foi possível carregar suas atividades. Atualize a página.</p>';rows[1].innerHTML='';delivered.innerHTML='';});
})();
