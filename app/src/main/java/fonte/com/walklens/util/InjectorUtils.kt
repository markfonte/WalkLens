package fonte.com.walklens.util

import android.content.Context
import fonte.com.walklens.MainActivityViewModelFactory
import fonte.com.walklens.data.AppDatabase
import fonte.com.walklens.data.MainRepository
import fonte.com.walklens.ui.main.MapsViewModelFactory
import fonte.com.walklens.ui.main.SettingsViewModelFactory

object InjectorUtils {
    private fun getMainRepositorySingleton(context: Context): MainRepository {
        return MainRepository.getInstance(
            AppDatabase.getInstance(context.applicationContext).userSettingsDao()
        )
    }

    fun provideMapsFragmentViewModelFactory(context: Context): MapsViewModelFactory {
        val repository: MainRepository = getMainRepositorySingleton(context)
        return MapsViewModelFactory(repository)
    }

    fun provideSettingsFragmentViewModelFactory(context: Context): SettingsViewModelFactory {
        val repository: MainRepository = getMainRepositorySingleton(context)
        return SettingsViewModelFactory(repository)
    }

    fun provideMainActivityViewModelFactory(context: Context): fonte.com.walklens.MainActivityViewModelFactory {
        val repository: MainRepository = getMainRepositorySingleton(context)
        return MainActivityViewModelFactory(repository)
    }
}