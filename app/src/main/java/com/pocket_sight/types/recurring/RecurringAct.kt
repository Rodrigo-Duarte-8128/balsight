package com.pocket_sight.types.recurring

open class RecurringAct {
    fun getId(): Int {
        if (this is RecurringTransaction) {
            return this.recurringTransactionId
        }
        if (this is RecurringTransfer) {
            return this.recurringTransferId
        }
        return 0
    }
}
