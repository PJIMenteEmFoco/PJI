/* Persistência dos dados visíveis do perfil de demonstração. */
(() => {
  'use strict';
  const $=s=>document.querySelector(s);if(!$('#val-name'))return;
  const actor=MEF.audience(),role=MEF.role(),modal=$('#edit-modal'),input=$('#modal-field-input');let target='';
  const profile=()=>MEF.load().profiles?.[actor]||{};
  function render(){
    const p=profile();$('#val-name').textContent=p.name||(role==='aluno'?MEF.load().students.find(s=>s.id===MEF.studentId())?.nome:role==='professor'?'Professor':'Psicopedagogo')||'Aluno';
    $('#val-phone').textContent=p.phone||'Não informado';
    if(p.photo){const img=document.createElement('img');img.src=p.photo;img.alt='Foto do perfil';img.className='profile-photo';$('.profile-avatar-large').replaceChildren(img);}
  }
  window.openEditModal=(label,id)=>{
    target=id;
    if(label==='Senha'){location.href='../login/email-verificacao.html?origem=ajustes&perfil='+encodeURIComponent(role);return;}
    if(label==='Foto de Perfil'){
      const picker=document.createElement('input');picker.type='file';picker.accept='.png,.jpg,.jpeg,image/png,image/jpeg';
      picker.onchange=()=>{
        const file=picker.files[0];if(!file)return;
        if(file.size>524288||!/^.+\.(png|jpe?g)$/i.test(file.name)||!['image/png','image/jpeg'].includes(file.type)){MEFUI.toast('Selecione uma foto PNG, JPG ou JPEG de até 512 KB.');return;}
        const reader=new FileReader();reader.onload=()=>{try{MEF.transaction(d=>{d.profiles||={};d.profiles[actor]={...profile(),photo:reader.result};});render();}catch(e){MEFUI.toast('Não foi possível salvar a foto.');}};reader.onerror=()=>MEFUI.toast('Não foi possível ler a foto.');reader.readAsDataURL(file);
      };picker.click();return;
    }
    $('#modal-title-text').textContent='Editar '+label;input.type=id==='val-phone'?'tel':'text';input.value=$('#'+id).textContent==='Não informado'?'':$('#'+id).textContent;modal.classList.add('active');input.focus();
  };
  window.saveEditModal=()=>{
    const value=input.value.trim();if(!value){MEFUI.toast('Preencha o campo antes de salvar.');input.focus();return;}
    if(target==='val-phone'&&!/^[+()\d\s-]{8,20}$/.test(value)){MEFUI.toast('Informe um telefone válido.');return;}
    try{MEF.transaction(d=>{
      d.profiles||={};d.profiles[actor]={...profile(),[target==='val-name'?'name':'phone']:value};
      if(role==='aluno'&&target==='val-name'){const student=d.students.find(s=>s.id===MEF.studentId());if(student)student.nome=value;}
    });modal.classList.remove('active');render();MEFUI.toast('Alteração salva.');}catch(e){MEFUI.toast('Não foi possível salvar a alteração.');}
  };
  function $$(selector){return [...document.querySelectorAll(selector)];}
  $$('.btn-toggle-status').slice(1).forEach((button,i)=>{
    const key='ajuste:'+actor+':'+i,saved=MEF.load().preferences[key];
    const apply=enabled=>{button.classList.toggle('disabled',!enabled);button.textContent=enabled?'Ativado':'Desativado';button.setAttribute('aria-pressed',String(enabled));};
    if(saved!==undefined)apply(saved);button.removeAttribute('onclick');button.onclick=()=>{try{const enabled=button.classList.contains('disabled');MEF.transaction(d=>d.preferences[key]=enabled);apply(enabled);}catch(e){MEFUI.toast('Não foi possível salvar a preferência.');}};
  });
  document.addEventListener('keydown',e=>{if(e.key==='Escape')document.querySelectorAll('.modal-overlay.active').forEach(el=>el.classList.remove('active'));});
  render();
})();
