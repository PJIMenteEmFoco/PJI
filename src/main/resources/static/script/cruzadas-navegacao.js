/* Avanço de letras dentro da palavra, preservando as casas de cruzamento. */
(() => {
  'use strict';
  function mount(content,layout){
    let direction='H';
    const position=input=>input.dataset.cell.split(',').map(Number);
    const cells=word=>Array.from({length:word.word.length},(_,i)=>[word.row+(word.dir==='V'?i:0),word.col+(word.dir==='H'?i:0)]);
    const at=(r,c)=>content.querySelector(`[data-cell="${r},${c}"]`);
    const wordAt=input=>{const [r,c]=position(input),list=layout.words.filter(word=>cells(word).some(([y,x])=>y===r&&x===c));return list.find(word=>word.dir===direction)||list[0];};
    function move(input,delta){
      const word=wordAt(input);if(!word)return;const points=cells(word),[r,c]=position(input),index=points.findIndex(([y,x])=>y===r&&x===c),next=points[index+delta];
      if(next){direction=word.dir;at(...next)?.focus();}
    }
    for(const input of content.querySelectorAll('[data-cell]')){
      input.onfocus=()=>input.select();
      input.oninput=()=>{input.value=MEF.norm(input.value).replace(/[^A-Z]/g,'').slice(0,1);if(input.value)move(input,1);};
      input.onkeydown=event=>{
        if(event.key==='Backspace'&&!input.value){event.preventDefault();move(input,-1);const previous=document.activeElement;if(previous?.matches('[data-cell]'))previous.value='';return;}
        if(event.key==='Enter'){event.preventDefault();direction=direction==='H'?'V':'H';return;}
        const deltas={ArrowUp:[-1,0],ArrowDown:[1,0],ArrowLeft:[0,-1],ArrowRight:[0,1]},delta=deltas[event.key];
        if(!delta)return;event.preventDefault();direction=delta[0]?'V':'H';const [r,c]=position(input);at(r+delta[0],c+delta[1])?.focus();
      };
      input.onpaste=event=>{
        const text=MEF.norm(event.clipboardData.getData('text')).replace(/[^A-Z]/g,'');if(!text)return;event.preventDefault();
        const word=wordAt(input);if(!word)return;const points=cells(word),[r,c]=position(input),start=points.findIndex(([y,x])=>y===r&&x===c);
        let last=input;for(let i=0;i<text.length&&start+i<points.length;i++){const target=at(...points[start+i]);target.value=text[i];last=target;}last.focus();move(last,1);
      };
    }
  }
  window.MEFCrosswordNavigation={mount};
})();
