package jatx.smoketime.presentation

import android.content.Context
import jatx.common.Appearance
import jatx.smoketime.R

class AppearanceImpl(context: Context): Appearance {

    override val buttonLabel = context.getString(R.string.button_label)
    override val lastLabel = context.getString(R.string.last_label)
    override val areYouSureAdd = context.getString(R.string.are_you_sure_add)
    override val areYouSureDelete = context.getString(R.string.are_you_sure_delete)
    override val yes = context.getString(R.string.yes)
    override val no = context.getString(R.string.no)
    override val todayLabel = context.getString(R.string.today_label)
    override val firstTodayLabel = context.getString(R.string.first_today_label)
    override val currentMonthLabel = context.getString(R.string.current_month_label)
    override val packsLabel = context.getString(R.string.packs_label)
    override val hourLetter = context.getString(R.string.hour_letter)
    override val minuteLetter = context.getString(R.string.minute_letter)
}