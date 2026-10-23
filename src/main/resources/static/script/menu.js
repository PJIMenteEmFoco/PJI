/* Restaura o menu antes de desenhar seu conteúdo, evitando recolhimento ao navegar. */
(() => {
  'use strict';
  const sidebar=document.currentScript.parentElement;
  if(!sidebar.matches('.sidebar')||!matchMedia('(min-width:651px)').matches)return;
  try{
    if(sessionStorage.getItem('mef-menu:psicopedagogo')==='expanded'){
      sidebar.style.transition='none';sidebar.classList.add('expanded');
      requestAnimationFrame(()=>sidebar.style.removeProperty('transition'));
    }
  }catch{}
})();
