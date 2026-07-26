"""LRU+TTLキャッシュ(§6.4)。

設計理由: 同一条件でのデザイン生成はAIプロバイダを再度呼ばずに済ませたい
(§6.3のコスト防御と表裏一体)。LRU(最近使われていない項目から追い出す方式)で
メモリ上限を守りつつ、TTL(生存時間)切れの古い提案を返さないようにする。
現在時刻を`now_fn`として外から注入できるようにしているのは、テストで
`time.sleep()`を使わずに「時間が経過した」状態を再現するため。
"""

import time
from collections import OrderedDict
from collections.abc import Callable


class LRUTTLCache[V]:
    """キーの挿入順を保持し、上限超過時に最古参から追い出すキャッシュ。"""

    def __init__(
        self,
        max_size: int,
        ttl_seconds: float,
        now_fn: Callable[[], float] = time.monotonic,
    ) -> None:
        self._max_size = max_size
        self._ttl_seconds = ttl_seconds
        self._now_fn = now_fn
        # OrderedDict: 先頭が最古参、末尾が最近アクセスされた項目。
        self._store: OrderedDict[str, tuple[V, float]] = OrderedDict()
        self.hit_count = 0
        self.miss_count = 0

    def get(self, key: str) -> V | None:
        """キーに対応する値を返す。TTL切れ・未登録はNone(ミス扱い)。"""
        entry = self._store.get(key)
        if entry is None:
            self.miss_count += 1
            return None

        value, inserted_at = entry
        if self._now_fn() - inserted_at > self._ttl_seconds:
            # TTL切れの項目は「ヒット扱いしない」(§6.4)ため、ここで削除してミス計上。
            del self._store[key]
            self.miss_count += 1
            return None

        self._store.move_to_end(key)  # LRU: アクセスされたので最新扱いにする
        self.hit_count += 1
        return value

    def set(self, key: str, value: V) -> None:
        """値を登録する。上限を超えたら最古参を1件追い出す。"""
        if key in self._store:
            del self._store[key]
        self._store[key] = (value, self._now_fn())
        if len(self._store) > self._max_size:
            self._store.popitem(last=False)

    @property
    def size(self) -> int:
        """現在キャッシュに保持している件数。"""
        return len(self._store)

    @property
    def hit_rate(self) -> float:
        """ヒット率(呼び出しが1件もない場合は0.0)。"""
        total = self.hit_count + self.miss_count
        return self.hit_count / total if total else 0.0
