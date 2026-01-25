package com.ericho.myhospital.app

import android.app.Application
import com.ericho.myhospital.data.repository.LocalRepository
import com.ericho.myhospital.data.repository.LocalRepositoryImpl
import com.ericho.myhospital.data.source.local.LocalDataSource
import com.ericho.myhospital.data.source.remote.HttpClientProvider
import com.ericho.myhospital.viewmodel.HospitalWaitTimeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.dsl.onClose

class MyHospitalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyHospitalApplication)
            modules(appModule)
        }
    }

    override fun onTerminate() {
        stopKoin()
        super.onTerminate()
    }
}

private val appModule = module {
    single { LocalDataSource(androidContext()) }
    single<LocalRepository> { LocalRepositoryImpl(get()) }
    single {
        HttpClientProvider.create()
    } onClose {
        it?.close()
    }
    viewModel { HospitalWaitTimeViewModel(get(), get()) }
}
