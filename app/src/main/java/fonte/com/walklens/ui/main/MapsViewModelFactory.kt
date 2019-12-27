package fonte.com.walklens.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import fonte.com.walklens.data.MainRepository

class MapsViewModelFactory(private val repository: MainRepository) : ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MapsViewModel(repository) as T
    }
}