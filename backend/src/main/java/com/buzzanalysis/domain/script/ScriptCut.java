package com.buzzanalysis.domain.script;

/**
 * 動画台本の1カット分（Phase11）。{@code startSecond}/{@code endSecond}は動画全体の尺の中での
 * 相対秒数。生成時のカット検証・補正ロジックは{@code OpenAiScriptGenerationService}側で行い、
 * 本レコード自体は検証済みの値のみを保持する単純な値オブジェクトとする。
 */
public record ScriptCut(int cutNumber, int startSecond, int endSecond, String narration, String telop,
                         String visualDirection) {
}
