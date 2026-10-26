/* Ponte com Spring Boot para a tela de criação, sem alterar os JS definitivos do ZIP. */
(() => {
  'use strict';
  const $=(selector,root=document)=>root.querySelector(selector);
  const $$=(selector,root=document)=>[...root.querySelectorAll(selector)];
  const ui=window.MEFUI, esc=ui.esc;
  const query=new URLSearchParams(location.search);
  const names={quiz:'Quiz',memoria:'Jogo da Memória',cruzadas:'Palavras Cruzadas',leitura:'Leitura Guiada'};
  const db=()=>MEF.load(); // utilizado somente se o usuário escolher 'Preencher com exemplo'.
  const option=(value,label,selected=false)=>`<option value="${esc(value)}" ${selected?'selected':''}>${esc(label)}</option>`;
  const alertError=(error)=>ui.message(error,$('.create-task-container')||$('main'));
  function common(){
    const sidebar=$('#sidebar'),toggle=$('#toggle-btn');
    toggle?.addEventListener('click',()=>sidebar?.classList.toggle('expanded'));
    $('#btn-open-logout')?.addEventListener('click',event=>{event.preventDefault();$('#logout-modal')?.classList.add('active');});
    $('#btn-cancel-logout')?.addEventListener('click',()=>$('#logout-modal')?.classList.remove('active'));
    $('#btn-confirm-logout')?.addEventListener('click',()=>$('#mef-logout-form')?.requestSubmit());
    $$('.modal-overlay').forEach(modal=>modal.addEventListener('click',event=>{if(event.target===modal)modal.classList.remove('active');}));
    document.addEventListener('keydown',event=>{if(event.key==='Escape')$$('.modal-overlay.active').forEach(modal=>modal.classList.remove('active'));});
    window.lucide?.createIcons();
  }
  function editor(){
    // Na edição de rascunho o GET é carregado do banco antes de montar os controles.
    let task=null;
    const aliases={'hq':'leitura','leitura-guiada':'leitura','caca-palavras':'cruzadas'};
    const type=aliases[query.get('tipo')]||query.get('tipo');
    let model=task?structuredClone(task):{id:MEF.id(),titulo:'',instrucoes:'',prazo:$('#task-deadline').value,tipo:names[type]?type:'quiz',alunos:$$('.chk-student:checked').map(input=>input.value),perguntas:[{pergunta:'',opcoes:['',''],correta:0}],pares:[{termo:'',significado:''},{termo:'',significado:''}],palavras:[{palavra:'',dica:''},{palavra:'',dica:''}],trechos:['']};
    model.perguntas||=[{pergunta:'',opcoes:['',''],correta:0}];model.pares||=[{termo:'',significado:''},{termo:'',significado:''}];model.palavras||=[{palavra:'',dica:''},{palavra:'',dica:''}];model.trechos||=[''];
    $('#task-title').value=model.titulo;$('#task-instructions').value=model.instrucoes;$('#task-deadline').value=model.prazo||'';
    if(task){$('.page-title').textContent='Editar Tarefa';$('#btn-submit-task').textContent='Salvar Alterações';}
    const roster=$('.students-list-box'); // Alunos chegam do Thymeleaf; não substituir por dados do localStorage.
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
    async function save(status){
      const saveBtn=$('#btn-submit-task'),draftBtn=$('#btn-save-draft');
      try{
        capture();
        if(!model.titulo)throw new Error('Preencha o título da tarefa.');
        if(status==='Ativa'){
          if(!model.alunos.length)throw new Error('Selecione pelo menos um aluno.');
          if(!model.prazo)throw new Error('Informe o prazo.');
          ui.validateContent(model);
        }
        const url=document.body.dataset.taskSaveUrl;
        if(!url)throw new Error('O Controller ainda não informou o endpoint para salvar atividades (salvarAtividadeUrl). A tarefa não foi salva.');
        // O ID da atividade é atribuído pelo backend; não envie o identificador demonstrativo.
        const {id, pdf, ...dados}=model;
        const payload={...dados,status};
        const headers={'Content-Type':'application/json','Accept':'application/json'};
        const csrf=document.querySelector('meta[name="_csrf"]')?.content;
        const headerName=document.querySelector('meta[name="_csrf_header"]')?.content;
        if(csrf && headerName)headers[headerName]=csrf;
        saveBtn.disabled=true;draftBtn.disabled=true;
        const response=await fetch(url,{method:loadUrl?'PUT':'POST',credentials:'same-origin',headers,body:JSON.stringify(payload)});
        if(!response.ok){
          let reason='';
          try{const data=await response.json();reason=data.mensagem||data.erro||data.message||'';}catch{}
          throw new Error(reason||`Não foi possível salvar a tarefa (HTTP ${response.status}).`);
        }
        const salvo=await response.json().catch(()=>({}));
        if(model.tipo==='leitura' && MEFDocuments.valid(pdf)){
          const identificador=salvo.id||loadUrl?.match(/\/tarefas\/(\d+)/)?.[1];
          if(!identificador)throw new Error('Rascunho registrado, mas não foi possível identificar a atividade para anexar o PDF.');
          const data64=localStorage.getItem(pdf.key)||pdf.data;
          const bytes=Uint8Array.from(atob(data64.split(',')[1]),c=>c.charCodeAt(0));
          const form=new FormData();form.append('arquivo',new Blob([bytes],{type:'application/pdf'}),pdf.name);
          const uploadHeaders={};if(csrf && headerName)uploadHeaders[headerName]=csrf;
          const pdfPattern=document.body.dataset.taskPdfUrlPattern||'/professor/tarefas/{id}/pdf';
          const uploadUrl=pdfPattern.replace('{id}',encodeURIComponent(identificador));
          const upload=await fetch(uploadUrl,{method:'POST',credentials:'same-origin',headers:uploadHeaders,body:form});
          if(!upload.ok){const info=await upload.json().catch(()=>({}));
            throw new Error(`O rascunho foi salvo (#${identificador}), mas o PDF não foi anexado: ${info.mensagem||upload.status}. Reabra o rascunho para reenviar o PDF.`);}
        }
        window.location.assign(document.body.dataset.taskReturnUrl||'/professor/tarefas');
      }catch(error){alertError(error);}
      finally{saveBtn.disabled=false;draftBtn.disabled=false;}
    }
    $('#btn-save-draft').onclick=()=>save('Rascunho');$('#btn-submit-task').onclick=()=>save('Ativa');
    render();
    const loadUrl=document.body.dataset.taskLoadUrl;
    if(loadUrl){
      $('#btn-save-draft').disabled=true;
      $('#btn-submit-task').disabled=true;
      (async()=>{
        try{
          const res=await fetch(loadUrl,{credentials:'same-origin',headers:{'Accept':'application/json'}});
          if(!res.ok)throw new Error(`Rascunho indisponível (HTTP ${res.status}).`);
          const data=await res.json();
          model={...model,...data,perguntas:data.perguntas?.length?data.perguntas:[{pergunta:'',opcoes:['',''],correta:0}],
            pares:data.pares?.length?data.pares:[{termo:'',significado:''},{termo:'',significado:''}],
            palavras:data.palavras?.length?data.palavras:[{palavra:'',dica:''},{palavra:'',dica:''}],trechos:data.trechos||[]};
          $('#task-title').value=model.titulo||'';
          $('#task-instructions').value=model.instrucoes||'';
          $('#task-deadline').value=model.prazo||'';
          const selected=new Set((model.alunos||[]).map(String));
          $$('.chk-student').forEach(input=>input.checked=selected.has(input.value));
          $('.page-title').textContent='Editar Rascunho';
          $('#btn-submit-task').textContent='Publicar Atividade';
          render();
          $('#btn-save-draft').disabled=false;
          $('#btn-submit-task').disabled=false;
        }catch(e){alertError(e);$('#btn-save-draft').disabled=true;$('#btn-submit-task').disabled=true;}
      })();
    }
  }
  try{common();editor();}catch(error){alertError(error);}
})();
