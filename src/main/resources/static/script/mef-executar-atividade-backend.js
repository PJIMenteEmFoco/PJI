/* Executa atividades reais vindas do Spring Boot, mantendo a estrutura visual do HTML original. */
(() => {
  'use strict';
  const $ = (s, root=document) => root.querySelector(s);
  const $$ = (s, root=document) => [...root.querySelectorAll(s)];
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const board = $('#quiz-dynamic-container'), submit = $('#btn-finish-quiz');
  if (!board || !submit) return;
  const id = location.pathname.match(/\/aluno\/tarefas\/(\d+)\/executar\/?$/)?.[1];
  const error = (msg) => { const el=$('#warning-modal-text'); if(el) el.textContent=msg; $('#warning-modal')?.classList.add('active'); };
  $('#btn-warning-ok')?.addEventListener('click',()=>$('#warning-modal')?.classList.remove('active'));
  $('#btn-open-logout')?.addEventListener('click',()=>$('#logout-modal')?.classList.add('active'));
  $('#btn-cancel-logout')?.addEventListener('click',()=>$('#logout-modal')?.classList.remove('active'));
  $('#btn-confirm-logout')?.addEventListener('click',()=>$('#mef-logout-form')?.requestSubmit());
  if (!id) { board.textContent='Endereço da atividade inválido.';submit.disabled=true;return; }
  const base=`/aluno/tarefas/${id}`;
  let data, started=Date.now(), matched=new Set(), memoryCards=[], crossCells=new Map(), ready=false;
  const timer=setInterval(()=>{if(!ready)return;const s=Math.max(0,Math.floor((Date.now()-started)/1000));
    $('#timer-display').textContent=String(Math.floor(s/60)).padStart(2,'0')+':'+String(s%60).padStart(2,'0');},1000);
  window.addEventListener('pagehide',()=>clearInterval(timer),{once:true});
  function quiz() {
    board.innerHTML=data.perguntas.map((q,i)=>`<div class="quiz-question-card"><div class="question-title"><span class="question-number">${i+1}</span><h3>${esc(q.pergunta)}</h3></div><div class="options-group">${q.opcoes.map((o,j)=>`<label class="quiz-option"><input type="radio" name="r${i}" value="${j}"><span class="option-custom"></span><span class="option-text">${esc(o)}</span></label>`).join('')}</div></div>`).join('');
  }
  function memoria() {
    memoryCards=[];
    for(const pair of data.pares) { memoryCards.push({id:pair.id,face:'A',label:pair.termo}); memoryCards.push({id:pair.id,face:'B',label:pair.significado}); }
    for(let i=memoryCards.length-1;i>0;i--){const j=Math.floor(Math.random()*(i+1));[memoryCards[i],memoryCards[j]]=[memoryCards[j],memoryCards[i]];}
    board.innerHTML=`<p class="mef-play-help">Encontre os pares de conceito e significado.</p><div class="mef-memory-grid">${memoryCards.map((c,i)=>`<button type="button" class="mef-flip" data-index="${i}" aria-label="Virar carta ${i+1}">?</button>`).join('')}</div><p id="mef-memory-count">0 de ${data.pares.length} pares encontrados</p>`;
    let open=[],locked=false;
    $$('.mef-flip',board).forEach(btn=>btn.onclick=()=>{
      if(locked||btn.disabled||open.some(o=>o.btn===btn))return;
      const card=memoryCards[Number(btn.dataset.index)];btn.textContent=card.label;btn.classList.add('opened');open.push({card,btn});
      if(open.length===2){locked=true;const[a,b]=open;
        if(a.card.id===b.card.id&&a.card.face!==b.card.face){
          matched.add(a.card.id);a.btn.disabled=true;b.btn.disabled=true;a.btn.classList.add('matched');b.btn.classList.add('matched');
          $('#mef-memory-count').textContent=`${matched.size} de ${data.pares.length} pares encontrados`;
          open=[];locked=false;
        }else setTimeout(()=>{a.btn.textContent='?';b.btn.textContent='?';a.btn.classList.remove('opened');b.btn.classList.remove('opened');open=[];locked=false;},850);
      }
    });
  }
  function cruzadas() {
    const {palavras,linhas,colunas}=data;
    if(linhas<1||colunas<1||linhas>100||colunas>100)throw Error('Dimensões da cruzadinha inválidas.');
    const positions = new Map();
    for(const p of palavras)for(let i=0;i<p.tamanho;i++){
      const r=p.linha+(p.direcao==='VERTICAL'?i:0),c=p.coluna+(p.direcao==='HORIZONTAL'?i:0);
      if(r<0||r>=linhas||c<0||c>=colunas)throw Error('Palavra fora da grade.');
      positions.set(`${r},${c}`,true);
    }
    let cells='';
    for(let r=0;r<linhas;r++)for(let c=0;c<colunas;c++){
      const key=`${r},${c}`;
      cells+=`<div class="mef-word-cell">${positions.has(key)?`<input data-cell="${key}" maxlength="1" autocomplete="off" spellcheck="false" aria-label="Linha ${r+1}, coluna ${c+1}">`:''}</div>`;
    }
    board.innerHTML=`<p class="mef-play-help">Preencha os quadrinhos usando as dicas abaixo.</p><div class="mef-cross-scroll"><div class="mef-word-grid" style="grid-template-columns:repeat(${colunas},36px)">${cells}</div></div><div class="mef-clues">${palavras.map((p,i)=>`<p><strong>${i+1}. ${p.direcao==='VERTICAL'?'Vertical':'Horizontal'}</strong> — ${esc(p.dica)} (${p.tamanho} letras)</p>`).join('')}</div>`;
    crossCells=new Map($$('[data-cell]',board).map(el=>[el.dataset.cell,el]));
    $$('[data-cell]',board).forEach(el=>el.addEventListener('input',()=>{el.value=el.value.normalize('NFD').replace(/[\u0300-\u036f]/g,'').replace(/[^a-zA-Z]/g,'').toUpperCase().slice(0,1);}));
  }
  async function finish() {
    if(!ready||submit.disabled)return;
    const payload={alternativas:[],palavras:[],paresEncontrados:[],segundos:Math.floor((Date.now()-started)/1000)};
    try {
      if(data.tipo==='quiz'){
        payload.alternativas=data.perguntas.map((_,i)=>{
          const checked=$(`input[name="r${i}"]:checked`,board);if(!checked)throw Error(`Responda a pergunta ${i+1}.`);return Number(checked.value);
        });
      }else if(data.tipo==='memoria'){
        if(matched.size!==data.pares.length)throw Error('Encontre todos os pares antes de concluir.');
        payload.paresEncontrados=[...matched];
      }else if(data.tipo==='cruzadas'){
        payload.palavras=data.palavras.map(p=>{
          let texto='';for(let i=0;i<p.tamanho;i++){
            const r=p.linha+(p.direcao==='VERTICAL'?i:0),c=p.coluna+(p.direcao==='HORIZONTAL'?i:0);
            texto+=crossCells.get(`${r},${c}`)?.value||'';
          }
          if(texto.length!==p.tamanho)throw Error('Preencha todos os quadrinhos.');
          return {id:p.id,texto};
        });
      }
      submit.disabled=true;
      const headers={'Content-Type':'application/json','Accept':'application/json'};
      const csrf=$('meta[name="_csrf"]')?.content;
      const csrfHeader=$('meta[name="_csrf_header"]')?.content;
      if(csrf&&csrfHeader)headers[csrfHeader]=csrf;
      const response=await fetch(`${base}/responder`,{method:'POST',headers,body:JSON.stringify(payload),credentials:'same-origin'});
      const result=await response.json().catch(()=>({}));
      if(!response.ok)throw Error(result.mensagem||`Falha ao registrar conclusão: ${response.status}`);
      clearInterval(timer);ready=false;
      const banner=document.createElement('div');banner.className='mef-play-result';
      banner.textContent=`Atividade concluída! Você acertou ${result.acertos} de ${result.total} (${result.percentual}%). Resultado registrado.`;
      board.prepend(banner); submit.textContent='Concluída';
    }catch(e){submit.disabled=false;error(e.message);}
  }
  submit.addEventListener('click',finish);
  (async()=>{
    try {
      submit.disabled=true;
      const response=await fetch(`${base}/dados`,{credentials:'same-origin',headers:{'Accept':'application/json'}});
      if(!response.ok)throw Error('Você não tem acesso a esta atividade, ou ela ainda não foi publicada.');
      data=await response.json();
      $('#task-display-title').textContent=data.titulo;
      $('#task-display-instructions').textContent=data.instrucoes||'Faça a atividade no seu ritmo.';
      if(data.tipo==='quiz')quiz();
      else if(data.tipo==='memoria')memoria();
      else if(data.tipo==='cruzadas')cruzadas();
      else throw Error('Este tipo de atividade ainda está em preparação.');
      ready=true;started=Date.now();submit.disabled=false;
      window.lucide?.createIcons();
    }catch(e){board.textContent=e.message;submit.disabled=true;clearInterval(timer);}
  })();
})();
