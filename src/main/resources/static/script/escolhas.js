/* Menus de escolha compartilhados. Os selects originais mantêm dados e eventos. */
(() => {
  'use strict';
  const controls=new WeakMap();let serial=0,opened=null;
  const normalize=value=>value.normalize('NFD').replace(/[\u0300-\u036f]/g,'').toLowerCase().trim();
  function enhance(select){
    if(select.multiple||select.size>1||controls.has(select))return;
    const wrapper=document.createElement('span');wrapper.className='mef-choice';
    if(select.matches('.cal-select,.sort-select,.select-filter'))wrapper.classList.add('mef-choice--compact');
    const button=document.createElement('button');button.type='button';button.className='mef-choice-button';
    const text=document.createElement('span');button.append(text);
    const list=document.createElement('ul');list.className='mef-choice-list';list.id='mef-choices-'+(++serial);list.role='listbox';list.hidden=true;
    if('showPopover' in list)list.popover='manual';
    button.setAttribute('role','combobox');button.setAttribute('aria-haspopup','listbox');button.setAttribute('aria-expanded','false');button.setAttribute('aria-controls',list.id);
    const label=select.labels?.[0];let labelText='';
    if(label){const copy=label.cloneNode(true);copy.querySelectorAll('select,input,button').forEach(el=>el.remove());labelText=copy.textContent.trim();}
    button.setAttribute('aria-label',select.getAttribute('aria-label')||labelText||select.title||({ 'sort-select':'Ordenar atividades','select-filter-psico':'Filtrar turmas','cal-month-select':'Mês do calendário','cal-year-select':'Ano do calendário','new-event-priority':'Prioridade do evento' }[select.id])||'Escolha uma opção');
    if(select.hasAttribute('aria-labelledby'))button.setAttribute('aria-labelledby',select.getAttribute('aria-labelledby'));
    if(select.hasAttribute('aria-describedby'))button.setAttribute('aria-describedby',select.getAttribute('aria-describedby'));
    list.setAttribute('aria-label',button.getAttribute('aria-label'));
    select.before(wrapper);wrapper.append(select,button);select.classList.add('mef-native-choice');select.tabIndex=-1;select.setAttribute('aria-hidden','true');
    let active=-1,search='',timer,signature='';
    const options=()=>[...select.options];
    const enabled=i=>{const o=select.options[i];return o&&!o.disabled&&!o.hidden&&!o.parentElement.disabled;};
    function highlight(i){
      if(!enabled(i))return;active=i;
      [...list.children].forEach((item,index)=>item.classList.toggle('is-active',index===i));
      const item=list.children[i];button.setAttribute('aria-activedescendant',item.id);
      if(item.offsetTop<list.scrollTop)list.scrollTop=item.offsetTop;
      else if(item.offsetTop+item.offsetHeight>list.scrollTop+list.clientHeight)list.scrollTop=item.offsetTop+item.offsetHeight-list.clientHeight;
    }
    function render(){
      list.replaceChildren(...options().map((o,i)=>{
        const item=document.createElement('li');item.id=list.id+'-'+i;item.className='mef-choice-option';item.role='option';item.textContent=o.textContent;item.hidden=o.hidden;
        item.setAttribute('aria-selected',String(i===select.selectedIndex));item.setAttribute('aria-disabled',String(!enabled(i)));
        item.addEventListener('pointermove',()=>highlight(i));item.addEventListener('pointerdown',e=>e.preventDefault());item.addEventListener('click',()=>choose(i));return item;
      }));
    }
    function position(){
      if(opened!==api)return;
      const r=button.getBoundingClientRect(),gap=6,below=innerHeight-r.bottom-14,above=r.top-14;
      if(r.bottom<=0||r.top>=innerHeight){close();return;}
      list.style.width=Math.min(Math.max(r.width,160),innerWidth-24)+'px';
      list.style.maxHeight=Math.max(44,Math.min(280,Math.max(below,above)))+'px';
      const height=list.getBoundingClientRect().height,width=list.getBoundingClientRect().width;
      list.style.left=Math.max(12,Math.min(r.left,innerWidth-width-12))+'px';
      list.style.top=(height<=below||below>=above?Math.min(r.bottom+gap,innerHeight-height-8):Math.max(8,r.top-height-gap))+'px';
    }
    function close(){
      if(list.matches(':popover-open'))list.hidePopover();list.remove();list.hidden=true;
      button.setAttribute('aria-expanded','false');button.removeAttribute('aria-activedescendant');search='';clearTimeout(timer);if(opened===api)opened=null;
    }
    function sync(){
      const value=select.selectedOptions[0]?.textContent||'Selecione';if(text.textContent!==value)text.textContent=value;
      button.disabled=select.disabled||!options().some((_,i)=>enabled(i));button.setAttribute('aria-required',String(select.required));
      if(select.validity.valid)button.removeAttribute('aria-invalid');
      if(opened!==api)return;
      if(!select.isConnected||button.disabled||!button.getClientRects().length||select.closest('dialog:not([open]),.modal-overlay:not(.active)')){close();return;}
      const next=JSON.stringify(options().map((o,i)=>[o.textContent,o.value,enabled(i),o.hidden,i===select.selectedIndex]));
      if(next!==signature){signature=next;render();position();highlight(enabled(active)?active:options().findIndex((_,i)=>enabled(i)));}
    }
    function open(){
      if(button.disabled)return;opened?.close();opened=api;
      (select.closest('dialog[open]')||document.body).append(list);list.hidden=false;signature='';sync();
      if(list.showPopover)list.showPopover();position();
      button.setAttribute('aria-expanded','true');highlight(enabled(select.selectedIndex)?select.selectedIndex:options().findIndex((_,i)=>enabled(i)));
    }
    function choose(i){
      if(!enabled(i))return;const changed=select.selectedIndex!==i;select.selectedIndex=i;sync();close();button.focus();
      if(changed){select.dispatchEvent(new Event('input',{bubbles:true}));select.dispatchEvent(new Event('change',{bubbles:true}));}
    }
    const api={close,position,sync};controls.set(select,api);
    button.addEventListener('click',e=>{e.preventDefault();opened===api?close():open();});
    button.addEventListener('keydown',e=>{
      const isOpen=opened===api;
      if(e.key==='Escape'){if(isOpen){e.preventDefault();e.stopPropagation();close();}return;}
      if(e.key==='Tab'){close();return;}
      if(['ArrowDown','ArrowUp','Home','End'].includes(e.key)){
        e.preventDefault();if(!isOpen)open();const indices=options().map((_,i)=>i).filter(enabled),index=indices.indexOf(active);
        highlight(e.key==='Home'?indices[0]:e.key==='End'?indices.at(-1):!isOpen?active:indices[Math.max(0,Math.min(indices.length-1,index+(e.key==='ArrowDown'?1:-1)))]);return;
      }
      if((e.key==='Enter'||e.key===' ')&&(!search||e.key==='Enter')){e.preventDefault();isOpen?choose(active):open();return;}
      if(e.key.length===1&&!e.ctrlKey&&!e.metaKey&&!e.altKey){
        e.preventDefault();if(!isOpen)open();search+=normalize(e.key);clearTimeout(timer);timer=setTimeout(()=>search='',700);
        highlight(options().findIndex((o,i)=>enabled(i)&&normalize(o.textContent).startsWith(search)));
      }
    });
    select.addEventListener('change',sync);select.addEventListener('focus',()=>button.focus());
    select.addEventListener('invalid',e=>{e.preventDefault();button.setAttribute('aria-invalid','true');button.focus();});
    select.form?.addEventListener('reset',()=>setTimeout(sync,0));sync();
  }
  /* Os menus antigos de tema/mês/ano mantêm seus callbacks e usam a mesma escolha. */
  function adaptLegacy(){
    document.querySelectorAll('#theme-dropdown,#dropdown-month,#dropdown-year').forEach(container=>{
      if(container.classList.contains('mef-legacy-choice'))return;
      const oldOptions=[...container.querySelectorAll('.custom-dropdown-option,.dropdown-option')];if(!oldOptions.length)return;
      const select=document.createElement('select');select.id=container.id+'-choice';
      select.setAttribute('aria-label',container.id==='theme-dropdown'?'Esquema de cores':container.id==='dropdown-month'?'Mês do calendário':'Ano do calendário');
      oldOptions.forEach(item=>{const o=document.createElement('option');o.textContent=item.textContent.trim();o.value=item.dataset.value||item.id.replace('opt-','');o.selected=item.classList.contains('selected');select.append(o);});
      if(container.id==='theme-dropdown')select.value=window.MEFTheme?.current()||'claro';
      select.addEventListener('change',()=>oldOptions[select.selectedIndex]?.click());
      new MutationObserver(()=>{const index=oldOptions.findIndex(o=>o.classList.contains('selected'));if(index>=0)select.selectedIndex=index;controls.get(select)?.sync();}).observe(container,{subtree:true,attributes:true,attributeFilter:['class']});
      container.classList.add('mef-legacy-choice');container.append(select);
    });
  }
  function scan(root){if(root.matches?.('select'))enhance(root);root.querySelectorAll?.('select').forEach(enhance);}
  adaptLegacy();scan(document);
  new MutationObserver(records=>{
    records.forEach(record=>{
      record.addedNodes.forEach(node=>{if(node.nodeType===1)scan(node);});
      const select=record.target.closest?.('select');if(select)controls.get(select)?.sync();
    });opened?.sync();
  }).observe(document.body,{subtree:true,childList:true,characterData:true,attributes:true,attributeFilter:['disabled','selected','required','hidden','class','open']});
  document.addEventListener('pointerdown',e=>{if(!e.target.closest('.mef-choice,.mef-choice-list'))opened?.close();});
  document.addEventListener('scroll',e=>{if(!e.target.closest?.('.mef-choice-list'))opened?.position();},true);
  document.addEventListener('close',()=>opened?.close(),true);window.addEventListener('resize',()=>opened?.position());
  window.MEFChoices={closeOpen:()=>{if(!opened)return false;opened.close();return true;},refresh:()=>document.querySelectorAll('select').forEach(s=>controls.get(s)?.sync())};
})();
