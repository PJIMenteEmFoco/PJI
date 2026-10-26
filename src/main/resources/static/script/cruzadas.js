/* Grade de palavras cruzadas com cruzamentos e verificação de vizinhança. */
(() => {
  'use strict';
  function build(items) {
    const words = items.map((v, i) => ({ ...v, word: MEF.norm(v.palavra), index: i })).sort((a, b) => b.word.length - a.word.length);
    if (!words.length) throw new Error('Adicione palavras e dicas.');
    let best = null;
    for (let seed = 0; seed < words.length; seed++) {
      const grid = new Map(), placed = [];
      const key = (r, c) => `${r},${c}`;
      function fit(word, row, col, dir) {
        const dr = dir === 'V' ? 1 : 0, dc = dir === 'H' ? 1 : 0;
        if (grid.has(key(row - dr, col - dc)) || grid.has(key(row + dr * word.length, col + dc * word.length))) return false;
        let crosses = 0;
        for (let n = 0; n < word.length; n++) {
          const r = row + dr * n, c = col + dc * n, cell = grid.get(key(r, c));
          if (cell) {
            if (cell.letter !== word[n] || cell.dirs.includes(dir)) return false;
            crosses++;
          } else if (grid.has(key(r + dc, c + dr)) || grid.has(key(r - dc, c - dr))) return false;
        }
        return crosses > 0;
      }
      function place(w, row, col, dir) {
        placed.push({ ...w, row, col, dir });
        for (let n = 0; n < w.word.length; n++) {
          const r = row + (dir === 'V' ? n : 0), c = col + (dir === 'H' ? n : 0), k = key(r, c), old = grid.get(k);
          grid.set(k, { row: r, col: c, letter: w.word[n], dirs: [...(old?.dirs || []), dir] });
        }
      }
      place(words[seed], 0, 0, 'H');
      let pending = words.filter((_, i) => i !== seed), progress = true;
      while (pending.length && progress) {
        progress = false;
        for (const w of [...pending]) {
          let candidate = null;
          for (const cell of grid.values()) {
            for (let n = 0; n < w.word.length; n++) {
              if (w.word[n] !== cell.letter) continue;
              for (const dir of ['H', 'V']) {
                const r = cell.row - (dir === 'V' ? n : 0), c = cell.col - (dir === 'H' ? n : 0);
                if (!fit(w.word, r, c, dir)) continue;
                const score = Math.abs(r) + Math.abs(c);
                if (!candidate || score < candidate.score) candidate = { r, c, dir, score };
              }
            }
          }
          if (candidate) { place(w, candidate.r, candidate.c, candidate.dir); pending = pending.filter(v => v !== w); progress = true; }
        }
      }
      if (!best || placed.length > best.placed.length) best = { grid, placed, pending };
      if (!pending.length) break;
    }
    if (best.pending.length) throw new Error('Não foi possível cruzar todas as palavras. Use palavras que compartilhem letras e permitam cruzamentos.');
    const cells = [...best.grid.values()], minR = Math.min(...cells.map(c => c.row)), minC = Math.min(...cells.map(c => c.col));
    const placed = best.placed.map(w => ({ ...w, row: w.row - minR, col: w.col - minC })).sort((a, b) => a.row - b.row || a.col - b.col);
    let num = 0, last = '';
    placed.forEach(w => { const k = `${w.row},${w.col}`; if (k !== last) { num++; last = k; } w.number = num; });
    return { cells: cells.map(c => ({ ...c, row: c.row - minR, col: c.col - minC })), words: placed, rows: Math.max(...cells.map(c => c.row)) - minR + 1, cols: Math.max(...cells.map(c => c.col)) - minC + 1 };
  }
  window.MEFCrossword = { build };
})();
