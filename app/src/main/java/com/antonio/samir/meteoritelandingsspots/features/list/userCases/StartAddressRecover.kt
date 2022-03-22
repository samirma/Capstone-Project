package com.antonio.samir.meteoritelandingsspots.features.list.userCases

import android.content.Context
import androidx.work.*
import com.antonio.samir.meteoritelandingsspots.common.userCase.UserCaseBase
import com.antonio.samir.meteoritelandingsspots.service.address.AddressRecoverWorker
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import java.util.*

@FlowPreview
class StartAddressRecover(val context: Context) : UserCaseBase<Unit, UUID>() {

    override fun action(input: Unit) = flow {

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request: WorkRequest =
            OneTimeWorkRequestBuilder<AddressRecoverWorker>()
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(context).enqueue(request)

        val uuid = request.id

        emit(uuid)
    }


}