package com.ericho.myhospital

import android.app.Application
import com.ericho.myhospital.data.LocalDataSource
import com.ericho.myhospital.data.LocalRepository
import com.ericho.myhospital.data.LocalRepositoryImpl
import com.ericho.myhospital.data.remote.HttpClientProvider
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

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
        it.close()
    }
    viewModel { HospitalWaitTimeViewModel(get(), get()) }
}
