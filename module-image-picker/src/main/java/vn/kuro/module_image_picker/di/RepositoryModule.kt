package vn.kuro.module_image_picker.di

import android.content.ContentResolver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import vn.kuro.module_image_picker.data.repository.ImagePickerRepositoryImpl
import vn.kuro.module_image_picker.domain.repository.ImagePickerRepository


@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {
    @Provides
    @ViewModelScoped
    fun provideImagePickerRepository(
        contentResolver: ContentResolver
    ): ImagePickerRepository = ImagePickerRepositoryImpl(contentResolver)
}