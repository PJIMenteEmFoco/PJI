/* Conversas do protótipo: mesmas classes e componentes das telas originais. */
(() => {
'use strict';
// Quando o chat usa o WebSocket/Spring Boot, o controlador de dados demonstrativos deve ceder controle.
if (document.body?.dataset.chatMode === 'backend') return;
const $=(s,r=document)=>r.querySelector(s),$$=(s,r=document)=>[...r.querySelectorAll(s)];
if(!$('#messages-wrapper'))return;
const role=MEF.role(),db=()=>MEF.load(),esc=MEFUI.esc;
const fail=e=>MEFUI.toast(e.message||String(e));
const activate=(el,fn)=>{el.tabIndex=0;el.setAttribute('role','button');el.onclick=fn;el.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();fn();}};};
  function chat(){
    MEF.transaction(d=>{d.messages||=[];d.profiles||={};});const actor=MEF.audience(),input=$('#chat-input'),wrapper=$('#messages-wrapper');let contact=null;
    const contacts=()=>[{id:'professor',name:db().profiles.professor?.name||'Professor',role:'Professor',icon:'user'},{id:'psicopedagogo',name:db().profiles.psicopedagogo?.name||'Psicopedagogo',role:'Psicopedagogo',icon:'user-check'},...db().students.map(s=>({id:'aluno:'+s.id,name:s.nome,role:s.turma||'Aluno',icon:'user'}))].filter(c=>c.id!==actor&&(role!=='aluno'||!c.id.startsWith('aluno:')));
    $('.chat-sidebar-header').insertAdjacentHTML('afterend','<input id="contact-search" class="chat-text-input contact-search" aria-label="Pesquisar contatos" placeholder="Pesquisar contatos...">');
    const attach=document.createElement('input');attach.type='file';attach.hidden=true;attach.accept='image/png,image/jpeg,application/pdf,text/plain';document.body.append(attach);
    input.removeAttribute('onkeypress');input.maxLength=4000;input.onkeydown=e=>{if(e.key==='Enter'&&!e.isComposing){e.preventDefault();send();}};
    const sendButton=$('.btn-send-msg');sendButton.removeAttribute('onclick');sendButton.onclick=()=>send();
    function renderContacts(){
      const q=MEF.norm($('#contact-search').value),data=db(),list=contacts().filter(c=>MEF.norm(c.name+' '+c.role).includes(q));
      $('#contacts-list').innerHTML=list.map(c=>`<div class="contact-item ${contact?.id===c.id?'active':''}" data-contact="${esc(c.id)}"><div class="avatar-box"><i data-lucide="${c.icon}"></i>${data.messages.some(m=>m.from===c.id&&m.to===actor&&!m.read)?'<span class="unread-dot"></span>':''}</div><div class="contact-info"><span class="contact-name">${esc(c.name)}</span><span class="contact-role-badge">${esc(c.role)}</span></div></div>`).join('')||'<p class="chat-empty">Nenhum contato encontrado.</p>';
      $$('[data-contact]').forEach(el=>activate(el,()=>select(contacts().find(c=>c.id===el.dataset.contact))));window.lucide?.createIcons();
    }
    function renderMessages(){
      if(!contact){wrapper.innerHTML='<p class="chat-empty">Escolha um contato para começar.</p>';input.disabled=true;sendButton.disabled=true;return;}
      input.disabled=false;sendButton.disabled=false;$('#active-contact-name').textContent=contact.name;$('#active-avatar-box').innerHTML=`<i data-lucide="${contact.icon}"></i>`;
      const messages=db().messages.filter(m=>(m.from===actor&&m.to===contact.id)||(m.to===actor&&m.from===contact.id));
      wrapper.innerHTML=messages.map(m=>`<div class="message-row ${m.from===actor?'sent':'received'}"><div class="message-bubble"><span>${esc(m.text)}</span>${m.attachment?`<a class="chat-file-link" href="${esc(m.attachment.data)}" download="${esc(m.attachment.name)}">${m.attachment.type.startsWith('image/')?`<img class="chat-attachment" src="${esc(m.attachment.data)}" alt="${esc(m.attachment.name)}">`:''}${esc(m.attachment.name)}</a>`:''}<div class="message-meta">${new Date(m.date).toLocaleTimeString('pt-BR',{hour:'2-digit',minute:'2-digit'})}${m.from===actor?' · '+(m.read?'Lida neste navegador':'Salva neste navegador'):''}</div></div></div>`).join('')||'<p class="chat-empty">Nenhuma mensagem ainda. Escreva para iniciar a conversa.</p>';wrapper.scrollTop=wrapper.scrollHeight;window.lucide?.createIcons();
    }
    function select(c){contact=c;try{MEF.transaction(d=>d.messages.filter(m=>m.to===actor&&m.from===c.id).forEach(m=>{m.read=true;}));renderContacts();renderMessages();}catch(e){fail(e);}}
    function send(attachment=null){try{const text=input.value.trim();if(!contact)throw new Error('Escolha um contato.');if(!text&&!attachment)return;MEF.transaction(d=>d.messages.push({id:MEF.id(),from:actor,to:contact.id,text,date:new Date().toISOString(),read:false,attachment}));input.value='';renderMessages();}catch(e){fail(e);}}
    $('.btn-plus-attach').onclick=()=>{if(!contact){fail('Escolha um contato.');return;}attach.click();};
    attach.onchange=()=>{const f=attach.files[0];if(!f)return;if(f.size>1048576||!['image/png','image/jpeg','application/pdf','text/plain'].includes(f.type)){fail('Escolha uma imagem PNG/JPG, PDF ou TXT de até 1 MB.');attach.value='';return;}const reader=new FileReader();reader.onload=()=>{send({name:f.name,type:f.type,data:reader.result});attach.value='';};reader.onerror=()=>fail('Não foi possível ler o arquivo.');reader.readAsDataURL(f);};
    $('#contact-search').oninput=renderContacts;
    if(contacts().length)select(contacts()[0]);else{renderContacts();renderMessages();}
    window.addEventListener('storage',e=>{if(e.key===MEF.KEY){renderContacts();renderMessages();}});
  }

chat();
})();
