package com.buzzanalysis.application.script.dto;

import com.buzzanalysis.domain.script.ScriptCut;

/** {@link ScriptCut}（ドメイン）のapplication層向けDTO。 */
public record ScriptCutDto(int cutNumber, int startSecond, int endSecond, String narration, String telop,
                            String visualDirection) {
    public static ScriptCutDto from(ScriptCut c) {
        return new ScriptCutDto(c.cutNumber(), c.startSecond(), c.endSecond(), c.narration(), c.telop(),
                c.visualDirection());
    }
}
