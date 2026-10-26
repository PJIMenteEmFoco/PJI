/* Uma única preferência de tema, compartilhada por todas as telas. */
(() => {
  'use strict';
  const current=()=>{try{return localStorage.getItem('theme')==='escuro'?'escuro':'claro';}catch{return 'claro';}};
  function apply(theme){
    const value=theme==='escuro'?'escuro':'claro';document.documentElement.dataset.theme=value;
    document.documentElement.style.colorScheme=value==='escuro'?'dark':'light';
    document.body?.classList.toggle('dark-mode',value==='escuro');
    const label=document.getElementById('theme-btn-label');if(label)label.textContent=value==='escuro'?'Escuro':'Claro';
    for(const option of document.querySelectorAll('#theme-dropdown .custom-dropdown-option'))option.classList.toggle('selected',option.id==='opt-'+value);
  }
  function set(theme){const value=theme==='escuro'?'escuro':'claro';try{localStorage.setItem('theme',value);}catch{}apply(value);document.getElementById('theme-dropdown')?.classList.remove('open');}
  apply(current());document.addEventListener('DOMContentLoaded',()=>{apply(current());if(document.getElementById('theme-dropdown'))window.selectTheme=set;});
  window.addEventListener('storage',event=>{if(event.key==='theme')apply(current());});window.addEventListener('pageshow',()=>apply(current()));
  window.MEFTheme={current,set,apply};
})();
