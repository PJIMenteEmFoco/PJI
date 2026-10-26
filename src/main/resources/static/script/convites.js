/* Interface de convites. O envio autenticado e a validação serão feitos pela API. */
(() => {
  'use strict';
  if(!window.MEFUI||!window.MEF)return;
  const $=(s,r=document)=>r.querySelector(s),file=location.pathname.split('/').at(-1),esc=MEFUI.esc;
  const base=new URL('../',document.currentScript.src);
  function open(className){
    if(!className){MEFUI.toast('Escolha uma turma para criar o convite.');return;}
    MEFUI.dialog('Convidar para a turma',`<p>${esc(className)}</p><label class="mef-field" for="invite-role">Quem deseja convidar?<select id="invite-role"><option value="aluno">Aluno</option><option value="psicopedagogo">Psicopedagogo</option></select></label><label class="mef-field" for="invite-link">Link do convite<input id="invite-link" readonly></label><div class="mef-actions"><button class="mef-btn primary" type="button" id="copy-invite">Copiar link</button><button class="mef-btn" type="button" data-close>Fechar</button></div><label class="mef-field invite-email" for="invite-email">E-mail do destinatário<input id="invite-email" type="email" placeholder="nome@exemplo.com"></label><a class="mef-btn" id="email-invite">Abrir e-mail com o convite</a><p class="attachment-help">O botão abre seu aplicativo de e-mail com a mensagem pronta para você enviar.</p>`,modal=>{
      let invitation;
      function generate(){
        const role=$('#invite-role',modal).value;
        invitation={id:MEF.id(),turma:className,role,createdAt:new Date().toISOString(),author:MEF.role()};
        MEF.transaction(d=>{d.invitations||=[];d.invitations.push(invitation);});
        const url=new URL('convite.html',base);url.searchParams.set('codigo',invitation.id);url.searchParams.set('turma',className);url.searchParams.set('perfil',role);
        $('#invite-link',modal).value=url.href;emailLink();
      }
      function emailLink(){
        const email=$('#invite-email',modal).value.trim(),subject='Convite para '+className,body=`Olá!\n\nVocê recebeu um convite para participar de ${className} como ${$('#invite-role',modal).value==='aluno'?'aluno':'psicopedagogo'}.\n\nAcesse: ${$('#invite-link',modal).value}\n\nMente em Foco`;
        $('#email-invite',modal).href='mailto:'+encodeURIComponent(email)+'?subject='+encodeURIComponent(subject)+'&body='+encodeURIComponent(body);
      }
      $('#invite-role',modal).onchange=()=>{try{generate();}catch(e){MEFUI.toast('Não foi possível criar o convite.');}};
      $('#invite-email',modal).oninput=emailLink;
      $('#email-invite',modal).onclick=event=>{const input=$('#invite-email',modal);if(input.value&&!input.checkValidity()){event.preventDefault();input.reportValidity();}};
      $('#copy-invite',modal).onclick=async()=>{
        const input=$('#invite-link',modal);try{await navigator.clipboard.writeText(input.value);MEFUI.toast('Link copiado.');}
        catch {input.focus();input.select();let copied=false;try{copied=!!document.execCommand?.('copy');}catch{}MEFUI.toast(copied?'Link copiado.':'Selecione o link e use Ctrl+C para copiar.');}
      };
      try{generate();}catch(e){MEFUI.message(e,modal);}
    });
  }
  const roster=$('#btn-open-vincular-aluno');
  if(roster){const button=document.createElement('button');button.type='button';button.className='btn-vincular-aluno invite-open';button.id='open-class-invite';button.innerHTML='<i data-lucide="user-plus"></i> Convidar participantes';roster.after(button);button.onclick=()=>open($('#turma-titulo')?.textContent||localStorage.getItem('turmaSelecionadaNome'));}
  if(file.startsWith('turmas')){
    const grid=$('#turmas-grid');
    function mount(){for(const card of grid?.querySelectorAll('.turma-card')||[]){if($('.invite-card-button',card))continue;const button=document.createElement('button');button.type='button';button.className='mef-btn invite-card-button';button.textContent='Convidar participantes';button.onclick=event=>{event.stopPropagation();open($('.turma-card-title',card).textContent);};card.append(button);}}
    if(grid){new MutationObserver(mount).observe(grid,{childList:true});mount();}
  }
  if(file==='convite.html'){
    const params=new URLSearchParams(location.search),code=params.get('codigo'),className=params.get('turma'),role=params.get('perfil');
    const stored=MEF.load().invitations?.find(invitation=>invitation.id===code);
    const valid=!!code&&!!className&&['aluno','psicopedagogo'].includes(role)&&(!stored||stored.turma===className&&stored.role===role);
    const root=$('#invitation-root');
    if(!valid){root.innerHTML='<h1>Convite não encontrado</h1><p>Peça um novo link ao responsável pela turma.</p><a class="mef-btn" href="index.html">Voltar ao início</a>';return;}
    root.innerHTML=`<h1>Você recebeu um convite</h1><p>Turma: <strong>${esc(className)}</strong></p><p>Perfil: ${role==='aluno'?'Aluno':'Psicopedagogo'}</p><form id="accept-invite"><label class="mef-field" for="invite-name">Nome completo<input id="invite-name" required maxlength="150"></label><label class="mef-field" for="accept-email">E-mail<input id="accept-email" type="email" required></label><div class="mef-actions"><button class="mef-btn primary" type="submit">Aceitar convite</button><a class="mef-btn" href="index.html">Cancelar</a></div><p id="invite-accepted" role="status"></p></form>`;
    $('#accept-invite').onsubmit=event=>{
      event.preventDefault();const name=$('#invite-name').value.trim(),email=$('#accept-email').value.trim();if(!name)return;
      try{
        MEF.transaction(d=>{
          d.acceptedInvitations||=[];if(d.acceptedInvitations.some(item=>item.code===code&&item.email===email))return;
          let studentId;
          if(role==='aluno'){let student=d.students.find(s=>s.email===email&&s.turma===className);if(!student){student={id:MEF.id(),nome:name,email,turma:className,serie:'Não informada'};d.students.push(student);}studentId=student.id;}
          else {d.classMembers||=[];d.classMembers.push({id:MEF.id(),nome:name,email,turma:className,role});}
          d.acceptedInvitations.push({code,email,name,role,turma:className,studentId,date:new Date().toISOString()});
        });
        const item=MEF.load().acceptedInvitations.find(item=>item.code===code&&item.email===email);if(item.studentId)localStorage.setItem('mef_aluno_atual',item.studentId);
        $('#invite-accepted').textContent='Convite aceito. Seu vínculo foi registrado.';
        const target=role==='aluno'?'aluno/dashboard_aluno.html':'psicopedagogo/dashboard_psico.html';const link=document.createElement('a');link.className='mef-btn primary';link.href=target;link.textContent='Continuar para minha área';$('#invite-accepted').append(document.createElement('br'),link);$('#accept-invite button[type=submit]').disabled=true;
      }catch(e){MEFUI.toast('Não foi possível aceitar o convite.');}
    };
  }
  window.MEFInvites={open};window.lucide?.createIcons();
})();
