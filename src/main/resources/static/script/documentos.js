/* PDF da leitura guiada: validação, anexo e visualização no navegador. */
(() => {
  'use strict';
  const LIMIT=2*1024*1024;
  const dataOf=pdf=>{try{return pdf?.data||(pdf?.key?.startsWith('mef_pdf:')?localStorage.getItem(pdf.key):null);}catch{return null;}};
  const valid=pdf=>!!pdf&&typeof pdf.name==='string'&&pdf.size>0&&pdf.size<=LIMIT&&dataOf(pdf)?.startsWith('data:application/pdf;base64,');
  const escape=value=>String(value||'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const urls=new Set();
  function url(pdf){
    if(!valid(pdf))throw new Error('O PDF anexado não é válido.');
    const raw=atob(dataOf(pdf).split(',')[1]),bytes=Uint8Array.from(raw,c=>c.charCodeAt(0));
    const result=URL.createObjectURL(new Blob([bytes],{type:'application/pdf'}));urls.add(result);return result;
  }
  function view(pdf){
    if(!valid(pdf))return '';
    const href=url(pdf);
    return `<section class="reading-pdf"><h3>Texto em PDF</h3><p>${escape(pdf.name)}</p><object class="reading-pdf-viewer" type="application/pdf" data="${escape(href)}" aria-label="Texto de leitura: ${escape(pdf.name)}"><p>Use o botão abaixo para abrir o PDF no seu leitor.</p></object><a class="mef-btn" href="${escape(href)}" target="_blank" rel="noopener">Abrir PDF em outra aba</a><a class="mef-btn" href="${escape(href)}" download="${escape(pdf.name)}">Baixar PDF</a></section>`;
  }
  function mount(container,model,capture){
    if(model.tipo!=='leitura')return;
    const box=document.createElement('section');box.className='question-box reading-pdf-upload';
    box.innerHTML='<label class="form-label" for="reading-pdf">Anexar texto em PDF</label><input id="reading-pdf" type="file" accept="application/pdf,.pdf"><p class="attachment-help">PDF de até 2 MB. Você também pode escrever ou complementar os trechos abaixo.</p><div id="reading-pdf-state" role="status"></div>';
    container.prepend(box);const input=box.querySelector('input'),state=box.querySelector('#reading-pdf-state');
    function render(){
      state.replaceChildren();if(!valid(model.pdf))return;
      const name=document.createElement('p');name.textContent=model.pdf.name+' • '+Math.ceil(model.pdf.size/1024)+' KB';
      const preview=document.createElement('button');preview.type='button';preview.className='mef-btn';preview.textContent='Visualizar PDF';preview.id='preview-reading-pdf';
      const remove=document.createElement('button');remove.type='button';remove.className='mef-btn';remove.textContent='Remover PDF';remove.id='remove-reading-pdf';
      preview.onclick=()=>MEFUI.dialog('Texto da leitura',view(model.pdf)+'<div class="mef-actions"><button type="button" class="mef-btn" data-close>Fechar</button></div>');
      remove.onclick=()=>{capture();delete model.pdf;input.value='';render();};state.append(name,preview,remove);
    }
    input.onchange=async()=>{
      const file=input.files[0];if(!file)return;
      try{
        if(file.size>LIMIT)throw new Error('Selecione um PDF de até 2 MB.');
        if(new TextDecoder().decode(await file.slice(0,5).arrayBuffer())!=='%PDF-')throw new Error('O arquivo selecionado não é um PDF válido.');
        const data=await new Promise((resolve,reject)=>{const reader=new FileReader();reader.onload=()=>resolve(reader.result);reader.onerror=()=>reject(new Error('Não foi possível ler o PDF.'));reader.readAsDataURL(new Blob([file],{type:'application/pdf'}));});
        if(model.tipo!=='leitura')return;
        capture();const key='mef_pdf:'+MEF.id();localStorage.setItem(key,data);model.pdf={name:file.name,size:file.size,type:'application/pdf',key};render();
      }catch(error){input.value='';MEFUI.toast(error.name==='QuotaExceededError'?'Não há espaço para este PDF no navegador. Escolha um arquivo menor.':error.message);}
    };render();
  }
  window.addEventListener('pagehide',()=>{urls.forEach(value=>URL.revokeObjectURL(value));urls.clear();});
  window.MEFDocuments={valid,view,mount,LIMIT};
})();
