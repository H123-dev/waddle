// Waddle UI. Talks to the Spring API on the same domain, so there is no CORS.
// Endpoints used:
//   POST /waddle/auth/signup, /waddle/auth/signin
//   /waddle/requests/friendrequest/{create, answer, friends, incoming, outgoing}
//   /waddle/requests/matchrequest/{create, answer, incoming}
//   POST /waddle/game/guess, GET /waddle/game/status/{wordleId}
//   POST /waddle/admin/word

const FLIP_MS = 500;   // one tile flip, must match the flip animation in style.css
const FLIP_GAP = 300;  // delay between tiles
const MATCH_WAIT_SECONDS = 120;

const state = {
  token: null,
  user: null,        // { id, name }
  game: null,        // the match on screen, see startGame()
  lobbyTimer: null,
  statusTimer: null,
  clockTimer: null,
};

// ---------- small helpers ----------

const $ = (id) => document.getElementById(id);

function el(tag, className, text) {
  const node = document.createElement(tag);
  if (className) node.className = className;
  if (text !== undefined) node.textContent = text; // textContent, never innerHTML, so names can't inject HTML
  return node;
}

function store(key, value) {
  try {
    if (value === null) localStorage.removeItem(key);
    else localStorage.setItem(key, JSON.stringify(value));
  } catch (e) { /* storage blocked, the app still works without it */ }
}

function load(key) {
  try { return JSON.parse(localStorage.getItem(key)); } catch (e) { return null; }
}

let toastTimer = null;
function toast(message) {
  const t = $('toast');
  t.textContent = message;
  t.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { t.hidden = true; }, 3000);
}

function showScreen(name) {
  for (const s of ['login', 'lobby', 'game']) $('screen-' + s).hidden = s !== name;
}

// ---------- API ----------

// Every call goes through here. Returns { status, data }.
async function api(method, path, body) {
  const headers = {};
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (state.token) headers['Authorization'] = 'Bearer ' + state.token;

  let res;
  try {
    res = await fetch(path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch (e) {
    toast("Can't reach the server.");
    return { status: 0, data: null };
  }

  const text = await res.text();
  let data = null;
  try { data = text ? JSON.parse(text) : null; } catch (e) { data = null; }

  // An expired or bad token on a protected call means we have to log in again
  if (res.status === 401 && !path.startsWith('/waddle/auth/')) {
    logout();
    toast('Your session ended. Sign in again.');
  }
  return { status: res.status, data };
}

// ---------- login / signup ----------

function setupAuth() {
  for (const tab of document.querySelectorAll('.tab')) {
    tab.addEventListener('click', () => {
      for (const t of document.querySelectorAll('.tab')) t.classList.toggle('active', t === tab);
      $('form-signin').hidden = tab.dataset.tab !== 'signin';
      $('form-signup').hidden = tab.dataset.tab !== 'signup';
    });
  }

  $('form-signin').addEventListener('submit', async (e) => {
    e.preventDefault();
    await signIn($('signin-email').value.trim(), $('signin-password').value);
  });

  $('form-signup').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = $('signup-email').value.trim();
    const password = $('signup-password').value;
    const { status } = await api('POST', '/waddle/auth/signup', {
      email, name: $('signup-name').value.trim(), password,
    });
    if (status === 201) await signIn(email, password); // sign in right after signing up
    else if (status === 409) toast('That email or name is already taken.');
    else if (status === 400) toast('Check the fields: a valid email and a password of 8+ characters.');
  });

  $('btn-logout').addEventListener('click', logout);
}

async function signIn(email, password) {
  const { status, data } = await api('POST', '/waddle/auth/signin', { email, password });
  if (status === 200) {
    state.token = data.token;
    state.user = { id: data.id, name: data.name };
    store('waddle.auth', { token: data.token, id: data.id, name: data.name });
    openLobby();
  } else if (status === 401) {
    toast('Wrong email or password.');
  } else if (status === 400) {
    toast('Enter a valid email and a password of 8+ characters.');
  }
}

function logout() {
  stopAllTimers();
  state.token = null;
  state.user = null;
  state.game = null;
  store('waddle.auth', null);
  $('waiting').hidden = true;
  showScreen('login');
}

function stopAllTimers() {
  clearInterval(state.lobbyTimer);
  clearInterval(state.statusTimer);
  clearInterval(state.clockTimer);
}

// ---------- lobby ----------

function openLobby() {
  stopAllTimers();
  state.game = null;
  $('lobby-username').textContent = state.user.name;
  showScreen('lobby');
  renderResumeBanner();
  checkAdmin();
  refreshLobby();
  state.lobbyTimer = setInterval(refreshLobby, 4000); // new requests show up without a reload
}

// GET /waddle/admin is guarded by RoleAuth: 200 for admins, 403 for players.
// Hiding the box is only cosmetic; the server still blocks non-admins.
async function checkAdmin() {
  $('admin-card').hidden = true;
  const { status } = await api('GET', '/waddle/admin');
  $('admin-card').hidden = status !== 200;
}

function activeGameKey() { return 'waddle.active.' + state.user.id; }

function renderResumeBanner() {
  const active = load(activeGameKey());
  $('resume-banner').hidden = !active;
  if (active) $('resume-text').textContent = 'You have a match running against ' + active.opponent + '.';
}

async function refreshLobby() {
  if (!state.token) return;
  const [friends, fIn, fOut, mIn] = await Promise.all([
    api('GET', '/waddle/requests/friendrequest/friends'),
    api('GET', '/waddle/requests/friendrequest/incoming'),
    api('GET', '/waddle/requests/friendrequest/outgoing'),
    api('GET', '/waddle/requests/matchrequest/incoming'),
  ]);
  if (!state.token) return; // logged out while loading

  renderList('friends-list', 'friends-empty', friends.data || [], (f) => {
    const li = el('li');
    li.append(el('span', '', f.name));
    const btn = el('button', 'btn small primary', 'Challenge');
    btn.type = 'button';
    btn.addEventListener('click', () => challenge(f.name));
    li.append(btn);
    return li;
  });

  renderList('friend-incoming', 'friend-incoming-empty', fIn.data || [], (r) =>
    requestItem(r.sender, () => answerFriend(r.id, 'Accepted'), () => answerFriend(r.id, 'Rejected')));

  renderList('friend-outgoing', 'friend-outgoing-empty', fOut.data || [], (r) => {
    const li = el('li');
    li.append(el('span', '', r.receiver), el('span', 'muted', 'pending'));
    return li;
  });

  renderList('match-incoming', 'match-empty', mIn.data || [], (r) =>
    requestItem(r.sender + ' wants to play', () => answerMatch(r, 'Accepted'), () => answerMatch(r, 'Rejected')));
}

function renderList(listId, emptyId, items, makeItem) {
  const list = $(listId);
  list.replaceChildren(...items.map(makeItem));
  $(emptyId).hidden = items.length > 0;
}

function requestItem(label, onAccept, onReject) {
  const li = el('li');
  li.append(el('span', '', label));
  const actions = el('div', 'actions');
  const yes = el('button', 'btn small primary', 'Accept');
  const no = el('button', 'btn small danger', 'Reject');
  yes.type = no.type = 'button';
  yes.addEventListener('click', onAccept);
  no.addEventListener('click', onReject);
  actions.append(yes, no);
  li.append(actions);
  return li;
}

function setupLobby() {
  $('form-add-friend').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = $('add-friend-name').value.trim();
    const { status } = await api('POST', '/waddle/requests/friendrequest/create', { name });
    if (status === 201) { toast('Friend request sent to ' + name + '.'); $('add-friend-name').value = ''; refreshLobby(); }
    else if (status === 404) toast('No user called ' + name + '.');
    else if (status === 409) toast("You can't add yourself, or a request between you already exists.");
  });

  $('form-add-word').addEventListener('submit', async (e) => {
    e.preventDefault();
    const word = $('add-word').value.trim().toUpperCase();
    const { status } = await api('POST', '/waddle/admin/word', { word });
    if (status === 201) { toast(word + ' added to the word list.'); $('add-word').value = ''; }
    else if (status === 409) toast(word + ' is already in the list.');
    else if (status === 400) toast('A word must be exactly 5 letters, A to Z.');
    else if (status === 403) toast('Only admins can add words.');
  });

  $('btn-resume').addEventListener('click', () => {
    const active = load(activeGameKey());
    if (active) startGame(active.wordleId, active.opponent);
  });
}

async function answerFriend(id, ans) {
  const { status } = await api('POST', '/waddle/requests/friendrequest/answer', { ans, id });
  if (status === 200) toast(ans === 'Accepted' ? 'Friend added.' : 'Request rejected.');
  else if (status === 409) toast('That request was already answered.');
  else if (status === 404) toast('That request no longer exists.');
  refreshLobby();
}

async function answerMatch(req, ans) {
  const { status, data } = await api('POST', '/waddle/requests/matchrequest/answer', { ans, id: req.id });
  if (status === 200 && ans === 'Accepted' && data.wordleID) {
    startGame(data.wordleID, req.sender);
    return;
  }
  if (status === 200) toast('Challenge rejected.');
  else if (status === 404) toast('That challenge expired.');
  else if (status === 409) toast('That challenge was already answered.');
  refreshLobby();
}

// The server holds this request open for up to 2 minutes until the friend answers (long polling)
async function challenge(name) {
  $('waiting-text').textContent = 'Waiting for ' + name + ' to accept...';
  $('waiting').hidden = false;
  let left = MATCH_WAIT_SECONDS;
  $('waiting-countdown').textContent = left + 's left';
  const countdown = setInterval(() => {
    left = Math.max(0, left - 1);
    $('waiting-countdown').textContent = left + 's left';
  }, 1000);

  const { status, data } = await api('POST', '/waddle/requests/matchrequest/create', { name });
  clearInterval(countdown);
  $('waiting').hidden = true;
  if (!state.token) return;

  if (status === 201 && data.reqState === 'Accepted' && data.wordleID) startGame(data.wordleID, name);
  else if (status === 201) toast(name + " didn't accept the challenge.");
  else if (status === 403) toast("You can only challenge friends.");
  else if (status === 409) toast('There is already a challenge between you and ' + name + '.');
  else if (status === 404) toast('No user called ' + name + '.');
}

// ---------- game ----------

// The board is kept in localStorage so a page reload doesn't lose the guesses
function gameKey(wordleId) { return 'waddle.game.' + state.user.id + '.' + wordleId; }

function startGame(wordleId, opponent) {
  stopAllTimers();
  const saved = load(gameKey(wordleId));
  state.game = {
    id: wordleId,
    opponent,
    rows: saved ? saved.rows : [],   // [{ word, colors }]
    current: '',
    busy: false,       // a guess is being sent or revealed
    done: false,       // I solved it or used all 6 guesses
    finished: false,   // the whole match is over
    deadline: null,
  };
  store(activeGameKey(), { wordleId, opponent });

  $('opponent-name').textContent = opponent;
  $('result').hidden = true;
  $('game-message').textContent = '';
  buildBoard();
  buildKeyboard();
  for (let i = 0; i < state.game.rows.length; i++) paintRow(i, state.game.rows[i], false);
  const last = state.game.rows[state.game.rows.length - 1];
  if (state.game.rows.length === 6 || (last && last.colors.every((c) => c === 'INPLACE'))) {
    state.game.done = true;
    $('game-message').textContent = 'Waiting for ' + opponent + '...';
  }
  renderDots('my-dots', state.game.rows.length);
  renderDots('enemy-dots', 0);

  showScreen('game');
  pollStatus();
  state.statusTimer = setInterval(pollStatus, 1000);
  state.clockTimer = setInterval(renderClock, 250);
}

function buildBoard() {
  const board = $('board');
  board.replaceChildren();
  for (let r = 0; r < 6; r++) {
    const row = el('div', 'row');
    for (let c = 0; c < 5; c++) row.append(el('div', 'tile'));
    board.append(row);
  }
}

function buildKeyboard() {
  const rows = ['QWERTYUIOP', 'ASDFGHJKL', '+ZXCVBNM-'];
  const kb = $('keyboard');
  kb.replaceChildren();
  for (const letters of rows) {
    const row = el('div', 'key-row');
    for (const ch of letters) {
      let key;
      if (ch === '+') key = keyButton('ENTER', 'Enter', true);
      else if (ch === '-') key = keyButton('⌫', 'Backspace', true);
      else key = keyButton(ch, ch, false);
      row.append(key);
    }
    kb.append(row);
  }
}

function keyButton(label, value, wide) {
  const b = el('button', 'key' + (wide ? ' wide' : ''), label);
  b.type = 'button';
  b.dataset.key = value;
  b.addEventListener('click', () => handleKey(value));
  return b;
}

function renderDots(id, used) {
  const dots = [];
  for (let i = 0; i < 6; i++) dots.push(el('span', 'dot' + (i < used ? ' used' : '')));
  $(id).replaceChildren(...dots);
}

function rowTiles(index) { return $('board').children[index].children; }

function handleKey(key) {
  const g = state.game;
  if (!g || g.busy || g.done || g.finished) return;
  const rowIndex = g.rows.length;

  if (key === 'Enter') { submitGuess(); return; }
  if (key === 'Backspace') {
    if (!g.current) return;
    g.current = g.current.slice(0, -1);
    const tile = rowTiles(rowIndex)[g.current.length];
    tile.textContent = '';
    tile.classList.remove('filled');
    return;
  }
  if (/^[A-Z]$/.test(key) && g.current.length < 5) {
    const tile = rowTiles(rowIndex)[g.current.length];
    g.current += key;
    tile.textContent = key;
    tile.classList.add('filled');
  }
}

function shakeRow(index, message) {
  const row = $('board').children[index];
  row.classList.remove('shake');
  void row.offsetWidth; // restart the animation
  row.classList.add('shake');
  if (message) toast(message);
}

async function submitGuess() {
  const g = state.game;
  const rowIndex = g.rows.length;
  if (g.current.length < 5) { shakeRow(rowIndex, 'Not enough letters.'); return; }

  g.busy = true;
  const word = g.current;
  const { status, data } = await api('POST', '/waddle/game/guess', { g: word, waddleID: g.id });
  if (state.game !== g) return; // left the game while waiting

  if (status === 201) {
    const result = { word, colors: data.g.map((t) => t.p) };
    g.rows.push(result);
    g.current = '';
    store(gameKey(g.id), { rows: g.rows });
    paintRow(rowIndex, result, true);
    renderDots('my-dots', data.progress);

    // wait for the flip animation to finish before reacting
    setTimeout(() => {
      g.busy = false;
      const solved = result.colors.every((c) => c === 'INPLACE');
      if (solved) {
        celebrate(rowIndex);
        g.done = true;
        $('game-message').textContent = 'Solved in ' + data.progress + '! Waiting for ' + g.opponent + '...';
      } else if (data.progress >= 6) {
        g.done = true;
        $('game-message').textContent = 'Out of guesses. Waiting for ' + g.opponent + '...';
      }
    }, FLIP_GAP * 4 + FLIP_MS);
    return;
  }

  g.busy = false;
  if (status === 400) shakeRow(rowIndex, 'Not in the word list.');
  else if (status === 403) { shakeRow(rowIndex, "Time's up."); pollStatus(); }
  else if (status === 404) toast('This game no longer exists.');
}

// Flip each tile in turn and set its color halfway through the flip
function paintRow(index, result, animate) {
  const tiles = rowTiles(index);
  result.colors.forEach((color, i) => {
    const tile = tiles[i];
    tile.textContent = result.word[i];
    const cls = color.toLowerCase(); // INPLACE / INWORD / NON -> css class
    if (!animate) {
      tile.className = 'tile ' + cls;
      colorKey(result.word[i], color);
      return;
    }
    tile.style.setProperty('--delay', i * FLIP_GAP + 'ms');
    tile.classList.add('reveal');
    setTimeout(() => {
      tile.classList.remove('filled');
      tile.classList.add(cls);
      colorKey(result.word[i], color);
    }, i * FLIP_GAP + FLIP_MS / 2);
  });
}

function celebrate(index) {
  [...rowTiles(index)].forEach((tile, i) => {
    tile.classList.remove('reveal');
    tile.style.setProperty('--delay', i * 100 + 'ms');
    tile.classList.add('win');
  });
}

// A key keeps its best color: green beats yellow beats gray
const RANK = { NON: 1, INWORD: 2, INPLACE: 3 };
function colorKey(letter, color) {
  const key = document.querySelector('.key[data-key="' + letter + '"]');
  if (!key) return;
  const current = key.dataset.color;
  if (current && RANK[current] >= RANK[color]) return;
  key.dataset.color = color;
  key.classList.remove('inplace', 'inword', 'non');
  key.classList.add(color.toLowerCase());
}

async function pollStatus() {
  const g = state.game;
  if (!g || g.finished) return;
  const { status, data } = await api('GET', '/waddle/game/status/' + g.id);
  if (state.game !== g || status !== 200) return;

  g.deadline = parseInstant(data.deadline);
  renderDots('enemy-dots', data.enemyProgress);

  if (data.state === 'Finished') {
    g.done = true;
    // the result is saved a moment after the match finishes, so keep polling until it shows up
    if (data.result) finishGame(data.result, data.word);
    else $('game-message').textContent = 'Match over, working out the winner...';
  }
}

function parseInstant(value) {
  if (typeof value === 'number') return value < 1e12 ? value * 1000 : value;
  return value ? Date.parse(value) : null;
}

function renderClock() {
  const g = state.game;
  const timer = $('timer');
  if (!g || !g.deadline) { timer.textContent = '--:--'; return; }
  const left = g.finished ? 0 : Math.max(0, Math.round((g.deadline - Date.now()) / 1000));
  timer.textContent = String(Math.floor(left / 60)).padStart(2, '0') + ':' + String(left % 60).padStart(2, '0');
  timer.classList.toggle('low', left <= 60 && !g.finished);
}

function finishGame(result, word) {
  const g = state.game;
  g.finished = true;
  clearInterval(state.statusTimer);
  store(activeGameKey(), null);
  store(gameKey(g.id), null);

  const titles = { WIN: 'You won!', LOSE: 'You lost.', TIE: "It's a tie." };
  $('result-title').textContent = titles[result] || 'Match over';
  $('result-word').textContent = word ? 'The word was ' + word + '.' : '';
  $('game-message').textContent = '';
  $('result').hidden = false;
  renderClock();
}

function setupGame() {
  document.addEventListener('keydown', (e) => {
    if ($('screen-game').hidden || e.ctrlKey || e.metaKey || e.altKey) return;
    if (e.key === 'Enter' || e.key === 'Backspace') { e.preventDefault(); handleKey(e.key); }
    else if (/^[a-zA-Z]$/.test(e.key)) handleKey(e.key.toUpperCase());
  });
  $('btn-back').addEventListener('click', openLobby);
  $('btn-result-lobby').addEventListener('click', openLobby);
}

// ---------- start ----------

setupAuth();
setupLobby();
setupGame();

const savedAuth = load('waddle.auth');
if (savedAuth && savedAuth.token) {
  state.token = savedAuth.token;
  state.user = { id: savedAuth.id, name: savedAuth.name };
  openLobby();
} else {
  showScreen('login');
}
