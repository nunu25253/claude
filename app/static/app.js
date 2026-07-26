// NailMuseフロントエンドロジック(ビルド工程なしのvanilla JS、§10)。
//
// 設計理由: フレームワーク(React等)は導入禁止のため、状態は素朴なJSオブジェクト
// (STATE)で持ち、DOM更新はテンプレート文字列を innerHTML に流し込むだけの
// 単純な仕組みにしている。プロトタイプの規模ではこれで十分であり、
// 仮想DOM等の仕組みを持ち込むと逆に理解しづらくなる。

const SCENE_OPTIONS = [
  { value: "office", label: "オフィス" },
  { value: "date", label: "デート" },
  { value: "bridal", label: "ブライダル" },
  { value: "event", label: "イベント" },
  { value: "daily", label: "普段使い" },
];

const SHAPE_OPTIONS = [
  { value: "round", label: "ラウンド" },
  { value: "oval", label: "オーバル" },
  { value: "square", label: "スクエア" },
  { value: "almond", label: "アーモンド" },
];

const LENGTH_OPTIONS = [
  { value: "short", label: "ショート" },
  { value: "medium", label: "ミディアム" },
  { value: "long", label: "ロング" },
];

const STYLE_OPTIONS = [
  { value: "simple", label: "シンプル" },
  { value: "french", label: "フレンチ" },
  { value: "gradient", label: "グラデーション" },
  { value: "nuance", label: "ニュアンス" },
  { value: "one_color", label: "ワンカラー" },
];

// mock_provider.pyのCOLOR_BASE_HEXと揃えたスウォッチ色(見た目の一貫性のため)。
const COLOR_OPTIONS = [
  { value: "red", label: "レッド", hex: "#D1495B" },
  { value: "pink", label: "ピンク", hex: "#F4B6C2" },
  { value: "beige", label: "ベージュ", hex: "#E8D3C0" },
  { value: "white", label: "ホワイト", hex: "#FFFFFF" },
  { value: "black", label: "ブラック", hex: "#2B2B2B" },
  { value: "blue", label: "ブルー", hex: "#5B7C99" },
  { value: "green", label: "グリーン", hex: "#7A9E7E" },
  { value: "purple", label: "パープル", hex: "#9B7EBD" },
  { value: "gold", label: "ゴールド", hex: "#D4AF37" },
  { value: "silver", label: "シルバー", hex: "#C0C0C0" },
];

const MAX_COLORS = 3;

// アプリ全体の入力状態。グローバル変数だが、DOM操作イベントハンドラから
// 読み書きする単純なプロトタイプなのでモジュールスコープの変数で十分とする。
const state = {
  scene: null,
  colors: [],
  shape: null,
  length: null,
  style: null,
  favoriteIds: new Set(),
};

function qs(selector) {
  return document.querySelector(selector);
}

function showToast(message) {
  const toast = qs("#toast");
  toast.textContent = message;
  toast.classList.remove("hidden");
  window.clearTimeout(showToast._timer);
  showToast._timer = window.setTimeout(() => toast.classList.add("hidden"), 3000);
}

// --- 条件入力(チップ・スウォッチ) -------------------------------------------

function renderSingleSelect(containerId, options, field) {
  const container = qs(containerId);
  container.innerHTML = options
    .map(
      (option) => `
      <button type="button" data-field="${field}" data-value="${option.value}"
        class="chip px-3 py-1.5 rounded-full text-sm border border-stone-300">
        ${option.label}
      </button>`
    )
    .join("");
}

function renderColorSwatches() {
  const container = qs("#color-options");
  container.innerHTML = COLOR_OPTIONS.map(
    (option) => `
      <button type="button" data-field="colors" data-value="${option.value}"
        class="color-swatch w-9 h-9 rounded-full border-2 border-transparent"
        style="background-color: ${option.hex};" title="${option.label}"></button>`
  ).join("");
}

function refreshChipStyles() {
  document.querySelectorAll(".chip").forEach((chip) => {
    const isSelected = state[chip.dataset.field] === chip.dataset.value;
    chip.classList.toggle("bg-pink-500", isSelected);
    chip.classList.toggle("text-white", isSelected);
    chip.classList.toggle("border-pink-500", isSelected);
  });
  document.querySelectorAll(".color-swatch").forEach((swatch) => {
    const isSelected = state.colors.includes(swatch.dataset.value);
    swatch.classList.toggle("border-pink-500", isSelected);
    swatch.classList.toggle("ring-2", isSelected);
    swatch.classList.toggle("ring-pink-300", isSelected);
  });
}

function updateGenerateButtonState() {
  const ready = state.scene && state.colors.length >= 1 && state.shape && state.length && state.style;
  qs("#generate-button").disabled = !ready;
}

function onOptionClick(event) {
  const target = event.target.closest("[data-field]");
  if (!target) return;

  const { field, value } = target.dataset;
  if (field === "colors") {
    const index = state.colors.indexOf(value);
    if (index >= 0) {
      state.colors.splice(index, 1);
    } else if (state.colors.length < MAX_COLORS) {
      state.colors.push(value);
    } else {
      showToast("色は3つまで選べます");
    }
  } else {
    state[field] = value;
  }
  refreshChipStyles();
  updateGenerateButtonState();
}

function setupFreeTextCounter() {
  const input = qs("#free-text-input");
  const counter = qs("#free-text-count");
  input.addEventListener("input", () => {
    counter.textContent = String(input.value.length);
  });
}

// --- デザイン提案カード ------------------------------------------------------

function renderProposalCard(proposal) {
  const isFavorited = state.favoriteIds.has(proposal.id);
  const paletteSwatches = proposal.palette
    .map((color) => `<span class="w-6 h-6 rounded-full border border-stone-200" style="background-color: ${color};"></span>`)
    .join("");
  const points = proposal.points.map((point) => `<li>${point}</li>`).join("");

  return `
    <article class="rounded-2xl border border-stone-200 bg-white p-3 shadow-sm" data-proposal-id="${proposal.id}">
      <img src="${proposal.image_url}" alt="${proposal.title}" class="w-full rounded-xl bg-stone-50" />
      <h3 class="mt-3 font-bold text-stone-800">${proposal.title}</h3>
      <p class="mt-1 text-sm text-stone-600">${proposal.concept}</p>
      <div class="mt-2 flex gap-1.5">${paletteSwatches}</div>
      <ul class="mt-2 text-sm text-stone-600 list-disc list-inside space-y-0.5">${points}</ul>
      <button type="button" class="heart-button mt-3 text-2xl leading-none" data-proposal-id="${proposal.id}">
        ${isFavorited ? "♥" : "♡"}
      </button>
    </article>`;
}

function renderSkeletons(container) {
  const skeleton = `
    <div class="rounded-2xl border border-stone-200 bg-white p-3 shadow-sm animate-pulse">
      <div class="w-full h-40 rounded-xl bg-stone-200"></div>
      <div class="mt-3 h-4 w-2/3 bg-stone-200 rounded"></div>
      <div class="mt-2 h-3 w-full bg-stone-200 rounded"></div>
    </div>`;
  container.innerHTML = skeleton.repeat(3);
}

// --- API呼び出し --------------------------------------------------------------

async function requestJson(url, options) {
  const response = await fetch(url, options);
  const body = await response.json();
  if (!response.ok) {
    const error = new Error(body?.error?.message || "エラーが発生しました。");
    error.code = body?.error?.code;
    throw error;
  }
  return body;
}

async function handleGenerateClick() {
  const resultArea = qs("#result-area");
  renderSkeletons(resultArea);
  qs("#generate-button").disabled = true;

  try {
    const body = await requestJson("/api/designs/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        scene: state.scene,
        colors: state.colors,
        shape: state.shape,
        length: state.length,
        style: state.style,
        free_text: qs("#free-text-input").value || null,
      }),
    });
    resultArea.innerHTML = body.proposals.map(renderProposalCard).join("");
  } catch (error) {
    resultArea.innerHTML = "";
    if (error.code === "budget_exceeded") {
      showToast("本日の上限に達しました");
    } else if (error.code === "suspicious_input" || error.code === "validation_error") {
      showToast("入力内容を確認してください");
    } else {
      showToast(error.message);
    }
  } finally {
    updateGenerateButtonState();
  }
}

async function toggleFavorite(proposalId, heartButton) {
  const isFavorited = state.favoriteIds.has(proposalId);
  try {
    if (isFavorited) {
      await fetch(`/api/favorites/${proposalId}`, { method: "DELETE" });
      state.favoriteIds.delete(proposalId);
    } else {
      await requestJson("/api/favorites", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ proposal_id: proposalId }),
      });
      state.favoriteIds.add(proposalId);
    }
    heartButton.textContent = state.favoriteIds.has(proposalId) ? "♥" : "♡";
    if (qs("#tab-favorites").classList.contains("hidden") === false) {
      loadFavorites();
    }
  } catch (error) {
    showToast(error.message);
  }
}

async function loadFavorites() {
  const favoritesArea = qs("#favorites-area");
  const proposals = await requestJson("/api/favorites");
  state.favoriteIds = new Set(proposals.map((proposal) => proposal.id));
  favoritesArea.innerHTML =
    proposals.length > 0
      ? proposals.map(renderProposalCard).join("")
      : '<p class="text-sm text-stone-500 text-center py-8">お気に入りはまだありません</p>';
}

function formatDateTime(isoString) {
  const date = new Date(isoString);
  return date.toLocaleString("ja-JP", { dateStyle: "medium", timeStyle: "short" });
}

async function loadHistory() {
  const historyArea = qs("#history-area");
  const items = await requestJson("/api/designs/history?limit=20&offset=0");

  if (items.length === 0) {
    historyArea.innerHTML = '<p class="text-sm text-stone-500 text-center py-8">履歴はまだありません</p>';
    return;
  }

  historyArea.innerHTML = items
    .map(
      (item, index) => `
      <div class="rounded-xl border border-stone-200 bg-white">
        <button type="button" class="history-toggle w-full text-left px-3 py-2 text-sm font-medium"
          data-target="history-group-${index}">
          ${formatDateTime(item.created_at)}(${item.proposals.length}案)
        </button>
        <div id="history-group-${index}" class="hidden px-3 pb-3 space-y-3">
          ${item.proposals.map(renderProposalCard).join("")}
        </div>
      </div>`
    )
    .join("");
}

// --- タブ切り替え --------------------------------------------------------------

function switchTab(tabName) {
  document.querySelectorAll(".tab-panel").forEach((panel) => {
    panel.classList.toggle("hidden", panel.id !== `tab-${tabName}`);
  });
  document.querySelectorAll(".tab-button").forEach((button) => {
    const isActive = button.dataset.tab === tabName;
    button.classList.toggle("border-pink-500", isActive);
    button.classList.toggle("text-pink-600", isActive);
    button.classList.toggle("border-transparent", !isActive);
  });
  qs("#generate-bar").classList.toggle("hidden", tabName !== "create");

  if (tabName === "favorites") loadFavorites().catch((error) => showToast(error.message));
  if (tabName === "history") loadHistory().catch((error) => showToast(error.message));
}

// --- 初期化 ----------------------------------------------------------------

function setupEventDelegation() {
  document.addEventListener("click", (event) => {
    const tabButton = event.target.closest(".tab-button");
    if (tabButton) {
      switchTab(tabButton.dataset.tab);
      return;
    }

    const heartButton = event.target.closest(".heart-button");
    if (heartButton) {
      toggleFavorite(heartButton.dataset.proposalId, heartButton);
      return;
    }

    const historyToggle = event.target.closest(".history-toggle");
    if (historyToggle) {
      qs(`#${historyToggle.dataset.target}`).classList.toggle("hidden");
      return;
    }

    onOptionClick(event);
  });
}

function init() {
  renderSingleSelect("#scene-options", SCENE_OPTIONS, "scene");
  renderColorSwatches();
  renderSingleSelect("#shape-options", SHAPE_OPTIONS, "shape");
  renderSingleSelect("#length-options", LENGTH_OPTIONS, "length");
  renderSingleSelect("#style-options", STYLE_OPTIONS, "style");
  setupFreeTextCounter();
  setupEventDelegation();
  qs("#generate-button").addEventListener("click", handleGenerateClick);
  switchTab("create");
}

document.addEventListener("DOMContentLoaded", init);
