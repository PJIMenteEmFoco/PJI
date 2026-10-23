/* Padrão visual e navegação de teclado para todos os diálogos do site. */
(() => {
  'use strict';
  window.MEFTheme?.apply(window.MEFTheme.current());
  const states=new WeakMap(),stack=[];let serial=0,lastInteraction=null,previousFocus=null,lastFocus=document.activeElement;
  const actions='.modal-actions,.modal-logout-actions,.modal-btn-row,.wireframe-actions-grid,.delete-modal-actions,.logout-btn-row,.mef-actions,.modal-card-footer';
  const visible=el=>el.isConnected&&(el.matches('dialog')?el.open:el.classList.contains('active'));
  const top=()=>stack.filter(visible).at(-1);
  const focusable=modal=>[...modal.querySelectorAll('button,a[href],input,textarea,select,[tabindex]')].filter(el=>!el.disabled&&el.tabIndex>=0&&!el.closest('[hidden],.hidden,[inert]')&&el.getClientRects().length&&getComputedStyle(el).visibility!=='hidden');
  const addClass=(el,name)=>{if(!el.classList.contains(name))el.classList.add(name);};
  function prepare(modal){
    const card=modal.matches('dialog')?modal:modal.firstElementChild;if(!card)return;
    addClass(card,'mef-popup');
    const wide=!!card.querySelector('form,.ficha-corpo,.relatorio-corpo,.mef-table');
    if(card.classList.contains('mef-popup--wide')!==wide)card.classList.toggle('mef-popup--wide',wide);
    const title=card.querySelector('h2,h3,h4');
    if(title){addClass(title,'mef-popup-title');if(!title.id)title.id='mef-popup-title-'+(++serial);modal.setAttribute('aria-labelledby',title.id);}
    modal.setAttribute('role','dialog');modal.setAttribute('aria-modal','true');
    card.querySelectorAll(actions).forEach(row=>addClass(row,'mef-popup-actions'));
    card.querySelectorAll('button:not(.mef-choice-button),a.mef-btn,a.btn-modal-confirm').forEach(button=>{
      addClass(button,'mef-popup-button');
      let primary=(button.type==='submit'&&button.form)||button.matches('.primary,.accent,.danger,.btn-modal-confirm,.btn-modal-logout,.btn-modal-save,.btn-logout-confirm,.btn-delete-confirm,.btn-turma-save,.btn-popup-primary,.btn-warning-ok')||button.id==='mef-confirm';
      if(modal.id==='delete-modal')primary=button.matches('.btn-delete-cancel');
      const value=primary?'primary':'secondary';if(button.dataset.popupAction!==value)button.dataset.popupAction=value;
    });
    card.querySelectorAll(actions).forEach(row=>{
      const children=[...row.children],sorted=[...children].sort((a,b)=>(a.dataset.popupAction==='primary'?0:1)-(b.dataset.popupAction==='primary'?0:1));
      if(children.some((child,i)=>child!==sorted[i]))row.append(...sorted);
    });
    if(!states.has(modal)){
      states.set(modal,{active:false,opener:null});
      if(modal.matches('dialog'))modal.addEventListener('close',sync);
    }
  }
  function sync(){
    const candidates=[...document.querySelectorAll('.modal-overlay,dialog.mef-dialog,dialog[data-mef-alert]')];
    for(const modal of candidates){
      prepare(modal);const state=states.get(modal);if(!state)continue;
      if(visible(modal)&&!state.active){
        state.active=true;state.opener=[lastInteraction,previousFocus,lastFocus].find(el=>el?.isConnected&&!modal.contains(el))||null;
        stack.push(modal);modal.setAttribute('aria-hidden','false');
        requestAnimationFrame(()=>{if(top()===modal&&!modal.contains(document.activeElement)){
          const first=focusable(modal).find(el=>el.matches('input,textarea,select'))||focusable(modal)[0];
          if(first)first.focus();else {modal.tabIndex=-1;modal.focus();}
        }});
      }
    }
    for(let i=stack.length-1;i>=0;i--){
      const modal=stack[i];if(visible(modal))continue;
      const state=states.get(modal);state.active=false;modal.setAttribute('aria-hidden','true');stack.splice(i,1);
      const opener=state.opener;
      if(opener?.isConnected&&(!top()||top().contains(opener)))opener.focus();
    }
  }
  function close(modal){if(modal.matches('dialog'))modal.close();else modal.classList.remove('active');sync();}
  document.addEventListener('focusin',e=>{previousFocus=lastFocus;lastFocus=e.target;});
  document.addEventListener('pointerdown',e=>{lastInteraction=e.target.closest('button,a,[role="button"],input,select,textarea')||e.target;},true);
  document.addEventListener('keydown',e=>{
    if(['Enter',' '].includes(e.key))lastInteraction=e.target;
    const modal=top();if(!modal)return;
    if(e.key==='Escape'){e.preventDefault();e.stopImmediatePropagation();if(!window.MEFChoices?.closeOpen())close(modal);return;}
    if(e.key!=='Tab')return;
    const items=focusable(modal);if(!items.length){e.preventDefault();modal.focus();return;}
    const first=items[0],last=items.at(-1),current=document.activeElement;
    if(e.shiftKey&&(current===first||!modal.contains(current))){e.preventDefault();last.focus();}
    else if(!e.shiftKey&&(current===last||!modal.contains(current))){e.preventDefault();first.focus();}
  },true);
  document.addEventListener('click',e=>{
    const modal=top();if(modal?.matches('.modal-overlay')&&e.target===modal)close(modal);
  });
  function alert(message,title='Atenção'){
    return new Promise(resolve=>{
      const modal=document.createElement('dialog');modal.className='mef-dialog';modal.dataset.mefAlert='true';
      const heading=document.createElement('h2');heading.textContent=title;
      const description=document.createElement('p');description.textContent=message;
      const row=document.createElement('div');row.className='mef-actions';
      const button=document.createElement('button');button.type='button';button.className='mef-btn primary';button.textContent='Entendi';
      row.append(button);modal.append(heading,description,row);document.body.append(modal);prepare(modal);
      button.onclick=()=>close(modal);modal.addEventListener('close',()=>{modal.remove();sync();resolve();},{once:true});
      modal.showModal();sync();
    });
  }
  new MutationObserver(sync).observe(document.body,{childList:true,subtree:true,attributes:true,attributeFilter:['class','open']});
  window.MEFPopups={prepare,alert};sync();
})();
