package com.antonio.samir.meteoritelandingsspots.features.list.userCases

import com.antonio.samir.meteoritelandingsspots.common.ResultOf
import com.antonio.samir.meteoritelandingsspots.common.userCase.IsDarkTheme
import com.antonio.samir.meteoritelandingsspots.common.userCase.UserCaseBase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

@FlowPreview
class SwitchUITheme(
    private val setUITheme: SetUITheme,
    private val isDarkTheme: IsDarkTheme
) : UserCaseBase<Unit, ResultOf<Unit>>() {

    override fun action(input: Unit) = flow {
        try {
            val isDark = isDarkTheme(Unit).first()
            emitAll(setUITheme(SetUITheme.Input(!isDark)))
        } catch (e: Exception) {
            emit(ResultOf.Error(e))
        }
    }

}