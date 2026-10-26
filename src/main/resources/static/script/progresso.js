/* Regras de demonstração centralizadas para a futura integração com a API. */
(() => {
  'use strict';
  const LEVEL_XP=100;
  const dateKey=(value=new Date())=>{const d=value instanceof Date?value:new Date(value);return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;};
  function forStudent(sid=MEF.studentId()){
    const reports=MEF.load().reports.filter(r=>r.kind==='automatico'&&r.studentId===sid),xp=reports.reduce((sum,r)=>sum+Math.max(0,Number(r.xp)||0),0);
    const days=new Set(reports.map(r=>dateKey(r.finishedAt)));let cursor=new Date(),streak=0;
    if(!days.has(dateKey(cursor)))cursor.setDate(cursor.getDate()-1);
    while(days.has(dateKey(cursor))){streak++;cursor.setDate(cursor.getDate()-1);}
    return {xp,level:Math.floor(xp/LEVEL_XP)+1,current:xp%LEVEL_XP,remaining:LEVEL_XP-xp%LEVEL_XP,streak,days,reports};
  }
  window.MEFProgress={LEVEL_XP,dateKey,forStudent};
  const images=new URL('../imagens/',document.currentScript.src).href;
  window.MEFActivityArt={image:type=>`<img class="activity-visual" src="${images}atividade-${['quiz','memoria','cruzadas','leitura'].includes(type)?type:'quiz'}.svg" alt="" loading="lazy">`};
})();
