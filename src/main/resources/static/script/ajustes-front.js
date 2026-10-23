/* Apenas apresentação e abertura do componente de notificações existente. */
(() => {
  'use strict';
  const source=document.currentScript.src,profile=document.body.dataset.mefProfile;
  if(!['aluno','professor','psicopedagogo'].includes(profile))return;
  const bell='<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9"/><path d="M10 21h4"/></svg>';
  let panel=null,opener=null,loading=null,choicesLoading=null;
  const isPage=document.body.dataset.page==='notificacoes';
  function enableChoices(){
    if(window.MEFChoices){window.MEFChoices.refresh();return;}
    if(choicesLoading)return;
    if(!document.querySelector('link[href*="/css/escolhas.css"]')){const css=document.createElement('link');css.rel='stylesheet';css.href=new URL('../css/escolhas.css',source).href;document.head.append(css);}
    const script=document.createElement('script');script.src=new URL('escolhas.js',source).href;choicesLoading=script;
    script.onerror=()=>{choicesLoading=null;script.remove();};document.body.append(script);
  }
  function loadController(host){
    if(window.MEFNotifications?.root===host)return window.MEFNotifications.reload();
    if(loading)return loading;
    loading=new Promise((resolve,reject)=>{
      const script=document.createElement('script');script.src=new URL('mef-notificacoes-backend.js',source).href;script.dataset.navigation='external';
      script.onload=()=>{loading=null;resolve();};script.onerror=()=>{loading=null;script.remove();reject(new Error('Não foi possível abrir as notificações.'));};document.body.append(script);
    });return loading;
  }
  function displayLoadError(host){
    host.replaceChildren();const text=document.createElement('p');text.textContent='Não foi possível abrir as notificações. Tente novamente.';text.className='mef-error';text.setAttribute('role','alert');
    const retry=document.createElement('button');retry.type='button';retry.className='mef-btn';retry.textContent='Tentar novamente';retry.onclick=()=>loadController(host).catch(()=>displayLoadError(host));host.append(text,retry);
  }
  function open(trigger){
    if(isPage){document.querySelector('.main-content')?.scrollTo({top:0,behavior:'smooth'});return;}
    opener=trigger;
    if(!panel){
      panel=document.createElement('dialog');panel.className='mef-notification-dialog';panel.setAttribute('aria-label','Notificações');
      panel.innerHTML='<div class="mef-notification-dialog-header"><button type="button" class="mef-notification-close" aria-label="Fechar notificações">Fechar</button></div><section class="mef-notification-surface" data-mef-notifications-host aria-live="polite"></section>';
      document.body.append(panel);panel.querySelector('.mef-notification-close').onclick=()=>panel.close();
      panel.addEventListener('close',()=>{opener?.setAttribute('aria-expanded','false');if(opener?.isConnected)opener.focus();});
      panel.addEventListener('click',event=>{if(event.target!==panel)return;const r=panel.getBoundingClientRect();if(event.clientX<r.left||event.clientX>r.right||event.clientY<r.top||event.clientY>r.bottom)panel.close();});
    }
    trigger.setAttribute('aria-expanded','true');if(!panel.open)panel.showModal();
    const host=panel.querySelector('[data-mef-notifications-host]');enableChoices();loadController(host).catch(()=>displayLoadError(host));
  }
  document.querySelectorAll('a[href]').forEach(link=>{if(/\/(?:aluno|professor|psicopedagogo)\/notificacoes\/?$/.test(new URL(link.href,location.href).pathname))link.dataset.mefNotifications='';});
  let triggers=[...document.querySelectorAll('[data-mef-notifications]')];
  if(!triggers.length){const trigger=document.createElement('button');trigger.type='button';trigger.className='mef-notification-shortcut';trigger.dataset.mefNotifications='';trigger.innerHTML=bell;document.body.append(trigger);triggers=[trigger];}
  triggers.forEach(trigger=>{trigger.setAttribute('aria-label','Notificações');if(!isPage){trigger.setAttribute('aria-haspopup','dialog');trigger.setAttribute('aria-expanded','false');}else if(trigger.closest('.sidebar-nav')){trigger.classList.add('active');trigger.setAttribute('aria-current','page');}});
  document.addEventListener('click',event=>{const trigger=event.target.closest('[data-mef-notifications]');if(!trigger)return;event.preventDefault();event.stopImmediatePropagation();open(trigger);},true);
  if(isPage){const host=document.getElementById('mef-root');if(host){host.classList.add('mef-notification-surface');enableChoices();if(window.MEFNotifications?.root!==host)loadController(host).catch(()=>displayLoadError(host));}}
})();
