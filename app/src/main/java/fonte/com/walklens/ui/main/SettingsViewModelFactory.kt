package fonte.com.walklens.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fonte.com.walklens.data.MainRepository

class SettingsViewModelFactory(private val repository: MainRepository) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository) as T
    }
}