package com.pocket_sight.types

import com.pocket_sight.types.transactions.Transaction
import com.pocket_sight.types.transfers.Transfer

open class Act {
    fun getId(): Long {
        if (this is Transaction) {
            return this.transactionId
        }
        if (this is Transfer) {
            return this.transferId
        }
        return 0L
    }
}


